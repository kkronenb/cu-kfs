package edu.cornell.kfs.module.purap.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService;

public class IWantDocumentFeedStep extends AbstractStep {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IWantDocumentFeedStep.class);

	protected IWantDocumentFeedService iWantDocumentFeedService;

	/**
	 * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, Date jobRunDate) {
		return iWantDocumentFeedService.processIWantDocumentFiles();

	}

	/**
	 * Sete the iWantDocumentFeedService.
	 * 
	 * @param iWantDocumentFeedService
	 */
	public void setiWantDocumentFeedService(IWantDocumentFeedService iWantDocumentFeedService) {
		this.iWantDocumentFeedService = iWantDocumentFeedService;
	}

}
