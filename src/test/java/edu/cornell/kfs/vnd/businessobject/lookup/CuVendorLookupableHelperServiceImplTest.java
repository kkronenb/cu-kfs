package edu.cornell.kfs.vnd.businessobject.lookup;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

@ConfigureContext
public class CuVendorLookupableHelperServiceImplTest extends KualiTestBase{

	private VendorLookupableHelperService vendorLookupableHelperService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		vendorLookupableHelperService = SpringContext.getBean(VendorLookupableHelperService.class);
	}
	
	public void testGetSearchResults() {
		
	}
}
