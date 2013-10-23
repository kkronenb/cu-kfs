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
package org.kuali.kfs.fp.batch.service.impl;

import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.AUTO_APPROVE_DOCUMENTS_IND;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.AUTO_APPROVE_NUMBER_OF_DAYS;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.DEFAULT_TRANS_ACCOUNT_PARM_NM;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.DEFAULT_TRANS_CHART_CODE_PARM_NM;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.DEFAULT_TRANS_OBJECT_CODE_PARM_NM;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.ERROR_TRANS_ACCOUNT_PARM_NM;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.ERROR_TRANS_OBJECT_CODE_PARM_NM;
import static org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants.SINGLE_TRANSACTION_IND_PARM_NM;
import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.FinancialDocumentTypeCodes.PROCUREMENT_CARD;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.fp.batch.ProcurementCardAutoApproveDocumentsStep;
import org.kuali.kfs.fp.batch.ProcurementCardCreateDocumentsStep;
import org.kuali.kfs.fp.batch.ProcurementCardLoadStep;
import org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService;
import org.kuali.kfs.fp.businessobject.ProcurementCardHolder;
import org.kuali.kfs.fp.businessobject.ProcurementCardSourceAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTargetAccountingLine;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransaction;
import org.kuali.kfs.fp.businessobject.ProcurementCardTransactionDetail;
import org.kuali.kfs.fp.businessobject.ProcurementCardVendor;
import org.kuali.kfs.fp.document.ProcurementCardDocument;
import org.kuali.kfs.fp.document.validation.impl.ProcurementCardDocumentRuleConstants;
import org.kuali.kfs.integration.cab.CapitalAssetBuilderModuleService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.AccountingLineRuleHelperService;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.document.validation.event.DocumentSystemSaveEvent;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.kew.dto.DocumentSearchCriteriaDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultRowDTO;
import org.kuali.rice.kew.dto.KeyValueDTO;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.bo.DocumentHeader;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.DateUtils;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowInfo;
import org.kuali.rice.kns.workflow.service.WorkflowDocumentService;
import org.springframework.transaction.annotation.Transactional;


/**
 * This is the default implementation of the ProcurementCardCreateDocumentService interface.
 * 
 * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService
 */

