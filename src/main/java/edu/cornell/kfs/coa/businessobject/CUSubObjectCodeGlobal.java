/*
 * Copyright 2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cornell.kfs.coa.businessobject;


import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.businessobject.SubObjectCodeGlobal;
import org.kuali.kfs.coa.businessobject.SubObjectCodeGlobalDetail;
import org.kuali.rice.krad.bo.GlobalBusinessObject;

/**
 * 
 */
public class CUSubObjectCodeGlobal extends SubObjectCodeGlobal implements GlobalBusinessObject {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SubObjectCodeGlobal.class);    
    private String subObjectCodeDescr;

    public void populate(SubObjectCode old, AccountGlobalDetail accountGlobalDetail, SubObjectCodeGlobalDetail subObjCdGlobalDetail) {
    	SubObjectCodeExtendedAttribute cuSubObjectCodeExtendedData = (SubObjectCodeExtendedAttribute) old.getExtension();
        cuSubObjectCodeExtendedData.setSubObjectCodeDescr(update(subObjectCodeDescr, cuSubObjectCodeExtendedData.getSubObjectCodeDescr()));
    }
	
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
