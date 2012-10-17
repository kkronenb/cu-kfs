package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.web.struts.FinancialSystemTransactionalDocumentActionBase;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.entity.KimEntityEmploymentInformation;
import org.kuali.rice.kim.bo.entity.dto.KimEntityInfo;
import org.kuali.rice.kim.bo.entity.dto.KimPrincipalInfo;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.rule.event.KualiAddLineEvent;
import org.kuali.rice.kns.rule.event.RouteDocumentEvent;
import org.kuali.rice.kns.service.KualiRuleService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocUserOptions;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;

public class IWantDocumentAction extends FinancialSystemTransactionalDocumentActionBase {

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {

        super.loadDocument(kualiDocumentFormBase);
        IWantDocumentForm iWantForm = (IWantDocumentForm) kualiDocumentFormBase;
        IWantDocument iWantDocument = iWantForm.getIWantDocument();

        if (iWantDocument.getDocumentHeader().getWorkflowDocument().stateIsSaved()) {
            iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
            iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP);
        }

        KualiWorkflowDocument workflowDoc = iWantDocument.getDocumentHeader().getWorkflowDocument();
        GlobalVariables.getUserSession().setWorkflowDocument(workflowDoc);

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
        String step = request.getParameter("step");

        if (step != null) {
            iWantForm.setStep(step);
        }

