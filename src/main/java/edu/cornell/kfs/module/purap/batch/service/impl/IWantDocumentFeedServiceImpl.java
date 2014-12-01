package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

import com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService;

import edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService;
import edu.cornell.kfs.module.purap.businessobject.IWantDocumentBatchFeed;

public class IWantDocumentFeedServiceImpl implements IWantDocumentFeedService {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IWantDocumentFeedServiceImpl.class);
	
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType iWantDocumentInputFileType;

	@Override
	public boolean processIWantDocumentFiles() {
		
		 List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(iWantDocumentInputFileType);

	        for (String incomingFileName : fileNamesToLoad) {
	            try {
	                LOG.debug("processIWantDocumentFiles  () Processing " + incomingFileName);
	                IWantDocumentBatchFeed batchFeed = parseInputFile(incomingFileName);

	                if (batchFeed != null && !batchFeed.getBatchIWantDocuments().isEmpty()) {
	                    loadIWantDocuments(batchFeed, incomingFileName, GlobalVariables.getMessageMap());
	                }
	            }
	            catch (RuntimeException e) {
	                LOG.error("Caught exception trying to load disbursement voucher file: " + incomingFileName, e);
	                throw new RuntimeException("Caught exception trying to load disbursement voucher file: " + incomingFileName, e);
	            }
	        }
		return true;

	}
	
	private IWantDocumentBatchFeed parseInputFile(String incomingFileName){
		FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        Object parsed = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            parsed = batchInputFileService.parse(iWantDocumentInputFileType, fileByteContent);
        }
        catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        }
        catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());
        }
        
        return (IWantDocumentBatchFeed)parsed;

	}
	
    public void loadIWantDocuments(IWantDocumentBatchFeed batchFeed, String incomingFileName, MessageMap MessageMap) {
//        // get new batch record for the load
//
//        boolean batchHasErrors = false;
//        for (BatchDisbursementVoucherDocument batchDisbursementVoucherDocument : batchFeed.getBatchDisbursementVoucherDocuments()) {
//            batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_DV_RECORDS_READ, 1);
//            batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_ACCOUNTING_RECORDS_READ, batchDisbursementVoucherDocument.getSourceAccountingLines().size());
//
//            // get defaults for DV chart/org
//            DisbursementVoucherBatchDefault batchDefault = null;
//            if (StringUtils.isNotBlank(batchFeed.getUnitCode())) {
//                batchDefault = getDisbursementVoucherBatchDefault(batchFeed.getUnitCode());
//            }
//
//            MessageMap documentMessageMap = new MessageMap();
//            batchFeedHelperService.performForceUppercase(DisbursementVoucherDocument.class.getName(), batchDisbursementVoucherDocument);
//
//            // create and route doc as system user
//            // create and route doc as system user
//            UserSession actualUserSession = GlobalVariables.getUserSession();
//            GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
//            MessageMap globalMessageMap = GlobalVariables.getMessageMap();
//            GlobalVariables.setMessageMap(documentMessageMap);
//
//            DisbursementVoucherDocument disbursementVoucherDocument = null;
//            try {
//                disbursementVoucherDocument = populateDisbursementVoucherDocument(disbursementVoucherBatch, batchDisbursementVoucherDocument, batchDefault, documentMessageMap);
//
//                // if the document is valid create GLPEs, Save and Approve
//                if (documentMessageMap.hasNoErrors()) {
//                    businessObjectService.save(disbursementVoucherDocument.getExtension());
//                    documentService.routeDocument(disbursementVoucherDocument, "", null);
//
//                    if (documentMessageMap.hasNoErrors()) {
//                        batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_DV_RECORDS_WRITTEN, 1);
//                        batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_ACCOUNTING_RECORDS_WRITTEN, disbursementVoucherDocument.getSourceAccountingLines().size());
//                        batchStatus.updateStatistics(FPConstants.BatchReportStatisticKeys.NUM_GLPE_RECORDS_WRITTEN, disbursementVoucherDocument.getGeneralLedgerPendingEntries().size());
//
//                        batchStatus.getBatchDisbursementVoucherDocuments().add(disbursementVoucherDocument);
//                    }
//                }
//            }
//            catch (WorkflowException e) {
//                LOG.error("Unable to route DV: " + e.getMessage());
//                throw new RuntimeException("Unable to route DV: " + e.getMessage(), e);
//            }
//            catch (ValidationException e1) {
//                // will be reported in audit report
//            }
//            finally {
//                GlobalVariables.setUserSession(actualUserSession);
//                GlobalVariables.setMessageMap(globalMessageMap);
//            }
//
//            if (documentMessageMap.hasErrors()) {
//                batchHasErrors = true;
//            }
//
//            // populate summary line and add to report
//            DisbursementVoucherBatchSummaryLine batchSummaryLine = populateBatchSummaryLine(disbursementVoucherDocument, documentMessageMap);
//            batchStatus.getBatchSummaryLines().add(batchSummaryLine);
//        }
//
//        // indicate in global map there were errors (for batch upload screen)
//        if (batchHasErrors) {
//            MessageMap.putError(KFSConstants.GLOBAL_ERRORS, FPKeyConstants.ERROR_BATCH_DISBURSEMENT_VOUCHER_ERRORS_NOTIFICATION);
//        }
//
//        batchFeedHelperService.removeDoneFile(incomingFileName);
    }

	
    /**
     * @return Returns the batchInputFileService.
     */
    protected BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * @param batchInputFileService The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

	public BatchInputFileType getiWantDocumentInputFileType() {
		return iWantDocumentInputFileType;
	}

	public void setiWantDocumentInputFileType(
			BatchInputFileType iWantDocumentInputFileType) {
		this.iWantDocumentInputFileType = iWantDocumentInputFileType;
	}

}
