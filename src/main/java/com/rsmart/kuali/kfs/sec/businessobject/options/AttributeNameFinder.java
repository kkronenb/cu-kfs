/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.sec.businessobject.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;

import com.rsmart.kuali.kfs.sec.SecConstants.NonSecurityAttributeNames;
import com.rsmart.kuali.kfs.sec.SecConstants.SecurityAttributeNames;

/**
 * Returns list of attribute names
 */
public class AttributeNameFinder extends KeyValuesBase {

    /**
     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        List activeLabels = new ArrayList();

        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.ACCOUNT, SecurityAttributeNames.ACCOUNT));
        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.CHART, SecurityAttributeNames.CHART));
        activeLabels.add(new KeyLabelPair(NonSecurityAttributeNames.OBJECT_CODE, NonSecurityAttributeNames.OBJECT_CODE));
        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.OBJECT_CONSOLIDATION, SecurityAttributeNames.OBJECT_CONSOLIDATION));
        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.OBJECT_LEVEL, SecurityAttributeNames.OBJECT_LEVEL));
        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.ORGANIZATION, SecurityAttributeNames.ORGANIZATION));
        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.PROJECT_CODE, SecurityAttributeNames.PROJECT_CODE));
        activeLabels.add(new KeyLabelPair(SecurityAttributeNames.SUB_ACCOUNT, SecurityAttributeNames.SUB_ACCOUNT));
        activeLabels.add(new KeyLabelPair(NonSecurityAttributeNames.SUB_OBJECT_CODE, NonSecurityAttributeNames.SUB_OBJECT_CODE));

        return activeLabels;
    }
}