package edu.cornell.kfs.vnd.service;

import java.util.List;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;
import edu.cornell.kfs.vnd.fixture.AddressParameterFixture;
import edu.cornell.kfs.vnd.fixture.PhoneNumberParameterFixture;
import edu.cornell.kfs.vnd.fixture.SupplierDiversityParameterFixture;
import edu.cornell.kfs.vnd.fixture.VendorContactFixture;
import edu.cornell.kfs.vnd.fixture.VendorDetailExtensionFixture;
import edu.cornell.kfs.vnd.fixture.VendorDetailFixture;
import edu.cornell.kfs.vnd.service.params.VendorAddressParam;
import edu.cornell.kfs.vnd.service.params.VendorContactParam;
import edu.cornell.kfs.vnd.service.params.VendorPhoneNumberParam;
import edu.cornell.kfs.vnd.service.params.VendorSupplierDiversityParam;

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
			VendorDetailFixture vdf = VendorDetailFixture.VENDOR_TO_CREATE;

			List<VendorPhoneNumberParam> thePhoneNumbers = PhoneNumberParameterFixture.ONE.getAllFixtures();
			List<VendorAddressParam> theAddresses = AddressParameterFixture.ONE.getAllFixtures();
			List<VendorSupplierDiversityParam> theSupplierDiversities = SupplierDiversityParameterFixture.getAllFixtures();
			List<VendorContactParam> theContacts = VendorContactFixture.ONE.getAllFixtures();
			
			for (int i=0; i!=thePhoneNumbers.size(); i++) {
				VendorPhoneNumberParam vppp = thePhoneNumbers.get(i);
				System.out.println("phone: " + vppp.getVendorPhoneNumber() + " type: " + vppp.getVendorPhoneTypeCode() + " extension: " + vppp.getVendorPhoneExtensionNumber());
			}
			for (int j=0; j!=theAddresses.size();j++) {
				VendorAddressParam vap = theAddresses.get(j);
				System.out.println("address: " + vap.getVendorCityName());
			}
			for (int k=0; k!=theSupplierDiversities.size();k++) {
				VendorSupplierDiversityParam vsdp = theSupplierDiversities.get(k);
				System.out.println("supplier diversity code: " +vsdp.getVendorSupplierDiversityCode());
			}
			for (int i=0; i!=theContacts.size(); i++) {
				VendorContactParam vcp = theContacts.get(i);
				System.out.println("contact name: " + vcp.getVendorContactName() + " - C/O " + vcp.getVendorAttentionName());
			}
			
			
			VendorDetail vd = vdf.createVendorDetail();				
			VendorHeader vh = vd.getVendorHeader();
			VendorDetailExtension vdx = VendorDetailExtensionFixture.EXTENSION.createVendorDetailExtension();
			
			String anotherTry = kfsVendorWebService.addVendor(vd.getVendorName(), vh.getVendorTypeCode(), vh.getVendorForeignIndicator(), vh.getVendorTaxNumber(),
					vh.getVendorTaxTypeCode(), vh.getVendorOwnershipCode(), vd.isTaxableIndicator(), vdx.isEinvoiceVendorIndicator(), theAddresses, theContacts, thePhoneNumbers, theSupplierDiversities);
			
			System.out.println("another try result: " + anotherTry);
			
			String theResult = kfsVendorWebService.addVendor(vdf.vendorName, vdf.vendorTypeCode, vdf.isForeign, vdf.taxNumber, vdf.taxNumberType, 
					vdf.ownershipTypeCode, vdf.isTaxable, vdf.isEInvoice, theAddresses, theContacts, thePhoneNumbers, theSupplierDiversities);

			
			System.out.println("\n\n\nthe end result of all this: " + theResult + "\n\n\n");
			
//			public String addVendor(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber, String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
//	                
			//List<VendorAddressParam> addresses,List<VendorContactParam> contacts,
			//List<VendorPhoneNumberParam> phoneNumbers, 
			//List<VendorSupplierDiversityParam> supplierDiversitys) 
					//throws Exception {

			
			
			
			
//			kfsVendorWebService.addVendor(vendorName, vendorTypeCode, isForeign, taxNumber, taxNumberType, ownershipTypeCode, isTaxable, isEInvoice, addresses, contacts, phoneNumbers, supplierDiversitys);
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
