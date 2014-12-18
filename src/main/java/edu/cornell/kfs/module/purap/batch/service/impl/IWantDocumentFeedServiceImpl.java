package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kns.rule.event.KualiAddLineEvent;
import org.kuali.rice.krad.bo.AdHocRouteRecipient;
import org.kuali.rice.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.rice.krad.service.DictionaryValidationService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.KualiRuleService;
import org.kuali.rice.krad.util.ErrorMessage;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

import com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService;

import edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService;
import edu.cornell.kfs.module.purap.businessobject.IWantAccount;
import edu.cornell.kfs.module.purap.businessobject.IWantDocumentBatchFeed;
import edu.cornell.kfs.module.purap.businessobject.IWantItem;
import edu.cornell.kfs.module.purap.document.BatchIWantDocument;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.module.purap.document.validation.event.AddIWantItemEvent;

@Transactional
public class IWantDocumentFeedServiceImpl implements IWantDocumentFeedService {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(IWantDocumentFeedServiceImpl.class);
	
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType iWantDocumentInputFileType;
    protected DocumentService documentService;
    protected PersonService personService;
    protected IWantDocumentService iWantDocumentService;

	@Override
	public boolean processIWantDocumentFiles() {
		
		 List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(iWantDocumentInputFileType);

	        for (String incomingFileName : fileNamesToLoad) {
	            try {
	                LOG.debug("processIWantDocumentFiles  () Processing " + incomingFileName);
	                IWantDocumentBatchFeed batchFeed = parseInputFile(incomingFileName);

	                if (batchFeed != null && !batchFeed.getBatchIWantDocuments().isEmpty()) {
	                    loadIWantDocuments(batchFeed, incomingFileName, GlobalVariables.getMessageMap());
	                }
	            }
	            catch (RuntimeException e) {
	                LOG.error("Caught exception trying to load disbursement voucher file: " + incomingFileName, e);
	                throw new RuntimeException("Caught exception trying to load disbursement voucher file: " + incomingFileName, e);
	            }
	        }
		return true;

	}
	
