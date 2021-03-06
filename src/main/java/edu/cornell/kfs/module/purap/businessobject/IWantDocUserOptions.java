package edu.cornell.kfs.module.purap.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

public class IWantDocUserOptions extends PersistableBusinessObjectBase {
    
	private static final long serialVersionUID = 1L;
	private String principalId;
    private String optionId;
    private String optionValue;
    
    

    @SuppressWarnings("rawtypes")
	protected LinkedHashMap toStringMapper() {
        return null;
    }



    public String getPrincipalId() {
        return principalId;
    }



    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }



    public String getOptionId() {
        return optionId;
    }



    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }



    public String getOptionValue() {
        return optionValue;
    }



    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

}
