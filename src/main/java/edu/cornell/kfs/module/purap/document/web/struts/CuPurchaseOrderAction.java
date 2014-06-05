package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderSensitiveData;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderAction;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuPurchaseOrderAction extends PurchaseOrderAction {
	// ==== CU Customization (KFSPTS-1457): Added some new constants and variables. ====
	private static final String MOVE_CXML_ERROR_PO_PERM = "Move CXML Error PO";
	private static final String MOVE_CXML_ERROR_PO_SUCCESS = "moveCxmlErrorPoSuccess";
	private static final String STATUS_OVERRIDE_QUESTION = "statusOverrideQuestion";
	
	  // ==== CU Customization (KFSPTS-1457): Added the ability for certain users to move "CXER"-status POs into "OPEN" or "VOID" status. ====
	   

	@Override
	public ActionForward save(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ActionForward forward =  super.save(mapping, form, request, response);
        //reindex the document to pick up the changes.
        PurchaseOrderDocument document = (PurchaseOrderDocument) ((PurchaseOrderForm)form).getDocument();
        updateSensitiveData(document);
        this.reIndexDocument(document);
        return forward;
	}

    /**
     * This method is being added to handle calls to perform re-indexing of documents following change events performed on the documents.  This is necessary to correct problems
     * with searches not returning accurate results due to changes being made to documents, but those changes not be indexed.
     * 
     * @param document - The document to be re-indexed.
     */
    private void reIndexDocument(PurchaseOrderDocument document) {
        //force reindexing
//        DocumentRouteHeaderValue routeHeader = KEWServiceLocator.getRouteHeaderService().getRouteHeader(document.getDocumentNumber());
//		SearchableAttributeProcessingService searchableAttributeService = MessageServiceNames.getSearchableAttributeService(routeHeader);
//		searchableAttributeService.indexDocument(Long.valueOf(document.getDocumentNumber()));
        //RICE20 replaced searchableAttributeProcessingService.indexDocument with DocumentAttributeIndexingQueue.indexDocument
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();

        documentAttributeIndexingQueue.indexDocument(document.getDocumentNumber());

    }
    
	public ActionForward openPoCxer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return movePoCxer(mapping, form, request, response, PurchaseOrderStatuses.APPDOC_OPEN, "Open");
	}
	   
	public ActionForward voidPoCxer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    return movePoCxer(mapping, form, request, response, PurchaseOrderStatuses.APPDOC_VOID, "Void");
	}
	   
	protected ActionForward movePoCxer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response,
	            String newStatus, String newStatusLabel) throws Exception {
		KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
	    PurchaseOrderDocument po = (PurchaseOrderDocument) kualiDocumentFormBase.getDocument();
	    Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
	       
	    // If the user has not received the question screen yet, then perform the PO status update.
	    if (ObjectUtils.isNull(question)) {
	    	
	    	// Check authorization.
	    	checkMovePoCxerAuthorization(po);
	    	
	    	// Use logic similar to the executeManualStatusChange() method to override the document's status.
	    	try {
	    		PurapService purapService = SpringContext.getBean(PurapService.class);
	    		po.updateAndSaveAppDocStatus(newStatus);
	    		purapService.saveDocumentNoValidation(po);
	    		}
	    	catch (Exception e) {
	    		throw new RuntimeException(e);
	    		}
	    	
	    	// Add a message to the route log.
	    	po.getDocumentHeader().getWorkflowDocument().logAnnotation("Moved PO document from 'Error occurred sending cxml' status to '" + newStatusLabel + "' status.");
	    	
	    	// Present a success message to the user.
	    	String message = "PO document " + po.getDocumentNumber() + " was successfully moved to '" + newStatusLabel + "' status.";
	    	
	    	return this.performQuestionWithoutInput(mapping, form, request, response, MOVE_CXML_ERROR_PO_SUCCESS, message, STATUS_OVERRIDE_QUESTION, MOVE_CXML_ERROR_PO_SUCCESS, "");
	    	}
	    
	    // If the user already went to the success "question" (which only had a "close" button), then simply perform the "returnToSender" logic.
	    
	    return returnToSender(request, mapping, kualiDocumentFormBase);
	    }
	// A helper method for checking if the user has authorization to move CXML Error POs and if the document is indeed in CXML error status.
	
	private void checkMovePoCxerAuthorization(PurchaseOrderDocument po) throws Exception {
		
		// Check if the user is authorized to open/void the PO.
		if (!KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM)) {
			throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(), "manuallyOverridePurchaseOrderStatus",
					po.getDocumentNumber(), "You are not authorized to manually move purchase order statuses on PO documents in 'Error occurred sending cxml' status.", null);
			}
		
		// Ensure that the document is currently in "CXER" status before trying to update it.
		if (!PurchaseOrderStatuses.APPDOC_CXML_ERROR.equals(po.getApplicationDocumentStatus())) {
			throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(), "manuallyOverridePurchaseOrderStatus",
					po.getDocumentNumber(), "You are not authorized to perform this action on PO documents not in 'Error occurred sending cxml' status.", null);
			}
		}
	   
	   // ==== End CU Customization ====

    public ActionForward cancel(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        ActionForward forward = super.cancel(mapping, form, request, response);
        if (question == null) {
            return forward;
        } else {
            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if ((KFSConstants.DOCUMENT_CANCEL_QUESTION.equals(question)) && ConfirmationQuestion.NO.equals(buttonClicked)) {

                // if no button clicked just reload the doc
                return forward;
            }
            // else go to cancel logic below
        }

        // TODO : need to check note length ?
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        String reason = request.getParameter(KFSConstants.QUESTION_REASON_ATTRIBUTE_NAME);
        if (StringUtils.isNotBlank(reason)) {
            String noteText = "Reason for cancelling PO : " + reason;
            Note newNote = new Note();
            newNote.setNoteText(noteText);
            newNote.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
            kualiDocumentFormBase.setNewNote(newNote);
            try {
                insertBONote(mapping, kualiDocumentFormBase, request, response);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return forward;
    }

    private void updateSensitiveData(PurchaseOrderDocument po) {
        List<PurchaseOrderSensitiveData> savedSensitiveData = new ArrayList<PurchaseOrderSensitiveData>();
        Set<String> savedSensitiveDataCodes = new HashSet<String>();
        if (po.getPurchaseOrderSensitiveData() != null) {
            for (PurchaseOrderSensitiveData sensitiveData : po.getPurchaseOrderSensitiveData()) {
                savedSensitiveData.add(sensitiveData);
                savedSensitiveDataCodes.add(sensitiveData.getSensitiveDataCode());
            }
        }
        Set<String> currentSensitiveDataCodes = new HashSet<String>();
        for (PurchasingItemBase item : (List<PurchasingItemBase>)po.getItems()) {
            if (item.getCommodityCode() != null 
                    && item.getCommodityCode().getSensitiveDataCode() != null 
                    && item.getCommodityCode().getSensitiveDataCode().length() != 0) {
                currentSensitiveDataCodes.add(item.getCommodityCode().getSensitiveDataCode());
            }
        }
        if (CollectionUtils.isNotEmpty(savedSensitiveData)) {
            List<PurchaseOrderSensitiveData> removeSensitiveData = new ArrayList<PurchaseOrderSensitiveData>();
            for (PurchaseOrderSensitiveData sensitiveData : savedSensitiveData) {
                if (!currentSensitiveDataCodes.contains(sensitiveData.getSensitiveDataCode())) {
                    removeSensitiveData.add(sensitiveData);
                }
            }
            if (CollectionUtils.isNotEmpty(removeSensitiveData)) {
                getBusinessObjectService().delete(removeSensitiveData);
                
            }
        }
        // TODO : 'add' is not in jira scope.  so comment out for now
        if (CollectionUtils.isNotEmpty(currentSensitiveDataCodes)) {
            List<PurchaseOrderSensitiveData> addedSensitiveData = new ArrayList<PurchaseOrderSensitiveData>();            
            for (String code : currentSensitiveDataCodes) {
                if (!savedSensitiveDataCodes.contains(code)) {
                    addedSensitiveData.add(new PurchaseOrderSensitiveData(po.getPurapDocumentIdentifier(),po.getRequisitionIdentifier(), code));                   
                }
            }
            if (CollectionUtils.isNotEmpty(addedSensitiveData)) {
                getBusinessObjectService().save(addedSensitiveData);
                
            }
        }

    }
 
    @Override
    public ActionForward close(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiDocumentFormBase docForm = (KualiDocumentFormBase) form;
        ActionForward forward = super.close(mapping, docForm, request, response);
        // only want to prompt them to save if they already can save
        if (canSave(docForm)) {
            Object question = getQuestion(request);
            Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
           if ((KRADConstants.DOCUMENT_SAVE_BEFORE_CLOSE_QUESTION.equals(question)) && ConfirmationQuestion.YES.equals(buttonClicked)) {
                updateSensitiveData((PurchaseOrderDocument) ((PurchaseOrderForm)form).getDocument());
           }
        }

        return forward;
    }

    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward forward = super.route(mapping, form, request, response);
        if (GlobalVariables.getMessageMap().hasNoErrors()) {
            updateSensitiveData((PurchaseOrderDocument) ((PurchaseOrderForm)form).getDocument());
        }
        return forward;
    }

}
