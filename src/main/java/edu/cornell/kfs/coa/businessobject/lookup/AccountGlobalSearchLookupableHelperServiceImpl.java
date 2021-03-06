/*
 * Copyright 2011 The Kuali Foundation.
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
package edu.cornell.kfs.coa.businessobject.lookup;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.lookup.KualiAccountLookupableHelperServiceImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.web.format.BooleanFormatter;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.util.BeanPropertyComparator;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;

import edu.cornell.kfs.coa.dataaccess.AccountGlobalSearchDao;

public class AccountGlobalSearchLookupableHelperServiceImpl extends KualiAccountLookupableHelperServiceImpl {
    private AccountGlobalSearchDao accountGlobalSearchDao;

    /**
     * @see org.kuali.kfs.coa.businessobject.lookup.KualiAccountLookupableHelperServiceImpl#getSearchResults(java.util.Map)
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> parameters) {
        setBackLocation(parameters.get(KRADConstants.BACK_LOCATION));
        setDocFormKey(parameters.get(KRADConstants.DOC_FORM_KEY));
        setReferencesToRefresh(parameters.get(KRADConstants.REFERENCES_TO_REFRESH));

        if (parameters.containsKey("useOrgHierarchy")) {
            String includeOrgHierarchyStr = parameters.get("useOrgHierarchy");
            Boolean includeOrgHierarchy = (Boolean) new BooleanFormatter().convertFromPresentationFormat(includeOrgHierarchyStr);
            if (includeOrgHierarchy == null ) 
            	//Both was selected
            	includeOrgHierarchy = true;
            if (includeOrgHierarchy) {
                String chartOfAccountsCode = null;
                if (parameters.containsKey(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE)) {
                    chartOfAccountsCode = parameters.get(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
                }

                String organizationCode = null;
                if (parameters.containsKey(KFSPropertyConstants.ORGANIZATION_CODE)) {
                    organizationCode = parameters.get(KFSPropertyConstants.ORGANIZATION_CODE);
                }
                
                if (parameters.containsKey(KFSPropertyConstants.CLOSED)) {
                    final String closedValue = parameters.get(KFSPropertyConstants.CLOSED);

                    if (closedValue != null && closedValue.length() != 0) {
                        if ("Y1T".indexOf(closedValue) > -1) {
                            parameters.put(KFSPropertyConstants.ACTIVE, "N");
                        }
                        else if ("N0F".indexOf(closedValue) > -1) {
                            parameters.put(KFSPropertyConstants.ACTIVE, "Y");
                        }
                    }

                    parameters.remove(KFSPropertyConstants.CLOSED);
                }
                parameters.remove("useOrgHierarchy");

                List<? extends BusinessObject> searchResults = (List<? extends BusinessObject>) accountGlobalSearchDao.getAccountsByOrgHierarchy(chartOfAccountsCode, organizationCode, parameters);

                // sort list if default sort column given
                List defaultSortColumns = getDefaultSortColumns();
                if (defaultSortColumns.size() > 0) {
                    Collections.sort(searchResults, new BeanPropertyComparator(defaultSortColumns, true));
                }
                
                return searchResults;
            }
        }
        parameters.remove("useOrgHierarchy");

        return super.getSearchResults(parameters);
    }


    /**
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#validateSearchParameters(java.util.Map)
     */
    @Override
    public void validateSearchParameters(Map parameters) {
        super.validateSearchParameters(parameters);

        if (parameters.containsKey("useOrgHierarchy")) {
            String includeOrgHierarchyStr = (String) parameters.get("useOrgHierarchy");
            Boolean includeOrgHierarchy = (Boolean) new BooleanFormatter().convertFromPresentationFormat(includeOrgHierarchyStr);
            if (includeOrgHierarchy == null ) 
            	//Both was selected
            	includeOrgHierarchy = true;
            if (includeOrgHierarchy) {
                String chartOfAccountsCode = null;
                if (parameters.containsKey(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE)) {
                    chartOfAccountsCode = (String) parameters.get(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
                }

                String organizationCode = null;
                if (parameters.containsKey(KFSPropertyConstants.ORGANIZATION_CODE)) {
                    organizationCode = (String) parameters.get(KFSPropertyConstants.ORGANIZATION_CODE);
                }

                if (StringUtils.isBlank(chartOfAccountsCode)) {
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, "error.accountGlobalSearch.requiredChart");
                }
 
                if (StringUtils.isBlank(organizationCode)) {
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ORGANIZATION_CODE, "error.accountGlobalSearch.requiredOrg");
                }
            }
        }
    }

    protected AccountGlobalSearchDao getAccountGlobalSearchDao() {
        return accountGlobalSearchDao;
    }


    public void setAccountGlobalSearchDao(AccountGlobalSearchDao accountGlobalSearchDao) {
        this.accountGlobalSearchDao = accountGlobalSearchDao;
    }


}
