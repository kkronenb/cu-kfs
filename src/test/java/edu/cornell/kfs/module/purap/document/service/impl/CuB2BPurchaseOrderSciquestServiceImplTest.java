package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.B2BPurchaseOrderService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class CuB2BPurchaseOrderSciquestServiceImplTest extends KualiTestBase {
	
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(CuB2BPurchaseOrderSciquestServiceImplTest.class);

	private B2BPurchaseOrderService b2bPurchaseOrderService; 
	private CuB2BPurchaseOrderSciquestServiceImpl cuB2BPurchaseOrderSciquestServiceImpl;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		b2bPurchaseOrderService = SpringContext.getBean(B2BPurchaseOrderService.class);
		 cuB2BPurchaseOrderSciquestServiceImpl = (CuB2BPurchaseOrderSciquestServiceImpl)TestUtils.getUnproxiedService("b2bPurchaseOrderService");

	}

	public void testSendPurchaseOrder()
			throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B.createPurchaseOrderdDocument();

		String errors = b2bPurchaseOrderService.sendPurchaseOrder(po);

		assertTrue(errors.isEmpty());
	}

	public void testGetCxml() throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B.createPurchaseOrderdDocument();

		String cxml = b2bPurchaseOrderService.getCxml(po, UserNameFixture.ccs1.getPerson().getPrincipalId(), cuB2BPurchaseOrderSciquestServiceImpl.getB2bPurchaseOrderPassword(), po.getVendorContract().getContractManager(), cuB2BPurchaseOrderSciquestServiceImpl.getContractManagerEmail(po.getVendorContract().getContractManager()), po.getVendorDetail().getVendorDunsNumber());

		assertTrue(!cxml.isEmpty());


	}
	
	public void testVerifyCxmlPOData() throws Exception {
		PurchaseOrderDocument po = PurchaseOrderFixture.PO_B2B.createPurchaseOrderdDocument();

		String errors = b2bPurchaseOrderService.verifyCxmlPOData(po, UserNameFixture.ccs1.getPerson().getPrincipalId(), cuB2BPurchaseOrderSciquestServiceImpl.getB2bPurchaseOrderPassword(), po.getVendorContract().getContractManager(), cuB2BPurchaseOrderSciquestServiceImpl.getContractManagerEmail(po.getVendorContract().getContractManager()), po.getVendorDetail().getVendorDunsNumber());

		assertTrue(errors.isEmpty());

	}


}