public class ProcurementCardCreateDocumentServiceImpl implements ProcurementCardCreateDocumentService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ProcurementCardCreateDocumentServiceImpl.class);
    
    public static final String WORKFLOW_SEARCH_RESULT_KEY = "routeHeaderId";

    private ParameterService parameterService;
    private BusinessObjectService businessObjectService;
    private FinancialSystemDocumentService documentService;
    private DataDictionaryService dataDictionaryService;
    private DateTimeService dateTimeService;
    private WorkflowDocumentService workflowDocumentService;
    private AccountingLineRuleHelperService accountingLineRuleUtil;
    private CapitalAssetBuilderModuleService capitalAssetBuilderModuleService;


    /**
     * This method retrieves a collection of credit card transactions and traverses through this list, creating 
     * ProcurementCardDocuments for each card.
     * 
     * @return True if the procurement card documents were created successfully.  If any problem occur while creating the 
     * documents, a runtime exception will be thrown.
     * 
     * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService#createProcurementCardDocuments()
     */
    
    @Transactional
    public boolean createProcurementCardDocuments() {
        List documents = new ArrayList();
        List cardTransactions = retrieveTransactions();

        // iterate through card transaction list and create documents
        for (Iterator iter = cardTransactions.iterator(); iter.hasNext();) {
            documents.add(createProcurementCardDocument((List) iter.next()));
        }

        // now store all the documents
        for (Iterator iter = documents.iterator(); iter.hasNext();) {
            ProcurementCardDocument pcardDocument = (ProcurementCardDocument) iter.next();
            try {
                documentService.saveDocument(pcardDocument, DocumentSystemSaveEvent.class);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Saved Procurement Card document: "+pcardDocument.getDocumentNumber());
                }
            }
            catch (Exception e) {
                LOG.error("Error persisting document # " + pcardDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
                throw new RuntimeException("Error persisting document # " + pcardDocument.getDocumentHeader().getDocumentNumber() + " " + e.getMessage(), e);
            }
        }

        return true;
    }

    /**
     * This method retrieves all the procurement card documents with a status of 'I' and routes them to the next step in the
     * routing path.
     * 
     * @return True if the routing was performed successfully.  A runtime exception will be thrown if any errors occur while routing.
     * 
     * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService#routeProcurementCardDocuments(java.util.List)
     */
    public boolean routeProcurementCardDocuments() {
        List<String> documentIdList = null;
        try {
            documentIdList = retrieveProcurementCardDocumentsToRoute(KEWConstants.ROUTE_HEADER_SAVED_CD);
        } catch (WorkflowException e1) {
            LOG.error("Error retrieving pcdo documents for routing: " + e1.getMessage(),e1);
            throw new RuntimeException(e1.getMessage(),e1);
        } catch (RemoteException re) {
            LOG.error("Error retrieving pcdo documents for routing: " + re.getMessage(),re);
            throw new RuntimeException(re.getMessage(),re);
        }
        
        //Collections.reverse(documentIdList);
        if ( LOG.isInfoEnabled() ) {
            LOG.info("PCards to Route: "+documentIdList);
        }
        
        for (String pcardDocumentId: documentIdList) {
            try {
            	if ( LOG.isInfoEnabled() ) {
                    LOG.info("Retrieving PCDO document # " + pcardDocumentId + ".");
                }
                ProcurementCardDocument pcardDocument = (ProcurementCardDocument)documentService.getByDocumentHeaderId(pcardDocumentId);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("Routing PCDO document # " + pcardDocumentId + ".");
                }
                documentService.prepareWorkflowDocument(pcardDocument);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("PCDO document # " + pcardDocumentId + " prepared for workflow.");
                }
                // calling workflow service to bypass business rule checks
                workflowDocumentService.route(pcardDocument.getDocumentHeader().getWorkflowDocument(), "", null);
                if ( LOG.isInfoEnabled() ) {
                    LOG.info("PCDO document # " + pcardDocumentId + " routed.");
                }
            }
            catch (WorkflowException e) {
                LOG.error("Error routing document # " + pcardDocumentId + " " + e.getMessage());
                throw new RuntimeException(e.getMessage(),e);
            }
        }

        return true;
    }
    
    /**
     * Returns a list of all initiated but not yet routed procurement card documents, using the KualiWorkflowInfo service.
     * @return a list of procurement card documents to route
     */
    protected List<String> retrieveProcurementCardDocumentsToRoute(String statusCode) throws WorkflowException, RemoteException {
        List<String> documentIds = new ArrayList<String>();
        
        DocumentSearchCriteriaDTO criteria = new DocumentSearchCriteriaDTO();
        criteria.setDocTypeFullName(dataDictionaryService.getDocumentTypeNameByClass(ProcurementCardDocument.class));
        criteria.setDocRouteStatus(statusCode);
        DocumentSearchResultDTO results = SpringContext.getBean(KualiWorkflowInfo.class).performDocumentSearch(GlobalVariables.getUserSession().getPerson().getPrincipalId(), criteria);
        
        for (DocumentSearchResultRowDTO resultRow: results.getSearchResults()) {
            for (KeyValueDTO field : resultRow.getFieldValues()) {
                if (field.getKey().equals(WORKFLOW_SEARCH_RESULT_KEY)) {
                    documentIds.add(parseDocumentIdFromRouteDocHeader(field.getValue()));
                }
            }
        }
        
        return documentIds;
    }
    
    /**
     * Retrieves the document id out of the route document header
     * @param routeDocHeader the String representing an HTML link to the document
     * @return the document id
     */
    protected String parseDocumentIdFromRouteDocHeader(String routeDocHeader) {
        int rightBound = routeDocHeader.indexOf('>') + 1;
        int leftBound = routeDocHeader.indexOf('<', rightBound);
        return routeDocHeader.substring(rightBound, leftBound);
    }

    /**
     * This method determines if procurement card documents can be auto approved.  A document can be auto approved if 
     * the grace period for allowing auto approval of a procurement card document has passed.  The grace period is defined
     * by a parameter in the parameters table.  The create date of the document is then compared against the current date and 
     * if the difference is larger than the grace period defined, then the document is auto approved.
     * 
     * @return This method always returns true.
     * 
     * @see org.kuali.kfs.fp.batch.service.ProcurementCardCreateDocumentService#autoApproveProcurementCardDocuments()
     */
    public boolean autoApproveProcurementCardDocuments() {
        // check if auto approve is turned on
        boolean autoApproveOn = parameterService.getIndicatorParameter(ProcurementCardAutoApproveDocumentsStep.class, AUTO_APPROVE_DOCUMENTS_IND);

        if (!autoApproveOn) { // no auto approve?  then skip out of here...
            return true;
        }

        List<String> documentIdList = null;
        try {
            documentIdList = retrieveProcurementCardDocumentsToRoute(KEWConstants.ROUTE_HEADER_ENROUTE_CD);
        }
        catch (WorkflowException e1) {
            throw new RuntimeException(e1.getMessage(),e1);
        }
        catch (RemoteException re) {
            throw new RuntimeException(re.getMessage(),re);
        }

        // get number of days and type for auto approve
        int autoApproveNumberDays = Integer.parseInt(parameterService.getParameterValue(ProcurementCardAutoApproveDocumentsStep.class, AUTO_APPROVE_NUMBER_OF_DAYS));

        Timestamp currentDate = dateTimeService.getCurrentTimestamp();
        for (String pcardDocumentId: documentIdList) {
            try {
                ProcurementCardDocument pcardDocument = (ProcurementCardDocument)documentService.getByDocumentHeaderId(pcardDocumentId);
                
                // prevent PCard documents from auto approving if they have capital asset info to collect
                if(capitalAssetBuilderModuleService.hasCapitalAssetObjectSubType(pcardDocument)) {
                    continue;
                }

                // if number of days in route is passed the allowed number, call doc service for super user approve
                Timestamp docCreateDate = pcardDocument.getDocumentHeader().getWorkflowDocument().getCreateDate();
                if (DateUtils.getDifferenceInDays(docCreateDate, currentDate) > autoApproveNumberDays) {
                    // update document description to reflect the auto approval
                    pcardDocument.getDocumentHeader().setDocumentDescription("Auto Approved On " + dateTimeService.toDateTimeString(currentDate) + ".");
                
                    if ( LOG.isInfoEnabled() ) {
                        LOG.info("Auto approving document # " + pcardDocument.getDocumentHeader().getDocumentNumber());
                    }
                    documentService.superUserApproveDocument(pcardDocument, "");
                }
            } catch (WorkflowException e) {
                LOG.error("Error auto approving document # " + pcardDocumentId + " " + e.getMessage(),e);
                throw new RuntimeException(e.getMessage(),e);
            }
        }

        return true;
    }


    /**
     * This method retrieves a list of transactions from a temporary table, and groups them into document lists, based on 
     * single transaction indicator or a grouping by card.
     * 
     * @return List containing transactions for document.
     */
    protected List retrieveTransactions() {
        List groupedTransactions = new ArrayList();

        // retrieve records from transaction table order by card number
        List transactions = (List) businessObjectService.findMatchingOrderBy(ProcurementCardTransaction.class, new HashMap(), KFSPropertyConstants.TRANSACTION_CREDIT_CARD_NUMBER, true);

        // check apc for single transaction documents or multiple by card
        boolean singleTransaction = parameterService.getIndicatorParameter(ProcurementCardCreateDocumentsStep.class, SINGLE_TRANSACTION_IND_PARM_NM);

        List documentTransactions = new ArrayList();
        if (singleTransaction) {
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                documentTransactions.add(iter.next());
                groupedTransactions.add(documentTransactions);
                documentTransactions = new ArrayList();
            }
        }
        else {
            Map cardTransactionsMap = new HashMap();
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                ProcurementCardTransaction transaction = (ProcurementCardTransaction) iter.next();
                if (!cardTransactionsMap.containsKey(transaction.getTransactionCreditCardNumber())) {
                    cardTransactionsMap.put(transaction.getTransactionCreditCardNumber(), new ArrayList());
                }
                ((List) cardTransactionsMap.get(transaction.getTransactionCreditCardNumber())).add(transaction);
            }

            for (Iterator iter = cardTransactionsMap.values().iterator(); iter.hasNext();) {
                groupedTransactions.add(iter.next());

            }
        }

        return groupedTransactions;
    }


    /**
     * Creates a ProcurementCardDocument from the List of transactions given.
     * 
     * @param transactions List of ProcurementCardTransaction objects to be used for creating the document.
     * @return A ProcurementCardDocument populated with the transactions provided.
     */
    protected ProcurementCardDocument createProcurementCardDocument(List transactions) {
        ProcurementCardDocument pcardDocument = null;

        try {
            // get new document from doc service
            pcardDocument = (ProcurementCardDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PROCUREMENT_CARD);
            if (ObjectUtils.isNotNull(pcardDocument.getCapitalAssetInformation())) {
                pcardDocument.getCapitalAssetInformation().setDocumentNumber(pcardDocument.getDocumentNumber());
            }

            // set the card holder record on the document from the first transaction
            
            ProcurementCardTransaction trans = (ProcurementCardTransaction) transactions.get(0);
            String errors = validateTransaction(trans);
            createCardHolderRecord(pcardDocument, trans);

            // for each transaction, create transaction detail object and then acct lines for the detail
            int transactionLineNumber = 1;
            KualiDecimal documentTotalAmount = KualiDecimal.ZERO;
            String errorText = errors;
            for (Iterator iter = transactions.iterator(); iter.hasNext();) {
                ProcurementCardTransaction transaction = (ProcurementCardTransaction) iter.next();

                // create transaction detail record with accounting lines
                errorText += createTransactionDetailRecord(pcardDocument, transaction, transactionLineNumber);

                // update document total
                documentTotalAmount = documentTotalAmount.add(transaction.getFinancialDocumentTotalAmount());

                transactionLineNumber++;
            }
            
            pcardDocument.getDocumentHeader().setFinancialDocumentTotalAmount(documentTotalAmount);
            pcardDocument.getDocumentHeader().setDocumentDescription("SYSTEM Generated");

            // Remove duplicate messages from errorText
            String messages[] = StringUtils.split(errorText, ".");
            for (int i = 0; i < messages.length; i++) {
                int countMatches = StringUtils.countMatches(errorText, messages[i]) - 1;
                errorText = StringUtils.replace(errorText, messages[i] + ".", "", countMatches);
            }
            // In case errorText is still too long, truncate it and indicate so.
            Integer documentExplanationMaxLength = dataDictionaryService.getAttributeMaxLength(DocumentHeader.class.getName(), KFSPropertyConstants.EXPLANATION);
            if (documentExplanationMaxLength != null && errorText.length() > documentExplanationMaxLength.intValue()) {
                String truncatedMessage = " ... TRUNCATED.";
                errorText = errorText.substring(0, documentExplanationMaxLength - truncatedMessage.length()) + truncatedMessage;
            }
            pcardDocument.getDocumentHeader().setExplanation(errorText);
        }
        catch (WorkflowException e) {
            LOG.error("Error creating pcdo documents: " + e.getMessage(),e);
            throw new RuntimeException("Error creating pcdo documents: " + e.getMessage(),e);
        }

        return pcardDocument;
    }

    /**
     * Creates card holder record and sets that record to the document given.
     * 
     * @param pcardDocument Procurement card document to place the record in.
     * @param transaction The transaction to set the card holder record fields from.
     */
    protected void createCardHolderRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction) {
        ProcurementCardHolder cardHolder = new ProcurementCardHolder();

        cardHolder.setDocumentNumber(pcardDocument.getDocumentNumber());
        cardHolder.setAccountNumber(transaction.getAccountNumber());
        cardHolder.setCardCycleAmountLimit(transaction.getCardCycleAmountLimit());
        cardHolder.setCardCycleVolumeLimit(transaction.getCardCycleVolumeLimit());
        cardHolder.setCardHolderAlternateName(transaction.getCardHolderAlternateName());
        cardHolder.setCardHolderCityName(transaction.getCardHolderCityName());
        cardHolder.setCardHolderLine1Address(transaction.getCardHolderLine1Address());
        cardHolder.setCardHolderLine2Address(transaction.getCardHolderLine2Address());
        cardHolder.setCardHolderName(transaction.getCardHolderName());
        cardHolder.setCardHolderStateCode(transaction.getCardHolderStateCode());
        cardHolder.setCardHolderWorkPhoneNumber(transaction.getCardHolderWorkPhoneNumber());
        cardHolder.setCardHolderZipCode(transaction.getCardHolderZipCode());
//        KITI-2149 - The "Card Limit" field should not be displayed per the KFS Interface Specifications for pcard upload.
//        cardHolder.setCardLimit(transaction.getCardLimit());
        cardHolder.setCardNoteText(transaction.getCardNoteText());
        cardHolder.setCardStatusCode(transaction.getCardStatusCode());
        cardHolder.setChartOfAccountsCode(transaction.getChartOfAccountsCode());
        cardHolder.setSubAccountNumber(transaction.getSubAccountNumber());
        cardHolder.setTransactionCreditCardNumber(transaction.getTransactionCreditCardNumber());

        pcardDocument.setProcurementCardHolder(cardHolder);
    }

    /**
     * Creates a transaction detail record and adds that record to the document provided.
     * 
     * @param pcardDocument Document to place record in.
     * @param transaction Transaction to set fields from.
     * @param transactionLineNumber Line number of the new transaction detail record within the procurement card document.
     * @return The error text that was generated from the creation of the detail records.  If the text is empty, no errors were encountered.
     */
    protected String createTransactionDetailRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, Integer transactionLineNumber) {
        ProcurementCardTransactionDetail transactionDetail = new ProcurementCardTransactionDetail();

        // set the document transaction detail fields from the loaded transaction record
        transactionDetail.setDocumentNumber(pcardDocument.getDocumentNumber());
        transactionDetail.setFinancialDocumentTransactionLineNumber(transactionLineNumber);
        transactionDetail.setTransactionDate(transaction.getTransactionDate());
        transactionDetail.setTransactionReferenceNumber(transaction.getTransactionReferenceNumber());
        transactionDetail.setTransactionBillingCurrencyCode(transaction.getTransactionBillingCurrencyCode());
        transactionDetail.setTransactionCurrencyExchangeRate(transaction.getTransactionCurrencyExchangeRate());
        transactionDetail.setTransactionDate(transaction.getTransactionDate());
        transactionDetail.setTransactionOriginalCurrencyAmount(transaction.getTransactionOriginalCurrencyAmount());
        transactionDetail.setTransactionOriginalCurrencyCode(transaction.getTransactionOriginalCurrencyCode());
        transactionDetail.setTransactionPointOfSaleCode(transaction.getTransactionPointOfSaleCode());
        transactionDetail.setTransactionPostingDate(transaction.getTransactionPostingDate());
        transactionDetail.setTransactionPurchaseIdentifierDescription(transaction.getTransactionPurchaseIdentifierDescription());
        transactionDetail.setTransactionPurchaseIdentifierIndicator(transaction.getTransactionPurchaseIdentifierIndicator());
        transactionDetail.setTransactionSalesTaxAmount(transaction.getTransactionSalesTaxAmount());
        transactionDetail.setTransactionSettlementAmount(transaction.getTransactionSettlementAmount());
        transactionDetail.setTransactionTaxExemptIndicator(transaction.getTransactionTaxExemptIndicator());
        transactionDetail.setTransactionTravelAuthorizationCode(transaction.getTransactionTravelAuthorizationCode());
        transactionDetail.setTransactionUnitContactName(transaction.getTransactionUnitContactName());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount().negated());
        }
        else {
            transactionDetail.setTransactionTotalAmount(transaction.getFinancialDocumentTotalAmount());
        }

        // create transaction vendor record
        createTransactionVendorRecord(pcardDocument, transaction, transactionDetail);

        // add transaction detail to document
        pcardDocument.getTransactionEntries().add(transactionDetail);

        // now create the initial source and target lines for this transaction
        return createAndValidateAccountingLines(pcardDocument, transaction, transactionDetail);
    }


    /**
     * Creates a transaction vendor detail record and adds it to the transaction detail.
     * 
     * @param pcardDocument The procurement card document to retrieve values from.
     * @param transaction Transaction to set fields from.
     * @param transactionDetail The transaction detail to set the vendor record on.
     */
    protected void createTransactionVendorRecord(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, ProcurementCardTransactionDetail transactionDetail) {
        ProcurementCardVendor transactionVendor = new ProcurementCardVendor();

        transactionVendor.setDocumentNumber(pcardDocument.getDocumentNumber());
        transactionVendor.setFinancialDocumentTransactionLineNumber(transactionDetail.getFinancialDocumentTransactionLineNumber());
        transactionVendor.setTransactionMerchantCategoryCode(transaction.getTransactionMerchantCategoryCode());
        transactionVendor.setVendorCityName(transaction.getVendorCityName());
        transactionVendor.setVendorLine1Address(transaction.getVendorLine1Address());
        transactionVendor.setVendorLine2Address(transaction.getVendorLine2Address());
        transactionVendor.setVendorName(transaction.getVendorName());
        transactionVendor.setVendorOrderNumber(transaction.getVendorOrderNumber());
        transactionVendor.setVendorStateCode(transaction.getVendorStateCode());
        transactionVendor.setVendorZipCode(transaction.getVendorZipCode());
        transactionVendor.setVisaVendorIdentifier(transaction.getVisaVendorIdentifier());

        transactionDetail.setProcurementCardVendor(transactionVendor);
    }

    /**
     * From the transaction accounting attributes, creates source and target accounting lines. Attributes are validated first, and
     * replaced with default and error values if needed. There will be 1 source and 1 target line generated.
     * 
     * @param pcardDocument The procurement card document to add the new accounting lines to.
     * @param transaction The transaction to process into account lines.
     * @param docTransactionDetail The transaction detail to create source and target accounting lines from.
     * @return String containing any error messages.
     */
    protected String createAndValidateAccountingLines(ProcurementCardDocument pcardDocument, ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        // build source lines
        ProcurementCardSourceAccountingLine sourceLine = createSourceAccountingLine(transaction, docTransactionDetail);
        sourceLine.setPostingYear(pcardDocument.getPostingYear());

        // add line to transaction through document since document contains the next sequence number fields
        pcardDocument.addSourceAccountingLine(sourceLine);

        // build target lines
        ProcurementCardTargetAccountingLine targetLine = createTargetAccountingLine(transaction, docTransactionDetail);
        targetLine.setPostingYear(pcardDocument.getPostingYear());
        
        // add line to transaction through document since document contains the next sequence number fields
        pcardDocument.addTargetAccountingLine(targetLine);

        return validateTargetAccountingLine(targetLine);
    }

    /**
     * Creates the to record for the transaction. The chart of account attributes from the transaction are used to create 
     * the accounting line.
     * 
     * @param transaction The transaction to pull information from to create the accounting line.
     * @param docTransactionDetail The transaction detail to pull information from to populate the accounting line.
     * @return The target accounting line fully populated with values from the parameters passed in. 
     */
    protected ProcurementCardTargetAccountingLine createTargetAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        ProcurementCardTargetAccountingLine targetLine = new ProcurementCardTargetAccountingLine();

        targetLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        targetLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        targetLine.setChartOfAccountsCode(transaction.getChartOfAccountsCode());
        targetLine.setAccountNumber(transaction.getAccountNumber());
        targetLine.setFinancialObjectCode(transaction.getFinancialObjectCode());
        targetLine.setSubAccountNumber(transaction.getSubAccountNumber());
        targetLine.setFinancialSubObjectCode(transaction.getFinancialSubObjectCode());
        targetLine.setProjectCode(transaction.getProjectCode());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        }
        else {
            targetLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return targetLine;
    }

    /**
     * Creates the from record for the transaction. The clearing chart, account, and object code is used for creating the line.
     * 
     * @param transaction The transaction to pull information from to create the accounting line.
     * @param docTransactionDetail The transaction detail to pull information from to populate the accounting line.
     * @return The source accounting line fully populated with values from the parameters passed in.
     */
    protected ProcurementCardSourceAccountingLine createSourceAccountingLine(ProcurementCardTransaction transaction, ProcurementCardTransactionDetail docTransactionDetail) {
        ProcurementCardSourceAccountingLine sourceLine = new ProcurementCardSourceAccountingLine();

        sourceLine.setDocumentNumber(docTransactionDetail.getDocumentNumber());
        sourceLine.setFinancialDocumentTransactionLineNumber(docTransactionDetail.getFinancialDocumentTransactionLineNumber());
        sourceLine.setChartOfAccountsCode(getDefaultChartCode());
        sourceLine.setAccountNumber(getDefaultAccountNumber());
        sourceLine.setFinancialObjectCode(getDefaultObjectCode());

        if (GL_CREDIT_CODE.equals(transaction.getTransactionDebitCreditCode())) {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount().negated());
        }
        else {
            sourceLine.setAmount(transaction.getFinancialDocumentTotalAmount());
        }

        return sourceLine;
    }

    /**
     * Validates the chart of account attributes for existence and active indicator. Will substitute for defined 
     * default parameters or set fields to empty that if they have errors.
     * 
     * @param targetLine The target accounting line to be validated.
     * @return String with error messages discovered during validation.  An empty string indicates no validation errors were found.
     */
    protected String validateTargetAccountingLine(ProcurementCardTargetAccountingLine targetLine) {
        String errorText = "";

        targetLine.refresh();
        
        if (!accountingLineRuleUtil.isValidChart(targetLine.getChart(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " is invalid; using error Chart Code.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setChartOfAccountsCode(getErrorChartCode());
            targetLine.refresh();
        }   
        
        if (!accountingLineRuleUtil.isValidAccount(targetLine.getAccount(), dataDictionaryService.getDataDictionary()) || targetLine.getAccount().isExpired()) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Account " + targetLine.getAccountNumber() + " is invalid; using error account.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setChartOfAccountsCode(getErrorChartCode());
            targetLine.setAccountNumber(getErrorAccountNumber());
            targetLine.refresh();
        }

        if (!accountingLineRuleUtil.isValidObjectCode(targetLine.getObjectCode(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Object Code " + targetLine.getFinancialObjectCode() + " is invalid; using default Object Code.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setFinancialObjectCode(getErrorObjectCode());
            targetLine.refresh();
        }

        if (StringUtils.isNotBlank(targetLine.getSubAccountNumber()) && !accountingLineRuleUtil.isValidSubAccount(targetLine.getSubAccount(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Account " + targetLine.getAccountNumber() + " Sub Account " + targetLine.getSubAccountNumber() + " is invalid; Setting Sub Account to blank.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setSubAccountNumber("");
        }

        if (StringUtils.isNotBlank(targetLine.getFinancialSubObjectCode()) && !accountingLineRuleUtil.isValidSubObjectCode(targetLine.getSubObjectCode(), dataDictionaryService.getDataDictionary())) {
            String tempErrorText = "Chart " + targetLine.getChartOfAccountsCode() + " Account " + targetLine.getAccountNumber() + " Object Code " + targetLine.getFinancialObjectCode() + " Sub Object Code " + targetLine.getFinancialSubObjectCode() + " is invalid; setting Sub Object to blank.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            targetLine.setFinancialSubObjectCode("");
        }

        if (StringUtils.isNotBlank(targetLine.getProjectCode()) && !accountingLineRuleUtil.isValidProjectCode(targetLine.getProject(), dataDictionaryService.getDataDictionary())) {
            if ( LOG.isInfoEnabled() ) {
                LOG.info("Project Code " + targetLine.getProjectCode() + " is invalid; setting to blank.");
            }
            errorText += " Project Code " + targetLine.getProjectCode() + " is invalid; setting to blank.";

            targetLine.setProjectCode("");
        }

        // clear out GlobalVariable message map, since we have taken care of the errors
        GlobalVariables.setMessageMap(new MessageMap());

        return errorText;
    }

    
    /**
     * Validates the chart of account attributes for existence and active indicator. Will substitute for defined 
     * default parameters or set fields to empty that if they have errors.
     * 
     * @param targetLine The target accounting line to be validated.
     * @return String with error messages discovered during validation.  An empty string indicates no validation errors were found.
     */
    protected String validateTransaction(ProcurementCardTransaction transaction) {
        String errorText = "";

        transaction.refresh();
        ChartService cs = SpringContext.getBean(ChartService.class);
        Chart chart = cs.getByPrimaryId(transaction.getChartOfAccountsCode());
        if (ObjectUtils.isNull(chart)) {
            String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " is invalid; using error Chart Code.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            transaction.setChartOfAccountsCode(getErrorChartCode());
            transaction.refresh();
        }   
        AccountService accountService = SpringContext.getBean(AccountService.class);
        Account account = accountService.getByPrimaryIdWithCaching(transaction.getChartOfAccountsCode(), transaction.getAccountNumber());
        if (ObjectUtils.isNull(account) || account.isExpired()) {
            String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Account " + transaction.getAccountNumber() + " is invalid; using error account.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            transaction.setChartOfAccountsCode(getErrorChartCode());
            transaction.setAccountNumber(getErrorAccountNumber());
            transaction.refresh();
        }
        UniversityDateService uds = SpringContext.getBean(UniversityDateService.class);
        ObjectCodeService ocs = SpringContext.getBean(ObjectCodeService.class);
        ObjectCode objectCode = ocs.getByPrimaryIdWithCaching(uds.getCurrentFiscalYear(), transaction.getChartOfAccountsCode(), transaction.getFinancialObjectCode());
        
        if (ObjectUtils.isNull(objectCode)) {
            String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Object Code " + transaction.getFinancialObjectCode() + " is invalid; using default Object Code.";
            if ( LOG.isInfoEnabled() ) {
                LOG.info(tempErrorText);
            }
            errorText += " " + tempErrorText;

            transaction.setFinancialObjectCode(getDefaultObjectCode());
            transaction.refresh();
        }

        
        if (StringUtils.isNotBlank(transaction.getSubAccountNumber())) {
        	SubAccountService sas = SpringContext.getBean(SubAccountService.class);
        	SubAccount subAccount = sas.getByPrimaryIdWithCaching(transaction.getChartOfAccountsCode(), transaction.getAccountNumber(), transaction.getSubAccountNumber());
        
        
        	if (ObjectUtils.isNull(subAccount)) {
        		String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Account " + transaction.getAccountNumber() + " Sub Account " + transaction.getSubAccountNumber() + " is invalid; Setting Sub Account to blank.";
        		if ( LOG.isInfoEnabled() ) {
        			LOG.info(tempErrorText);
        		}
        		errorText += " " + tempErrorText;

        		transaction.setSubAccountNumber("");
        	}
        }

        
        if (StringUtils.isNotBlank(transaction.getFinancialSubObjectCode())) {

        	SubObjectCodeService socs = SpringContext.getBean(SubObjectCodeService.class);
        	SubObjectCode soc = socs.getByPrimaryIdForCurrentYear(transaction.getChartOfAccountsCode(), transaction.getAccountNumber(), transaction.getFinancialObjectCode(), transaction.getFinancialSubObjectCode());
        
        	if (ObjectUtils.isNull(soc)) {
        		String tempErrorText = "Chart " + transaction.getChartOfAccountsCode() + " Account " + transaction.getAccountNumber() + " Object Code " + transaction.getFinancialObjectCode() + " Sub Object Code " + transaction.getFinancialSubObjectCode() + " is invalid; setting Sub Object to blank.";
        		if ( LOG.isInfoEnabled() ) {
        			LOG.info(tempErrorText);
        		}
        		errorText += " " + tempErrorText;

        		transaction.setFinancialSubObjectCode("");
        	}
        }
        
        
        if (StringUtils.isNotBlank(transaction.getProjectCode())) {
        
        	ProjectCodeService pcs = SpringContext.getBean(ProjectCodeService.class);
        	ProjectCode pc = pcs.getByPrimaryId(transaction.getProjectCode());
        
        	if (ObjectUtils.isNull(pc)) {
        		if ( LOG.isInfoEnabled() ) {
        			LOG.info("Project Code " + transaction.getProjectCode() + " is invalid; setting to blank.");
        		}
        		errorText += " Project Code " + transaction.getProjectCode() + " is invalid; setting to blank.";

        		transaction.setProjectCode("");
        	}
        }

        // clear out GlobalVariable message map, since we have taken care of the errors
        GlobalVariables.setMessageMap(new MessageMap());

        return errorText;
    }
    
    
    /**
     * Retrieves the error chart code from the parameter table.
     * @return The error chart code defined in the parameter table.
     */
    protected String getErrorChartCode() {
        return parameterService.getParameterValue(ProcurementCardCreateDocumentsStep.class, ProcurementCardDocumentRuleConstants.ERROR_TRANS_CHART_CODE_PARM_NM);
    }

    /**
     * Retrieves the error account number from the parameter table.
     * @return The error account number defined in the parameter table.
     */
    protected String getErrorAccountNumber() {
        return parameterService.getParameterValue(ProcurementCardCreateDocumentsStep.class, ERROR_TRANS_ACCOUNT_PARM_NM);
    }

    /**
     * Retrieves the error object code from the parameter table.
     * @return The error object code defined in the parameter table.
     */
    protected String getErrorObjectCode() {
        return parameterService.getParameterValue(ProcurementCardCreateDocumentsStep.class, ERROR_TRANS_OBJECT_CODE_PARM_NM);
    }

    /**
     * Retrieves the default chard code from the parameter table.
     * @return The default chart code defined in the parameter table.
     */
    protected String getDefaultChartCode() {
        return parameterService.getParameterValue(ProcurementCardLoadStep.class, DEFAULT_TRANS_CHART_CODE_PARM_NM);
    }

    /**
     * Retrieves the default account number from the parameter table.
     * @return The default account number defined in the parameter table.
     */
    protected String getDefaultAccountNumber() {
        return parameterService.getParameterValue(ProcurementCardLoadStep.class, DEFAULT_TRANS_ACCOUNT_PARM_NM);
    }

    /**
     * Retrieves the default object code from the parameter table.
     * @return The default object code defined in the parameter table.
     */
    protected String getDefaultObjectCode() {
        return parameterService.getParameterValue(ProcurementCardLoadStep.class, DEFAULT_TRANS_OBJECT_CODE_PARM_NM);
    }

    /**
     * Calls businessObjectService to remove all the procurement card transaction rows from the transaction load table.
     */
    protected void cleanTransactionsTable() {
        businessObjectService.deleteMatching(ProcurementCardTransaction.class, new HashMap());
    }

    /**
     * Loads all the parsed XML transactions into the temp transaction table.
     * 
     * @param transactions List of ProcurementCardTransactions to load.
     */
    protected void loadTransactions(List transactions) {
        businessObjectService.save(transactions);
    }

    /**
     * Sets the parameterService attribute.
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Gets the businessObjectService attribute.
     * @return Returns the businessObjectService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService attribute.
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Gets the documentService attribute.
     * @return Returns the documentService.
     */
    public FinancialSystemDocumentService getDocumentService() {
        return documentService;
    }

    /**
     * Sets the documentService attribute.
     * @param documentService The documentService to set.
     */
    public void setDocumentService(FinancialSystemDocumentService documentService) {
        this.documentService = documentService;
    }


    /**
     * Gets the dataDictionaryService attribute.
     * @return Returns the dataDictionaryService.
     */
    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    /**
     * Sets the dataDictionaryService attribute.
     * @param dataDictionaryService dataDictionaryService to set.
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }


    /**
     * Gets the dateTimeService attribute.
     * @return Returns the dateTimeService.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute.
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Gets the workflowDocumentService attribute.
     * @return Returns the workflowDocumentService.
     */
    public WorkflowDocumentService getWorkflowDocumentService() {
        return workflowDocumentService;
    }

    /**
     * Sets the workflowDocumentService attribute value.
     * @param workflowDocumentService The workflowDocumentService to set.
     */
    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

    /**
     * Sets the accountingLineRuleUtil attribute value.
     * @param accountingLineRuleUtil The accountingLineRuleUtil to set.
     */
    public void setAccountingLineRuleUtil(AccountingLineRuleHelperService accountingLineRuleUtil) {
        this.accountingLineRuleUtil = accountingLineRuleUtil;
    }

    /**
     * Sets the capitalAssetBuilderModuleService attribute value.
     * @param capitalAssetBuilderModuleService The capitalAssetBuilderModuleService to set.
     */
    public void setCapitalAssetBuilderModuleService(CapitalAssetBuilderModuleService capitalAssetBuilderModuleService) {
        this.capitalAssetBuilderModuleService = capitalAssetBuilderModuleService;
    }

}