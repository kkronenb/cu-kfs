/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.pdp.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.Batch;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.LoadPaymentStatus;
import org.kuali.kfs.pdp.businessobject.PaymentFileLoad;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.service.CustomerProfileService;
import org.kuali.kfs.pdp.service.PaymentFileService;
import org.kuali.kfs.pdp.service.PaymentFileValidationService;
import org.kuali.kfs.pdp.service.PdpEmailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.ErrorMessage;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiInteger;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @see org.kuali.kfs.pdp.service.PaymentFileService
 */
@Transactional
public class PaymentFileServiceImpl implements PaymentFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentFileServiceImpl.class);

    private String outgoingDirectoryName;

    private ParameterService parameterService;
    private CustomerProfileService customerProfileService;
    private BatchInputFileService batchInputFileService;
    private PaymentFileValidationService paymentFileValidationService;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private PdpEmailService paymentFileEmailService;
    private KualiConfigurationService kualiConfigurationService;

    public PaymentFileServiceImpl() {
        super();
    }

    /**
     * @see org.kuali.kfs.pdp.service.PaymentFileService#processPaymentFiles(org.kuali.kfs.sys.batch.BatchInputFileType)
     */
    public void processPaymentFiles(BatchInputFileType paymentInputFileType) {
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(paymentInputFileType);

        for (String incomingFileName : fileNamesToLoad) {
            try {
                LOG.debug("processPaymentFiles() Processing " + incomingFileName);

                // collect various information for status of load
                LoadPaymentStatus status = new LoadPaymentStatus();
                status.setErrorMap(new ErrorMap());

                // process payment file
                PaymentFileLoad paymentFile = processPaymentFile(paymentInputFileType, incomingFileName, status.getErrorMap());
                if (ObjectUtils.isNotNull(paymentFile) && paymentFile.isPassedValidation()) {
                    // load payment data
                    loadPayments(paymentFile, status, incomingFileName);
                }

                createOutputFile(status, incomingFileName);
            }
            catch (RuntimeException e) {
                LOG.error("Caught exception trying to load payment file: " + incomingFileName, e);
                // swallow exception so we can continue processing files, the errors have been reported by email
            }
        }
    }

    /**
     * Attempt to parse the file, run validations, and store batch data
     * 
     * @param paymentInputFileType <code>BatchInputFileType</code> for payment files
     * @param incomingFileName name of payment file
     * @param errorMap <code>Map</code> of errors
     * @return <code>LoadPaymentStatus</code> containing status data for load
     */
    protected PaymentFileLoad processPaymentFile(BatchInputFileType paymentInputFileType, String incomingFileName, MessageMap errorMap) {
        // parse xml, if errors found return with failure
        PaymentFileLoad paymentFile = parsePaymentFile(paymentInputFileType, incomingFileName, errorMap);

        if (errorMap.hasNoErrors()) {
            // do validation
            doPaymentFileValidation(paymentFile, errorMap);
        }

        return paymentFile;
    }

    /**
     * @see org.kuali.kfs.pdp.service.PaymentFileService#doPaymentFileValidation(org.kuali.kfs.pdp.businessobject.PaymentFileLoad,
     *      org.kuali.rice.kns.util.ErrorMap)
     */
    public void doPaymentFileValidation(PaymentFileLoad paymentFile, MessageMap errorMap) {
        paymentFileValidationService.doHardEdits(paymentFile, errorMap);

        if (errorMap.hasErrors()) {
            paymentFileEmailService.sendErrorEmail(paymentFile, errorMap);
        }

        // Set validation flag using presence of errors in the error map as the flag.
        paymentFile.setPassedValidation(errorMap.hasNoErrors());
    }

    /**
     * @see org.kuali.kfs.pdp.service.PaymentFileService#loadPayments(java.lang.String)
     */
    public void loadPayments(PaymentFileLoad paymentFile, LoadPaymentStatus status, String incomingFileName) {
        status.setChart(paymentFile.getChart());
        status.setUnit(paymentFile.getUnit());
        status.setSubUnit(paymentFile.getSubUnit());
        status.setCreationDate(paymentFile.getCreationDate());
        status.setDetailCount(paymentFile.getActualPaymentCount());
        status.setDetailTotal(paymentFile.getCalculatedPaymentTotalAmount());

        // create batch record for payment load
        Batch batch = createNewBatch(paymentFile, getBaseFileName(incomingFileName));
        businessObjectService.save(batch);

        paymentFile.setBatchId(batch.getId());
        status.setBatchId(batch.getId());

        // do warnings and set defaults
        List<String> warnings = paymentFileValidationService.doSoftEdits(paymentFile);
        status.setWarnings(warnings);

        // store groups
        for (PaymentGroup paymentGroup : paymentFile.getPaymentGroups()) {
        	// PDP groups are defined in XML and need to have disbursement type code added after the parsing.
        	assignDisbursementTypeCode(paymentGroup);
        	
            businessObjectService.save(paymentGroup);
        }

        // send list of warnings
        paymentFileEmailService.sendLoadEmail(paymentFile, warnings);
        if (paymentFile.isTaxEmailRequired()) {
            paymentFileEmailService.sendTaxEmail(paymentFile);
        }

        removeDoneFile(incomingFileName);

        LOG.debug("loadPayments() was successful");
        status.setLoadStatus(LoadPaymentStatus.LoadStatus.SUCCESS);
    }

    /**
     * This method exists here because PaymentGroups are defined in the PaymentFileLoad objects based on the XML that is read in from the PDP Customers.  
     * The assignment of this attribute prior to submission of the PaymentGroup for Formatting is necessary to support the Format process allowing ACH only file generation.
     * See KFSPTS-918 for complete details.
     * 
     * This method takes a given PaymentGroup and sets the disbursement type code on that group.
     * 
     * @param pg
     */
    private void assignDisbursementTypeCode(PaymentGroup pg) {
        if(pg.isPayableByACH()) {
        	pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.ACH);
        }
        else {
        	pg.setDisbursementTypeCode(PdpConstants.DisbursementTypeCodes.CHECK);
        }
    }
    
    /**
     * Calls <code>BatchInputFileService</code> to validate XML against schema and parse.
     * 
     * @param paymentInputFileType <code>BatchInputFileType</code> for payment files
     * @param incomingFileName name of the payment file to parse
     * @param errorMap any errors encountered while parsing are adding to
     * @return <code>PaymentFile</code> containing the parsed values
     */
    protected PaymentFileLoad parsePaymentFile(BatchInputFileType paymentInputFileType, String incomingFileName, MessageMap errorMap) {
        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        PaymentFileLoad paymentFile = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            paymentFile = (PaymentFileLoad) batchInputFileService.parse(paymentInputFileType, fileByteContent);
        }
        catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        }
        catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());

            errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_PARSING_XML, new String[] { e1.getMessage() });

            // Get customer object from unparsable file so error email can be sent.
            paymentFile = getCustomerProfileFromUnparsableFile(incomingFileName, paymentFile);
            
            // Send error email
            paymentFileEmailService.sendErrorEmail(paymentFile, errorMap);
        } finally {
        	try {
        		// Make sure all input streams are closed when we're done.
        		fileContents.close();
        	} catch(IOException ioex) {
        		
        	}
        }

        return paymentFile;
    }

	/**
	 * @param incomingFileName
	 * @param paymentFile
	 * @return
	 */
	private PaymentFileLoad getCustomerProfileFromUnparsableFile(String incomingFileName, PaymentFileLoad paymentFile) {
		FileInputStream exFileContents;

		try {
			exFileContents = new FileInputStream(incomingFileName); 
		} catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

		try {	
			InputStreamReader inputReader = new InputStreamReader(exFileContents);
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			String line = "";
			boolean found = false;
			String chartVal = "";
			String unitVal = "";
			String subUnitVal = "";

			while(!found && (line=bufferedReader.readLine())!=null) {
				// Use multiple ifs instead of else/ifs because all values could occur on the same line.
				if(StringUtils.contains(line, PdpConstants.CustomerProfilePrimaryKeyTags.CHART_OPEN)) {
					chartVal = StringUtils.substringBetween(line, PdpConstants.CustomerProfilePrimaryKeyTags.CHART_OPEN, PdpConstants.CustomerProfilePrimaryKeyTags.CHART_CLOSE);
				}
		   		if(StringUtils.contains(line, PdpConstants.CustomerProfilePrimaryKeyTags.UNIT_OPEN)) {
					unitVal = StringUtils.substringBetween(line, PdpConstants.CustomerProfilePrimaryKeyTags.UNIT_OPEN, PdpConstants.CustomerProfilePrimaryKeyTags.UNIT_CLOSE);
		   		}
		   		if(StringUtils.contains(line, PdpConstants.CustomerProfilePrimaryKeyTags.SUBUNIT_OPEN)) {
					subUnitVal = StringUtils.substringBetween(line, PdpConstants.CustomerProfilePrimaryKeyTags.SUBUNIT_OPEN, PdpConstants.CustomerProfilePrimaryKeyTags.SUBUNIT_CLOSE);
					found = true;
		   		}
			}

			if(found) {
				// Note: the pdpEmailServiceImpl doesn't actually use the customer object from the paymentFile, but rather retrieves an instance using
				// the values provided for chart, unit and sub_unit.  However, it doesn't make sense to even populate the paymentFile object if 
				// the values retrieved don't map to a valid customer object, so we will retrieve the object here to validate the values.
				CustomerProfile customer = customerProfileService.get(chartVal, unitVal, subUnitVal);
				if(ObjectUtils.isNotNull(customer)) {
					if(ObjectUtils.isNull(paymentFile)) {
						paymentFile = new PaymentFileLoad();
					}
					paymentFile.setChart(chartVal);
					paymentFile.setUnit(unitVal);
					paymentFile.setSubUnit(subUnitVal);
					paymentFile.setCustomer(customer);
				}
			}
			
		} catch(Exception ex) {
	        LOG.error("Attempts to retrieve the customer profile from the unparsable XML file failed with the following error.", ex);
		} finally {
			try {
				exFileContents.close();
			} catch(IOException io) {
		        LOG.error("File stream object could not be closed.", io);            		
			}
		}
		return paymentFile;
	}

    /**
     * @see org.kuali.kfs.pdp.service.PaymentFileService#createOutputFile(org.kuali.kfs.pdp.businessobject.LoadPaymentStatus,
     *      java.lang.String)
     */
    public boolean createOutputFile(LoadPaymentStatus status, String inputFileName) {
        // construct the outgoing file name
        String filename = outgoingDirectoryName + "/" + getBaseFileName(inputFileName);

        // set code-message indicating overall load status
        String code;
        String message;
        if (LoadPaymentStatus.LoadStatus.SUCCESS.equals(status.getLoadStatus())) {
            code = "SUCCESS";
            message = "Successful Load";
        }
        else {
            code = "FAIL";
            message = "Load Failed: ";
            List<ErrorMessage> errorMessages = status.getErrorMap().getMessages(KFSConstants.GLOBAL_ERRORS);
            for (ErrorMessage errorMessage : errorMessages) {
                String resourceMessage = kualiConfigurationService.getPropertyString(errorMessage.getErrorKey());
                resourceMessage = MessageFormat.format(resourceMessage, (Object[]) errorMessage.getMessageParameters());
                message += resourceMessage + ", ";
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(filename);
            PrintStream p = new PrintStream(out);

            p.println("<pdp_load_status>");
            p.println("  <input_file_name>" + inputFileName + "</input_file_name>");
            p.println("  <code>" + code + "</code>");
            p.println("  <count>" + status.getDetailCount() + "</count>");
            if (status.getDetailTotal() != null) {
                p.println("  <total>" + status.getDetailTotal() + "</total>");
            }
            else {
                p.println("  <total>0</total>");
            }

            p.println("  <description>" + message + "</description>");
            if(ObjectUtils.isNotNull(status.getWarnings())) { // Warnings list may be null if file failed to load.
	            p.println("  <messages>");
	            for (String warning : status.getWarnings()) {
	                p.println("    <message>" + warning + "</message>");
	            }
	            p.println("  </messages>");
            }
            p.println("</pdp_load_status>");

            p.close();
            out.close();
        }
        catch (FileNotFoundException e) {
            LOG.error("createOutputFile() Cannot create output file", e);
            return false;
        }
        catch (IOException e) {
            LOG.error("createOutputFile() Cannot write to output file", e);
            return false;
        }

        return true;
    }

    /**
     * Create a new <code>Batch</code> record for the payment file.
     * 
     * @param paymentFile parsed payment file object
     * @param fileName payment file name (without path)
     * @return <code>Batch<code> object
     */
    protected Batch createNewBatch(PaymentFileLoad paymentFile, String fileName) {
        Timestamp now = dateTimeService.getCurrentTimestamp();

        Calendar nowPlus30 = Calendar.getInstance();
        nowPlus30.setTime(now);
        nowPlus30.add(Calendar.DATE, 30);

        Calendar nowMinus30 = Calendar.getInstance();
        nowMinus30.setTime(now);
        nowMinus30.add(Calendar.DATE, -30);

        Batch batch = new Batch();

        CustomerProfile customer = customerProfileService.get(paymentFile.getChart(), paymentFile.getUnit(), paymentFile.getSubUnit());
        batch.setCustomerProfile(customer);
        batch.setCustomerFileCreateTimestamp(new Timestamp(paymentFile.getCreationDate().getTime()));
        batch.setFileProcessTimestamp(now);
        batch.setPaymentCount(new KualiInteger(paymentFile.getPaymentCount()));

        if (fileName.length() > 30) {
            batch.setPaymentFileName(fileName.substring(0, 30));
        }
        else {
            batch.setPaymentFileName(fileName);
        }

        batch.setPaymentTotalAmount(paymentFile.getPaymentTotalAmount());
        batch.setSubmiterUserId(GlobalVariables.getUserSession().getPerson().getPrincipalId());

        return batch;
    }


    /**
     * @returns the file name from the file full path.
     */
    protected String getBaseFileName(String filename) {
        // Replace any backslashes with forward slashes. Works on Windows or Unix
        filename = filename.replaceAll("\\\\", "/");

        int startingPointer = filename.length() - 1;
        while ((startingPointer > 0) && (filename.charAt(startingPointer) != '/')) {
            startingPointer--;
        }

        return filename.substring(startingPointer + 1);
    }

    /**
     * Clears out the associated .done file for the processed data file
     * 
     * @param dataFileName the name of date file with done file to remove
     */
    protected void removeDoneFile(String dataFileName) {
        File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
        if (doneFile.exists()) {
            doneFile.delete();
        }
    }

    /**
     * Sets the outgoingDirectoryName attribute value.
     * 
     * @param outgoingDirectoryName The outgoingDirectoryName to set.
     */
    public void setOutgoingDirectoryName(String outgoingDirectoryName) {
        this.outgoingDirectoryName = outgoingDirectoryName;
    }

    /**
     * Sets the parameterService attribute value.
     * 
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Sets the customerProfileService attribute value.
     * 
     * @param customerProfileService The customerProfileService to set.
     */
    public void setCustomerProfileService(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    /**
     * Sets the batchInputFileService attribute value.
     * 
     * @param batchInputFileService The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Sets the paymentFileValidationService attribute value.
     * 
     * @param paymentFileValidationService The paymentFileValidationService to set.
     */
    public void setPaymentFileValidationService(PaymentFileValidationService paymentFileValidationService) {
        this.paymentFileValidationService = paymentFileValidationService;
    }

    /**
     * Sets the businessObjectService attribute value.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Sets the paymentFileEmailService attribute value.
     * 
     * @param paymentFileEmailService The paymentFileEmailService to set.
     */
    public void setPaymentFileEmailService(PdpEmailService paymentFileEmailService) {
        this.paymentFileEmailService = paymentFileEmailService;
    }

    /**
     * Sets the kualiConfigurationService attribute value.
     * 
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setKualiConfigurationService(KualiConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

}
