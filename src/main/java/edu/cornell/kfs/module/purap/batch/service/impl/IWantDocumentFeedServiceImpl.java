package edu.cornell.kfs.module.purap.batch.service.impl;

import java.io.File;
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
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kns.rule.event.KualiAddLineEvent;
import org.kuali.rice.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.KualiRuleService;
import org.kuali.rice.krad.util.ErrorMessage;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;
import org.kuali.rice.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

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
    protected KualiRuleService ruleService;

    /**
     * @see edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService#processIWantDocumentFiles()
     */
    @Override
    public boolean processIWantDocumentFiles() {

        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(iWantDocumentInputFileType);

        List<String> processedFiles = new ArrayList<String>();

        for (String incomingFileName : fileNamesToLoad) {
            try {
                LOG.debug("processIWantDocumentFiles  () Processing " + incomingFileName);
                IWantDocumentBatchFeed batchFeed = parseInputFile(incomingFileName);

                if (batchFeed != null && !batchFeed.getBatchIWantDocuments().isEmpty()) {
                    loadIWantDocuments(batchFeed, incomingFileName, GlobalVariables.getMessageMap());
                    processedFiles.add(incomingFileName);
                }
            } catch (RuntimeException e) {
                LOG.error("Caught exception trying to load i want document file: " + incomingFileName, e);
                throw new RuntimeException("Caught exception trying to load i want document file: " + incomingFileName, e);
            }
        }
        // remove done files
        removeDoneFiles(processedFiles);

        return true;

    }

    /**
     * Parses the input file.
     * 
     * @param incomingFileName
     * @return an IWantDocumentBatchFeed containing input data
     */
    protected IWantDocumentBatchFeed parseInputFile(String incomingFileName) {
        LOG.info("Parsing file: " + incomingFileName);

        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        } catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        Object parsed = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            parsed = batchInputFileService.parse(iWantDocumentInputFileType, fileByteContent);
        } catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        } catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());
        }

        return (IWantDocumentBatchFeed) parsed;

    }

    /**
     * Creates I Wantd documents from the data in the input files.
     * 
     * @param batchFeed
     * @param incomingFileName
     * @param MessageMap
     */
    public void loadIWantDocuments(IWantDocumentBatchFeed batchFeed, String incomingFileName, MessageMap MessageMap) {

        LOG.info("Loading I Want documents from incoming file file: " + incomingFileName);

        for (BatchIWantDocument batchIWantDocument : batchFeed.getBatchIWantDocuments()) {

            populateIWantDocument(batchIWantDocument);

        }

    }

    /**
     * Populates an I Want document based on the input data.
     * 
     * @param batchIWantDocument
     */
    private void populateIWantDocument(BatchIWantDocument batchIWantDocument) {

        boolean noErrors = true;
        LOG.info("Creating I Want document from data related to source number: " + batchIWantDocument.getSourceNumber());

        try {
            if (StringUtils.isBlank(batchIWantDocument.getInitiator())) {
                LOG.error("Initiator net ID cannot be empty: " + batchIWantDocument.getInitiator());
                noErrors = false;
            }

            Person initiator = personService.getPersonByPrincipalName(batchIWantDocument.getInitiator());

            if (ObjectUtils.isNull(initiator)) {
                LOG.error("Initiator net ID is not valid: " + batchIWantDocument.getInitiator());
                noErrors = false;
            }

            IWantDocument iWantDocument = (IWantDocument) documentService.getNewDocument("IWNT", batchIWantDocument.getInitiator());

            iWantDocument.setInitiatorNetID(batchIWantDocument.getInitiatorNetID());
            iWantDocument.getDocumentHeader().setExplanation(batchIWantDocument.getBusinessPurpose());
            iWantDocument.setExplanation(batchIWantDocument.getBusinessPurpose());
            iWantDocument.setCollegeLevelOrganization(batchIWantDocument.getCollegeLevelOrganization());
            iWantDocument.setDepartmentLevelOrganization(batchIWantDocument.getDepartmentLevelOrganization());

            if (StringUtils.isNotEmpty(batchIWantDocument.getInitiatorName())) {
                iWantDocument.setInitiatorName(batchIWantDocument.getInitiatorName());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getInitiatorEmailAddress())) {
                iWantDocument.setInitiatorEmailAddress(batchIWantDocument.getInitiatorEmailAddress());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getInitiatorPhoneNumber())) {
                iWantDocument.setInitiatorPhoneNumber(batchIWantDocument.getInitiatorPhoneNumber());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getInitiatorAddress())) {
                iWantDocument.setInitiatorAddress(batchIWantDocument.getInitiatorAddress());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getDeliverToNetID())) {
                iWantDocument.setDeliverToNetID(batchIWantDocument.getDeliverToNetID());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getDeliverToName())) {
                iWantDocument.setDeliverToName(batchIWantDocument.getDeliverToName());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getDeliverToEmailAddress())) {
                iWantDocument.setDeliverToEmailAddress(batchIWantDocument.getDeliverToEmailAddress());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getDeliverToPhoneNumber())) {
                iWantDocument.setDeliverToPhoneNumber(batchIWantDocument.getDeliverToPhoneNumber());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getDeliverToAddress())) {
                iWantDocument.setDeliverToAddress(batchIWantDocument.getDeliverToAddress());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getVendorName())) {
                iWantDocument.setVendorName(batchIWantDocument.getVendorName());
            }

            if (StringUtils.isNotEmpty(batchIWantDocument.getVendorDescription())) {
                iWantDocument.setVendorDescription(batchIWantDocument.getVendorDescription());
            }

            // add items
            noErrors &= populateIWantDocItems(batchIWantDocument, iWantDocument);

            // add accounts
            noErrors &= populateIWantDocAccounts(batchIWantDocument, iWantDocument);

            // account Description
            if (StringUtils.isNotBlank(batchIWantDocument.getAccountDescriptionTxt())) {
                iWantDocument.setAccountDescriptionTxt(batchIWantDocument.getAccountDescriptionTxt());
            }

            if (StringUtils.isNotBlank(batchIWantDocument.getCommentsAndSpecialInstructions())) {
                iWantDocument.setCommentsAndSpecialInstructions(batchIWantDocument.getCommentsAndSpecialInstructions());
            }

            iWantDocument.setGoods(batchIWantDocument.isGoods());

            if (StringUtils.isNotBlank(batchIWantDocument.getServicePerformedOnCampus())) {
                iWantDocument.setServicePerformedOnCampus(batchIWantDocument.getServicePerformedOnCampus());
            }

            iWantDocumentService.setIWantDocumentDescription(iWantDocument);

            boolean rulePassed = true;

            // call business rules
            rulePassed &= ruleService.applyRules(new RouteDocumentEvent("", iWantDocument));
            if (!rulePassed) {
                loggErrorMessages();

            } else if (noErrors) {
                documentService.routeDocument(iWantDocument, "", null);
            }

        } catch (Exception e) {
            LOG.error("error while creating I Want document:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to create I Want document " + e.getMessage(), e);
        }

    }

    /**
     * Populates I Want doc items.
     * 
     * @param batchIWantDocument
     * @param iWantDocument
     * @return true if no errors encountered, false otherwise
     */
    protected boolean populateIWantDocItems(BatchIWantDocument batchIWantDocument, IWantDocument iWantDocument) {
        LOG.info("Populate I Want doc items");

        boolean noErrors = true;
        // items
        List<IWantItem> iWantItems = batchIWantDocument.getItems();
        if (CollectionUtils.isNotEmpty(iWantItems)) {
            for (IWantItem item : iWantItems) {
                IWantItem addItem = new IWantItem();
                addItem.setItemDescription(item.getItemDescription());
                addItem.setItemTypeCode(item.getItemUnitOfMeasureCode());
                addItem.setItemCatalogNumber(item.getItemCatalogNumber());
                addItem.setItemUnitPrice(item.getItemUnitPrice());
                addItem.setPurchasingCommodityCode(item.getPurchasingCommodityCode());
                addItem.setItemQuantity(item.getItemQuantity());

                boolean rulePassed = ruleService.applyRules(new AddIWantItemEvent(StringUtils.EMPTY, iWantDocument, addItem));
                if (rulePassed) {
                    iWantDocument.addItem(addItem);
                } else {
                    loggErrorMessages();
                }
                noErrors &= rulePassed;
            }
        }

        return noErrors;
    }

    /**
     * Populates the I Want document accounts
     * 
     * @param batchIWantDocument
     * @param iWantDocument
     * @return true if no errors encountered, false otherwise
     */
    protected boolean populateIWantDocAccounts(BatchIWantDocument batchIWantDocument, IWantDocument iWantDocument) {
        LOG.info("Populate I Want doc accounts");
        boolean noErrors = true;

        // accounts
        List<IWantAccount> iWantAccounts = batchIWantDocument.getAccounts();
        if (CollectionUtils.isNotEmpty(iWantAccounts)) {
            for (IWantAccount account : iWantAccounts) {
                IWantAccount addAccount = new IWantAccount();
                addAccount.setAccountNumber(account.getAccountNumber());
                addAccount.setSubAccountNumber(account.getSubAccountNumber());
                addAccount.setChartOfAccountsCode(account.getChartOfAccountsCode());
                addAccount.setFinancialObjectCode(account.getFinancialObjectCode());
                addAccount.setFinancialSubObjectCode(account.getFinancialSubObjectCode());
                addAccount.setOrganizationReferenceId(account.getOrganizationReferenceId());
                addAccount.setProjectCode(account.getProjectCode());
                addAccount.setAmountOrPercent(account.getAmountOrPercent());
                addAccount.setUseAmountOrPercent(account.getUseAmountOrPercent());

                boolean rulePassed = ruleService.applyRules(new KualiAddLineEvent(iWantDocument, "accounts", addAccount));

                if (rulePassed) {
                    iWantDocument.addAccount(addAccount);
                } else {
                    loggErrorMessages();
                }

                noErrors &= rulePassed;
            }
        }

        return noErrors;
    }

    /**
     * Logs error messages from GlobalVariables.
     */
    protected void loggErrorMessages() {
        Map<String, AutoPopulatingList<ErrorMessage>> errors = GlobalVariables.getMessageMap().getErrorMessages();
        for (AutoPopulatingList<ErrorMessage> error : errors.values()) {
            Iterator<ErrorMessage> iterator = error.iterator();
            while (iterator.hasNext()) {
                ErrorMessage errorMessage = iterator.next();
                LOG.error(errorMessage.toString());
            }
        }

        GlobalVariables.getMessageMap().clearErrorMessages();
    }

    /**
     * Clears out associated .done files for the processed data files.
     * 
     * @param dataFileNames
     */
    protected void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
     * Gets the batchInputFileService.
     * 
     * @return Returns the batchInputFileService.
     */
    protected BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * Sets the batchInputFileService.
     * 
     * @param batchInputFileService
     *            The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Gets the iWantDocumentInputFileType.
     * 
     * @return iWantDocumentInputFileType
     */
    public BatchInputFileType getiWantDocumentInputFileType() {
        return iWantDocumentInputFileType;
    }

    /**
     * Sets the iWantDocumentInputFileType
     * 
     * @param iWantDocumentInputFileType
     */
    public void setiWantDocumentInputFileType(BatchInputFileType iWantDocumentInputFileType) {
        this.iWantDocumentInputFileType = iWantDocumentInputFileType;
    }

    /**
     * Gets the documentService.
     * 
     * @return documentService
     */
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Sets the documentService.
     * 
     * @param documentService
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Gets the personService.
     * 
     * @return personService
     */
    public PersonService getPersonService() {
        return personService;
    }

    /**
     * Sets the personService.
     * 
     * @param personService
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Gets the iWantDocumentService.
     * 
     * @return iWantDocumentService
     */
    public IWantDocumentService getiWantDocumentService() {
        return iWantDocumentService;
    }

    /**
     * Sets the iWantDocumentService.
     * 
     * @param iWantDocumentService
     */
    public void setiWantDocumentService(IWantDocumentService iWantDocumentService) {
        this.iWantDocumentService = iWantDocumentService;
    }

    /**
     * Gets the ruleService.
     * 
     * @return ruleService
     */
    public KualiRuleService getRuleService() {
        return ruleService;
    }

    /**
     * Sets the ruleService.
     * 
     * @param ruleService
     */
    public void setRuleService(KualiRuleService ruleService) {
        this.ruleService = ruleService;
    }

}