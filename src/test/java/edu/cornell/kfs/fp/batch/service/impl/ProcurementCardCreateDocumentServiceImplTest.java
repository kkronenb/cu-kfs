package edu.cornell.kfs.fp.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;
import edu.cornell.kfs.fp.batch.service.ProcurementCardSummaryFeedService;
import edu.cornell.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl;
import edu.cornell.kfs.fp.batch.service.impl.ProcurementCardLoadFlatTransactionsServiceImpl;

import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.dataaccess.UnitTestSqlDao;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.core.api.config.property.ConfigurationService;


@ConfigureContext(session = kfs)
public class ProcurementCardCreateDocumentServiceImplTest  extends KualiTestBase {

    private ProcurementCardLoadFlatTransactionsServiceImpl procurementCardLoadFlatTransactionsService;
    private ProcurementCardCreateDocumentService procurementCardCreateDocumentService;
    private ConfigurationService  kualiConfigurationService;
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardSummaryFeedService.class);
    
    private UnitTestSqlDao unitTestSqlDao;
	
    private static String delTable = "DELETE FROM FP_PRCRMNT_CARD_TRN_MT";
	private static String transAmt = "SELECT * FROM FP_PRCRMNT_TRN_DTL_T";

    
    private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/fp/batch/service/fixture/fp_pcdo_usbank_2014267.data";    
    private String batchDirectory;  

    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        procurementCardLoadFlatTransactionsService = SpringContext.getBean(ProcurementCardLoadFlatTransactionsServiceImpl.class);
        procurementCardCreateDocumentService = SpringContext.getBean(ProcurementCardCreateDocumentService.class);
        
        kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);
        batchDirectory = kualiConfigurationService.getPropertyValueAsString(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY) + "/fp/procurementCard";
        unitTestSqlDao = SpringContext.getBean(UnitTestSqlDao.class);
        
        //make sure we have a batch directory
        //batchDirectory = SpringContext.getBean(ReceiptProcessingService.class).getDirectoryPath();
        File batchDirectoryFile = new File(batchDirectory);
        batchDirectoryFile.mkdir();

        //copy the data file into place
        File dataFileSrc = new File(DATA_FILE_PATH);
        File dataFileDest = new File(batchDirectory + "/fp_pcdo_usbank_2014267.data");
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        //create .done file
        String doneFileName = batchDirectory + "/fp_pcdo_usbank_2014267.done";
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            LOG.info("Creating done file: " + doneFile.getAbsolutePath());
            doneFile.createNewFile();
        }
                       
        
    }
    
    public void testCanLoadFiles() {        
       
    	unitTestSqlDao.sqlCommand(delTable);
    	List summaryResults1 =  unitTestSqlDao.sqlSelect(transAmt);
    	assertTrue(procurementCardLoadFlatTransactionsService.loadProcurementCardFile(batchDirectory + "/fp_pcdo_usbank_2014267.data"));                                                       
    	assertTrue(procurementCardCreateDocumentService.createProcurementCardDocuments());
    	List summaryResults2 =  unitTestSqlDao.sqlSelect(transAmt);
    	
    	assertEquals(summaryResults1.size()+1, summaryResults2.size());
    }

	
    
}