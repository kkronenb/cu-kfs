/*
 * Copyright 2007-2009 The Kuali Foundation
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
package com.rsmart.kuali.kfs.sec.businessobject.defaultvalue;

import org.kuali.rice.krad.service.KRADServiceLocator;
import org.kuali.rice.krad.service.SequenceAccessorService;
import org.kuali.rice.krad.valuefinder.ValueFinder;

import com.rsmart.kuali.kfs.sec.SecConstants;

/**
 * Returns the next security model identifier available.
 */
public class NextSecurityModelIdFinder implements ValueFinder {

    /**
     * @see org.kuali.rice.kns.lookup.valueFinder.ValueFinder#getValue()
     */
    public String getValue() {
        return getLongValue().toString();
    }

    /**
     * Get the next sequence number value as a Long.
     * 
     * @return Long
     */
    public static Long getLongValue() {
        SequenceAccessorService sas = KRADServiceLocator.getSequenceAccessorService();
        return sas.getNextAvailableSequenceNumber(SecConstants.SECURITY_MODEL_ID_SEQUENCE_NAME);
    }
}
