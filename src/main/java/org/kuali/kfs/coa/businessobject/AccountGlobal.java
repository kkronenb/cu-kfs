/*
 * Copyright 2007 The Kuali Foundation
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

package org.kuali.kfs.coa.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.ld.businessobject.LaborBenefitRateCategory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.bo.GlobalBusinessObject;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.KualiModuleService;
import org.kuali.rice.krad.service.ModuleService;
import org.kuali.rice.krad.service.PersistenceStructureService;
import org.kuali.rice.location.api.LocationConstants;
import org.kuali.rice.location.api.postalcode.PostalCode;
import org.kuali.rice.location.api.state.State;
import org.kuali.rice.location.api.state.StateService;
import org.kuali.rice.location.framework.postalcode.PostalCodeEbo;
import org.kuali.rice.location.framework.state.StateEbo;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.MajorReportingCategory;

/**
 * 
 */
public class AccountGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {

    private String documentNumber;
    private String accountFiscalOfficerSystemIdentifier;
    private String accountsSupervisorySystemsIdentifier;
    private String accountManagerSystemIdentifier;
    private String chartOfAccountsCode;
    private String organizationCode;
    private String subFundGroupCode;
    private String accountCityName;
    private String accountStateCode;
    private String accountStreetAddress;
    private String accountZipCode;
    private Date accountExpirationDate;
    private String continuationFinChrtOfAcctCd;
    private String continuationAccountNumber;
    private String incomeStreamFinancialCoaCode;
    private String incomeStreamAccountNumber;
    private String accountCfdaNumber;
    private String financialHigherEdFunctionCd;
    private String accountSufficientFundsCode;
    private Boolean pendingAcctSufficientFundsIndicator;
    private String accountSearchCriteriaTxt;
    private List<AccountGlobalDetail> accountGlobalDetails;

    private DocumentHeader financialDocument;
    private Person accountFiscalOfficerUser;
    private Person accountSupervisoryUser;
    private Person accountManagerUser;
    private Chart continuationFinChrtOfAcct;
    private Account continuationAccount;
    private Account incomeStreamAccount;
    private Chart incomeStreamFinancialCoa;
    private Chart chartOfAccounts;
    private Organization organization;
    private SubFundGroup subFundGroup;
    private StateEbo accountState;
    private HigherEducationFunction financialHigherEdFunction;
    private PostalCodeEbo postalZipCode;
    private SufficientFundsCode sufficientFundsCode;
    
    //added for the employee labor benefit calculation
    private String laborBenefitRateCategoryCode;
    private LaborBenefitRateCategory laborBenefitRateCategory;

    //added for major reporting category code extended attribute
    private String majorReportingCategoryCode;
    private MajorReportingCategory majorReportingCategory;
    
    /**
     * Default constructor.
     */
    public AccountGlobal() {
        accountGlobalDetails = new ArrayList<AccountGlobalDetail>();
    }

