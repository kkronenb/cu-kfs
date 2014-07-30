package edu.cornell.kfs.vnd.service;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.vnd.fixture.VendorDetailFixture;

@ConfigureContext
public class KFSVendorWebServiceImplTest extends KualiTestBase {

	KFSVendorWebService kfsVendorWebService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		kfsVendorWebService = new KFSVendorWebServiceImpl();
	}
	
	public void testVendorWebService() {
		try {
			kfsVendorWebService.retrieveKfsVendor(VendorDetailFixture.ANAK_INC.vendorName, "test");
//		kfsVendorWebService.addVendor(vendorName, vendorTypeCode, isForeign, taxNumber, taxNumberType, ownershipTypeCode, isTaxable, isEInvoice, addresses, contacts, phoneNumbers, supplierDiversitys)
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
