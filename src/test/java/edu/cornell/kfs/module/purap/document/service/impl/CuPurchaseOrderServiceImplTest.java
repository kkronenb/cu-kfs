package edu.cornell.kfs.module.purap.document.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.dataaccess.PurchaseOrderDao;
import org.kuali.kfs.module.purap.document.service.PurchaseOrderService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.exception.ValidationException;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuPurchaseOrderServiceImplTest extends KualiTestBase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(CuPurchaseOrderServiceImplTest.class);

	private PurchaseOrderService purchaseOrderService;
	protected PurchaseOrderDao purchaseOrderDao;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		purchaseOrderService = SpringContext
				.getBean(PurchaseOrderService.class);
		purchaseOrderDao = SpringContext.getBean(PurchaseOrderDao.class);
		documentService = SpringContext.getBean(DocumentService.class);

	}

	private RequisitionDocument createNonB2BReq() throws WorkflowException {
		RequisitionDocument requisitionDocument = (RequisitionDocument) documentService
				.getNewDocument(RequisitionDocument.class);
		requisitionDocument.initiateDocument();
		requisitionDocument.getDocumentHeader().setDocumentDescription(
				"Description");
		// set vendor info
		requisitionDocument.setVendorDetailAssignedIdentifier(0);
		requisitionDocument.setVendorHeaderGeneratedIdentifier(4291);

		requisitionDocument.setVendorContractGeneratedIdentifier(null);
		requisitionDocument.refreshReferenceObject("vendorContract");

		// retrieve vendor based on selection from vendor lookup
		requisitionDocument.refreshReferenceObject("vendorDetail");
		requisitionDocument.templateVendorDetail(requisitionDocument
				.getVendorDetail());

		// populate default address based on selected vendor
		VendorAddress defaultAddress = SpringContext.getBean(
				VendorService.class).getVendorDefaultAddress(
				requisitionDocument.getVendorDetail().getVendorAddresses(),
				requisitionDocument.getVendorDetail().getVendorHeader()
						.getVendorType().getAddressType()
						.getVendorAddressTypeCode(),
				requisitionDocument.getDeliveryCampusCode());
		requisitionDocument.templateVendorAddress(defaultAddress);

		// vendor address holds method of po transmission that should be used
		requisitionDocument
				.setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension) defaultAddress
						.getExtension())
						.getPurchaseOrderTransmissionMethodCode());

		requisitionDocument.setVendorLine1Address("line 1 address");
		requisitionDocument.setVendorLine2Address("line 2 address");
		requisitionDocument.setVendorCityName("city");
		requisitionDocument.setVendorStateCode("NY");
		requisitionDocument.setVendorPostalCode("14850");
		requisitionDocument.setVendorCountryCode("US");
		requisitionDocument.setVendorEmailAddress("abc@email.com");
		requisitionDocument.setVendorFaxNumber("6072203712");
		requisitionDocument.setVendorAttentionName("attn name");
		requisitionDocument.setVendorAddressGeneratedIdentifier(1);
		// Method of PO Transmission on Vendor Address should be the default
		// when a vendor is selected.
		// set purchasing document value for po transmission method
		// requisitionDocument.setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension)vendorAddress.getExtension()).getPurchaseOrderTransmissionMethodCode());

		documentService.saveDocument(requisitionDocument);
		requisitionDocument.refreshNonUpdateableReferences();

		return requisitionDocument;
	}

	private RequisitionDocument createB2BReq() throws WorkflowException {
		RequisitionDocument requisitionDocument = (RequisitionDocument) documentService
				.getNewDocument(RequisitionDocument.class);
		requisitionDocument.initiateDocument();
		requisitionDocument.getDocumentHeader().setDocumentDescription(
				"Description");

		requisitionDocument.setRequisitionSourceCode("B2B");
		// set vendor info
		requisitionDocument.setVendorDetailAssignedIdentifier(0);
		requisitionDocument.setVendorHeaderGeneratedIdentifier(5314);

		requisitionDocument.setVendorContractGeneratedIdentifier(4190);
		requisitionDocument.refreshReferenceObject("vendorContract");

		// retrieve vendor based on selection from vendor lookup
		requisitionDocument.refreshReferenceObject("vendorDetail");
		requisitionDocument.templateVendorDetail(requisitionDocument
				.getVendorDetail());

		// populate default address based on selected vendor
		VendorAddress defaultAddress = SpringContext.getBean(
				VendorService.class).getVendorDefaultAddress(
				requisitionDocument.getVendorDetail().getVendorAddresses(),
				requisitionDocument.getVendorDetail().getVendorHeader()
						.getVendorType().getAddressType()
						.getVendorAddressTypeCode(),
				requisitionDocument.getDeliveryCampusCode());
		requisitionDocument.templateVendorAddress(defaultAddress);

		// vendor address holds method of po transmission that should be used
		requisitionDocument
				.setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension) defaultAddress
						.getExtension())
						.getPurchaseOrderTransmissionMethodCode());

		requisitionDocument.setVendorLine1Address("line 1 address");
		requisitionDocument.setVendorLine2Address("line 2 address");
		requisitionDocument.setVendorCityName("city");
		requisitionDocument.setVendorStateCode("NY");
		requisitionDocument.setVendorPostalCode("14850");
		requisitionDocument.setVendorCountryCode("US");
		requisitionDocument.setVendorEmailAddress("abc@email.com");
		requisitionDocument.setVendorFaxNumber("6072203712");
		requisitionDocument.setVendorAttentionName("attn name");
		requisitionDocument.setVendorAddressGeneratedIdentifier(1);


		requisitionDocument
				.setDeliveryBuildingLine1Address("Delivery Line 1 address");
		requisitionDocument
				.setDeliveryBuildingLine2Address("Delivery Line 2 address");
		requisitionDocument.setDeliveryCityName("Delivery City Name");
		requisitionDocument.setDeliveryBuildingRoomNumber("110");
		requisitionDocument.setDeliveryCountryCode("US");
		requisitionDocument.setDeliveryStateCode("NY");
		requisitionDocument.setDeliveryPostalCode("14850");

		requisitionDocument.setBillingCityName("City Name");
		requisitionDocument.setBillingCountryCode("US");
		requisitionDocument.setBillingEmailAddress("abc@email.com");
		requisitionDocument.setBillingLine1Address("billing line 1 address");
		requisitionDocument.setBillingPhoneNumber("607-220-3712");
		requisitionDocument.setBillingPostalCode("14850");
		requisitionDocument.setBillingStateCode("NY");
		requisitionDocument.setBillingName("Billing name");

		// item
		RequisitionItem item = new RequisitionItem();
		item.setItemLineNumber(new Integer(1));
		item.setItemUnitOfMeasureCode("EA");
		item.setItemCatalogNumber("1234567");
		item.setItemDescription("item desc");
		item.setItemTypeCode("ITEM");
		item.setExternalOrganizationB2bProductTypeName("Punchout");
		item.setExtendedPrice(new KualiDecimal(1));
		item.setItemQuantity(new KualiDecimal(1));
		item.setPurchasingCommodityCode("80141605");
		item.setItemUnitPrice(new BigDecimal(1));

		PurApAccountingLine purapAcctLine = new RequisitionAccount();
		purapAcctLine.setAccountLinePercent(new BigDecimal(100));
		purapAcctLine.setAccountNumber("1000710");
		purapAcctLine.setChartOfAccountsCode("IT");
		purapAcctLine.setFinancialObjectCode("6100");
		purapAcctLine.setAmount(new KualiDecimal(1));

		item.getSourceAccountingLines().add(purapAcctLine);

		requisitionDocument.addItem(item);

		documentService.saveDocument(requisitionDocument);
		requisitionDocument.refreshNonUpdateableReferences();

		return requisitionDocument;
	}

	private PurchaseOrderDocument createPoFromReq(
			RequisitionDocument requisitionDocument) throws WorkflowException {
		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) documentService
				.getNewDocument(PurchaseOrderDocument.class);
		purchaseOrderDocument
				.populatePurchaseOrderFromRequisition(requisitionDocument);
		purchaseOrderDocument.setContractManagerCode(10);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(true);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription(
				"Description");

		documentService.saveDocument(purchaseOrderDocument);
		purchaseOrderDocument.refreshNonUpdateableReferences();

		return purchaseOrderDocument;
	}

	public void DISABLED_testPerformPurchaseOrderFirstTransmitViaPrinting()
			throws Exception {
		RequisitionDocument requisitionDocument = createNonB2BReq();

		PurchaseOrderDocument po = createPoFromReq(requisitionDocument);

		po.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_OPEN);

		po.refreshNonUpdateableReferences();

		po.prepareForSave();
		AccountingDocumentTestUtils.saveDocument(po, documentService);

		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
		try {
			DateTimeService dtService = SpringContext
					.getBean(DateTimeService.class);
			StringBuffer sbFilename = new StringBuffer();
			sbFilename.append("PURAP_PO_QUOTE_REQUEST_LIST");
			sbFilename.append(po.getPurapDocumentIdentifier());
			sbFilename.append("_");
			sbFilename.append(dtService.getCurrentDate().getTime());
			sbFilename.append(".pdf");
			purchaseOrderService.performPurchaseOrderFirstTransmitViaPrinting(
					po.getDocumentNumber(), baosPDF);
			assertTrue(baosPDF.size() > 0);
		} catch (ValidationException e) {
			LOG.warn("Caught ValidationException while trying to retransmit PO with doc id "
					+ po.getDocumentNumber());
		} finally {
			if (baosPDF != null) {
				baosPDF.reset();
			}
		}
	}

	/**
	 * Tests that the PurchaseOrderService would do the completePurchaseOrder
	 * for non B2B purchase orders.
	 * 
	 * @throws Exception
	 */
	public void DISABLED_testCompletePurchaseOrderAmendment_NonB2B()
			throws Exception {
		RequisitionDocument requisitionDocument = createNonB2BReq();

		PurchaseOrderDocument po = createPoFromReq(requisitionDocument);

		po.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_IN_PROCESS);
		po.refreshNonUpdateableReferences();
		po.prepareForSave();
		AccountingDocumentTestUtils.saveDocument(po, documentService);
		purchaseOrderService.completePurchaseOrderAmendment(po);

		assertTrue(po.isPurchaseOrderCurrentIndicator());
		assertFalse(po.isPendingActionIndicator());

	}

	/**
	 * Tests that the PurchaseOrderService would do the completePurchaseOrder
	 * for B2B purchase orders.
	 * 
	 * @throws Exception
	 */
	public void testCompletePurchaseOrderAmendment_B2B() throws Exception {
		RequisitionDocument requisitionDocument = createB2BReq();

		PurchaseOrderDocument po = createPoFromReq(requisitionDocument);
		po.setApplicationDocumentStatus(PurchaseOrderStatuses.APPDOC_PENDING_CXML);

		po.refreshNonUpdateableReferences();
		po.prepareForSave();
		AccountingDocumentTestUtils.saveDocument(po, documentService);
		purchaseOrderService.completePurchaseOrderAmendment(po);

		assertTrue(po.isPurchaseOrderCurrentIndicator());
		assertFalse(po.isPendingActionIndicator());
		// assertNotNull(po.getPurchaseOrderLastTransmitTimestamp());

	}

}
