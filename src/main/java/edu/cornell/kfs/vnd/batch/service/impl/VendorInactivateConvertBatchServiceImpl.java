/**
 * @author cab379
 */

package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.service.AttachmentService;
import org.kuali.rice.krad.service.NoteService;

import edu.cornell.kfs.module.receiptProcessing.businessobject.ReceiptProcessing;
import edu.cornell.kfs.vnd.batch.service.VendorInactivateConvertBatchService;

public class VendorInactivateConvertBatchServiceImpl implements VendorInactivateConvertBatchService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VendorInactivateConvertBatchServiceImpl.class);
    
    private BatchInputFileService batchInputFileService;    
    private DateTimeService dateTimeService;
    private List<BatchInputFileType> batchInputFileTypes;
    private AttachmentService attachmentService;
    private NoteService noteService;

	private String reportsDirectoryPath;

    
    public VendorInactivateConvertBatchServiceImpl() {
    }
    
    /**
     * @see org.kuali.kfs.module.ar.batch.service.ReceiptLoadService#loadFiles()
     */
    public boolean processVendorUpdates() {
        
        LOG.info("Beginning processing of all available files for Receipt Batch Upload.");
        boolean result = true;
                     
        //  create a list of the files to process
         Map<String, BatchInputFileType> fileNamesToLoad = getListOfFilesToProcess();
        LOG.info("Found " + fileNamesToLoad.size() + " file(s) to process.");
        
        //  process each file in turn
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad.keySet()) {
            
            LOG.info("Beginning processing of filename: " + inputFileName + ".");
   
            if (attachFiles(inputFileName, fileNamesToLoad.get(inputFileName))) {
                result &= true;
                LOG.info("Successfully loaded csv file");
                processedFiles.add(inputFileName);
            }
            else {
                LOG.error("Failed to load file");
                result &= false;
            }
        }

        //  remove done files
        removeDoneFiles(processedFiles);
       
        return result;
    }
    
    /**
     * Create a collection of the files to process with the mapped value of the BatchInputFileType
     * 
     * @return
     */
    protected Map<String, BatchInputFileType> getListOfFilesToProcess() {

        Map<String, BatchInputFileType> inputFileTypeMap = new LinkedHashMap<String, BatchInputFileType>();

        for (BatchInputFileType batchInputFileType : batchInputFileTypes) {

            List<String> inputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
            if (inputFileNames == null) {
                criticalError("BatchInputFileService.listInputFileNamesWithDoneFile(" + batchInputFileType.getFileTypeIdentifer() + ") returned NULL which should never happen.");
            }
            else {
                // update the file name mapping
                for (String inputFileName : inputFileNames) {

                    // filenames returned should never be blank/empty/null
                    if (StringUtils.isBlank(inputFileName)) {
                        criticalError("One of the file names returned as ready to process [" + inputFileName + "] was blank.  This should not happen, so throwing an error to investigate.");
                    }

                    inputFileTypeMap.put(inputFileName, batchInputFileType);
                }
            }
        }

        return inputFileTypeMap;
    }
    
    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
    */
    public boolean attachFiles(String fileName, BatchInputFileType batchInputFileType) {
        
        boolean result = true;
        
        //  load up the file into a byte array 
        byte[] fileByteContent = safelyLoadFileBytes(fileName);
        
        LOG.info("Attempting to parse the file");
        Object parsedObject = null;
        try {
             parsedObject =  batchInputFileService.parse(batchInputFileType, fileByteContent);
        }
        catch (ParseException e) {
            String errorMessage ="Error parsing batch file: " + e.getMessage();
            LOG.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
        
        //  make sure we got the type we expected, then cast it
        if (!(parsedObject instanceof List)) {
            String errorMessage = "Parsed file was not of the expected type.  Expected [" + List.class + "] but got [" + parsedObject.getClass() + "].";
            criticalError(errorMessage);
        }
        
        
        
        StringBuilder processResults = new StringBuilder();
        processResults.append("\"cardHolder\",\"amount\",\"purchasedate\",\"SharePointPath\",\"filename\",\"Success\"\n");        
       
        List<ReceiptProcessing> receipts =  ((List<ReceiptProcessing>) parsedObject);

        
               
        String outputCsv = processResults.toString();
        getcsvWriter(outputCsv);       
                
        return result;
    }  
    
    
    protected void getcsvWriter(String csvDoc) {
        

        String fileName = "CIT_OUT_" +
            new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(dateTimeService.getCurrentDate()) + ".csv";
        
         //  setup the writer
         File reportFile = new File(fileName);
         BufferedWriter writer = null;
         try {
             writer = new BufferedWriter(new FileWriter(reportFile));
             writer.write(csvDoc);
             writer.close();
         } catch (IOException e1) {
             LOG.error("IOException when trying to write report file");
             e1.printStackTrace();
         }
         
                         
     }
    
 
    protected byte[] safelyLoadFileBytes(String fileName) {
        
        InputStream fileContents;
        byte[] fileByteContent;
        try {
            fileContents = new FileInputStream(fileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("Batch file not found [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        }
        catch (IOException e1) {
            LOG.error("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        }
        return fileByteContent;
    }
    
    /**
     * LOG error and throw RunTimeException
     * 
     * @param errorMessage
     */
    private void criticalError(String errorMessage){
        LOG.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }    
    
    public void setBatchInputFileTypes(List<BatchInputFileType> batchInputFileType) {
        this.batchInputFileTypes = batchInputFileType;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }
    
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }    
    
    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }
             
    public NoteService getNoteService() {
        return noteService;
    }
    
    public void setReportsDirectoryPath(String reportsDirectoryPath) {
		this.reportsDirectoryPath = reportsDirectoryPath;
	}

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }
           
              
    
}

