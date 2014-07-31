package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.krad.service.BusinessObjectService;

public enum VendorDetailFixture {

	ANAK_INC("Anak Inc", false, 4506, 0),
	ADD_ASSOCIATES_INC("ADD Associates Inc", true, 4435, 0),
	NO_SUCH_VENDOR("NO SUCH VENDOR", false, -1, -1);
	
	public final String vendorName;
	public final boolean vendorParentIndicator;
	public final boolean isForeign;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorDetailAssignedIdentifier;
	public final String vendorTypeCode;
	public final String taxNumber;
	
	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier) 
	{
		this.vendorName = vendorName;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorParentIndicator = vendorParentIndicator;
	}
	//kfsVendorWebService.addVendor(vendorName, vendorTypeCode, isForeign, taxNumber, taxNumberType, 
	//ownershipTypeCode, isTaxable, isEInvoice, addresses, contacts, phoneNumbers, supplierDiversitys);

	//addVendor(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber, String taxNumberType, 
	//String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
	//List<VendorAddressParam> addresses,List<VendorContactParam> contacts, List<VendorPhoneNumberParam> phoneNumbers, 
	//List<VendorSupplierDiversityParam> supplierDiversitys) throws Exception {

	
	private VendorDetailFixture(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber,
			String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice) {
		this.vendorName = vendorName;
		this.vendorTypeCode = vendorTypeCode;
		this.isForeign = isForeign;
		this.taxNumber = taxNumber;
	}
	
	public VendorDetail createVendorDetail() {
		VendorDetail vendorDetail = new VendorDetail();
		vendorDetail.setVendorName(this.vendorName);
		vendorDetail.setVendorParentIndicator(this.vendorParentIndicator);
		vendorDetail.setVendorHeaderGeneratedIdentifier(this.vendorHeaderGeneratedIdentifier);
		vendorDetail.setVendorDetailAssignedIdentifier(this.vendorDetailAssignedIdentifier);
		return vendorDetail;
	}

	public VendorDetail createVendorDetail(BusinessObjectService businessObjectService) {
		return (VendorDetail) businessObjectService.retrieve(this.createVendorDetail());
	}
	
	
}
