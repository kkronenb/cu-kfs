/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.ld.batch.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hsqldb.lib.Set;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.gl.batch.service.ReconciliationParserService;
import org.kuali.kfs.gl.batch.service.impl.ExceptionCaughtStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconBadLoadAbortedStatus;
import org.kuali.kfs.gl.batch.service.impl.FileReconOkLoadOkStatus;
import org.kuali.kfs.gl.batch.service.impl.ReconciliationBlock;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.service.impl.EnterpriseFeederStatusAndErrorMessagesWrapper;
import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.LaborPropertyConstants;
import org.kuali.kfs.module.ld.batch.LaborEnterpriseFeedStep;
import org.kuali.kfs.module.ld.batch.service.ReconciliationService;
import org.kuali.kfs.module.ld.businessobject.BenefitsCalculation;
import org.kuali.kfs.module.ld.businessobject.LaborOriginEntry;
import org.kuali.kfs.module.ld.businessobject.PositionObjectBenefit;
import org.kuali.kfs.module.ld.report.EnterpriseFeederReportData;
import org.kuali.kfs.module.ld.service.LaborBenefitsCalculationService;
import org.kuali.kfs.module.ld.service.LaborPositionObjectBenefitService;
import org.kuali.kfs.module.ld.util.LaborOriginEntryFileIterator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.Message;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import com.rsmart.kuali.kfs.module.ld.LdConstants;
import com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculationExtension;

import edu.cornell.kfs.coa.businessobject.ObjectCodeExtendedAttribute;

