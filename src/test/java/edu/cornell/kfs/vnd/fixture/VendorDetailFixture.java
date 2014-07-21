package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.krad.service.BusinessObjectService;

public enum VendorDetailFixture {

	ANAK_INC("Anak Inc", false, 4506, 2);
	
	public final String vendorName;
	public final boolean vendorParentIndicator;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorDetailAssignedIdentifier;
	
	private VendorDetailFixture(String vendorName, boolean vendorParentIndicator, 
			Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier) 
	{
		this.vendorName = vendorName;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorParentIndicator = vendorParentIndicator;
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
