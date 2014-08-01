package edu.cornell.kfs.vnd.service;

import org.kuali.kfs.sys.ConfigureContext;

import edu.cornell.kfs.vnd.service.params.VendorSupplierDiversityParam;

@ConfigureContext
public enum SupplierDiversityParameterFixture {

	ONE();
	
	private SupplierDiversityParameterFixture () {
		
	}
	
	public VendorSupplierDiversityParam createSupplierDiversityParameter() {
		VendorSupplierDiversityParam supplierDiversityParameter = new VendorSupplierDiversityParam();
		
		return supplierDiversityParameter;
	}
}
