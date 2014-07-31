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
			String result = kfsVendorWebService.retrieveKfsVendor(VendorDetailFixture.NO_SUCH_VENDOR.vendorName, "not a type");
			
			assertTrue(result.equals("Vendor Not Found"));
			
			kfsVendorWebService.addVendor();
			kfsVendorWebService.addVendor(vendorName, vendorTypeCode, isForeign, taxNumber, taxNumberType, ownershipTypeCode, isTaxable, isEInvoice, addresses, contacts, phoneNumbers, supplierDiversitys);
//			kfsVendorWebService.retrieveKfsVendorByEin(vendorEin);
//			kfsVendorWebService.retrieveKfsVendorByNamePlusLastFour(vendorName, lastFour);
//			kfsVendorWebService.updateVendor(vendorName, vendorTypeCode, isForeign, vendorNumber, ownershipTypeCode, isTaxable, isEInvoice, addresses, contacts, phoneNumbers, supplierDiversitys);
//			kfsVendorWebService.uploadAtt(vendorId, fileData, fileName, noteText);
//			kfsVendorWebService.uploadAttachment(vendorId, fileData, fileName, noteText);
//			kfsVendorWebService.vendorExists(vendorId, vendorIdType);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
