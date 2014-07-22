/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

/**
 * @author kco26
 *
 */
public class SubObjectCodeExtendedAttribute extends PersistableBusinessObjectExtensionBase {


	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sub Object Code Description
	 */
    private String subObjectCodeDescr;
        
    
    /*
     * Class constructor
     */
    public SubObjectCodeExtendedAttribute() {
    	
    }
    
	/**
	 * @return the subObjectCodeDescr
	 */
	public String getSubObjectCodeDescr() {
		return subObjectCodeDescr;
	}
	
	/**
	 * @param subObjectCodeDescr the subObjectCodeDescr to set
	 */
	public void setSubObjectCodeDescr(String subObjectCodeDescr) {
		this.subObjectCodeDescr = subObjectCodeDescr;
	}
		
}
