package edu.cornell.kfs.vnd.fixture;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public enum VendorDetailExtensionFixture {
	
	EXTENSION(true);
	
	public final boolean isEinvoice;
	

	private VendorDetailExtensionFixture(boolean isEinvoice) {
		this.isEinvoice = isEinvoice;
	}
	
	public VendorDetailExtension createVendorDetailExtension() {
		VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
		vendorDetailExtension.setEinvoiceVendorIndicator(isEinvoice);
		return vendorDetailExtension;
	}
}