public class FileEnterpriseFeederOffsetHelperServiceImpl extends org.kuali.kfs.module.ld.batch.service.impl.FileEnterpriseFeederHelperServiceImpl {
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FileEnterpriseFeederHelperServiceImpl.class);
    private ReconciliationParserService reconciliationParserService;
    private ReconciliationService 		reconciliationService;
    private DateTimeService 			dateTimeService;

 

    /**
     * This method does the reading and the loading of reconciliation. Read
     * class description. This method DOES NOT handle the deletion of the done
     * file
     * 
     * @param doneFile
     *            a URL that must be present. The contents may be empty
     * @param dataFile
     *            a URL to a flat file of origin entry rows.
     * @param reconFile
     *            a URL to the reconciliation file. See the implementation of
     *            {@link ReconciliationParserService} for file format.
     * @param originEntryGroup
     *            the group into which the origin entries will be loaded
     * @param feederProcessName
     *            the name of the feeder process
     * @param reconciliationTableId
     *            the name of the block to use for reconciliation within the
     *            reconciliation file
     * @param statusAndErrors
     *            any status information should be stored within this object
     * @see org.kuali.module.gl.service.impl.FileEnterpriseFeederHelperService#feedOnFile(java.io.File,
     *      java.io.File, java.io.File,
     *      org.kuali.kfs.gl.businessobject.OriginEntryGroup)
     */
    
    protected void validateReconFile(File reconFile,File dataFile,String reconciliationTableId,List<Message> errorMessages,EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors) {
    	ReconciliationBlock reconciliationBlock = null;
        Reader reconReader 						= null;
        BufferedReader dataFileReader 			= null;
        
        try {
            reconReader = new FileReader(reconFile);
            reconciliationBlock = reconciliationParserService.parseReconciliationBlock(reconReader, reconciliationTableId);
        }
        catch (IOException e) {
            LOG.error("IO Error occured trying to read the recon file.", e);
            errorMessages.add(new Message("IO Error occured trying to read the recon file.", Message.TYPE_FATAL));
            reconciliationBlock = null;
            statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
            LOG.error("Error occured trying to parse the recon file.", e);
            errorMessages.add(new Message("Error occured trying to parse the recon file.", Message.TYPE_FATAL));
            reconciliationBlock = null;
            statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            throw e;
        }
        finally {
            if (reconReader != null) {
                try {
                    reconReader.close();
                }
                catch (IOException e) {
                    LOG.error("Error occured trying to close recon file: " + reconFile.getAbsolutePath(), e);
                }
            }
        }
        try {
            if (reconciliationBlock == null) {
                errorMessages.add(new Message("Unable to parse reconciliation file.", Message.TYPE_FATAL));
            }
            else {
                dataFileReader = new BufferedReader(new FileReader(dataFile));
                Iterator<LaborOriginEntry> fileIterator = new LaborOriginEntryFileIterator(dataFileReader, false);
                reconciliationService.reconcile(fileIterator, reconciliationBlock, errorMessages);

                fileIterator = null;
                dataFileReader.close();
                dataFileReader = null;
            }
        }
        catch(Exception e){
        	throw new RuntimeException(e.toString());
        }
       
    }
    
    public void feedOnFile(File doneFile, File dataFile, File reconFile, PrintStream enterpriseFeedPs,
            String feederProcessName, String reconciliationTableId,
            EnterpriseFeederStatusAndErrorMessagesWrapper statusAndErrors, LedgerSummaryReport ledgerSummaryReport,
            ReportWriterService errorStatisticsReport, EnterpriseFeederReportData feederReportData) {
        LOG.info("Processing done file: " + doneFile.getAbsolutePath());

        List<Message> errorMessages = statusAndErrors.getErrorMessages();
        BufferedReader dataFileReader = null;

        ReconciliationBlock reconciliationBlock = null;
        Reader reconReader = null;
        try {
            reconReader = new FileReader(reconFile);
            reconciliationBlock = reconciliationParserService.parseReconciliationBlock(reconReader, reconciliationTableId);
        }
        catch (IOException e) {
            LOG.error("IO Error occured trying to read the recon file.", e);
            errorMessages.add(new Message("IO Error occured trying to read the recon file.", Message.TYPE_FATAL));
            reconciliationBlock = null;
            statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            throw new RuntimeException(e);
        }
        catch (RuntimeException e) {
            LOG.error("Error occured trying to parse the recon file.", e);
            errorMessages.add(new Message("Error occured trying to parse the recon file.", Message.TYPE_FATAL));
            reconciliationBlock = null;
            statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            throw e;
        }
        finally {
            if (reconReader != null) {
                try {
                    reconReader.close();
                }
                catch (IOException e) {
                    LOG.error("Error occured trying to close recon file: " + reconFile.getAbsolutePath(), e);
                }
            }
        }

        try {
            if (reconciliationBlock == null) {
                errorMessages.add(new Message("Unable to parse reconciliation file.", Message.TYPE_FATAL));
            }
            else {
                dataFileReader = new BufferedReader(new FileReader(dataFile));
                Iterator<LaborOriginEntry> fileIterator = new LaborOriginEntryFileIterator(dataFileReader, false);
                reconciliationService.reconcile(fileIterator, reconciliationBlock, errorMessages);

                fileIterator = null;
                dataFileReader.close();
                dataFileReader = null;
            }

            if (reconciliationProcessSucceeded(errorMessages)) {
                dataFileReader = new BufferedReader(new FileReader(dataFile));
                String line;
                int count = 0;
                
                
                List<LaborOriginEntry> entries = new ArrayList<LaborOriginEntry>();
                String offsetParmValue = getParameterService().getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_CALCULATION_OFFSET);
                String offsetDocTypes = null;
                if(StringUtils.isNotEmpty(getParameterService().getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE))) {
                    offsetDocTypes = "," + getParameterService().getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_DOCTYPE).replace(";", ",").replace("|", ",") + ",";
                }
                while ((line = dataFileReader.readLine()) != null) {
                    try {
                        LaborOriginEntry tempEntry = new LaborOriginEntry();
                        tempEntry.setFromTextFileForBatch(line, count);
                        
                        feederReportData.incrementNumberOfRecordsRead();
                        feederReportData.addToTotalAmountRead(tempEntry.getTransactionLedgerEntryAmount());
                        
                        enterpriseFeedPs.printf("%s\n", line);
                        
                        ledgerSummaryReport.summarizeEntry(tempEntry);
                        feederReportData.incrementNumberOfRecordsWritten();
                        feederReportData.addToTotalAmountWritten(tempEntry.getTransactionLedgerEntryAmount());
                        
                        List<LaborOriginEntry> benefitEntries = generateBenefits(tempEntry, errorStatisticsReport, feederReportData);
                        KualiDecimal benefitTotal = new KualiDecimal (0);
                        KualiDecimal offsetTotal = new KualiDecimal (0);
                        
                        for(LaborOriginEntry benefitEntry : benefitEntries) {
                        	benefitEntry.setTransactionLedgerEntryDescription("FRINGE EXPENSE");
                            enterpriseFeedPs.printf("%s\n", benefitEntry.getLine());
                            
                            feederReportData.incrementNumberOfRecordsWritten();
                            feederReportData.addToTotalAmountWritten(benefitEntry.getTransactionLedgerEntryAmount());
                            
                           if(benefitEntry.getTransactionLedgerEntryAmount().isZero()) 		continue;
                            benefitTotal = benefitTotal.add(benefitEntry.getTransactionLedgerEntryAmount());
                        }
                        
                        if(tempEntry.getFinancialBalanceTypeCode() == null || tempEntry.getFinancialBalanceTypeCode().equalsIgnoreCase("IE")) continue;
                        List<LaborOriginEntry> offsetEntries =  generateOffsets(tempEntry,offsetDocTypes);
                        for(LaborOriginEntry offsetEntry : offsetEntries){
                        	if(offsetEntry.getTransactionLedgerEntryAmount().isZero()) continue;
                        	enterpriseFeedPs.printf("%s\n", offsetEntry.getLine());	
                            offsetTotal = offsetTotal.add(offsetEntry.getTransactionLedgerEntryAmount());                        	
                        }


                        if(!benefitTotal.equals(offsetTotal)){
                        	LOG.info("** count:offsetTotal: benefitTotal="+count +":"+ offsetTotal+""+benefitTotal);
                        }
                        
                    } catch (Exception e) {
                        throw new IOException(e.toString());
                    }
                    
                    count++;
                    LOG.info("Processed Entry # "+ count);
                }
                
                
                dataFileReader.close();
                dataFileReader = null;
             //   LOG.info("TotalBenifits : " + totalBenefitValue);

                statusAndErrors.setStatus(new FileReconOkLoadOkStatus());
            }
            else {
                statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
            }
        }
        catch (Exception e) {
            LOG.error("Caught exception when reconciling/loading done file: " + doneFile, e);
            statusAndErrors.setStatus(new ExceptionCaughtStatus());
            errorMessages.add(new Message("Caught exception attempting to reconcile/load done file: " + doneFile + ".  File contents are NOT loaded", Message.TYPE_FATAL));
            // re-throw the exception rather than returning a value so that Spring will auto-rollback
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            else {
                // Spring only rolls back when throwing a runtime exception (by default), so we throw a new exception
                throw new RuntimeException(e);
            }
        }
        finally {
            if (dataFileReader != null) {
                try {
                    dataFileReader.close();
                }
                catch (IOException e) {
                    LOG.error("IO Exception occured trying to close connection to the data file", e);
                    errorMessages.add(new Message("IO Exception occured trying to close connection to the data file", Message.TYPE_FATAL));
                }
            }
        }
    }
    
    
   //
    
	protected List<LaborOriginEntry> generateOffsets(LaborOriginEntry wageEntry,String offsetDocTypes){
		List<LaborOriginEntry> offsetEntries = new ArrayList<LaborOriginEntry>();
		String benefitRateCategoryCode = laborBenefitsCalculationService.getBenefitRateCategoryCode(wageEntry.getChartOfAccountsCode(), wageEntry.getAccountNumber(), wageEntry.getSubAccountNumber());
        Collection<PositionObjectBenefit> positionObjectBenefits = laborPositionObjectBenefitService.getPositionObjectBenefits(wageEntry.getUniversityFiscalYear(), wageEntry.getChartOfAccountsCode(), wageEntry.getFinancialObjectCode());
        
        if (positionObjectBenefits == null || positionObjectBenefits.isEmpty()) {
        	return offsetEntries;
		}

		for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
	//		BenefitsCalculation benefitsCalculation = null;
//			benefitsCalculation = laborBenefitsCalculationService.getBenefitsCalculation(wageEntry.getUniversityFiscalYear(), wageEntry.getChartOfAccountsCode(),positionObjectBenefit.getFinancialObjectBenefitsTypeCode(), benefitRateCategoryCode);
  //          BenefitsCalculationExtension extension = (BenefitsCalculationExtension) benefitsCalculation.getExtension();

            Map<String, Object> fieldValues = new HashMap<String, Object>();
            fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, wageEntry.getUniversityFiscalYear());
            fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, wageEntry.getChartOfAccountsCode());
            fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
            fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, benefitRateCategoryCode);
            
            //entry.refreshReferenceObject("account");

            //com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation benefitsCalculation = (com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation) getBusinessObjectService().findByPrimaryKey(com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation.class, fieldValues);
            org.kuali.kfs.module.ld.businessobject.BenefitsCalculation benefitsCalculation = (org.kuali.kfs.module.ld.businessobject.BenefitsCalculation) getBusinessObjectService().findByPrimaryKey(com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation.class, fieldValues);
            
            BenefitsCalculationExtension extension = (BenefitsCalculationExtension) benefitsCalculation.getExtension();

            
            
            String offsetAccount = extension.getAccountCodeOffset();

			if(ObjectUtils.isNull(benefitsCalculation)) continue;

			LaborOriginEntry offsetEntry = new LaborOriginEntry(wageEntry);
			offsetEntry.setFinancialObjectCode(benefitsCalculation.getPositionFringeBenefitObjectCode());

			

			// calculate the offsetAmount amount (ledger amt * (benfit pct/100) )
			KualiDecimal fringeBenefitPercent = benefitsCalculation.getPositionFringeBenefitPercent();
			KualiDecimal offsetAmount = fringeBenefitPercent.multiply(
			wageEntry.getTransactionLedgerEntryAmount()).divide(KFSConstants.ONE_HUNDRED.kualiDecimalValue());
			offsetEntry.setTransactionLedgerEntryAmount(offsetAmount.abs());
			
			
            offsetEntry.setAccountNumber(extension.getAccountCodeOffset());
            offsetEntry.setFinancialObjectCode(extension.getObjectCodeOffset());
            
            //Set all the fields required to process through the scrubber and poster jobs
            offsetEntry.setUniversityFiscalPeriodCode(wageEntry.getUniversityFiscalPeriodCode());
            offsetEntry.setChartOfAccountsCode(wageEntry.getChartOfAccountsCode());
            offsetEntry.setUniversityFiscalYear(wageEntry.getUniversityFiscalYear());
            offsetEntry.setSubAccountNumber("-----");
            offsetEntry.setFinancialSubObjectCode("---");
            offsetEntry.setOrganizationReferenceId("");
            offsetEntry.setProjectCode("");
            
            offsetEntry.setTransactionLedgerEntryDescription("GENERATED BENEFIT OFFSET");
            
            String originCode = getParameterService().getParameterValue(LaborEnterpriseFeedStep.class, LdConstants.LABOR_BENEFIT_OFFSET_ORIGIN_CODE);
            
            offsetEntry.setFinancialSystemOriginationCode(originCode);
            offsetEntry.setDocumentNumber(dateTimeService.toString(dateTimeService.getCurrentDate(), "yyyyMMddhhmmssSSS"));


            if(wageEntry.getTransactionDebitCreditCode().equalsIgnoreCase("D")) 
            	offsetAmount = offsetAmount;
            else 
            	offsetAmount = offsetAmount.multiply(new KualiDecimal(-1));
        
            
            
            
            if(offsetAmount.isGreaterThan(new KualiDecimal(0))) {
                offsetEntry.setTransactionDebitCreditCode("C");
            } else if(offsetAmount.isLessThan(new KualiDecimal(0))) {
                offsetEntry.setTransactionDebitCreditCode("D");
            }
            
 
            String docTypeCode = offsetDocTypes;
            if (offsetDocTypes.contains(",")) {
                String[] splits = offsetDocTypes.split(",");
                for(String split : splits) {
                    if(!StringUtils.isEmpty(split)) {
                        docTypeCode = split;
                        break;
                    }
                }
            }
            offsetEntry.setFinancialDocumentTypeCode(docTypeCode);


			offsetEntries.add(offsetEntry);
		}
	
		return offsetEntries;
	}

