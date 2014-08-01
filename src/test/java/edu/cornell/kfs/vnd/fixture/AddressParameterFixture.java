package edu.cornell.kfs.vnd.fixture;

import org.kuali.kfs.sys.ConfigureContext;

import edu.cornell.kfs.vnd.service.params.VendorAddressParam;

@ConfigureContext
public enum AddressParameterFixture {

	ONE();
	
	private AddressParameterFixture() {
		
	}
	
	public VendorAddressParam createVendorAddressParam() {
		VendorAddressParam vendorAddressParam = new VendorAddressParam();
		
		return vendorAddressParam;
	}
}
