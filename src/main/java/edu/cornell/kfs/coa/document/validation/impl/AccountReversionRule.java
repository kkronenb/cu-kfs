/*
 * Copyright 2006 The Kuali Foundation
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
package edu.cornell.kfs.coa.document.validation.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.FundGroup;
import org.kuali.kfs.coa.businessobject.OrganizationReversion;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;
import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

/**
 * This class implements the business rules specific to the {@link OrganizationReversion} Maintenance Document.
 */
public class AccountReversionRule extends MaintenanceDocumentRuleBase {

    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionRule.class);

    protected AccountReversion oldAcctReversion;
    protected AccountReversion newAcctReversion;

    /**
     * No-Args Constructor for an AccountReversionRule.
     */
    public AccountReversionRule() {

    }

    /**
     * This performs rules checks on document route
     * <ul>
     * <li>{@link AccountReversionRule#validateDetailBusinessObjects(OrganizationReversion)}</li>
     * </ul>
     * This rule fails on business rule failures
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {

        boolean success = true;

        // make sure its a valid organization reversion MaintenanceDocument
        if (!isCorrectMaintenanceClass(document, AccountReversion.class)) {
            throw new IllegalArgumentException("Maintenance Document passed in was of the incorrect type.  Expected " + "'" + AccountReversion.class.toString() + "', received " + "'" + document.getOldMaintainableObject().getBoClass().toString() + "'.");
        }

        // get the real business object
        newAcctReversion = (AccountReversion) document.getNewMaintainableObject().getBusinessObject();

        // add check to validate document recursively to get to the collection attributes
        success &= validateDetailBusinessObjects(newAcctReversion);
        success &= validateAccountFundGroup(newAcctReversion);

        return success;
    }

    /**
     * Tests each option attached to the main business object and validates its properties.
     * 
     * @param orgReversion
     * @return false if any of the detail objects fail with their validation
     */
    protected boolean validateDetailBusinessObjects(AccountReversion orgReversion) {
        GlobalVariables.getMessageMap().addToErrorPath("document.newMaintainableObject");
        List<AccountReversionDetail> details = orgReversion.getAccountReversionDetails();
        int index = 0;
        int originalErrorCount = GlobalVariables.getMessageMap().getErrorCount();
        for (AccountReversionDetail dtl : details) {
            String errorPath = "accountReversionDetails[" + index + "]";
            GlobalVariables.getMessageMap().addToErrorPath(errorPath);
            validateAccountReversionDetail(dtl);
            GlobalVariables.getMessageMap().removeFromErrorPath(errorPath);
            index++;
        }
        GlobalVariables.getMessageMap().removeFromErrorPath("document.newMaintainableObject");
        return GlobalVariables.getMessageMap().getErrorCount() == originalErrorCount;
    }

    /**
     * 
     * This checks to make sure that the organization reversion object on the detail object actually exists
     * @param detail
     * @return false if the organization reversion object doesn't exist
     */
    protected boolean validateAccountReversionDetail(AccountReversionDetail detail) {
        boolean result = true; // let's assume this detail will pass the rule
        // 1. makes sure the financial object code exists
        detail.refreshReferenceObject("reversionObject");
        LOG.debug("reversion finanical object = " + detail.getReversionObject().getName());
        if (ObjectUtils.isNull(detail.getReversionObject())) {
            result = false;
            GlobalVariables.getMessageMap().putError("accountReversionObjectCode", KFSKeyConstants.ERROR_EXISTENCE, new String[] { "Financial Object Code: " + detail.getReversionObjectCode() });
        }
        return result;
    }
    
    protected boolean validateAccountFundGroup(AccountReversion acctReversion){
    	boolean valid = true;
    	String fundGroups  = SpringContext.getBean(ParameterService.class).getParameterValueAsString(Reversion.class, CUKFSConstants.Reversion.SELECTION_1);
        String propertyName = StringUtils.substringBefore(fundGroups, "=");
        List<String> ruleValues = Arrays.asList(StringUtils.substringAfter(fundGroups, "=").split(";"));
    	
    	String accountFundGroupCode = acctReversion.getAccount().getSubFundGroup().getFundGroupCode();
    	String budgetAccountFundGroupCode = acctReversion.getBudgetReversionAccount().getSubFundGroup().getFundGroupCode();
    	String cashAccountFundGroupCode = acctReversion.getCashReversionAccount().getSubFundGroup().getFundGroupCode();
    	
    	GlobalVariables.getMessageMap().addToErrorPath("document.newMaintainableObject");
    	
    	if(!ruleValues.contains(accountFundGroupCode)){
    		valid = false;
    		GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER,
    				RiceKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER, 					new String[] {
    				getDataDictionaryService().getAttributeLabel( AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER) + " " + getDataDictionaryService().getAttributeLabel( FundGroup.class, KFSPropertyConstants.CODE),
					accountFundGroupCode,
					toStringForMessage(),
					getParameterValuesForMessage(ruleValues),
					getDataDictionaryService().getAttributeLabel( AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_ACCT_NUMBER),
					} );
    	}
    	if(!ruleValues.contains(budgetAccountFundGroupCode)){
    		valid = false;
    		GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER, RiceKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER, 					new String[] {
					getDataDictionaryService().getAttributeLabel( AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER) + " " + getDataDictionaryService().getAttributeLabel( FundGroup.class, KFSPropertyConstants.CODE),
					budgetAccountFundGroupCode,
					toStringForMessage(),
					getParameterValuesForMessage(ruleValues),
					getDataDictionaryService().getAttributeLabel( AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER) 
					} );
    	}
    	
    	if(!ruleValues.contains(cashAccountFundGroupCode)){
    		valid = false;
    		GlobalVariables.getMessageMap().putError(CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER, RiceKeyConstants.ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER, 					new String[] {
					getDataDictionaryService().getAttributeLabel( AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER) + " " + getDataDictionaryService().getAttributeLabel( FundGroup.class, KFSPropertyConstants.CODE),
					cashAccountFundGroupCode,
					toStringForMessage(),
					getParameterValuesForMessage(ruleValues),
					getDataDictionaryService().getAttributeLabel( AccountReversion.class, CUKFSPropertyConstants.ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER) 
					} );
    	}
    	
    	GlobalVariables.getMessageMap().removeFromErrorPath("document.newMaintainableObject");
    	return valid;
    }
    
	public String getParameterValuesForMessage(Collection<String> fundGroups) {
		String result = KFSConstants.EMPTY_STRING;
		for(String fundGroup : fundGroups){
			result += fundGroup + ",";
		}
		result = result.substring(0, result.length() - 1);
		return result;
	}
    
	private String toStringForMessage() {
		return new StringBuffer("parameter: ").append(CUKFSConstants.Reversion.SELECTION_1)
				.append(", module: ").append("KFS-COA")
				.append(", component: ").append("Reversion")
				.toString();
	}

}