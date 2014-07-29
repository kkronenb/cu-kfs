package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.context.TestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.fixture.VendorCreditMemoDocumentFixture;

@ConfigureContext(session = UserNameFixture.mo14)
public class CuCreditMemoServiceImplTest extends KualiTestBase {

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuCreditMemoServiceImplTest.class);

	private CuCreditMemoServiceImpl creditMemoServiceImpl;
	private DocumentService documentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		creditMemoServiceImpl = (CuCreditMemoServiceImpl) TestUtils.getUnproxiedService("creditMemoService");
		documentService = SpringContext.getBean(DocumentService.class);

	}

	public void testAddHoldOnCreditMemo() throws Exception {
		VendorCreditMemoDocument creditMemoDocument = VendorCreditMemoDocumentFixture.VENDOR_CREDIT_MEMO.createVendorCreditMemoDocument();

		creditMemoServiceImpl.addHoldOnCreditMemo(creditMemoDocument, "unit test");

		assertTrue(creditMemoDocument.isHoldIndicator());
	}

}
