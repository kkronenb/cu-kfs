/*
 * Copyright 2008 The Kuali Foundation
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
package org.kuali.kfs.module.purap.document.validation.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.vnd.businessobject.CampusParameter;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.util.GlobalVariables;

public class PurchasingDeliveryValidation extends GenericValidation {

    DateTimeService dateTimeService;
    BusinessObjectService businessObjectService;
    PostalCodeValidationService postalCodeValidationService;
    
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingDocument purDocument = (PurchasingDocument)event.getDocument();
        
        /* KFSPTS-1796 : a quick fix for REQ delivery addr message issue.  A complete solution for all document should have a separate jira to fix it */
        boolean clearErrorPath = false;
        if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorPath())) {
        	GlobalVariables.getMessageMap().addToErrorPath("document");
        	clearErrorPath = true;
        }
        postalCodeValidationService.validateAddress(purDocument.getDeliveryCountryCode(), purDocument.getDeliveryStateCode(), purDocument.getDeliveryPostalCode(), PurapPropertyConstants.DELIVERY_STATE_CODE, PurapPropertyConstants.DELIVERY_POSTAL_CODE);
        if (clearErrorPath) {
        	GlobalVariables.getMessageMap().clearErrorPath();
        }
        Map<String, String> fieldValues = new HashMap<String, String>();
        fieldValues.put("campusCode", purDocument.getDeliveryCampusCode());
        
        int match = businessObjectService.countMatching(CampusParameter.class, fieldValues);
        if (match < 1) {
            valid = false;
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.DELIVERY_CAMPUS_CODE, PurapKeyConstants.ERROR_DELIVERY_CAMPUS_INVALID);
        }
        
        GlobalVariables.getMessageMap().clearErrorPath();
        return valid;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPostalCodeValidationService(PostalCodeValidationService postalCodeValidationService) {
        this.postalCodeValidationService = postalCodeValidationService;
    }

    
}