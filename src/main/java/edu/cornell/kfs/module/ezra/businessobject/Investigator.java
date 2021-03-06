/**
 * 
 */
package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * @author kwk43
 *
 */
public class Investigator extends PersistableBusinessObjectBase {

	private static final long serialVersionUID = 1L;
	private String investigatorId;
	private String netId;
	private Date lastUpdated;
	
	/**
	 * @return the investigatorId
	 */
	public String getInvestigatorId() {
		return investigatorId;
	}

	/**
	 * @param investigatorId the investigatorId to set
	 */
	public void setInvestigatorId(String investigatorId) {
		this.investigatorId = investigatorId;
	}

	/**
	 * @return the netId
	 */
	public String getNetId() {
		return netId;
	}

	/**
	 * @param netId the netId to set
	 */
	public void setNetId(String netId) {
		this.netId = netId;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/* (non-Javadoc)
	 * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();

        m.put("investigatorId", investigatorId);
	    return m;
	}

}
