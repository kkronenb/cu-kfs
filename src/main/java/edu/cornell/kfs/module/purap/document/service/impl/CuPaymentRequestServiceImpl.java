package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapConstants.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.businessobject.AutoApproveExclude;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.impl.PaymentRequestServiceImpl;
import org.kuali.kfs.module.purap.util.VendorGroupingHelper;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.kfs.vnd.businessobject.PaymentTermType;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.module.purap.document.dataaccess.CuPaymentRequestDao;
import edu.cornell.kfs.sys.service.CUBankService;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuPaymentRequestServiceImpl extends PaymentRequestServiceImpl {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuPaymentRequestServiceImpl.class);
    
    private CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService;


    @Override
    @NonTransactional
    public void removeIneligibleAdditionalCharges(PaymentRequestDocument document) {

        List<PaymentRequestItem> itemsToRemove = new ArrayList<PaymentRequestItem>();

        for (PaymentRequestItem item : (List<PaymentRequestItem>) document.getItems()) {

        	// KFSUPGRADE-473
            //if no extended price or purchase order item unit price, and its an order discount or trade in, remove
            if ((ObjectUtils.isNull(item.getPurchaseOrderItemUnitPrice()) && ObjectUtils.isNull(item.getExtendedPrice())) &&
                    (ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE.equals(item.getItemTypeCode()) || ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE.equals(item.getItemTypeCode())) ){            
                itemsToRemove.add(item);
                continue;
            }

            // if a payment terms discount exists but not set on teh doc, remove
            if (StringUtils.equals(item.getItemTypeCode(), PurapConstants.ItemTypeCodes.ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE)) {
                PaymentTermType pt = document.getVendorPaymentTerms();
                if ((pt != null) && (pt.getVendorPaymentTermsPercent() != null) && (BigDecimal.ZERO.compareTo(pt.getVendorPaymentTermsPercent()) != 0)) {
                    // discount ok
                }
                else {
                    // remove discount
                    itemsToRemove.add(item);
                }
                continue;
            }

        }

        // remove items marked for removal
        for (PaymentRequestItem item : itemsToRemove) {
            document.getItems().remove(item);
        }
    }

    @Override
    @NonTransactional
    public PaymentRequestDocument addHoldOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setHoldIndicator(true);
        document.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

        return document;
    }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#removeHoldOnPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    @Override
    @NonTransactional
    public PaymentRequestDocument removeHoldOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setHoldIndicator(false);
        document.setLastActionPerformedByPersonId(null);
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

        return document;
    }

    @Override
    @NonTransactional
    public void requestCancelOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        document.setPaymentRequestedCancelIndicator(true);
        document.setLastActionPerformedByPersonId(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        document.setAccountsPayableRequestCancelIdentifier(GlobalVariables.getUserSession().getPerson().getPrincipalId());
        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);
    }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#removeHoldOnPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument)
     */
    @Override
    @NonTransactional
    public void removeRequestCancelOnPaymentRequest(PaymentRequestDocument document, String note) throws Exception {
        // save the note
        Note noteObj = documentService.createNoteFromDocument(document, note);
        document.addNote(noteObj);
        noteService.save(noteObj);

        clearRequestCancelFields(document);

        purapService.saveDocumentNoValidation(document);
        //force reindexing
        reIndexDocument(document);

    }

    @Override
    @NonTransactional
    public void cancelExtractedPaymentRequest(PaymentRequestDocument paymentRequest, String note) {
        LOG.debug("cancelExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("cancelExtractedPaymentRequest() ended");
            return;
        }

        try {
            Note cancelNote = documentService.createNoteFromDocument(paymentRequest, note);
            paymentRequest.addNote(cancelNote);
            noteService.save(cancelNote);
        }
        catch (Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE, e);
        }

        // cancel extracted should not reopen PO
        paymentRequest.setReopenPurchaseOrderIndicator(false);

        getAccountsPayableService().cancelAccountsPayableDocument(paymentRequest, ""); // Performs save, so
        // no explicit save
        // is necessary
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelExtractedPaymentRequest() PREQ " + paymentRequest.getPurapDocumentIdentifier() + " Cancelled Without Workflow");
            LOG.debug("cancelExtractedPaymentRequest() ended");
        }
        //force reindexing
        reIndexDocument(paymentRequest);
   }

    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#resetExtractedPaymentRequest(org.kuali.kfs.module.purap.document.PaymentRequestDocument,
     *      java.lang.String)
     */
    @Override
    @NonTransactional
    public void resetExtractedPaymentRequest(PaymentRequestDocument paymentRequest, String note) {
        LOG.debug("resetExtractedPaymentRequest() started");
        if (PaymentRequestStatuses.CANCELLED_STATUSES.contains(paymentRequest.getApplicationDocumentStatus())) {
            LOG.debug("resetExtractedPaymentRequest() ended");
            return;
        }
        paymentRequest.setExtractedTimestamp(null);
        paymentRequest.setPaymentPaidTimestamp(null);
        String noteText = "This Payment Request is being reset for extraction by PDP " + note;
        try {
            Note resetNote = documentService.createNoteFromDocument(paymentRequest, noteText);
            paymentRequest.addNote(resetNote);
            noteService.save(resetNote);
        }
        catch (Exception e) {
            throw new RuntimeException(PurapConstants.REQ_UNABLE_TO_CREATE_NOTE + " " + e);
        }
        purapService.saveDocumentNoValidation(paymentRequest);
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetExtractedPaymentRequest() PREQ " + paymentRequest.getPurapDocumentIdentifier() + " Reset from Extracted status");
        }
        //force reindexing
        reIndexDocument(paymentRequest);
    }

    @Override
    @NonTransactional
    public void markPaid(PaymentRequestDocument pr, Date processDate) {
        LOG.debug("markPaid() started");

        pr.setPaymentPaidTimestamp(new Timestamp(processDate.getTime()));
        purapService.saveDocumentNoValidation(pr);
        //force reindexing
        reIndexDocument(pr);
   }

   /**
    * KFSUPGRADE-508 : this is happened in multi-node env. also see KFSUPGRADE_347
     * This method is being added to handle calls to perform re-indexing of documents following change events performed on the documents.  This is necessary to correct problems
     * with searches not returning accurate results due to changes being made to documents, but those changes not be indexed.
     * 
     * @param document - The document to be re-indexed.
     */
    private void reIndexDocument(AccountsPayableDocument document) {
        //force reindexing
         final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();

        documentAttributeIndexingQueue.indexDocument(document.getDocumentNumber());

    }
    
    @Override
    protected PurApItem addTaxItem(PaymentRequestDocument preq, String itemTypeCode, BigDecimal taxableAmount) {

    	PurApItem item = super.addTaxItem(preq, itemTypeCode, taxableAmount);
    	
    	PurApAccountingLine taxLine = item.getSourceAccountingLines().get(0);
    	// KFSPTS-1891.  added to fix validation required field error. especially after calculate tax
    	if (taxLine.getAccountLinePercent() == null) {
    		taxLine.setAccountLinePercent(BigDecimal.ZERO);
    	}
    	
    	return item;
    }
    
    @Override
    @NonTransactional
    public void populatePaymentRequest(PaymentRequestDocument paymentRequestDocument) {
    	super.populatePaymentRequest(paymentRequestDocument);
    	
    	//KFSUPGRADE-779
    	//reset bank code
    	 paymentRequestDocument.setBankCode(null);
         paymentRequestDocument.setBank(null);
         
        // set bank code to default bank code in the system parameter
        Bank defaultBank = null;
        if (StringUtils.equals(PaymentMethod.PM_CODE_WIRE, ((CuPaymentRequestDocument)paymentRequestDocument).getPaymentMethodCode()) || StringUtils.equals(PaymentMethod.PM_CODE_FOREIGN_DRAFT, ((CuPaymentRequestDocument)paymentRequestDocument).getPaymentMethodCode())) {
        	defaultBank = SpringContext.getBean(CUBankService.class).getDefaultBankByDocType(CuPaymentRequestDocument.DOCUMENT_TYPE_NON_CHECK);
        } else if (!StringUtils.equals(PaymentMethod.PM_CODE_INTERNAL_BILLING, ((CuPaymentRequestDocument)paymentRequestDocument).getPaymentMethodCode())) {
            defaultBank = SpringContext.getBean(BankService.class).getDefaultBankByDocType(PaymentRequestDocument.class);
        }
        if (defaultBank != null) {
            paymentRequestDocument.setBankCode(defaultBank.getBankCode());
            paymentRequestDocument.setBank(defaultBank);
        }
    }
    
    @Override
    @NonTransactional
    public void changeVendor(PaymentRequestDocument preq, Integer headerId, Integer detailId) {
    	super.changeVendor(preq, headerId, detailId);
    	// KFSPTS-1891
        if ( preq instanceof PaymentRequestDocument ) {
            VendorDetail vdDetail = vendorService.getVendorDetail(headerId, detailId);
            if (vdDetail != null
                    && ObjectUtils.isNotNull(vdDetail.getExtension()) ) {
                if ( vdDetail.getExtension() instanceof VendorDetailExtension
                        && StringUtils.isNotBlank( ((VendorDetailExtension)vdDetail.getExtension()).getDefaultB2BPaymentMethodCode() ) ) {
                    ((CuPaymentRequestDocument)preq).setPaymentMethodCode(
                            ((VendorDetailExtension)vdDetail.getExtension()).getDefaultB2BPaymentMethodCode() );
                }
            }
        }
    }
    
    protected CUPaymentMethodGeneralLedgerPendingEntryService getPaymentMethodGeneralLedgerPendingEntryService() {
        return paymentMethodGeneralLedgerPendingEntryService;
    }

    public void setPaymentMethodGeneralLedgerPendingEntryService(CUPaymentMethodGeneralLedgerPendingEntryService paymentMethodGeneralLedgerPendingEntryService) {
        this.paymentMethodGeneralLedgerPendingEntryService = paymentMethodGeneralLedgerPendingEntryService;
    }

    public void clearTax(PaymentRequestDocument document) {
        // remove all existing tax items added by previous calculation
        removeTaxItems(document);
        // reset values
        document.setTaxClassificationCode(null);
        document.setTaxFederalPercent(null);
        document.setTaxStatePercent(null);
        document.setTaxCountryCode(null);
        document.setTaxNQIId(null);

        document.setTaxForeignSourceIndicator(false);
        document.setTaxExemptTreatyIndicator(false);
        document.setTaxOtherExemptIndicator(false);
        document.setTaxGrossUpIndicator(false);
        document.setTaxUSAIDPerDiemIndicator(false);
        document.setTaxSpecialW4Amount(null);

    }
 
    /**
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#getPaymentRequestsByStatusAndPurchaseOrderId(java.lang.String, java.lang.Integer)
     */
    @Override
    public Map <String, String> getPaymentRequestsByStatusAndPurchaseOrderId(String applicationDocumentStatus, Integer purchaseOrderId) {
    	Map <String, String> paymentRequestResults = new HashMap<String, String>();
    	paymentRequestResults.put("hasInProcess", "N");
    	paymentRequestResults.put("checkInProcess", "N");

    	final int numInProcess = ((CuPaymentRequestDao) paymentRequestDao).countDocumentsByPurchaseOrderId(purchaseOrderId, applicationDocumentStatus);
    	final int numTotal = ((CuPaymentRequestDao)paymentRequestDao).countDocumentsByPurchaseOrderId(purchaseOrderId, "");

    	if (numInProcess > 0) {
    	    paymentRequestResults.put("hasInProcess", "Y");
    	}

    	if (numTotal > 0 && numTotal != numInProcess) {
    	    paymentRequestResults.put("checkInProcess", "Y");
    	}
    	
    	return paymentRequestResults;
    }
    
    /**
     * Overridden to use the Net Due Number for the relevant calculations if the Discount Due Number is zero.
     * 
     * @see org.kuali.kfs.module.purap.document.service.PaymentRequestService#calculatePayDate(java.sql.Date,
     *      org.kuali.kfs.vnd.businessobject.PaymentTermType)
     */
    @Override
    @NonTransactional
    public java.sql.Date calculatePayDate(Date invoiceDate, PaymentTermType terms) {
        LOG.debug("calculatePayDate() started");
        // calculate the invoice + processed calendar
        Calendar invoicedDateCalendar = dateTimeService.getCalendar(invoiceDate);
        Calendar processedDateCalendar = dateTimeService.getCurrentCalendar();

        // add default number of days to processed
        String defaultDays = parameterService.getParameterValueAsString(PaymentRequestDocument.class, PurapParameterConstants.PURAP_PREQ_PAY_DATE_DEFAULT_NUMBER_OF_DAYS);
        processedDateCalendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(defaultDays));

        if (ObjectUtils.isNull(terms) || StringUtils.isEmpty(terms.getVendorPaymentTermsCode())) {
            invoicedDateCalendar.add(Calendar.DAY_OF_MONTH, PurapConstants.PREQ_PAY_DATE_EMPTY_TERMS_DEFAULT_DAYS);
            return returnLaterDate(invoicedDateCalendar, processedDateCalendar);
        }

        // Retrieve pay date variation parameter (currently defined as 2).  See parameter description for explanation of it's use.
        String payDateVariance = parameterService.getParameterValueAsString(PaymentRequestDocument.class, PurapParameterConstants.PURAP_PREQ_PAY_DATE_VARIANCE);
        Integer payDateVarianceInt = Integer.valueOf(payDateVariance);

        Integer discountDueNumber = terms.getVendorDiscountDueNumber();
        Integer netDueNumber = terms.getVendorNetDueNumber();
        // ==== CU Customization: If Discount Due Number is zero, use the Net Due Number instead. ====
        if (ObjectUtils.isNotNull(discountDueNumber) && !discountDueNumber.equals(Integer.valueOf(0))) {
            // Decrease discount due number by the pay date variance
            discountDueNumber -= payDateVarianceInt;
            if (discountDueNumber < 0) {
                discountDueNumber = 0;
            }
            String discountDueTypeDescription = terms.getVendorDiscountDueTypeDescription();
            paymentTermsDateCalculation(discountDueTypeDescription, invoicedDateCalendar, discountDueNumber);
        }
        else if (ObjectUtils.isNotNull(netDueNumber)) {
            // Decrease net due number by the pay date variance
            netDueNumber -= payDateVarianceInt;
            if (netDueNumber < 0) {
                netDueNumber = 0;
            }
            String netDueTypeDescription = terms.getVendorNetDueTypeDescription();
            paymentTermsDateCalculation(netDueTypeDescription, invoicedDateCalendar, netDueNumber);
        }
        else {
            throw new RuntimeException("Neither discount or net number were specified for this payment terms type");
        }

        // return the later date
        return returnLaterDate(invoicedDateCalendar, processedDateCalendar);
    }

}
