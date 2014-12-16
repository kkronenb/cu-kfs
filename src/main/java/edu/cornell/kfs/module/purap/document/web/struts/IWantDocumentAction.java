package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.employment.EntityEmployment;
import org.kuali.rice.kim.api.identity.entity.Entity;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.rule.event.KualiAddLineEvent;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.rice.krad.service.KualiRuleService;
import org.kuali.rice.krad.service.SessionDocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocUserOptions;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.module.purap.document.validation.event.AddIWantItemEvent;

@SuppressWarnings("deprecation")
public class IWantDocumentAction extends FinancialSystemTransactionalDocumentActionBase {

    private static final String IWANT_DEPT_ORGS_TO_EXCLUDE_PARM = "IWANT_DEPT_ORGS_TO_EXCLUDE";

    private static final int DOCUMENT_DESCRIPTION_MAX_LENGTH = 40;

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {

        super.loadDocument(kualiDocumentFormBase);
        IWantDocumentForm iWantForm = (IWantDocumentForm) kualiDocumentFormBase;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        if (iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved()) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            
            if (StringUtils.isNotBlank(iWantDocument.getCurrentRouteToNetId())) {
                iWantForm.getNewAdHocRoutePerson().setId(iWantDocument.getCurrentRouteToNetId());
            }
        } else if (!iWantDocument.getDocumentHeader().getWorkflowDocument().isInitiated()) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.REGULAR);
        }

        WorkflowDocument workflowDoc = iWantDocument.getDocumentHeader().getWorkflowDocument();
        SpringContext.getBean(SessionDocumentService.class).addDocumentToUserSession(GlobalVariables.getUserSession(), workflowDoc);

    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#docHandler(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward docHandler(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ActionForward actionForward = super.docHandler(mapping, form, request, response);
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();
        String command = iWantForm.getCommand();
        String step = request.getParameter(CUPurapConstants.IWNT_STEP_PARAMETER);

        if (step != null) {
            iWantForm.setStep(step);
        }

        if (iWantDocument != null) {
            if (iWantDocument.getDocumentHeader().getWorkflowDocument().isSaved()) {
                step = CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP;
            }

            iWantDocument.setStep(step);

            if (KewApiConstants.INITIATE_COMMAND.equalsIgnoreCase(command)) {
                iWantForm.setDocument(iWantDocument);

                if (iWantDocument != null) {

                    String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
                    Principal initiator = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
                    String initiatorPrincipalID = initiator.getPrincipalId();
                    String initiatorNetID = initiator.getPrincipalName();

                    iWantDocument.setInitiatorNetID(initiatorNetID);

                    Person currentUser = GlobalVariables.getUserSession().getPerson();
                    String initiatorName = currentUser.getName();
                    String initiatorPhoneNumber = currentUser.getPhoneNumber();
                    String initiatorEmailAddress = currentUser.getEmailAddress();

                    IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                    String address = iWantDocumentService.getPersonCampusAddress(initiatorNetID);

                    iWantDocument.setInitiatorName(initiatorName);
                    iWantDocument.setInitiatorPhoneNumber(initiatorPhoneNumber);
                    iWantDocument.setInitiatorEmailAddress(initiatorEmailAddress);
                    iWantDocument.setInitiatorAddress(address);

                    // check default user options
                    Map<String, String> primaryKeysCollegeOption = new HashMap<String, String>();
                    primaryKeysCollegeOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysCollegeOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
                    IWantDocUserOptions userOptionsCollege = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysCollegeOption);

                    Map<String, String> primaryKeysDepartmentOption = new HashMap<String, String>();
                    primaryKeysDepartmentOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDepartmentOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
                    IWantDocUserOptions userOptionsDepartment = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDepartmentOption);
                    
                    //check default deliver to address info

                    Map<String, String> primaryKeysdeliverToNetIDOption = new HashMap<String, String>();
                    primaryKeysdeliverToNetIDOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysdeliverToNetIDOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID);
                    IWantDocUserOptions userOptionsDeliverToNetID = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysdeliverToNetIDOption);

                    Map<String, String> primaryKeysDeliverToNameOption = new HashMap<String, String>();
                    primaryKeysDeliverToNameOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToNameOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME);
                    IWantDocUserOptions userOptionsDeliverToName = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToNameOption);
                    
                    Map<String, String> primaryKeysDeliverToEmailOption = new HashMap<String, String>();
                    primaryKeysDeliverToEmailOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToEmailOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID,
                            CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS);
                    IWantDocUserOptions userOptionsDeliverToEmail = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToEmailOption);
                    
                    Map<String, String> primaryKeysDeliverToPhnNbrOption = new HashMap<String, String>();
                    primaryKeysDeliverToPhnNbrOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToPhnNbrOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID,
                            CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER);
                    IWantDocUserOptions userOptionsDeliverToPhnNbr = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToPhnNbrOption);
                    
                    Map<String, String> primaryKeysDeliverToAddressOption = new HashMap<String, String>();
                    primaryKeysDeliverToAddressOption.put(CUPurapConstants.USER_OPTIONS_PRINCIPAL_ID, initiatorPrincipalID);
                    primaryKeysDeliverToAddressOption.put(CUPurapConstants.USER_OPTIONS_OPTION_ID, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS);
                    IWantDocUserOptions userOptionsDeliverToAddress = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDeliverToAddressOption);

                    if (ObjectUtils.isNotNull(userOptionsCollege)) {
                        iWantDocument.setCollegeLevelOrganization(userOptionsCollege.getOptionValue());
                    }

                    if (ObjectUtils.isNotNull(userOptionsDepartment)) {
                        iWantDocument.setDepartmentLevelOrganization(userOptionsDepartment.getOptionValue());
                    }

                    //if no default user options check primary department
                    if (ObjectUtils.isNull(userOptionsCollege) && ObjectUtils.isNull(userOptionsDepartment)) {

                        /// set college and department based on primary id
                        setCollegeAndDepartmentBasedOnPrimaryDepartment(iWantForm);
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToNetID)) {
                        iWantDocument.setDeliverToNetID(userOptionsDeliverToNetID.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToName)) {
                        iWantDocument.setDeliverToName(userOptionsDeliverToName.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToEmail)) {
                        iWantDocument.setDeliverToEmailAddress(userOptionsDeliverToEmail.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToPhnNbr)) {
                        iWantDocument.setDeliverToPhoneNumber(userOptionsDeliverToPhnNbr.getOptionValue());
                    }
                    
                    if (ObjectUtils.isNotNull(userOptionsDeliverToAddress)) {
                        iWantDocument.setDeliverToAddress(userOptionsDeliverToAddress.getOptionValue());
                    }
                }

                // put workflow doc on session

                WorkflowDocument workflowDoc = iWantDocument.getDocumentHeader().getWorkflowDocument();
                // KualiDocumentFormBase.populate() needs this updated in the session
                SpringContext.getBean(SessionDocumentService.class).addDocumentToUserSession(GlobalVariables.getUserSession(), workflowDoc);

                setIWantDocumentDescription(iWantDocument);
            }
        }

        return actionForward;
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#execute(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ActionForward actionForward = super.execute(mapping, form, request, response);
        IWantDocumentForm documentForm = (IWantDocumentForm) form;
        IWantDocument iWantDoc = documentForm.getIWantDocument();

        if (documentForm != null && documentForm.getDocument() != null) {

            iWantDoc.setExplanation(iWantDoc.getDocumentHeader().getExplanation());

            // populate department drop down
            if (!documentForm.getPreviousSelectedOrg().equalsIgnoreCase(((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {
                String cLevelOrg = ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization();

                documentForm.getDeptOrgKeyLabels().clear();
                documentForm.getDeptOrgKeyLabels().add(new ConcreteKeyValue("", "Please Select"));

                if (StringUtils.isNotEmpty(cLevelOrg)) {

                    IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                    List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

                    // Get the list of chart+org combos to forcibly exclude from the drop-down, if any.
                    String routingChart = ((IWantDocument) documentForm.getDocument()).getRoutingChart();
                    Collection<String> dLevelExcludesList = getParameterService().getParameterValuesAsString(PurapConstants.PURAP_NAMESPACE,
                            KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE, IWANT_DEPT_ORGS_TO_EXCLUDE_PARM);
                    Set<String> dLevelExcludes =
                            new HashSet<String>((dLevelExcludesList != null) ? dLevelExcludesList : Collections.<String>emptyList());

                    for (LevelOrganization levelOrganization : dLevelOrgs) {

                        // Add each department-level org to the drop-down as long as it is not marked for exclusion.
                        if (!dLevelExcludes.contains(routingChart + "=" + levelOrganization.getCode())) {
                            documentForm.getDeptOrgKeyLabels().add(
                                    new ConcreteKeyValue(levelOrganization.getCode(), levelOrganization.getCodeAndDescription()));
                        }
                    }
                }
            }

            //setIWantDocumentDescription(iWantDoc);

        }

        return actionForward;
    }

    private void setIWantDocumentDescription(IWantDocument iWantDocument) {
        // add selected chart and department to document description
        String routingChart = iWantDocument.getRoutingChart() == null ? StringUtils.EMPTY : iWantDocument
                .getRoutingChart() + "-";
        String routingOrg = iWantDocument.getRoutingOrganization() == null ? StringUtils.EMPTY : iWantDocument
                .getRoutingOrganization();
        String addChartOrgToDesc = routingChart + routingOrg;
        String vendorName = iWantDocument.getVendorName() == null ? StringUtils.EMPTY : iWantDocument.getVendorName();
        String description = addChartOrgToDesc + " " + vendorName;

        if (StringUtils.isNotBlank(description) && description.length() > DOCUMENT_DESCRIPTION_MAX_LENGTH) {
            description = description.substring(0, DOCUMENT_DESCRIPTION_MAX_LENGTH);
        }

        // If necessary, add a default description.
        if (StringUtils.isBlank(description)) {
            description = "New IWantDocument";
        }
        
        iWantDocument.getDocumentHeader().setDocumentDescription(description);
    }

    /**
     * Sets the College and Department based on the initiator primary department.
     * 
     * @param documentForm
     */
    private void setCollegeAndDepartmentBasedOnPrimaryDepartment(IWantDocumentForm documentForm) {
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        String primaryDeptOrg = null;

        IWantDocument iWantDocument = null;
        if (documentForm != null && documentForm.getDocument() != null) {
            iWantDocument = (IWantDocument) documentForm.getDocument();
        }
        
        if (iWantDocument != null && StringUtils.isEmpty(iWantDocument.getCollegeLevelOrganization())) {

            Person currentUser = GlobalVariables.getUserSession().getPerson();

            Entity entityInfo = KimApiServiceLocator.getIdentityService().getEntityByPrincipalId(currentUser.getPrincipalId());

            if (ObjectUtils.isNotNull(entityInfo)) {
                if (ObjectUtils.isNotNull(entityInfo.getEmploymentInformation()) && entityInfo.getEmploymentInformation().size() > 0) {
                    EntityEmployment employmentInformation = entityInfo.getEmploymentInformation().get(0);
                    String primaryDepartment = employmentInformation.getPrimaryDepartmentCode();
                    primaryDeptOrg = primaryDepartment.substring(primaryDepartment.lastIndexOf('-') + 1,
                            primaryDepartment.length());

                    String cLevelOrg = iWantDocumentService.getCLevelOrganizationForDLevelOrg(primaryDepartment);
                    ((IWantDocument) documentForm.getDocument()).setCollegeLevelOrganization(cLevelOrg);
                }
            }
        }

        if (iWantDocument != null && StringUtils.isNotEmpty(iWantDocument.getCollegeLevelOrganization())) {
            String cLevelOrg = iWantDocument.getCollegeLevelOrganization();
            documentForm.getDeptOrgKeyLabels().clear();
            documentForm.getDeptOrgKeyLabels().add(new ConcreteKeyValue("", "Please Select"));

            List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

            for (LevelOrganization levelOrganization : dLevelOrgs) {
                documentForm.getDeptOrgKeyLabels().add(
                        new ConcreteKeyValue(levelOrganization.getCode(), levelOrganization.getCodeAndDescription()));

            }

            if (primaryDeptOrg != null) {
                iWantDocument.setDepartmentLevelOrganization(primaryDeptOrg);
            }

        }
    }

    /**
     * Loads the D level orgs based on the selected C level org
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward loadDLevelOrgs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        return mapping.findForward("refresh");
    }

    /**
     * Takes the user to next page
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToCustomerData(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);

        iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes the user to next page
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToItems(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService
                .applyRules(new RouteDocumentEvent(KFSConstants.DOCUMENT_HEADER_ERRORS, iWantDocument));

        if (rulePassed) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);

            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.ITEMS_AND_ACCT_DATA_STEP);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes the user to next page
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToVendor(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();
        boolean added = true;
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantForm.getNewIWantItemLine();
        if (StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity() != null) {
            added &= addNewItem(iWantForm, iWantDocument, item);
        }
        
        if (added) {
            IWantAccount account = iWantForm.getNewSourceLine();
            if (StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber())
                    || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode())
                    || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())) {
                added &= addNewAccount(iWantForm, iWantDocument, account);
            }
        }

        // If addition of IWNT item or account failed, then skip the rest of the validation.
        if (!added) {
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService.applyRules(new RouteDocumentEvent("", iWantDocument));

        if (rulePassed) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);

            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.VENDOR_STEP);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Takes the user to next page
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueToRouting(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        // call business rules
        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;
        rulePassed &= ruleService.applyRules(new RouteDocumentEvent("", iWantDocument));

        if (rulePassed) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#close(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	// KFSPTS-3606 : Always redirect to Action List on close.
    	((IWantDocumentForm) form).setReturnToActionList(true);
    	return super.close(mapping, form, request, response);
    }

    /**
     * Adds an item to the document
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward addItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = (IWantDocument) iWantDocumentForm.getDocument();
        IWantItem item = iWantDocumentForm.getNewIWantItemLine();
        
        addNewItem(iWantDocumentForm, iWantDocument, item);
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the selected item
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward deleteItem(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;

        IWantDocument iWantDocument = (IWantDocument) iWantForm.getDocument();
        iWantDocument.deleteItem(getSelectedLine(request));

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Retrieves the selected line.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#getSelectedLine(javax.servlet.http.HttpServletRequest)
     */
    protected int getSelectedLine(HttpServletRequest request) {
        int selectedLine = -1;
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            String lineNumber = StringUtils.substringBetween(parameterName, ".line", ".");
            selectedLine = Integer.parseInt(lineNumber);
        }

        return selectedLine;
    }

    /**
     * Adds an accounting line
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward addAccountingLine(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
        IWantDocument iWantDoc = (IWantDocument) iWantDocumentForm.getDocument();
        IWantAccount account = iWantDocumentForm.getNewSourceLine();
        
        addNewAccount(iWantDocumentForm, iWantDoc, account);
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the selected account
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward deleteAccount(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;

        IWantDocument iWantDocument = (IWantDocument) iWantForm.getDocument();
        iWantDocument.deleteAccount(getSelectedLine(request));

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#route(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        boolean added = true;
        
        //add new item and new accounting line if not empty
        IWantItem item = iWantDocForm.getNewIWantItemLine();
        if (StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity() != null) {
            added &= addNewItem(iWantDocForm, iWantDocument, item);
        }
        
        if (added) {
            IWantAccount account = iWantDocForm.getNewSourceLine();
            if (StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber())
                    || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode())
                    || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())) {
                added &= addNewAccount(iWantDocForm, iWantDocument, account);
            }
        }

        // Do not route if there were failures adding new items or accounts.
        if (!added) {
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        String step = iWantDocForm.getStep();
        String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();

        boolean setDefaultCollegeDept = iWantDocument.isUseCollegeAndDepartmentAsDefault();

        if (setDefaultCollegeDept) {
            // set these values in user Options table
            String college = iWantDocument.getCollegeLevelOrganization();
            String department = iWantDocument.getDepartmentLevelOrganization();
            
            
            saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE, college);

            saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT, department);

        }
        
        if (iWantDocument.isSetDeliverToInfoAsDefault()) {
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToNetID())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID, iWantDocument.getDeliverToNetID());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToName())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_NAME, iWantDocument.getDeliverToName());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToEmailAddress())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS, iWantDocument.getDeliverToEmailAddress());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToPhoneNumber())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER, iWantDocument.getDeliverToPhoneNumber());
            }
            
            if (StringUtils.isNotBlank(iWantDocument.getDeliverToAddress())) {
                saveUserOption(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS, iWantDocument.getDeliverToAddress());
            }
            
        }

        setIWantDocumentDescription(iWantDocument);

        //insert adhoc route person first and the route
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            iWantDocument.setCurrentRouteToNetId(iWantDocForm.getNewAdHocRoutePerson().getId());
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
        ActionForward actionForward = super.route(mapping, form, request, response);

        return (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) ? mapping.findForward("finish") : actionForward;
    }
    
    private void saveUserOption(String principalId, String userOptionName, String userOptionValue) {
        IWantDocUserOptions userOption = new IWantDocUserOptions();
        userOption.setPrincipalId(principalId);
        userOption.setOptionId(userOptionName);
        userOption.setOptionValue(userOptionValue);

        IWantDocUserOptions retrievedUserOption = (IWantDocUserOptions) getBusinessObjectService()
                .retrieve(userOption);

        if (ObjectUtils.isNotNull(retrievedUserOption)) {
            retrievedUserOption.setOptionValue(userOptionValue);
            getBusinessObjectService().save(retrievedUserOption);
        } else {
            getBusinessObjectService().save(userOption);
        }
    }

    @Override
    public ActionForward sendAdHocRequests(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        @SuppressWarnings("unused")
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        //insert adhoc route person first and the route
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);

        }
        return super.sendAdHocRequests(mapping, form, request, response);
    }

    @Override
    public ActionForward approve(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        @SuppressWarnings("unused")
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        //insert adhoc route person first and then approve
        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            insertAdHocRoutePerson(mapping, iWantDocForm, request, response);
        }

        return super.approve(mapping, form, request, response);
    }
    
    /**
     * Use the new attachment description field to set the note text.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#insertBONote(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // If the note text is blank, set the attachment description as the text. Otherwise, concatenate both to form the text.
        IWantDocumentForm iWantDocumentForm = (IWantDocumentForm) form;
        Note note = iWantDocumentForm.getNewNote();
        if (StringUtils.isBlank(note.getNoteText())) {
            note.setNoteText(iWantDocumentForm.getIWantDocument().getAttachmentDescription());
        } else {
            note.setNoteText(iWantDocumentForm.getIWantDocument().getAttachmentDescription() + ": " + note.getNoteText());
        }
        
        ActionForward actionForward = super.insertBONote(mapping, form, request, response);
        
        iWantDocumentForm.getIWantDocument().setAttachmentDescription(StringUtils.EMPTY);

        return actionForward;
    }

    /**
     * Overridden to guarantee that form of copied document is set to whatever the entry
     * mode of the document is
     * @see org.kuali.rice.kns.web.struts.action.KualiTransactionalDocumentActionBase#copy(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ActionForward forward = null;

        if (request.getParameter(KRADConstants.PARAMETER_DOC_ID) == null) {
            forward = super.copy(mapping, form, request, response);
        } else {
            // this is copy document from Procurement Gateway:
            // use this link to call: http://localhost:8080/kfs-dev/purapIWant.do?methodToCall=copy&docId=xxxx
            String docId = request.getParameter(KRADConstants.PARAMETER_DOC_ID);
            KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

            IWantDocument document = null;
            document = (IWantDocument) getDocumentService().getByDocumentHeaderId(docId);
            document.toCopyFromGateway();

            kualiDocumentFormBase.setDocument(document);
            WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
            kualiDocumentFormBase.setDocTypeName(workflowDocument.getDocumentTypeName());
            SpringContext.getBean(SessionDocumentService.class).addDocumentToUserSession(GlobalVariables.getUserSession(), workflowDocument);

            forward = mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
        return forward;
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#refresh(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();

        ActionForward actionForward = super.refresh(mapping, form, request, response);
        String refreshCaller = request.getParameter(KFSConstants.REFRESH_CALLER);

        if (refreshCaller != null && refreshCaller.endsWith(KFSConstants.LOOKUPABLE_SUFFIX)) {

            String deliverToNetID = iWantDocument.getDeliverToNetID();
            if (StringUtils.isNotEmpty(deliverToNetID)) {
                IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                String address = iWantDocumentService.getPersonCampusAddress(deliverToNetID);

                iWantDocument.setDeliverToAddress(address);

            }

            if ("iWantDocVendorLookupable".equalsIgnoreCase(refreshCaller)) {
                Integer vendorHeaderId = iWantDocument.getVendorHeaderGeneratedIdentifier();
                Integer vendorId = iWantDocument.getVendorDetailAssignedIdentifier();
                String phoneNumber = "Phone: ";

                Map<String,Object> fieldValues = new HashMap<String,Object>();
                fieldValues.put("vendorHeaderGeneratedIdentifier", vendorHeaderId);
                fieldValues.put("vendorDetailAssignedIdentifier", vendorId);
                fieldValues.put("vendorPhoneTypeCode", "PH");
                Collection<VendorPhoneNumber> vendorPhoneNumbers = getBusinessObjectService().findMatching(VendorPhoneNumber.class,
                        fieldValues);
                if (ObjectUtils.isNotNull(vendorPhoneNumbers) && vendorPhoneNumbers.size() > 0) {
                    VendorPhoneNumber retrievedVendorPhoneNumber = vendorPhoneNumbers.toArray(new VendorPhoneNumber[1])[0];
                    phoneNumber += retrievedVendorPhoneNumber.getVendorPhoneNumber();
                }

                // populate vendor info
                String addressLine1 = iWantDocument.getVendorLine1Address() != null ? iWantDocument
                        .getVendorLine1Address() : StringUtils.EMPTY;
                String addressLine2 = iWantDocument.getVendorLine2Address() != null ? iWantDocument
                        .getVendorLine2Address() : StringUtils.EMPTY;
                String cityName = iWantDocument.getVendorCityName() != null ? iWantDocument.getVendorCityName()
                        : StringUtils.EMPTY;
                String stateCode = iWantDocument.getVendorStateCode() != null ? iWantDocument.getVendorStateCode()
                        : StringUtils.EMPTY;
                String countryCode = iWantDocument.getVendorCountryCode() != null ? iWantDocument
                        .getVendorCountryCode() : StringUtils.EMPTY;
                String postalCode = iWantDocument.getVendorPostalCode() != null ? iWantDocument.getVendorPostalCode()
                        : StringUtils.EMPTY;
                String faxNumber = "Fax: "
                        + (iWantDocument.getVendorFaxNumber() != null ? iWantDocument.getVendorFaxNumber()
                                : StringUtils.EMPTY);

                String url = "URL: "
                        + (iWantDocument.getVendorWebURL() != null ? iWantDocument.getVendorWebURL()
                                : StringUtils.EMPTY);

                String vendorInfo = new StringBuilder(100).append(addressLine1).append('\n').append(
                            addressLine2).append('\n').append(
                            cityName).append(", ").append(postalCode).append(", ").append(stateCode).append(", ").append(countryCode).append('\n').append(
                            faxNumber).append('\n').append(
                            phoneNumber).append(" \n").append(
                            url).toString();

                iWantDocument.setVendorDescription(vendorInfo);
            }

        }
        return actionForward;
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // Only recreate document description if in INITIATED or SAVED status.
        WorkflowDocument workflowDocument = ((KualiDocumentFormBase) form).getDocument().getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isInitiated() || workflowDocument.isSaved()) {
            setIWantDocumentDescription((IWantDocument) ((KualiDocumentFormBase) form).getDocument());
        }

        ActionForward actionForward = super.save(mapping, form, request, response);
        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        boolean added = true;

        //add new item and new accounting line if not empty
        IWantItem item = iWantDocForm.getNewIWantItemLine();
        if (StringUtils.isNotBlank(item.getItemDescription()) || item.getItemUnitPrice() != null || item.getItemQuantity() != null) {
            added &= addNewItem(iWantDocForm, iWantDocument, item);
        }
        
        if (added) {
            IWantAccount account = iWantDocForm.getNewSourceLine();
            if (StringUtils.isNotBlank(account.getAccountNumber()) || StringUtils.isNotBlank(account.getSubAccountNumber())
                    || StringUtils.isNotBlank(account.getFinancialObjectCode()) || StringUtils.isNotBlank(account.getFinancialSubObjectCode())
                    || StringUtils.isNotBlank(account.getProjectCode()) || StringUtils.isNotBlank(account.getOrganizationReferenceId())) {
                added &= addNewAccount(iWantDocForm, iWantDocument, account);
            }
        }

        // Do not save if item or account additions failed.
        if (!added) {
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }

        iWantDocument.setExplanation(iWantDocument.getDocumentHeader().getExplanation());

        if (StringUtils.isNotBlank(iWantDocForm.getNewAdHocRoutePerson().getId())) {
            iWantDocument.setCurrentRouteToNetId(iWantDocForm.getNewAdHocRoutePerson().getId());
        }

        return actionForward;
    }

    /**
     * Redirects the user to a URL that creates a new REQS doc with data from the current IWantDocument.
     * However, due to configuration on the associated button from the IWantDocumentForm, client-side
     * JavaScript should handle the redirect for us and in a separate window/tab, unless the client has
     * disabled JavaScript.
     */
    public ActionForward createRequisition(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        IWantDocumentForm iWantDocForm = (IWantDocumentForm) form;
        IWantDocument iWantDocument = iWantDocForm.getIWantDocument();
        @SuppressWarnings("unused")
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);

        // Make sure a related requisition does not already exist before creating one.
        if (StringUtils.isNotBlank(iWantDocument.getReqsDocId())) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CUPurapKeyConstants.ERROR_IWNT_REQUISITION_EXISTS);
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }

        String url = ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.APPLICATION_URL_KEY)
                + "/purapRequisition.do?methodToCall=createReqFromIWantDoc&docId=" + iWantDocument.getDocumentNumber();

        ActionForward actionForward = new ActionForward(url, true);

        return actionForward;
    }

    private boolean addNewAccount(IWantDocumentForm iWantDocumentForm, IWantDocument iWantDoc, IWantAccount account) {

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean acctRulesPassed = true;

        acctRulesPassed &= ruleService.applyRules(new KualiAddLineEvent(iWantDoc, "accounts", account));

        if (acctRulesPassed) {
            account = iWantDocumentForm.getAndResetNewIWantAccountLine();
            iWantDoc.addAccount(account);
        }  
        
        return acctRulesPassed;
        
    }
    
    private boolean addNewItem(IWantDocumentForm iWantDocumentForm, IWantDocument iWantDoc, IWantItem item) {

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService.applyRules(new AddIWantItemEvent(StringUtils.EMPTY, iWantDoc, item));

        if (rulePassed) {
            item = iWantDocumentForm.getAndResetNewIWantItemLine();
            iWantDoc.addItem(item);
        }
        
        return rulePassed;
    }

//  @Override
//  protected ActionForward returnToSender(HttpServletRequest request, ActionMapping mapping, KualiDocumentFormBase form) {
//	//String actionCalled = form.getMethodToCall();
//	
//
//    if (KewApiConstants.INITIATE_COMMAND.equalsIgnoreCase("close")) {
//    //if(StringUtils.isNotEmpty(actionCalled) && actionCalled.equalsIgnoreCase("close")) {
//	  // KFSPTS-3606 : Always redirect to Action List on close.
//	  String workflowBase = getKualiConfigurationService().getPropertyValueAsString(KRADConstants.WORKFLOW_URL_KEY);
//	  String actionListUrl = workflowBase + "/ActionList.do";
//
//      setupDocumentExit();
//	  return new ActionForward(actionListUrl, true);
//	} else {
//	  return super.returnToSender(request, mapping, form);
//	}
//}

}
