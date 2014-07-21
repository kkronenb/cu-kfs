package edu.cornell.kfs.vnd.batch.service.impl;

import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.vnd.batch.service.CommodityCodeUpdateService;

public class CuCommodityCodeUpdateServiceImplTest extends KualiTestBase {
	
	private CommodityCodeUpdateService commodityCodeUpdateService;
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CommodityCodeUpdateService.class);
	private static final String DATA_FILE_PATH = "src/test/java/edu/cornell/kfs/vnd/batch/fixture";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		commodityCodeUpdateService = SpringContext.getBean(CommodityCodeUpdateService.class);
		commodityCodeUpdateService.loadCommodityCodeFile(DATA_FILE_PATH);
	}
	
	public void testLoadCommodityCodeFile() {
		
	}

}