    /**
     * @see org.kuali.rice.kns.document.GlobalBusinessObject#getGlobalChangesToDelete()
     */
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        return null;
    }

    /**
     * @see org.kuali.rice.kns.document.GlobalBusinessObject#applyGlobalChanges(org.kuali.rice.kns.bo.BusinessObject)
     */
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {


        // the list of persist-ready BOs
        List<PersistableBusinessObject> persistables = new ArrayList();

        // walk over each change detail record
        for (AccountGlobalDetail detail : accountGlobalDetails) {

            // load the object by keys
            Account account = (Account) SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Account.class, detail.getPrimaryKeys());

            // if we got a valid account, do the processing
            if (account != null) {

                // NOTE that the list of fields that are updated may be a subset of the total
                // number of fields in this class. This is because the class may contain a superset
                // of the fields actually used in the Global Maintenance Document.

                // FISCAL OFFICER
                if (StringUtils.isNotBlank(accountFiscalOfficerSystemIdentifier)) {
                    account.setAccountFiscalOfficerSystemIdentifier(accountFiscalOfficerSystemIdentifier);
                }

                // ACCOUNT SUPERVISOR
                if (StringUtils.isNotBlank(accountsSupervisorySystemsIdentifier)) {
                    account.setAccountsSupervisorySystemsIdentifier(accountsSupervisorySystemsIdentifier);
                }

                // ACCOUNT MANAGER
                if (StringUtils.isNotBlank(accountManagerSystemIdentifier)) {
                    account.setAccountManagerSystemIdentifier(accountManagerSystemIdentifier);
                }

                // ORGANIZATION CODE
                if (StringUtils.isNotBlank(organizationCode)) {
                    account.setOrganizationCode(organizationCode);
                }

                // SUB FUND GROUP CODE
                if (StringUtils.isNotBlank(subFundGroupCode)) {
                    account.setSubFundGroupCode(subFundGroupCode);
                }

                // CITY NAME
                if (StringUtils.isNotBlank(accountCityName)) {
                    account.setAccountCityName(accountCityName);
                }

                // STATE CODE
                if (StringUtils.isNotBlank(accountStateCode)) {
                    account.setAccountStateCode(accountStateCode);
                }

                // STREET ADDRESS
                if (StringUtils.isNotBlank(accountStreetAddress)) {
                    account.setAccountStreetAddress(accountStreetAddress);
                }

                // ZIP CODE
                if (StringUtils.isNotBlank(accountZipCode)) {
                    account.setAccountZipCode(accountZipCode);
                }

                // EXPIRATION DATE
                if (accountExpirationDate != null) {
                    account.setAccountExpirationDate(new Date(accountExpirationDate.getTime()));
                }

                // CONTINUATION CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(continuationFinChrtOfAcctCd)) {
                    account.setContinuationFinChrtOfAcctCd(continuationFinChrtOfAcctCd);
                }

                // CONTINUATION ACCOUNT NUMBER
                if (StringUtils.isNotBlank(continuationAccountNumber)) {
                    account.setContinuationAccountNumber(continuationAccountNumber);
                }

                // INCOME STREAM CHART OF ACCOUNTS CODE
                if (StringUtils.isNotBlank(incomeStreamFinancialCoaCode)) {
                    account.setIncomeStreamFinancialCoaCode(incomeStreamFinancialCoaCode);
                }

                // INCOME STREAM ACCOUNT NUMBER
                if (StringUtils.isNotBlank(incomeStreamAccountNumber)) {
                    account.setIncomeStreamAccountNumber(incomeStreamAccountNumber);
                }

                // CG CATL FED DOMESTIC ASSIST NBR
                if (StringUtils.isNotBlank(accountCfdaNumber)) {
                    account.setAccountCfdaNumber(accountCfdaNumber);
                }

                // FINANCIAL HIGHER ED FUNCTION CODE
                if (StringUtils.isNotBlank(financialHigherEdFunctionCd)) {
                    account.setFinancialHigherEdFunctionCd(financialHigherEdFunctionCd);
                }

                // SUFFICIENT FUNDS CODE
                if (StringUtils.isNotBlank(accountSufficientFundsCode)) {
                    account.setAccountSufficientFundsCode(accountSufficientFundsCode);
                }

                // PENDING ACCOUNT SUFFICIENT FUNDS CODE INDICATOR
                if (pendingAcctSufficientFundsIndicator != null) {
                    account.setPendingAcctSufficientFundsIndicator(pendingAcctSufficientFundsIndicator);
                }
                
                // LABOR BENEFIT RATE CATEGORY CODE 
               // String laborBenefitRateCategoryCode = getLaborBenefitRateCategoryCode();
                if (StringUtils.isNotBlank(laborBenefitRateCategoryCode)){
                    ((AccountExtendedAttribute)account.getExtension()).setLaborBenefitRateCategoryCode(laborBenefitRateCategoryCode);
                }
                
                // MAJOR REPORTING CATEGORY CODE 
                if (StringUtils.isNotBlank(majorReportingCategoryCode)){
                    ((AccountExtendedAttribute)account.getExtension()).setMajorReportingCategoryCode(majorReportingCategoryCode);
                }

                persistables.add(account);

            }
        }

        return persistables;
    }

    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * Gets the chartOfAccountsCode attribute.
     * 
     * @return Returns the chartOfAccountsCode.
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    /**
     * Sets the chartOfAccountsCode attribute value.
     * 
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    /**
     * Gets the organizationCode attribute.
     * 
     * @return Returns the organizationCode
     */
    public String getOrganizationCode() {
        return organizationCode;
    }

    /**
     * Sets the organizationCode attribute.
     * 
     * @param organizationCode The organizationCode to set.
     */
    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    /**
     * Gets the subFundGroupCode attribute.
     * 
     * @return Returns the subFundGroupCode
     */
    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    /**
     * Sets the subFundGroupCode attribute.
     * 
     * @param subFundGroupCode The subFundGroupCode to set.
     */
    public void setSubFundGroupCode(String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
    }

    /**
     * Gets the accountCityName attribute.
     * 
     * @return Returns the accountCityName
     */
    public String getAccountCityName() {
        return accountCityName;
    }

    /**
     * Sets the accountCityName attribute.
     * 
     * @param accountCityName The accountCityName to set.
     */
    public void setAccountCityName(String accountCityName) {
        this.accountCityName = accountCityName;
    }


    /**
     * Gets the accountStateCode attribute.
     * 
     * @return Returns the accountStateCode
     */
    public String getAccountStateCode() {
        return accountStateCode;
    }

    /**
     * Sets the accountStateCode attribute.
     * 
     * @param accountStateCode The accountStateCode to set.
     */
    public void setAccountStateCode(String accountStateCode) {
        this.accountStateCode = accountStateCode;
    }


    /**
     * Gets the accountStreetAddress attribute.
     * 
     * @return Returns the accountStreetAddress
     */
    public String getAccountStreetAddress() {
        return accountStreetAddress;
    }

    /**
     * Sets the accountStreetAddress attribute.
     * 
     * @param accountStreetAddress The accountStreetAddress to set.
     */
    public void setAccountStreetAddress(String accountStreetAddress) {
        this.accountStreetAddress = accountStreetAddress;
    }


    /**
     * Gets the accountZipCode attribute.
     * 
     * @return Returns the accountZipCode
     */
    public String getAccountZipCode() {
        return accountZipCode;
    }

    /**
     * Sets the accountZipCode attribute.
     * 
     * @param accountZipCode The accountZipCode to set.
     */
    public void setAccountZipCode(String accountZipCode) {
        this.accountZipCode = accountZipCode;
    }

    /**
     * Gets the accountExpirationDate attribute.
     * 
     * @return Returns the accountExpirationDate
     */
    public Date getAccountExpirationDate() {
        return accountExpirationDate;
    }

    /**
     * Sets the accountExpirationDate attribute.
     * 
     * @param accountExpirationDate The accountExpirationDate to set.
     */
    public void setAccountExpirationDate(Date accountExpirationDate) {
        this.accountExpirationDate = accountExpirationDate;
    }


    /**
     * Gets the continuationFinChrtOfAcctCd attribute.
     * 
     * @return Returns the continuationFinChrtOfAcctCd
     */
    public String getContinuationFinChrtOfAcctCd() {
        return continuationFinChrtOfAcctCd;
    }

    /**
     * Sets the continuationFinChrtOfAcctCd attribute.
     * 
     * @param continuationFinChrtOfAcctCd The continuationFinChrtOfAcctCd to set.
     */
    public void setContinuationFinChrtOfAcctCd(String continuationFinChrtOfAcctCd) {
        this.continuationFinChrtOfAcctCd = continuationFinChrtOfAcctCd;
    }


    /**
     * Gets the continuationAccountNumber attribute.
     * 
     * @return Returns the continuationAccountNumber
     */
    public String getContinuationAccountNumber() {
        return continuationAccountNumber;
    }

    /**
     * Sets the continuationAccountNumber attribute.
     * 
     * @param continuationAccountNumber The continuationAccountNumber to set.
     */
    public void setContinuationAccountNumber(String continuationAccountNumber) {
        this.continuationAccountNumber = continuationAccountNumber;
    }

    /**
     * Gets the incomeStreamFinancialCoaCode attribute.
     * 
     * @return Returns the incomeStreamFinancialCoaCode
     */
    public String getIncomeStreamFinancialCoaCode() {
        return incomeStreamFinancialCoaCode;
    }

    /**
     * Sets the incomeStreamFinancialCoaCode attribute.
     * 
     * @param incomeStreamFinancialCoaCode The incomeStreamFinancialCoaCode to set.
     */
    public void setIncomeStreamFinancialCoaCode(String incomeStreamFinancialCoaCode) {
        this.incomeStreamFinancialCoaCode = incomeStreamFinancialCoaCode;
    }


    /**
     * Gets the incomeStreamAccountNumber attribute.
     * 
     * @return Returns the incomeStreamAccountNumber
     */
    public String getIncomeStreamAccountNumber() {
        return incomeStreamAccountNumber;
    }

    /**
     * Sets the incomeStreamAccountNumber attribute.
     * 
     * @param incomeStreamAccountNumber The incomeStreamAccountNumber to set.
     */
    public void setIncomeStreamAccountNumber(String incomeStreamAccountNumber) {
        this.incomeStreamAccountNumber = incomeStreamAccountNumber;
    }

    /**
     * Gets the accountSufficientFundsCode attribute.
     * 
     * @return Returns the accountSufficientFundsCode
     */
    public String getAccountSufficientFundsCode() {
        return accountSufficientFundsCode;
    }

    /**
     * Sets the accountSufficientFundsCode attribute.
     * 
     * @param accountSufficientFundsCode The accountSufficientFundsCode to set.
     */
    public void setAccountSufficientFundsCode(String accountSufficientFundsCode) {
        this.accountSufficientFundsCode = accountSufficientFundsCode;
    }


    /**
     * Gets the pendingAcctSufficientFundsIndicator attribute.
     * 
     * @return Returns the pendingAcctSufficientFundsIndicator
     */
    public Boolean getPendingAcctSufficientFundsIndicator() {
        return pendingAcctSufficientFundsIndicator;
    }


    /**
     * Sets the pendingAcctSufficientFundsIndicator attribute.
     * 
     * @param pendingAcctSufficientFundsIndicator The pendingAcctSufficientFundsIndicator to set.
     */
    public void setPendingAcctSufficientFundsIndicator(Boolean pendingAcctSufficientFundsIndicator) {
        this.pendingAcctSufficientFundsIndicator = pendingAcctSufficientFundsIndicator;
    }

    /**
     * Gets the accountCfdaNumber attribute.
     * 
     * @return Returns the accountCfdaNumber
     */
    public String getAccountCfdaNumber() {
        return accountCfdaNumber;
    }

    /**
     * Sets the accountCfdaNumber attribute.
     * 
     * @param accountCfdaNumber The accountCfdaNumber to set.
     */
    public void setAccountCfdaNumber(String accountCfdaNumber) {
        this.accountCfdaNumber = accountCfdaNumber;
    }

    /**
     * Gets the accountSearchCriteriaTxt attribute.
     * 
     * @return Returns the accountSearchCriteriaTxt
     */
    public String getAccountSearchCriteriaTxt() {
        return accountSearchCriteriaTxt;
    }

    /**
     * Sets the accountSearchCriteriaTxt attribute.
     * 
     * @param accountSearchCriteriaTxt The accountSearchCriteriaTxt to set.
     */
    public void setAccountSearchCriteriaTxt(String accountSearchCriteriaTxt) {
        this.accountSearchCriteriaTxt = accountSearchCriteriaTxt;
    }


    /**
     * Gets the financialDocument attribute.
     * 
     * @return Returns the financialDocument
     */
    public DocumentHeader getFinancialDocument() {
        return financialDocument;
    }

    /**
     * Sets the financialDocument attribute.
     * 
     * @param financialDocument The financialDocument to set.
     * @deprecated
     */
    public void setFinancialDocument(DocumentHeader financialDocument) {
        this.financialDocument = financialDocument;
    }

    public Person getAccountFiscalOfficerUser() {
        accountFiscalOfficerUser = SpringContext.getBean(PersonService.class).updatePersonIfNecessary(accountFiscalOfficerSystemIdentifier, accountFiscalOfficerUser);
        return accountFiscalOfficerUser;
    }


    /**
     * @param accountFiscalOfficerUser The accountFiscalOfficerUser to set.
     * @deprecated
     */
    public void setAccountFiscalOfficerUser(Person accountFiscalOfficerUser) {
        this.accountFiscalOfficerUser = accountFiscalOfficerUser;
    }

    public Person getAccountManagerUser() {
        accountManagerUser = SpringContext.getBean(PersonService.class).updatePersonIfNecessary(accountManagerSystemIdentifier, accountManagerUser);
        return accountManagerUser;
    }

    /**
     * @param accountManagerUser The accountManagerUser to set.
     * @deprecated
     */
    public void setAccountManagerUser(Person accountManagerUser) {
        this.accountManagerUser = accountManagerUser;
    }


    public Person getAccountSupervisoryUser() {
        accountSupervisoryUser = SpringContext.getBean(PersonService.class).updatePersonIfNecessary(accountsSupervisorySystemsIdentifier, accountSupervisoryUser);
        return accountSupervisoryUser;
    }


    /**
     * @param accountSupervisoryUser The accountSupervisoryUser to set.
     * @deprecated
     */
    public void setAccountSupervisoryUser(Person accountSupervisoryUser) {
        this.accountSupervisoryUser = accountSupervisoryUser;
    }

    /**
     * Gets the continuationFinChrtOfAcct attribute.
     * 
     * @return Returns the continuationFinChrtOfAcct
     */
    public Chart getContinuationFinChrtOfAcct() {
        return continuationFinChrtOfAcct;
    }

    /**
     * Sets the continuationFinChrtOfAcct attribute.
     * 
     * @param continuationFinChrtOfAcct The continuationFinChrtOfAcct to set.
     * @deprecated
     */
    public void setContinuationFinChrtOfAcct(Chart continuationFinChrtOfAcct) {
        this.continuationFinChrtOfAcct = continuationFinChrtOfAcct;
    }

    /**
     * Gets the continuationAccount attribute.
     * 
     * @return Returns the continuationAccount
     */
    public Account getContinuationAccount() {
        return continuationAccount;
    }

    /**
     * Sets the continuationAccount attribute.
     * 
     * @param continuationAccount The continuationAccount to set.
     * @deprecated
     */
    public void setContinuationAccount(Account continuationAccount) {
        this.continuationAccount = continuationAccount;
    }

    /**
     * Gets the incomeStreamAccount attribute.
     * 
     * @return Returns the incomeStreamAccount
     */
    public Account getIncomeStreamAccount() {
        return incomeStreamAccount;
    }

    /**
     * Sets the incomeStreamAccount attribute.
     * 
     * @param incomeStreamAccount The incomeStreamAccount to set.
     * @deprecated
     */
    public void setIncomeStreamAccount(Account incomeStreamAccount) {
        this.incomeStreamAccount = incomeStreamAccount;
    }

    /**
     * Gets the incomeStreamFinancialCoa attribute.
     * 
     * @return Returns the incomeStreamFinancialCoa
     */
    public Chart getIncomeStreamFinancialCoa() {
        return incomeStreamFinancialCoa;
    }

    /**
     * Sets the incomeStreamFinancialCoa attribute.
     * 
     * @param incomeStreamFinancialCoa The incomeStreamFinancialCoa to set.
     * @deprecated
     */
    public void setIncomeStreamFinancialCoa(Chart incomeStreamFinancialCoa) {
        this.incomeStreamFinancialCoa = incomeStreamFinancialCoa;
    }

    /**
     * @return Returns the accountGlobalDetail.
     */
    public List<AccountGlobalDetail> getAccountGlobalDetails() {
        return accountGlobalDetails;
    }

    /**
     * @param accountGlobalDetail The accountGlobalDetail to set.
     */
    public void setAccountGlobalDetails(List<AccountGlobalDetail> accountGlobalDetails) {
        this.accountGlobalDetails = accountGlobalDetails;
    }

    /**
     * Gets the financialHigherEdFunctionCd attribute.
     * 
     * @return Returns the financialHigherEdFunctionCd.
     */
    public String getFinancialHigherEdFunctionCd() {
        return financialHigherEdFunctionCd;
    }

    /**
     * Sets the financialHigherEdFunctionCd attribute value.
     * 
     * @param financialHigherEdFunctionCd The financialHigherEdFunctionCd to set.
     */
    public void setFinancialHigherEdFunctionCd(String financialHigherEdFunctionCd) {
        this.financialHigherEdFunctionCd = financialHigherEdFunctionCd;
    }

    /**
     * Gets the accountFiscalOfficerSystemIdentifier attribute.
     * 
     * @return Returns the accountFiscalOfficerSystemIdentifier.
     */
    public String getAccountFiscalOfficerSystemIdentifier() {
        return accountFiscalOfficerSystemIdentifier;
    }

    /**
     * Sets the accountFiscalOfficerSystemIdentifier attribute value.
     * 
     * @param accountFiscalOfficerSystemIdentifier The accountFiscalOfficerSystemIdentifier to set.
     */
    public void setAccountFiscalOfficerSystemIdentifier(String accountFiscalOfficerSystemIdentifier) {
        this.accountFiscalOfficerSystemIdentifier = accountFiscalOfficerSystemIdentifier;
    }

    /**
     * Gets the accountManagerSystemIdentifier attribute.
     * 
     * @return Returns the accountManagerSystemIdentifier.
     */
    public String getAccountManagerSystemIdentifier() {
        return accountManagerSystemIdentifier;
    }

    /**
     * Sets the accountManagerSystemIdentifier attribute value.
     * 
     * @param accountManagerSystemIdentifier The accountManagerSystemIdentifier to set.
     */
    public void setAccountManagerSystemIdentifier(String accountManagerSystemIdentifier) {
        this.accountManagerSystemIdentifier = accountManagerSystemIdentifier;
    }

    /**
     * Gets the accountsSupervisorySystemsIdentifier attribute.
     * 
     * @return Returns the accountsSupervisorySystemsIdentifier.
     */
    public String getAccountsSupervisorySystemsIdentifier() {
        return accountsSupervisorySystemsIdentifier;
    }

    /**
     * Sets the accountsSupervisorySystemsIdentifier attribute value.
     * 
     * @param accountsSupervisorySystemsIdentifier The accountsSupervisorySystemsIdentifier to set.
     */
    public void setAccountsSupervisorySystemsIdentifier(String accountsSupervisorySystemsIdentifier) {
        this.accountsSupervisorySystemsIdentifier = accountsSupervisorySystemsIdentifier;
    }

    /**
     * Gets the chartOfAccounts attribute.
     * 
     * @return Returns the chartOfAccounts.
     */
    public Chart getChartOfAccounts() {
        return chartOfAccounts;
    }

    /**
     * Sets the chartOfAccounts attribute value.
     * 
     * @param chartOfAccounts The chartOfAccounts to set.
     * @deprecated
     */
    public void setChartOfAccounts(Chart chartOfAccounts) {
        this.chartOfAccounts = chartOfAccounts;
    }

    /**
     * Gets the accountState attribute.
     * 
     * @return Returns the accountState.
     */
    public StateEbo getAccountState() {
        if ( StringUtils.isBlank(accountStateCode) || StringUtils.isBlank(KFSConstants.COUNTRY_CODE_UNITED_STATES ) ) {
            accountState = null;
        } else {
            if ( accountState == null || !StringUtils.equals( accountState.getCode(),accountStateCode) || !StringUtils.equals(accountState.getCountryCode(), KFSConstants.COUNTRY_CODE_UNITED_STATES ) ) {
                ModuleService moduleService = SpringContext.getBean(KualiModuleService.class).getResponsibleModuleService(StateEbo.class);
                if ( moduleService != null ) {
                    Map<String,Object> keys = new HashMap<String, Object>(2);
                    keys.put(LocationConstants.PrimaryKeyConstants.COUNTRY_CODE, KFSConstants.COUNTRY_CODE_UNITED_STATES);/*RICE20_REFACTORME*/
                    keys.put(LocationConstants.PrimaryKeyConstants.CODE, accountStateCode);
                    accountState = moduleService.getExternalizableBusinessObject(StateEbo.class, keys);
                } else {
                    throw new RuntimeException( "CONFIGURATION ERROR: No responsible module found for EBO class.  Unable to proceed." );
                }
            }
        }
        return accountState;
    }

    /**
     * Sets the accountState attribute value.
     * 
     * @param accountState The accountState to set.
     */
    public void setAccountState(StateEbo accountState) {
        this.accountState = accountState;
    }

    /**
     * Gets the financialHigherEdFunction attribute.
     * 
     * @return Returns the financialHigherEdFunction.
     */
    public HigherEducationFunction getFinancialHigherEdFunction() {
        return financialHigherEdFunction;
    }

    /**
     * Sets the financialHigherEdFunction attribute value.
     * 
     * @param financialHigherEdFunction The financialHigherEdFunction to set.
     */
    public void setFinancialHigherEdFunction(HigherEducationFunction financialHigherEdFunction) {
        this.financialHigherEdFunction = financialHigherEdFunction;
    }

    /**
     * Gets the organization attribute.
     * 
     * @return Returns the organization.
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Sets the organization attribute value.
     * 
     * @param organization The organization to set.
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Gets the postalZipCode attribute.
     * 
     * @return Returns the postalZipCode.
     */
    public PostalCodeEbo getPostalZipCode() {
        if ( StringUtils.isBlank(accountZipCode) || StringUtils.isBlank(KFSConstants.COUNTRY_CODE_UNITED_STATES ) ) {
            postalZipCode = null;
        } else {
            if ( postalZipCode == null || !StringUtils.equals( postalZipCode.getCode(),accountZipCode) || !StringUtils.equals(postalZipCode.getCountryCode(), KFSConstants.COUNTRY_CODE_UNITED_STATES ) ) {
                ModuleService moduleService = SpringContext.getBean(KualiModuleService.class).getResponsibleModuleService(PostalCodeEbo.class);
                if ( moduleService != null ) {
                    Map<String,Object> keys = new HashMap<String, Object>(2);
                    keys.put(LocationConstants.PrimaryKeyConstants.COUNTRY_CODE, KFSConstants.COUNTRY_CODE_UNITED_STATES);/*RICE20_REFACTORME*/
                    keys.put(LocationConstants.PrimaryKeyConstants.CODE, accountZipCode);
                    postalZipCode = moduleService.getExternalizableBusinessObject(PostalCodeEbo.class, keys);
                } else {
                    throw new RuntimeException( "CONFIGURATION ERROR: No responsible module found for EBO class.  Unable to proceed." );
                }
            }
        }
        return postalZipCode;
    }

    /**
     * Sets the postalZipCode attribute value.
     * 
     * @param postalZipCode The postalZipCode to set.
     */
    public void setPostalZipCode(PostalCodeEbo postalZipCode) {
        this.postalZipCode = postalZipCode;
    }

    /**
     * Gets the subFundGroup attribute.
     * 
     * @return Returns the subFundGroup.
     */
    public SubFundGroup getSubFundGroup() {
        return subFundGroup;
    }

    /**
     * Sets the subFundGroup attribute value.
     * 
     * @param subFundGroup The subFundGroup to set.
     */
    public void setSubFundGroup(SubFundGroup subFundGroup) {
        this.subFundGroup = subFundGroup;
    }

    /**
     * Gets the sufficientFundsCode attribute.
     * 
     * @return Returns the sufficientFundsCode.
     */
    public final SufficientFundsCode getSufficientFundsCode() {
        return sufficientFundsCode;
    }

    /**
     * Sets the sufficientFundsCode attribute value.
     * 
     * @param sufficientFundsCode The sufficientFundsCode to set.
     */
    public final void setSufficientFundsCode(SufficientFundsCode sufficientFundsCode) {
        this.sufficientFundsCode = sufficientFundsCode;
    }

    /**
     * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put(KFSPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);
        return m;
    }

    /**
     * @see org.kuali.rice.kns.document.GlobalBusinessObject#isPersistable()
     */
    public boolean isPersistable() {
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        // fail if the PK for this object is emtpy
        if (StringUtils.isBlank(documentNumber)) {
            return false;
        }

        // fail if the PKs for any of the contained objects are empty
        for (AccountGlobalDetail account : getAccountGlobalDetails()) {
            if (!persistenceStructureService.hasPrimaryKeyFieldValues(account)) {
                return false;
            }
        }

        // otherwise, its all good
        return true;
    }

    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return accountGlobalDetails;
    }

    /**
     * @see org.kuali.rice.kns.bo.PersistableBusinessObjectBase#buildListOfDeletionAwareLists()
     */
    @Override
    public List<Collection<PersistableBusinessObject>> buildListOfDeletionAwareLists() {
        List<Collection<PersistableBusinessObject>> managedLists = super.buildListOfDeletionAwareLists();

        managedLists.add( new ArrayList<PersistableBusinessObject>( getAccountGlobalDetails() ) );

        return managedLists;
    }

    /**
     * Gets the laborBenefitRateCategoryCode attribute. 
     * @return Returns the laborBenefitRateCategoryCode.
     */
    public String getLaborBenefitRateCategoryCode() {
        return laborBenefitRateCategoryCode;
    }

    /**
     * Sets the laborBenefitRateCategoryCode attribute value.
     * @param laborBenefitRateCategoryCode The laborBenefitRateCategoryCode to set.
     */
    public void setLaborBenefitRateCategoryCode(String laborBenefitRateCategoryCode) {
        this.laborBenefitRateCategoryCode = laborBenefitRateCategoryCode;
    }

    /**
     * Gets the laborBenefitRateCategory attribute. 
     * @return Returns the laborBenefitRateCategory.
     */
    public LaborBenefitRateCategory getLaborBenefitRateCategory() {
        return laborBenefitRateCategory;
    }

    /**
     * Sets the laborBenefitRateCategory attribute value.
     * @param laborBenefitRateCategory The laborBenefitRateCategory to set.
     */
    public void setLaborBenefitRateCategory(LaborBenefitRateCategory laborBenefitRateCategory) {
        this.laborBenefitRateCategory = laborBenefitRateCategory;
    }
    
	/**
	 * Gets the majorReportingCategoryCode attribute. 
	 * @return Returns the majorReportingCategoryCode.
	 */
	public String getMajorReportingCategoryCode() {
	    return majorReportingCategoryCode;
	}
	
	/**
	 * Sets the majorReportingCategoryCode attribute value.
	 * @param majorReportingCategoryCode The majorReportingCategoryCode to set.
	 */
	public void setMajorReportingCategoryCode(String majorReportingCategoryCode) {
	    this.majorReportingCategoryCode = majorReportingCategoryCode;
	}
	
	/**
	 * Gets the majorReportingCategory attribute. 
	 * @return Returns the majorReportingCategory.
	 */
	public MajorReportingCategory getMajorReportingCategory() {
	    return majorReportingCategory;
	}
	
	/**
	 * Sets the majorReportingCategory attribute value.
	 * @param majorReportingCategory The majorReportingCategory to set.
	 */
	public void setMajorReportingCategory(MajorReportingCategory majorReportingCategory) {
	    this.majorReportingCategory = majorReportingCategory;
	}
	    
}
