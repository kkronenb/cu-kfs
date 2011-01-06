/*
 * Copyright 2010 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.sec.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.bo.TransientBusinessObjectBase;

import com.rsmart.kuali.kfs.sec.SecPropertyConstants;
import com.rsmart.kuali.kfs.sec.service.AccessPermissionEvaluator;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Holds fields that provide metadata information for a security attribute
 */
public class SecurityAttributeMetadata extends TransientBusinessObjectBase {
    private Class attributeClass;
    private String attributeField;
    private String attributeNameField;

    public SecurityAttributeMetadata() {
    }

    public SecurityAttributeMetadata(Class attributeClass, String attributeField) {
        this.attributeClass = attributeClass;
        this.attributeField = attributeField;
    }

    public SecurityAttributeMetadata(Class attributeClass, String attributeField, String attributeNameField) {
        this.attributeClass = attributeClass;
        this.attributeField = attributeField;
        this.attributeNameField = attributeNameField;
    }

    /**
     * Gets the attributeClass attribute.
     * 
     * @return Returns the attributeClass.
     */
    public Class getAttributeClass() {
        return attributeClass;
    }


    /**
     * Sets the attributeClass attribute value.
     * 
     * @param attributeClass The attributeClass to set.
     */
    public void setAttributeClass(Class attributeClass) {
        this.attributeClass = attributeClass;
    }


    /**
     * Gets the attributeField attribute.
     * 
     * @return Returns the attributeField.
     */
    public String getAttributeField() {
        return attributeField;
    }


    /**
     * Sets the attributeField attribute value.
     * 
     * @param attributeField The attributeField to set.
     */
    public void setAttributeField(String attributeField) {
        this.attributeField = attributeField;
    }


    /**
     * Gets the attributeNameField attribute.
     * 
     * @return Returns the attributeNameField.
     */
    public String getAttributeNameField() {
        return attributeNameField;
    }

    /**
     * Sets the attributeNameField attribute value.
     * 
     * @param attributeNameField The attributeNameField to set.
     */
    public void setAttributeNameField(String attributeNameField) {
        this.attributeNameField = attributeNameField;
    }

    @Override
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put(SecPropertyConstants.ATRIBUTE_ID, this.attributeField);

        return m;
    }

}
