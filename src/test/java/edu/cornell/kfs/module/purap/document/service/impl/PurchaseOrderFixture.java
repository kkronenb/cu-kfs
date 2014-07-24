package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;

public enum PurchaseOrderFixture {
	PO_B2B(RequisitionFixture.REQ_B2B, 10, true, "Description", PurchaseOrderStatuses.APPDOC_PENDING_CXML),
	PO_NON_B2B_OPEN(RequisitionFixture.REQ_NON_B2B, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_IN_PROCESS(RequisitionFixture.REQ_NON_B2B, 10, true, "Description", PurchaseOrderStatuses.APPDOC_IN_PROCESS);

	public final RequisitionFixture requisitionFixture;
	public final Integer contractManagerCode;
	public final boolean purchaseOrderCurrentIndicator;
	public final String documentDescription;
	public final String applicationDocumentStatus;

	private PurchaseOrderFixture(RequisitionFixture requisitionFixture,
			Integer contractManagerCode, boolean purchaseOrderCurrentIndicator,
			String documentDescription, String applicationDocumentStatus) {
		this.requisitionFixture = requisitionFixture;
		this.contractManagerCode = contractManagerCode;
		this.purchaseOrderCurrentIndicator = purchaseOrderCurrentIndicator;
		this.documentDescription = documentDescription;
		this.applicationDocumentStatus = applicationDocumentStatus;
	}

	public PurchaseOrderDocument createPurchaseOrderdDocument()throws WorkflowException {
		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PurchaseOrderDocument.class);
		purchaseOrderDocument.populatePurchaseOrderFromRequisition(requisitionFixture.createRequisition());
		purchaseOrderDocument.setContractManagerCode(this.contractManagerCode);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(this.purchaseOrderCurrentIndicator);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription(this.documentDescription);
		purchaseOrderDocument.setApplicationDocumentStatus(applicationDocumentStatus);

		purchaseOrderDocument.refreshNonUpdateableReferences();
		purchaseOrderDocument.prepareForSave();
		AccountingDocumentTestUtils.saveDocument(purchaseOrderDocument, SpringContext.getBean(DocumentService.class));

		return purchaseOrderDocument;
	}

}
