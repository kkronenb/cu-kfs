package edu.cornell.kfs.vnd.batch.service.impl;

import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.vnd.batch.service.CommodityCodeUpdateService;

public class CuCommodityCodeUpdateServiceImplTest extends KualiTestBase {
	
	private CommodityCodeUpdateService commodityCodeUpdateService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		commodityCodeUpdateService = SpringContext.getBean(CommodityCodeUpdateService.class);
	}
	
	public void testLoadCommodityCodeFile() {
		
	}

}
