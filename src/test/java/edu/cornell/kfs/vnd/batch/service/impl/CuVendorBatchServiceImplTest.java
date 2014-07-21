package edu.cornell.kfs.vnd.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.vnd.batch.service.VendorBatchService;


@ConfigureContext(session = ccs1)
public class CuVendorBatchServiceImplTest extends KualiTestBase {

    private VendorBatchService vendorBatchService;
    private ConfigurationService  kualiConfigurationService;
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VendorBatchService.class);
    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/vnd/batch/service/impl/fixture/";
    private static final String HEADER_MISMATCH_FILE_NAME = "vendorBatch_column_header_mismatch";
    private static final String ADD_REQUIRED_FIELD_MISSING_FILE_NAME = "vendorBatch_add_required_field_missing";
    private static final String UPDATE_NOT_EXIST_VENDOR_FILE_NAME = "vendorBatch_update_not_exist_vendor";
    private static final String ADD_VENDOR_OK_FILE_NAME = "vendorBatch_add_vendor_ok";
    private static final String UPDATE_VENDOR_OK_FILE_NAME = "vendorBatch_update_vendor_ok";
    private String batchDirectory;  
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        vendorBatchService = SpringContext.getBean(VendorBatchService.class);
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/vnd/vendorBatch";
        
        //make sure we have a batch directory
        //batchDirectory = SpringContext.getBean(ReceiptProcessingService.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();
                
    }
    
    private void setUpTestFiles(String fileName) throws IOException {

        File dataFileSrc = new File(DATA_FILE_PATH + fileName +".csv");
        File dataFileDest = new File(batchDirectory + "/"+ fileName +".csv");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/"+ fileName +".done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }

    }
    
    private void runTest(String fileName, boolean isSuccess) {
        try {
            setUpTestFiles(fileName);
            if (isSuccess) {
                assertTrue(vendorBatchService.processVendors());                 
            } else {
                assertFalse(vendorBatchService.processVendors());  
            }
        } catch (RuntimeException e) {
            
        } catch (IOException ioe) {
            
        } finally {
            FileUtils.deleteQuietly(new File(batchDirectory + "/"+ fileName +".done"));
        }       
        

    }
    
    public void testCsvColumnHeaderMismatch() {
        
        runTest(HEADER_MISMATCH_FILE_NAME, false);
    }

    public void testAddVendorMissingRequiredField() {
       
        runTest(ADD_REQUIRED_FIELD_MISSING_FILE_NAME, false);        
    }

    public void testUpdateNotExistVendor() {
        
        runTest(UPDATE_NOT_EXIST_VENDOR_FILE_NAME, false);        
        
    }

    public void testAddVendorOK() {
        
        runTest(ADD_VENDOR_OK_FILE_NAME, true);        
    }

    public void testUpdateVendorOK() {
        
        runTest(UPDATE_VENDOR_OK_FILE_NAME, true);        
    }

}
