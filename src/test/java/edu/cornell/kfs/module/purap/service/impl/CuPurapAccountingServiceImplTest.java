package edu.cornell.kfs.module.purap.service.impl;

import java.math.BigDecimal;

import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.fixture.PaymentRequestFixture;
import edu.cornell.kfs.module.purap.fixture.PurapAccountingLineFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionItemFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuPurapAccountingServiceImplTest extends KualiTestBase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPurapAccountingServiceImplTest.class);

	private CuPurapAccountingServiceImpl cuPurapAccountingServiceImpl;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cuPurapAccountingServiceImpl = (CuPurapAccountingServiceImpl) TestUtils.getUnproxiedService("purapAccountingService");
		documentService = SpringContext.getBean(DocumentService.class);

	}
	
	public void testIsFiscalOfficersForAllAcctLines_True() throws Exception {
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		requisitionDocument.addItem(RequisitionItemFixture.REQ_ITEM3.createRequisitionItem());
		changeCurrentUser(UserNameFixture.nja3);
		assertTrue(cuPurapAccountingServiceImpl.isFiscalOfficersForAllAcctLines(requisitionDocument));
	}

	public void testIsFiscalOfficersForAllAcctLines_False() throws Exception {
		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		requisitionDocument.addItem(RequisitionItemFixture.REQ_ITEM2.createRequisitionItem());
		changeCurrentUser(UserNameFixture.nja3);

		assertFalse(cuPurapAccountingServiceImpl.isFiscalOfficersForAllAcctLines(requisitionDocument));
	}

	public void testUpdateAccountAmounts_AccountingLinePercentChanged() throws Exception {
		changeCurrentUser(UserNameFixture.ccs1);

		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		RequisitionItem item = RequisitionItemFixture.REQ_ITEM3.createRequisitionItem();
		item.getSourceAccountingLines().add(PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3.createRequisitionAccount(item.getItemIdentifier()));
		item.refreshNonUpdateableReferences();

		requisitionDocument.addItem(item);

		AccountingDocumentTestUtils.saveDocument(requisitionDocument, documentService);

		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) documentService.getNewDocument(PurchaseOrderDocument.class);

		purchaseOrderDocument
				.populatePurchaseOrderFromRequisition(requisitionDocument);

		purchaseOrderDocument.setContractManagerCode(10);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(true);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription("Description");
		purchaseOrderDocument.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_OPEN);

		purchaseOrderDocument.refreshNonUpdateableReferences();

		purchaseOrderDocument.setVendorShippingPaymentTermsCode("AL");
		purchaseOrderDocument.setVendorPaymentTermsCode("00N30");
		purchaseOrderDocument.refreshNonUpdateableReferences();
		AccountingDocumentTestUtils.saveDocument(purchaseOrderDocument, documentService);

		changeCurrentUser(UserNameFixture.mo14);
		PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(purchaseOrderDocument.getPurapDocumentIdentifier());
		paymentRequestDocument.initiateDocument();
		paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(purchaseOrderDocument);
		paymentRequestDocument.setApplicationDocumentStatus(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_FISCAL_REVIEW);

		paymentRequestDocument.getItem(0).setExtendedPrice(new KualiDecimal(1));
		paymentRequestDocument.getItem(1).setExtendedPrice(new KualiDecimal(4));

		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).setAmount(new KualiDecimal(3));
		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(1))).setAmount(new KualiDecimal(1));
		cuPurapAccountingServiceImpl.updateAccountAmounts(paymentRequestDocument);

		assertEquals(new BigDecimal(75).setScale(2),((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).getAccountLinePercent());
	}

	public void testUpdateAccountAmounts_AccountingLinePercentUnchanged() throws Exception {

		changeCurrentUser(UserNameFixture.ccs1);

		RequisitionDocument requisitionDocument = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
		requisitionDocument.getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(new KualiDecimal(200));
		RequisitionItem item = RequisitionItemFixture.REQ_ITEM3.createRequisitionItem();
		item.getSourceAccountingLines().add(PurapAccountingLineFixture.REQ_ITEM_ACCT_LINE3.createRequisitionAccount(item.getItemIdentifier()));
		item.refreshNonUpdateableReferences();

		requisitionDocument.addItem(item);

		AccountingDocumentTestUtils.saveDocument(requisitionDocument, documentService);

		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) documentService.getNewDocument(PurchaseOrderDocument.class);

		purchaseOrderDocument.populatePurchaseOrderFromRequisition(requisitionDocument);

		purchaseOrderDocument.setContractManagerCode(10);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(true);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription("Description");
		purchaseOrderDocument.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_OPEN);

		purchaseOrderDocument.refreshNonUpdateableReferences();

		purchaseOrderDocument.setVendorShippingPaymentTermsCode("AL");
		purchaseOrderDocument.setVendorPaymentTermsCode("00N30");
		purchaseOrderDocument.refreshNonUpdateableReferences();
		AccountingDocumentTestUtils.saveDocument(purchaseOrderDocument, documentService);

		changeCurrentUser(UserNameFixture.mo14);
		PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC.createPaymentRequestDocument(purchaseOrderDocument.getPurapDocumentIdentifier());
		paymentRequestDocument.initiateDocument();
		paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(purchaseOrderDocument);
		paymentRequestDocument.setApplicationDocumentStatus(PurapConstants.PaymentRequestStatuses.APPDOC_IN_PROCESS);

		paymentRequestDocument.getItem(0).setExtendedPrice(new KualiDecimal(1));
		paymentRequestDocument.getItem(1).setExtendedPrice(new KualiDecimal(4));

		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).setAmount(new KualiDecimal(3));
		((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(1))).setAmount(new KualiDecimal(1));
		cuPurapAccountingServiceImpl.updateAccountAmounts(paymentRequestDocument);

		assertEquals(new BigDecimal(50).setScale(2),((PaymentRequestAccount) (((PaymentRequestItem) paymentRequestDocument.getItems().get(1)).getSourceAccountingLine(0))).getAccountLinePercent());

	}
}
