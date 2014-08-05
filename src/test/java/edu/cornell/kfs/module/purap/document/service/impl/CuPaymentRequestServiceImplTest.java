package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.module.purap.fixture.PaymentRequestFixture;
import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;

@ConfigureContext(session = UserNameFixture.mo14)
public class CuPaymentRequestServiceImplTest extends KualiTestBase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(CuPaymentRequestServiceImplTest.class);

	private PaymentRequestService paymentRequestService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		paymentRequestService = SpringContext
				.getBean(PaymentRequestService.class);

	}

	public void testRemoveIneligibleAdditionalCharges_NoEligibleItems()
			throws Exception {
		changeCurrentUser(UserNameFixture.ccs1);
		// RequisitionDocument requisitionDocument =
		// RequisitionFixture.REQ_NON_B2B.createRequisition(SpringContext.getBean(DocumentService.class));
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN
				.createPurchaseOrderdDocument(SpringContext
						.getBean(DocumentService.class));
		changeCurrentUser(UserNameFixture.mo14);
		PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
				.createPaymentRequestDocument(po.getPurapDocumentIdentifier());
		paymentRequestDocument.initiateDocument();
		paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);
		int numberOfItems = paymentRequestDocument.getItems().size();
		paymentRequestService
				.removeIneligibleAdditionalCharges(paymentRequestDocument);

		assertEquals(numberOfItems, paymentRequestDocument.getItems().size());
	}

	public void testRemoveIneligibleAdditionalCharges_WithEligibleItems()
			throws Exception {
		changeCurrentUser(UserNameFixture.ccs1);
		// RequisitionDocument requisitionDocument =
		// RequisitionFixture.REQ_NON_B2B.createRequisition(SpringContext.getBean(DocumentService.class));
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_NON_B2B_OPEN_TRADE_IN_ITEMS
				.createPurchaseOrderdDocument(SpringContext
						.getBean(DocumentService.class));
		changeCurrentUser(UserNameFixture.mo14);
		PaymentRequestDocument paymentRequestDocument = PaymentRequestFixture.PAYMENT_REQ_DOC
				.createPaymentRequestDocument(po.getPurapDocumentIdentifier());
		paymentRequestDocument.initiateDocument();
		paymentRequestDocument.populatePaymentRequestFromPurchaseOrder(po);

		PaymentRequestItem preqItem = new PaymentRequestItem();
		preqItem.setItemTypeCode(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE);
		// preqItem.setItemQuantity(new KualiDecimal(1));
		preqItem.setItemDescription("test");
		// preqItem.setItemUnitPrice(new BigDecimal(1));
		preqItem.setItemUnitOfMeasureCode("EA");
		paymentRequestDocument.addItem(preqItem);

		int numberOfItems = paymentRequestDocument.getItems().size();
		paymentRequestService
				.removeIneligibleAdditionalCharges(paymentRequestDocument);

		assertFalse(numberOfItems == paymentRequestDocument.getItems().size());
	}

	protected void changeCurrentUser(UserNameFixture sessionUser)
			throws Exception {
		GlobalVariables.setUserSession(new UserSession(sessionUser.toString()));
	}
}
