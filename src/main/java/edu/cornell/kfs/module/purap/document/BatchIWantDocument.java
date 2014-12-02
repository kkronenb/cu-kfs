package edu.cornell.kfs.module.purap.document;

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

}
