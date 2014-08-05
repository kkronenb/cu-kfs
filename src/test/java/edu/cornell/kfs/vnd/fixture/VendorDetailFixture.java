package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.krad.service.BusinessObjectService;

public enum VendorDetailFixture {

	ANAK_INC("Anak Inc", false, 4506, 0),
	ADD_ASSOCIATES_INC("ADD Associates Inc", true, 4435, 0),
	NO_SUCH_VENDOR("NO SUCH VENDOR", false, -1, -1),
	VENDOR_TO_CREATE("Test Vendor", true, 1234567, 0, "", false, "999887676","", "", true, false );

	
//	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
//			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String vendorTypeCode, boolean isForeign, String taxNumber,
//			String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEinvoice) {

	//addVendor(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber, String taxNumberType, 
	//String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
	//List<VendorAddressParam> addresses,List<VendorContactParam> contacts, List<VendorPhoneNumberParam> phoneNumbers, 
	//List<VendorSupplierDiversityParam> supplierDiversitys) throws Exception {

	
	public final String vendorName;
	public final boolean vendorParentIndicator;
	public final boolean isForeign;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorDetailAssignedIdentifier;
	public final String vendorTypeCode;
	public final String taxNumber;
	public final String taxNumberType;
	public final String ownershipTypeCode;
	public final boolean isTaxable;
	public final boolean isEInvoice;
	
	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier) 
	{
		this.vendorName = vendorName;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorParentIndicator = vendorParentIndicator;
		this.vendorTypeCode = "";
		this.isForeign = false;
		this.taxNumber = "987654321";
		this.taxNumberType = "";
		this.ownershipTypeCode = "";
		this.isTaxable = true;
		this.isEInvoice = true;
	}
	
	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier, String vendorTypeCode, boolean isForeign, String taxNumber,
			String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice) {
		this.vendorName = vendorName;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorParentIndicator = vendorParentIndicator;
		this.vendorTypeCode = vendorTypeCode;
		this.isForeign = isForeign;
		this.taxNumber = taxNumber;
		this.taxNumberType = taxNumberType;
		this.ownershipTypeCode = ownershipTypeCode;
		this.isTaxable = isTaxable;
		this.isEInvoice = isEInvoice;
	}
	
	public VendorDetail createVendorDetail() {
		VendorDetail vendorDetail = new VendorDetail();
		vendorDetail.setVendorName(this.vendorName);
		vendorDetail.setVendorParentIndicator(this.vendorParentIndicator);
		vendorDetail.setVendorHeaderGeneratedIdentifier(this.vendorHeaderGeneratedIdentifier);
		vendorDetail.setVendorDetailAssignedIdentifier(this.vendorDetailAssignedIdentifier);
		vendorDetail.setVendorHeader(VendorHeaderFixture.ONE.createVendorHeader());
		return vendorDetail;
	}

	public VendorDetail createVendorDetail(BusinessObjectService businessObjectService) {
		return (VendorDetail) businessObjectService.retrieve(this.createVendorDetail());
	}
	
	
}
