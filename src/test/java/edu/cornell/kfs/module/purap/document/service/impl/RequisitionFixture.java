package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;

public enum RequisitionFixture {

	REQ_B2B("Description", "B2B", 0, 5314, 4190, "line 1 address",
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", "NY",
			"14850", "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM),

	REQ_B2B_INVALID("Description", "B2B", 0, 5314, 4190, null,
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", "NY",
			"14850", "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM),

	REQ_B2B_CXML("Description", "B2B", 0, 5314, 4190, "line 1 address",
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", "NY",
			"14850", "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM),

	REQ_B2B_CXML_INVALID("Description", "B2B", 0, 5314, 4190, null,
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", null,
			null, "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM),

	REQ_NON_B2B("Description", "STAN", 0, 4291, null, "line 1 address",
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null);

	public final String documentDescription;
	public final String requisitionSourceCode;
	public final Integer vendorDetailAssignedIdentifier;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorContractGeneratedIdentifier;
	public final String vendorLine1Address;
	public final String vendorLine2Address;
	public final String vendorCityName;
	public final String vendorStateCode;
	public final String vendorPostalCode;
	public final String vendorCountryCode;
	public final String vendorEmailAddress;
	public final String vendorFaxNumber;
	public final String vendorAttentionName;
	public final Integer vendorAddressGeneratedIdentifier;

	public final String deliveryBuildingLine1Address;
	public final String deliveryBuildingLine2Address;
	public final String deliveryCityName;
	public final String deliveryBuildingRoomNumber;
	public final String deliveryCountryCode;
	public final String deliveryStateCode;
	public final String deliveryPostalCode;

	public final String billingCityName;
	public final String billingCountryCode;
	public final String billingEmailAddress;
	public final String billingLine1Address;
	public final String billingPhoneNumber;
	public final String billingPostalCode;
	public final String billingStateCode;
	public final String billingName;

	public final RequisitionItemFixture itemFixture;

	private RequisitionFixture(String documentDescription,
			String requisitionSourceCode,
			Integer vendorDetailAssignedIdentifier,
			Integer vendorHeaderGeneratedIdentifier,
			Integer vendorContractGeneratedIdentifier,
			String vendorLine1Address, String vendorLine2Address,
			String vendorCityName, String vendorStateCode,
			String vendorPostalCode, String vendorCountryCode,
			String vendorEmailAddress, String vendorFaxNumber,
			String vendorAttentionName,
			Integer vendorAddressGeneratedIdentifier,
			String deliveryBuildingLine1Address,
			String deliveryBuildingLine2Address, String deliveryCityName,
			String deliveryBuildingRoomNumber, String deliveryCountryCode,
			String deliveryStateCode, String deliveryPostalCode,

			String billingCityName, String billingCountryCode,
			String billingEmailAddress, String billingLine1Address,
			String billingPhoneNumber, String billingPostalCode,
			String billingStateCode, String billingName,
			RequisitionItemFixture itemFixture) {

		this.documentDescription = documentDescription;
		this.requisitionSourceCode = requisitionSourceCode;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
		this.vendorLine1Address = vendorLine1Address;
		this.vendorLine2Address = vendorLine2Address;
		this.vendorCityName = vendorCityName;
		this.vendorStateCode = vendorStateCode;
		this.vendorPostalCode = vendorPostalCode;
		this.vendorCountryCode = vendorCountryCode;
		this.vendorEmailAddress = vendorEmailAddress;
		this.vendorFaxNumber = vendorFaxNumber;
		this.vendorAttentionName = vendorAttentionName;
		this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;

		this.deliveryBuildingLine1Address = deliveryBuildingLine1Address;
		this.deliveryBuildingLine2Address = deliveryBuildingLine2Address;
		this.deliveryCityName = deliveryCityName;
		this.deliveryBuildingRoomNumber = deliveryBuildingRoomNumber;
		this.deliveryCountryCode = deliveryCountryCode;
		this.deliveryStateCode = deliveryStateCode;
		this.deliveryPostalCode = deliveryPostalCode;

		this.billingCityName = billingCityName;
		this.billingCountryCode = billingCountryCode;
		this.billingEmailAddress = billingEmailAddress;
		this.billingLine1Address = billingLine1Address;
		this.billingPhoneNumber = billingPhoneNumber;
		this.billingPostalCode = billingPostalCode;
		this.billingStateCode = billingStateCode;
		this.billingName = billingName;

		this.itemFixture = itemFixture;

	}

	public RequisitionDocument createRequisition() throws WorkflowException {
		RequisitionDocument requisitionDocument = (RequisitionDocument) SpringContext
				.getBean(DocumentService.class).getNewDocument(
						RequisitionDocument.class);
		requisitionDocument.initiateDocument();
		requisitionDocument.getDocumentHeader().setDocumentDescription(
				documentDescription);

		requisitionDocument.setRequisitionSourceCode(requisitionSourceCode);
		// set vendor info
		requisitionDocument
				.setVendorDetailAssignedIdentifier(vendorDetailAssignedIdentifier);
		requisitionDocument
				.setVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedIdentifier);

		requisitionDocument
				.setVendorContractGeneratedIdentifier(vendorContractGeneratedIdentifier);
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

		requisitionDocument.setVendorLine1Address(vendorLine1Address);
		requisitionDocument.setVendorLine2Address(vendorLine2Address);
		requisitionDocument.setVendorCityName(vendorCityName);
		requisitionDocument.setVendorStateCode(vendorStateCode);
		requisitionDocument.setVendorPostalCode(vendorPostalCode);
		requisitionDocument.setVendorCountryCode(vendorCountryCode);
		requisitionDocument.setVendorEmailAddress(vendorEmailAddress);
		requisitionDocument.setVendorFaxNumber(vendorFaxNumber);
		requisitionDocument.setVendorAttentionName(vendorAttentionName);
		requisitionDocument
				.setVendorAddressGeneratedIdentifier(vendorAddressGeneratedIdentifier);

		requisitionDocument
				.setDeliveryBuildingLine1Address(deliveryBuildingLine1Address);
		requisitionDocument
				.setDeliveryBuildingLine2Address(deliveryBuildingLine2Address);
		requisitionDocument.setDeliveryCityName(deliveryCityName);
		requisitionDocument
				.setDeliveryBuildingRoomNumber(deliveryBuildingRoomNumber);
		requisitionDocument.setDeliveryCountryCode(deliveryCountryCode);
		requisitionDocument.setDeliveryStateCode(deliveryStateCode);
		requisitionDocument.setDeliveryPostalCode(deliveryPostalCode);

		requisitionDocument.setBillingCityName(billingCityName);
		requisitionDocument.setBillingCountryCode(billingCountryCode);
		requisitionDocument.setBillingEmailAddress(billingEmailAddress);
		requisitionDocument.setBillingLine1Address(billingLine1Address);
		requisitionDocument.setBillingPhoneNumber(billingPhoneNumber);
		requisitionDocument.setBillingPostalCode(billingPostalCode);
		requisitionDocument.setBillingStateCode(billingStateCode);
		requisitionDocument.setBillingName(billingName);

		if (itemFixture != null) {
			requisitionDocument.addItem(itemFixture.createRequisitionItem());
		}

		requisitionDocument.refreshNonUpdateableReferences();

		return requisitionDocument;
	}

	public RequisitionDocument createRequisition(DocumentService documentService)
			throws WorkflowException {
		RequisitionDocument requisitionDocument = this.createRequisition();

		documentService.saveDocument(requisitionDocument);
		requisitionDocument.refreshNonUpdateableReferences();

		return requisitionDocument;
	}
}
