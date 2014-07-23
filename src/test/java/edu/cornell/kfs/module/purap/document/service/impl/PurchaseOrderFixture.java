package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;

public enum PurchaseOrderFixture {
	PO_B2B(RequisitionFixture.REQ_B2B, 10, true, "Description");

	public final RequisitionFixture requisitionFixture;
	public final Integer contractManagerCode;
	public final boolean purchaseOrderCurrentIndicator;
	public final String documentDescription;

	private PurchaseOrderFixture(RequisitionFixture requisitionFixture,
			Integer contractManagerCode, boolean purchaseOrderCurrentIndicator,
			String documentDescription) {
		this.requisitionFixture = requisitionFixture;
		this.contractManagerCode = contractManagerCode;
		this.purchaseOrderCurrentIndicator = purchaseOrderCurrentIndicator;
		this.documentDescription = documentDescription;
	}

	public PurchaseOrderDocument createPurchaseOrderdDocument()throws WorkflowException {
		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PurchaseOrderDocument.class);
		purchaseOrderDocument.populatePurchaseOrderFromRequisition(requisitionFixture.createRequisition());
		purchaseOrderDocument.setContractManagerCode(this.contractManagerCode);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(this.purchaseOrderCurrentIndicator);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription(this.documentDescription);

		SpringContext.getBean(DocumentService.class).saveDocument(purchaseOrderDocument);
		purchaseOrderDocument.refreshNonUpdateableReferences();

		return purchaseOrderDocument;
	}

}