///
    /**
     * Gets the reconciliationParserService attribute. 
     * @return Returns the reconciliationParserService.
     */
    public ReconciliationParserService getReconciliationParserService() {
        return reconciliationParserService;
    }

    /**
     * Sets the reconciliationParserService attribute value.
     * @param reconciliationParserService The reconciliationParserService to set.
     */
    public void setReconciliationParserService(ReconciliationParserService reconciliationParserService) {
        this.reconciliationParserService = reconciliationParserService;
    }

    /**
     * Gets the reconciliationService attribute. 
     * @return Returns the reconciliationService.
     */
    public ReconciliationService getReconciliationService() {
        return reconciliationService;
    }

    /**
     * Sets the reconciliationService attribute value.
     * @param reconciliationService The reconciliationService to set.
     */
    public void setReconciliationService(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    /**
     * Gets the dateTimeService attribute. 
     * @return Returns the dateTimeService.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
    
    
    
    
    
   
    protected LaborBenefitsCalculationService getLaborBenefitsCalculationService() {
        return laborBenefitsCalculationService;
    }

    public void setLaborBenefitsCalculationService(LaborBenefitsCalculationService laborBenefitsCalculationService) {
        this.laborBenefitsCalculationService = laborBenefitsCalculationService;
    }

    protected LaborPositionObjectBenefitService getLaborPositionObjectBenefitService() {
        return laborPositionObjectBenefitService;
    }

    public void setLaborPositionObjectBenefitService(LaborPositionObjectBenefitService laborPositionObjectBenefitService) {
        this.laborPositionObjectBenefitService = laborPositionObjectBenefitService;
    }

    
    
}


/*

if(!offsetParmValue.equalsIgnoreCase("n") && offsetDocTypes != null) {
    for(List<LaborOriginEntry> entryList : salaryBenefitOffsets.values()) {
        if(entryList != null && entryList.size() > 0) {
            LaborOriginEntry offsetEntry = new LaborOriginEntry();
            KualiDecimal total = new KualiDecimal(0);
            String offsetAccount = "";
            String offsetObjectCode = "";
            
            
            
            //Loop through all the benefit entries to calculate the total for the salary benefit offset entry
            
            for(LaborOriginEntry entry : entryList) {
                if(entry.getTransactionDebitCreditCode().equalsIgnoreCase("D")) {
                    total = total.add(entry.getTransactionLedgerEntryAmount());
                } else {
                    total = total.subtract(entry.getTransactionLedgerEntryAmount());
                }
            }
            
            //No need to process for the salary benefit offset if the total is 0
            if(!total.equals(new KualiDecimal(0))) {
                
                //Lookup the position object benefit to get the object benefit type code
                Collection<PositionObjectBenefit> positionObjectBenefits = getLaborPositionObjectBenefitService().getPositionObjectBenefits(entryList.get(0).getUniversityFiscalYear(), entryList.get(0).getChartOfAccountsCode(), entryList.get(0).getFinancialObjectCode());
                LaborOriginEntry entry = entryList.get(0);
                if (positionObjectBenefits == null || positionObjectBenefits.isEmpty()) {
                    writeMissingBenefitsTypeError(entry, errorStatisticsReport, feederReportData);
                } else {
                    for (PositionObjectBenefit positionObjectBenefit : positionObjectBenefits) {
                        Map<String, Object> fieldValues = new HashMap<String, Object>();
                        fieldValues.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, entry.getUniversityFiscalYear());
                        fieldValues.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, entry.getChartOfAccountsCode());
                        fieldValues.put(LaborPropertyConstants.POSITION_BENEFIT_TYPE_CODE, positionObjectBenefit.getFinancialObjectBenefitsTypeCode());
                        entry.refreshReferenceObject("account");

                        // Cornell Code replaces the original code below
            
                        Map<String, Object> fv = new HashMap<String, Object>();
                        fv.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, entry.getChartOfAccountsCode());
                        fv.put(KFSPropertyConstants.ACCOUNT_NUMBER, entry.getAccount().getAccountNumber());
                        edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute accountExtAttribute = (edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute) getBusinessObjectService().findByPrimaryKey(edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute.class, fv);
                        fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, accountExtAttribute.getLaborBenefitRateCategoryCode());  

                        
                        edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute accountExtAttribute  = (edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute ) entry.getAccount().getExtension();
                        fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, accountExtAttribute.getLaborBenefitRateCategoryCode()); 

                        //original code by rSmart
                    //fieldValues.put(LaborPropertyConstants.LABOR_BENEFIT_RATE_CATEGORY_CODE, entry.getAccount().getLaborBenefitRateCategoryCode());  VRK4

                        //Lookup the benefit calculation to get the offset account number and object code
                        com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation benefitsCalculation = (com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation) getBusinessObjectService().findByPrimaryKey(com.rsmart.kuali.kfs.module.ld.businessobject.BenefitsCalculation.class, fieldValues);
                        
                        BenefitsCalculationExtension extension = (BenefitsCalculationExtension) benefitsCalculation.getExtension();
                        
                        offsetEntry.setAccountNumber(extension.getAccountCodeOffset());
                        offsetEntry.setFinancialObjectCode(extension.getObjectCodeOffset());
                    }
                }
                
                //Set all the fields required to process through the scrubber and poster jobs
                offsetEntry.setUniversityFiscalPeriodCode(entry.getUniversityFiscalPeriodCode());
                offsetEntry.setChartOfAccountsCode(entry.getChartOfAccountsCode());
                offsetEntry.setUniversityFiscalYear(entry.getUniversityFiscalYear());
                offsetEntry.setTransactionLedgerEntryDescription("GENERATED BENEFIT OFFSET");
                offsetEntry.setFinancialSystemOriginationCode("RN");
                offsetEntry.setDocumentNumber(dateTimeService.toString(dateTimeService.getCurrentDate(), "yyyyMMddhhmmssSSS"));
                
                //Only + signed amounts
                offsetEntry.setTransactionLedgerEntryAmount(total.abs());
                
                //Credit if the total is positive and Debit of the total is negative
                if(total.isGreaterThan(new KualiDecimal(0))) {
                    offsetEntry.setTransactionDebitCreditCode("C");
                } else if(total.isLessThan(new KualiDecimal(0))) {
                    offsetEntry.setTransactionDebitCreditCode("D");
                }
                
                //Set the doc type to the value in the LABOR_BENEFIT_OFFSET_DOCTYPE system parameter (the first value if there is a list)
                String docTypeCode = offsetDocTypes;
                if (offsetDocTypes.contains(",")) {
                    String[] splits = offsetDocTypes.split(",");
                    for(String split : splits) {
                        if(!StringUtils.isEmpty(split)) {
                            docTypeCode = split;
                            break;
                        }
                    }
                }
                offsetEntry.setFinancialDocumentTypeCode(docTypeCode);
                
                //Write the offset entry to the file
                enterpriseFeedPs.printf("%s\n", offsetEntry.getLine());
            }
        }
    }
}

dataFileReader.close();
dataFileReader = null;

statusAndErrors.setStatus(new FileReconOkLoadOkStatus());
}
else {
statusAndErrors.setStatus(new FileReconBadLoadAbortedStatus());
}
}
catch (Exception e) {
LOG.error("Caught exception when reconciling/loading done file: " + doneFile, e);
statusAndErrors.setStatus(new ExceptionCaughtStatus());
errorMessages.add(new Message("Caught exception attempting to reconcile/load done file: " + doneFile + ".  File contents are NOT loaded", Message.TYPE_FATAL));
// re-throw the exception rather than returning a value so that Spring will auto-rollback
if (e instanceof RuntimeException) {
throw (RuntimeException) e;
}
else {
// Spring only rolls back when throwing a runtime exception (by default), so we throw a new exception
throw new RuntimeException(e);
}
}
finally {
if (dataFileReader != null) {
try {
    dataFileReader.close();
}
catch (IOException e) {
    LOG.error("IO Exception occured trying to close connection to the data file", e);
    errorMessages.add(new Message("IO Exception occured trying to close connection to the data file", Message.TYPE_FATAL));
}
}
}
}
*/