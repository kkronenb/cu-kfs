package edu.cornell.kfs.vnd.batch.service.impl;

//import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.vnd.document.service.CUVendorService;
import edu.cornell.kfs.vnd.fixture.VendorDetailFixture;

@ConfigureContext
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
		
		assertEquals(theDetail.isVendorParentIndicator(), myDetail.isVendorParentIndicator());
		assertEquals(theDetail.getVendorDetailAssignedIdentifier(), myDetail.getVendorDetailAssignedIdentifier());
		
		System.out.println("got the detail");
		System.out.println(" alt name:" + theDetail.getAltVendorName());
		System.out.println(" default address city: " + theDetail.getDefaultAddressCity());
	}
	
}