	private IWantDocumentBatchFeed parseInputFile(String incomingFileName){
		FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        Object parsed = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            parsed = batchInputFileService.parse(iWantDocumentInputFileType, fileByteContent);
        }
        catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        }
        catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());
        }
        
        return (IWantDocumentBatchFeed)parsed;

	}
	
    public void loadIWantDocuments(IWantDocumentBatchFeed batchFeed, String incomingFileName, MessageMap MessageMap) {
    	
    	for(BatchIWantDocument batchIWantDocument :  batchFeed.getBatchIWantDocuments()){
    		
    		populateIWantDocument(batchIWantDocument);
    		
    	}

    }
    
    private void populateIWantDocument(BatchIWantDocument batchIWantDocument){
    	try {
			IWantDocument iWantDocument = (IWantDocument)documentService.getNewDocument("IWNT", batchIWantDocument.getInitiator());
			
			iWantDocument.setInitiatorNetID(batchIWantDocument.getInitiatorNetID());
			iWantDocument.getDocumentHeader().setExplanation(batchIWantDocument.getBusinessPurpose());
			iWantDocument.setExplanation(batchIWantDocument.getBusinessPurpose());		
			iWantDocument.setCollegeLevelOrganization(batchIWantDocument.getCollegeLevelOrganization());
			iWantDocument.setDepartmentLevelOrganization(batchIWantDocument.getDepartmentLevelOrganization());
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getInitiatorName())){
				iWantDocument.setInitiatorName(batchIWantDocument.getInitiatorName());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getInitiatorEmailAddress())){
				iWantDocument.setInitiatorEmailAddress(batchIWantDocument.getInitiatorEmailAddress());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getInitiatorPhoneNumber())){
				iWantDocument.setInitiatorPhoneNumber(batchIWantDocument.getInitiatorPhoneNumber());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getInitiatorAddress())){
				iWantDocument.setInitiatorAddress(batchIWantDocument.getInitiatorAddress());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getDeliverToNetID())){
				iWantDocument.setDeliverToNetID(batchIWantDocument.getDeliverToNetID());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getDeliverToName())){
				iWantDocument.setDeliverToName(batchIWantDocument.getDeliverToName());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getDeliverToEmailAddress())){
				iWantDocument.setDeliverToEmailAddress(batchIWantDocument.getDeliverToEmailAddress());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getDeliverToPhoneNumber())){
				iWantDocument.setDeliverToPhoneNumber(batchIWantDocument.getDeliverToPhoneNumber());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getDeliverToAddress())){
				iWantDocument.setDeliverToAddress(batchIWantDocument.getDeliverToAddress());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getVendorName())){
				iWantDocument.setVendorName(batchIWantDocument.getVendorName());
			}
			
			if(StringUtils.isNotEmpty(batchIWantDocument.getVendorDescription())){
				iWantDocument.setVendorDescription(batchIWantDocument.getVendorDescription());
			}
			 KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);
			
			//items
			List<IWantItem> iWantItems = batchIWantDocument.getItems();
			if(CollectionUtils.isNotEmpty(iWantItems)){
				for(IWantItem item : iWantItems){
					boolean rulePassed = ruleService.applyRules(new AddIWantItemEvent(StringUtils.EMPTY, iWantDocument, item));
					if(rulePassed){
						iWantDocument.addItem(item);
					}
					else{
						loggErrorMessages();
					}
				}
			}
			
			//accounts
			List<IWantAccount> iWantAccounts = batchIWantDocument.getAccounts();
			if(CollectionUtils.isNotEmpty(iWantAccounts)){
				for(IWantAccount account : iWantAccounts){
					
					boolean rulePassed = ruleService.applyRules(new KualiAddLineEvent(iWantDocument, "accounts", account));
					
					if(rulePassed){
						iWantDocument.addAccount(account);
					}
					else{
						loggErrorMessages();
					}
				}
			}
			
			//account Description
			if(StringUtils.isNotBlank(batchIWantDocument.getAccountDescriptionTxt())){
				iWantDocument.setAccountDescriptionTxt(batchIWantDocument.getAccountDescriptionTxt());
			}
			
			if(StringUtils.isNotBlank(batchIWantDocument.getCommentsAndSpecialInstructions())){
				iWantDocument.setCommentsAndSpecialInstructions(batchIWantDocument.getCommentsAndSpecialInstructions());
			}
			
			iWantDocument.setGoods(batchIWantDocument.isGoods());
			
			if(StringUtils.isNotBlank(batchIWantDocument.getServicePerformedOnCampus())){
				iWantDocument.setServicePerformedOnCampus(batchIWantDocument.getServicePerformedOnCampus());
			}
			
			iWantDocumentService.setIWantDocumentDescription(iWantDocument);
	       
	        boolean rulePassed = true;

	        // call business rules
	        rulePassed &= ruleService.applyRules(new RouteDocumentEvent("", iWantDocument));
	        if(!rulePassed){
	        	LOG.error("Errors for I Want doc related to source number: " + batchIWantDocument.getSourceNumber());
	        	loggErrorMessages();

	        }
			
	        if(rulePassed){
	        	documentService.routeDocument(iWantDocument,"", null);
	        }
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    
    protected void loggErrorMessages(){
        Map<String,AutoPopulatingList<ErrorMessage>> errors = GlobalVariables.getMessageMap().getErrorMessages();
        for(AutoPopulatingList<ErrorMessage> error: errors.values()){
        	Iterator<ErrorMessage> iterator = error.iterator();
        	while(iterator.hasNext()){
        		ErrorMessage errorMessage = iterator.next();
        		LOG.error(errorMessage.toString());
        	}
        }
        GlobalVariables.getMessageMap().clearErrorMessages();
    }

	
    /**
     * @return Returns the batchInputFileService.
     */
    protected BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * @param batchInputFileService The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

	public BatchInputFileType getiWantDocumentInputFileType() {
		return iWantDocumentInputFileType;
	}

	public void setiWantDocumentInputFileType(
			BatchInputFileType iWantDocumentInputFileType) {
		this.iWantDocumentInputFileType = iWantDocumentInputFileType;
	}
	
    
	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public IWantDocumentService getiWantDocumentService() {
		return iWantDocumentService;
	}

	public void setiWantDocumentService(IWantDocumentService iWantDocumentService) {
		this.iWantDocumentService = iWantDocumentService;
	}

}