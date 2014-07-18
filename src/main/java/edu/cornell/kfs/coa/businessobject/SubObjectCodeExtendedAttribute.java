/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.businessobject.YearEndPersistableBusinessObjectExtensionBase;

/**
 * @author kco26
 *
 */
public class SubObjectCodeExtendedAttribute extends YearEndPersistableBusinessObjectExtensionBase {


	/**
	 * 
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