        if (iWantDocument != null) {
            if (iWantDocument.getDocumentHeader().getWorkflowDocument().stateIsSaved()) {
                step = CUPurapConstants.IWantDocumentSteps.CUSTOMER_DATA_STEP;
            }

            iWantDocument.setStep(step);

            if (KEWConstants.INITIATE_COMMAND.equalsIgnoreCase(command)) {
                IdentityManagementService identityManagementService = SpringContext
                        .getBean(IdentityManagementService.class);

                iWantForm.setDocument(iWantDocument);

                if (iWantDocument != null) {
                    iWantDocument.getDocumentHeader().setDocumentDescription(
                            "I Want Document #" + iWantDocument.getDocumentHeader().getDocumentNumber());

                    String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument()
                            .getInitiatorPrincipalId();
                    KimPrincipalInfo initiator = identityManagementService.getPrincipal(principalId);
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
                    primaryKeysCollegeOption.put("principalId", initiatorPrincipalID);
                    primaryKeysCollegeOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
                    IWantDocUserOptions userOptionsCollege = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysCollegeOption);

                    Map<String, String> primaryKeysDepartmentOption = new HashMap<String, String>();
                    primaryKeysDepartmentOption.put("principalId", initiatorPrincipalID);
                    primaryKeysDepartmentOption.put("optionId", CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
                    IWantDocUserOptions userOptionsDepartment = (IWantDocUserOptions) getBusinessObjectService()
                            .findByPrimaryKey(IWantDocUserOptions.class, primaryKeysDepartmentOption);

                    if (ObjectUtils.isNotNull(userOptionsCollege)) {
                        iWantDocument.setCollegeLevelOrganization(userOptionsCollege.getOptionValue());
                    }

                    if (ObjectUtils.isNotNull(userOptionsDepartment)) {
                        iWantDocument.setDepartmentLevelOrganization(userOptionsDepartment.getOptionValue());
                    }

                    //                    UserOptionsService userOptionsService = SpringContext.getBean(UserOptionsService.class);
                    //                    UserOptions collegeUserOption = userOptionsService.findByOptionId(principalId,
                    //                            CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
                    //                    UserOptions departmentUserOption = userOptionsService.findByOptionId(principalId,
                    //                            CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
                    //
                    //                    if (ObjectUtils.isNotNull(collegeUserOption)) {
                    //                        iWantDocument.setCollegeLevelOrganization(collegeUserOption.getOptionVal());
                    //                    }
                    //
                    //                    if (ObjectUtils.isNotNull(departmentUserOption)) {
                    //                        iWantDocument.setDepartmentLevelOrganization(departmentUserOption.getOptionVal());
                    //                    }
                    //
                    //                    if (ObjectUtils.isNull(collegeUserOption) && ObjectUtils.isNull(departmentUserOption)) 

                    //if no default user options check primary department
                    if (ObjectUtils.isNull(userOptionsCollege) && ObjectUtils.isNull(userOptionsDepartment)) {

                        /// set college and department based on primary id
                        setCollegeAndDepartmentBasedOnPrimaryDepartment(iWantForm);
                    }
                }

                // put workflow doc on session

                KualiWorkflowDocument workflowDoc = iWantDocument.getDocumentHeader().getWorkflowDocument();
                // KualiDocumentFormBase.populate() needs this updated in the session
                GlobalVariables.getUserSession().setWorkflowDocument(workflowDoc);

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

            if (!documentForm.getPreviousSelectedOrg().equalsIgnoreCase(
                    ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {

                // populate department drop down
                if (documentForm != null
                        && documentForm.getDocument() != null) {

                    String cLevelOrg = ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization();

                    documentForm.getDeptOrgKeyLabels().clear();
                    documentForm.getDeptOrgKeyLabels().add(new KeyLabelPair("", "Please Select"));

                    if (StringUtils.isNotEmpty(cLevelOrg)) {

                        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
                        List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

                        for (LevelOrganization levelOrganization : dLevelOrgs) {

                            documentForm.getDeptOrgKeyLabels()
                                    .add(
                                            new KeyLabelPair(levelOrganization.getCode(), levelOrganization
                                                    .getCodeAndDescription()));

                        }
                    }
                }
            }
        }

        return actionForward;
    }

    /**
     * Sets the College and Department based on the initiator primary department.
     * 
     * @param documentForm
     */
    private void setCollegeAndDepartmentBasedOnPrimaryDepartment(IWantDocumentForm documentForm) {
        IWantDocumentService iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
        String primaryDeptOrg = null;

        if (documentForm != null && documentForm.getDocument() != null
                && StringUtils.isEmpty(((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {

            Person currentUser = GlobalVariables.getUserSession().getPerson();
            IdentityManagementService identityManagementService = SpringContext
                    .getBean(IdentityManagementService.class);

            KimEntityInfo entityInfo = identityManagementService.getEntityInfoByPrincipalId(currentUser
                    .getPrincipalId());

            if (ObjectUtils.isNotNull(entityInfo)) {
                if (ObjectUtils.isNotNull(entityInfo.getEmploymentInformation())
                        && entityInfo.getEmploymentInformation().size() > 0) {
                    KimEntityEmploymentInformation employmentInformation = entityInfo.getEmploymentInformation().get(0);
                    String primaryDepartment = employmentInformation.getPrimaryDepartmentCode();
                    primaryDeptOrg = primaryDepartment.substring(primaryDepartment.lastIndexOf("-") + 1,
                            primaryDepartment.length());

                    String cLevelOrg = iWantDocumentService.getCLevelOrganizationForDLevelOrg(primaryDepartment);
                    ((IWantDocument) documentForm.getDocument()).setCollegeLevelOrganization(cLevelOrg);
                }
            }
        }

        if (documentForm != null && documentForm.getDocument() != null
                && StringUtils.isNotEmpty(((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization())) {
            String cLevelOrg = ((IWantDocument) documentForm.getDocument()).getCollegeLevelOrganization();
            documentForm.getDeptOrgKeyLabels().clear();
            documentForm.getDeptOrgKeyLabels().add(new KeyLabelPair("", "Please Select"));

            List<LevelOrganization> dLevelOrgs = iWantDocumentService.getDLevelOrganizations(cLevelOrg);

            for (LevelOrganization levelOrganization : dLevelOrgs) {
                documentForm.getDeptOrgKeyLabels().add(
                        new KeyLabelPair(levelOrganization.getCode(), levelOrganization.getCodeAndDescription()));

            }

            if (primaryDeptOrg != null) {
                ((IWantDocument) documentForm.getDocument()).setDepartmentLevelOrganization(primaryDeptOrg);
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
        iWantForm.setStep(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);
        IWantDocument iWantDocument = iWantForm.getIWantDocument();
        iWantDocument.setStep(CUPurapConstants.IWantDocumentSteps.ROUTING_STEP);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#close(org.apache.struts.action.ActionMapping,
     * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        super.close(mapping, form, request, response);

        return mapping.findForward(KFSConstants.MAPPING_PORTAL);
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
        IWantItem item = iWantDocumentForm.getNewIWantItemLine();
        IWantDocument iWantDocument = (IWantDocument) iWantDocumentForm.getDocument();

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        // call business rules
        rulePassed &= ruleService.applyRules(new KualiAddLineEvent(iWantDocument, "items", item));

        if (rulePassed) {
            item = iWantDocumentForm.getAndResetNewIWantItemLine();
            iWantDocument.addItem(item);
        }

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
        String parameterName = (String) request.getAttribute(KNSConstants.METHOD_TO_CALL_ATTRIBUTE);
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
        IWantAccount account = iWantDocumentForm.getNewSourceLine();
        IWantDocument iWantDoc = (IWantDocument) iWantDocumentForm.getDocument();

        KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
        boolean rulePassed = true;

        rulePassed &= ruleService.applyRules(new KualiAddLineEvent(iWantDoc, "accounts", account));

        if (rulePassed) {
            account = iWantDocumentForm.getAndResetNewIWantAccountLine();
            iWantDoc.addAccount(account);
        }

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
        String step = iWantDocForm.getStep();

        boolean setDefaultCollegeDept = iWantDocument.isUseCollegeAndDepartmentAsDefault();

        if (setDefaultCollegeDept) {
            // set these values in user Options table
            String college = iWantDocument.getCollegeLevelOrganization();
            String department = iWantDocument.getDepartmentLevelOrganization();
            String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();

            IWantDocUserOptions userOptionsCollege = new IWantDocUserOptions();
            userOptionsCollege.setPrincipalId(principalId);
            userOptionsCollege.setOptionId(CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE);
            userOptionsCollege.setOptionValue(college);

            IWantDocUserOptions retrievedUserOptionsCollege = (IWantDocUserOptions) getBusinessObjectService()
                    .retrieve(userOptionsCollege);

            if (ObjectUtils.isNotNull(retrievedUserOptionsCollege)) {
                retrievedUserOptionsCollege.setOptionValue(college);
                getBusinessObjectService().save(retrievedUserOptionsCollege);
            } else {

                getBusinessObjectService().save(userOptionsCollege);
            }

            IWantDocUserOptions userOptionsDepartment = new IWantDocUserOptions();
            userOptionsDepartment.setPrincipalId(principalId);
            userOptionsDepartment.setOptionId(CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT);
            userOptionsDepartment.setOptionValue(department);

            IWantDocUserOptions retrievedUserOptionsDepartment = (IWantDocUserOptions) getBusinessObjectService()
                    .retrieve(userOptionsDepartment);

            if (ObjectUtils.isNotNull(retrievedUserOptionsDepartment)) {
                retrievedUserOptionsDepartment.setOptionValue(department);
                getBusinessObjectService().save(retrievedUserOptionsDepartment);
            } else {
                getBusinessObjectService().save(userOptionsDepartment);
            }

            //            UserOptionsService userOptionsService = SpringContext.getBean(UserOptionsService.class);
            //            String principalId = iWantDocument.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
            //            userOptionsService.save(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_COLLEGE, college);
            //            userOptionsService.save(principalId, CUPurapConstants.USER_OPTIONS_DEFAULT_DEPARTMENT, department);
        }

        // add selected chart and department to document description
        String routingChart = iWantDocument.getRoutingChart() == null ? "" : iWantDocument.getRoutingChart() + "-";
        String routingOrg = iWantDocument.getRoutingOrganization() == null ? "" : iWantDocument
                .getRoutingOrganization();
        String addChartOrgToDesc = routingChart + routingOrg;
        String description = iWantDocument.getDocumentHeader().getDocumentDescription() + " "
                + addChartOrgToDesc;
        iWantDocument.getDocumentHeader().setDocumentDescription(description);

        ActionForward actionForward = super.route(mapping, form, request, response);

        if (CUPurapConstants.IWantDocumentSteps.ROUTING_STEP.equalsIgnoreCase(step)) {
            return mapping.findForward("finish");
        } else {
            return actionForward;
        }
    }

    /**
     * Use the new attachment description field to set the note text.
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#insertBONote(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward insertBONote(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IWantDocumentForm iWantForm = (IWantDocumentForm) form;

        IWantDocument iWantDocument = (IWantDocument) iWantForm.getDocument();
        Note newNote = iWantForm.getNewNote();
        newNote.setNoteText(iWantDocument.getAttachmentDescription());

        return super.insertBONote(mapping, form, request, response);
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

        if (request.getParameter("docId") == null) {
            forward = super.copy(mapping, form, request, response);
        } else {
            // this is copy document from Procurement Gateway:
            // use this link to call: http://localhost:8080/kfs-dev/purapIWant.do?methodToCall=copy&docId=xxxx
            String docId = request.getParameter("docId");
            KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

            IWantDocument document = null;
            document = (IWantDocument) getDocumentService().getByDocumentHeaderId(docId);
            document.toCopyFromGateway();

            kualiDocumentFormBase.setDocument(document);
            KualiWorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
            kualiDocumentFormBase.setDocTypeName(workflowDocument.getDocumentType());
            GlobalVariables.getUserSession().setWorkflowDocument(workflowDocument);

            forward = mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
        return forward;
    }

    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#refresh(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
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
        }
        return actionForward;
    }

}