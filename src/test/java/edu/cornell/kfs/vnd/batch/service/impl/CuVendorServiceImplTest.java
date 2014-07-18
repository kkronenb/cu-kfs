package edu.cornell.kfs.vnd.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.vnd.document.service.CUVendorService;

@ConfigureContext(session = ccs1)
public class CuVendorServiceImplTest extends KualiTestBase {
	
	private CUVendorService cuVendorService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cuVendorService = SpringContext.getBean(CUVendorService.class);
	}
	
	public void testGetVendorByVendorName() {
		VendorDetail myDetail = VendorDetailFixture.ANAK_INC.createVendorDetail();
		VendorDetail theDetail = cuVendorService.getVendorByVendorName("Anak Inc");
		System.out.println("got the detail");
		System.out.println(" alt name:" + theDetail.getAltVendorName());
		System.out.println(" default address city: " + theDetail.getDefaultAddressCity());
	}
	
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
		
//		public VendorDetail retrieveVendorDetail() {
//			return (VendorDetail) b
//		}
	}
}
