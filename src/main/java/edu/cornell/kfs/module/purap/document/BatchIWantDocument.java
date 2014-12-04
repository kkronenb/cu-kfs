package edu.cornell.kfs.module.purap.document;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.web.format.BooleanFormatter;

public class BatchIWantDocument extends IWantDocument {
	
	protected String maximoNumber;
	protected String businessPurpose;

	public String getMaximoNumber() {
		return maximoNumber;
	}

	public void setMaximoNumber(String maximoNumber) {
		this.maximoNumber = maximoNumber;
	}

	public String getBusinessPurpose() {
		return businessPurpose;
	}

	public void setBusinessPurpose(String businessPurpose) {
		this.businessPurpose = businessPurpose;
	}
	
	public void setGoods(String goods) {
        if (StringUtils.isNotBlank(goods)) {
            Boolean goodsB = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(goods);
            super.setGoods(goodsB);
        }
	}

}
