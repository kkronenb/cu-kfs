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
package org.kuali.kfs.module.purap.document.validation.impl;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherWireTransfer;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.PREQDocumentsStrings;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestWireTransfer;
import org.kuali.kfs.module.purap.document.AccountsPayableDocument;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PaymentRequestService;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.validation.event.AttributedExpiredAccountWarningEvent;
import org.kuali.kfs.module.purap.document.validation.event.AttributedTradeInWarningEvent;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.KualiRuleService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.web.format.CurrencyFormatter;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;

/**
 * Business pre rule(s) applicable to Payment Request documents.
 */
public class PaymentRequestDocumentPreRules extends AccountsPayableDocumentPreRulesBase {

    /**
     * Default Constructor
     */
    public PaymentRequestDocumentPreRules() {
        super();
    }

    /**
     * Main hook point to perform rules check.
     * 
     * @see org.kuali.rice.kns.rules.PromptBeforeValidationBase#doRules(org.kuali.rice.kns.document.Document)
     */
    @Override
    public boolean doPrompts(Document document) {
        boolean preRulesOK = true;

        PaymentRequestDocument preq = (PaymentRequestDocument) document;
        if ((!SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(preq)) || (StringUtils.equals(preq.getStatusCode(), PurapConstants.PaymentRequestStatuses.AWAITING_ACCOUNTS_PAYABLE_REVIEW))) {
            if (!confirmPayDayNotOverThresholdDaysAway(preq)) {
                return false;
            }
            if (!confirmUnusedTradeIn(preq)) {
                return false;
            }
            if (!confirmEncumberNextFiscalYear(preq)) {
                return false;
            }
            
            if (!confirmEncumberPriorFiscalYear(preq)) {
                return false;
            }
        }
        if (SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(preq)) {
            if (!confirmExpiredAccount(preq)) {
                return false;
            }
        }
        
        preRulesOK &= checkWireTransferTabState(preq);
       
        
        preRulesOK &= super.doPrompts(document);
        return preRulesOK;
    }

    /**
     * Prompts user to confirm with a Yes or No to a question being asked.
     * 
     * @param questionType - type of question
     * @param messageConstant - key to retrieve message
     * @return - true if overriding, false otherwise
     */
    protected boolean askForConfirmation(String questionType, String messageConstant) {

        String questionText = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageConstant);
        if (questionText.contains("{")) {
            questionText = prepareQuestionText(questionType, questionText);
        }
        else if (StringUtils.equals(messageConstant, KFSKeyConstants.ERROR_ACCOUNT_EXPIRED) || StringUtils.equals(messageConstant,PurapKeyConstants.WARNING_ITEM_TRADE_IN_AMOUNT_UNUSED)) {
            questionText = questionType;
        }
        
        
        boolean confirmOverride = super.askOrAnalyzeYesNoQuestion(questionType, questionText);

        if (!confirmOverride) {
            event.setActionForwardName(KFSConstants.MAPPING_BASIC);
            return false;
        }
        return true;
    }

    /**
     * Creates the actual text of the question, replacing place holders like pay date threshold with an actual constant value.
     * 
     * @param questionType - type of question
     * @param questionText - actual text of question pulled from resource file
     * @return - question text with place holders replaced
     */
    protected String prepareQuestionText(String questionType, String questionText) {
        if (StringUtils.equals(questionType, PREQDocumentsStrings.THRESHOLD_DAYS_OVERRIDE_QUESTION)) {
            questionText = StringUtils.replace(questionText, "{0}", new Integer(PurapConstants.PREQ_PAY_DATE_DAYS_BEFORE_WARNING).toString());
        }
        return questionText;
    }

    /**
     * Validates if the pay date threshold has not been passed, if so confirmation is required by the user to
     * exceed the threshold.
     * 
     * @param preq - payment request document
     * @return - true if threshold has not been surpassed or if user confirmed ok to override, false otherwise
     */
    public boolean confirmPayDayNotOverThresholdDaysAway(PaymentRequestDocument preq) {
        
        // If the pay date is more than the threshold number of days in the future, ask for confirmation.                
        //boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedPayDateNotOverThresholdDaysAwayEvent("", preq));
        
        //if (!rulePassed) {
        // The problem is that rulePassed always return true
        int thresholdDays = PurapConstants.PREQ_PAY_DATE_DAYS_BEFORE_WARNING;
        if ((preq.getPaymentRequestPayDate() != null) && SpringContext.getBean(PurapService.class).isDateMoreThanANumberOfDaysAway(preq.getPaymentRequestPayDate(), thresholdDays)) {
            return askForConfirmation(PREQDocumentsStrings.THRESHOLD_DAYS_OVERRIDE_QUESTION, PurapKeyConstants.MESSAGE_PAYMENT_REQUEST_PAYDATE_OVER_THRESHOLD_DAYS);
        }
        return true;
    }

    public boolean confirmUnusedTradeIn(PaymentRequestDocument preq) {
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedTradeInWarningEvent("", preq));
        
        if (!rulePassed) {
            return askForConfirmation(PREQDocumentsStrings.UNUSED_TRADE_IN_QUESTION, PurapKeyConstants.WARNING_ITEM_TRADE_IN_AMOUNT_UNUSED);
        }
        return true;
    }
    
    public boolean confirmExpiredAccount(PaymentRequestDocument preq) {
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AttributedExpiredAccountWarningEvent("", preq));
        
        if (!rulePassed) {
            return askForConfirmation(PREQDocumentsStrings.EXPIRED_ACCOUNT_QUESTION, KFSKeyConstants.ERROR_ACCOUNT_EXPIRED);
        }
        return true;
    }
    
    public boolean confirmEncumberNextFiscalYear(PaymentRequestDocument preq) {
        Integer fiscalYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        if (preq.getPurchaseOrderDocument().getPostingYear().intValue() > fiscalYear) {
            return askForConfirmation(PREQDocumentsStrings.ENCUMBER_NEXT_FISCAL_YEAR_QUESTION, PurapKeyConstants.WARNING_ENCUMBER_NEXT_FY);
        }
       
        return true;
    }
    
    public boolean confirmEncumberPriorFiscalYear(PaymentRequestDocument preq) {
        
        Integer fiscalYear = SpringContext.getBean(UniversityDateService.class).getCurrentFiscalYear();
        if (preq.getPurchaseOrderDocument().getPostingYear().intValue() == fiscalYear && SpringContext.getBean(PaymentRequestService.class).allowBackpost(preq)) {
            return askForConfirmation(PREQDocumentsStrings.ENCUMBER_PRIOR_FISCAL_YEAR_QUESTION, PurapKeyConstants.WARNING_ENCUMBER_PRIOR_FY);
        }
        return true;
    }
    
    /**
     * @see org.kuali.kfs.module.purap.document.validation.impl.AccountsPayableDocumentPreRulesBase#getDocumentName()
     */
    @Override
    public String getDocumentName() {
        return "Payment Request";
    }

    /**
     * @see org.kuali.kfs.module.purap.document.validation.impl.AccountsPayableDocumentPreRulesBase#createInvoiceNoMatchQuestionText(org.kuali.kfs.module.purap.document.AccountsPayableDocument)
     */
    @Override
    public String createInvoiceNoMatchQuestionText(AccountsPayableDocument accountsPayableDocument){
                
        String questionText = super.createInvoiceNoMatchQuestionText(accountsPayableDocument);
        
        CurrencyFormatter cf = new CurrencyFormatter();
        PaymentRequestDocument preq = (PaymentRequestDocument) accountsPayableDocument;        
        
        StringBuffer questionTextBuffer = new StringBuffer("");        
        questionTextBuffer.append(questionText);
//        questionTextBuffer.append( "<style type=\"text/css\"> table.questionTable {border-collapse: collapse;} td.leftTd { border-bottom:1px solid #000000; border-right:1px solid #000000; padding:3px; width:300px; } td.rightTd { border-bottom:1px solid #000000; border-left:1px solid #000000; padding:3px; width:300px; } </style>" );
//                    
//        questionTextBuffer.append("<br/><br/>Summary Detail Below:<br/><br/><table class=\"questionTable\" align=\"center\">");
//        questionTextBuffer.append("<tr><td class=\"leftTd\">Vendor Invoice Amount entered on start screen:</td><td class=\"rightTd\">" + (String)cf.format(preq.getInitialAmount()) + "</td></tr>");
//        questionTextBuffer.append("<tr><td class=\"leftTd\">Invoice Total Prior to Additional Charges:</td><td class=\"rightTd\">" + (String)cf.format(preq.getTotalPreTaxDollarAmountAboveLineItems()) + "</td></tr>");
        
        questionTextBuffer.append("[br][br][b]Summary Detail Below[/b][br][br]");
        questionTextBuffer.append("Vendor Invoice Amount entered on start screen: ").append((String)cf.format(preq.getInitialAmount())).append("[br]");
        questionTextBuffer.append("Invoice Total Prior to Additional Charges: ").append((String)cf.format(preq.getTotalPreTaxDollarAmountAboveLineItems())).append("[br]");

        //only add this line if payment request has a discount
        if( preq.isDiscount() ){
            //questionTextBuffer.append("<tr><td class=\"leftTd\">Total Before Discount:</td><td class=\"rightTd\">" + (String)cf.format(preq.getGrandPreTaxTotalExcludingDiscount()) + "</td></tr>");
            questionTextBuffer.append("Total Before Discount: ").append((String)cf.format(preq.getGrandPreTaxTotalExcludingDiscount())).append("[br]");
        }
        
        //if sales tax is enabled, show additional summary lines
        boolean salesTaxInd = SpringContext.getBean(ParameterService.class).getIndicatorParameter(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_SALES_TAX_IND);                
        if(salesTaxInd){
//            questionTextBuffer.append("<tr><td class=\"leftTd\">Grand Total Prior to Tax:</td><td class=\"rightTd\">" + (String)cf.format(preq.getGrandPreTaxTotal()) + "</td></tr>");
//            questionTextBuffer.append("<tr><td class=\"leftTd\">Grand Total Tax:</td><td class=\"rightTd\">" + (String)cf.format(preq.getGrandTaxAmount()) + "</td></tr>");
            questionTextBuffer.append("Grand Total Prior to Tax: ").append((String)cf.format(preq.getGrandPreTaxTotal())).append("[br]");
            questionTextBuffer.append("Grand Total Tax: ").append((String)cf.format(preq.getGrandTaxAmount())).append("[br]");
        }
        
//        questionTextBuffer.append("<tr><td class=\"leftTd\">Grand Total:</td><td class=\"rightTd\">" + (String)cf.format(preq.getGrandTotal()) + "</td></tr></table>");
        questionTextBuffer.append("Grand Total: ").append((String)cf.format(preq.getGrandTotal())).append("[br][br]");
                        
        return questionTextBuffer.toString();
        
    }
    @Override
    protected boolean checkCAMSWarningStatus(PurchasingAccountsPayableDocument purapDocument) {
        return PurapConstants.CAMSWarningStatuses.PAYMENT_REQUEST_STATUS_WARNING_NO_CAMS_DATA.contains(purapDocument.getStatusCode());
    }

    protected boolean checkWireTransferTabState(PaymentRequestDocument preqDocument) {
        boolean tabStatesOK = true;

        PaymentRequestWireTransfer preqWireTransfer = preqDocument.getPreqWireTransfer();

        // if payment method is CHECK and wire tab contains data, ask user to clear tab
        if (!StringUtils.equals(PaymentMethod.PM_CODE_WIRE, preqDocument.getPaymentMethodCode()) && hasWireTransferValues(preqWireTransfer)) {
            String questionText = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(KFSKeyConstants.QUESTION_CLEAR_UNNEEDED_WIRW_TAB);

            Object[] args = { "payment method", preqDocument.getPaymentMethodCode(), "Wire Transfer", PaymentMethod.PM_CODE_WIRE };
            questionText = MessageFormat.format(questionText, args);

            boolean clearTab = super.askOrAnalyzeYesNoQuestion(KFSConstants.DisbursementVoucherDocumentConstants.CLEAR_WIRE_TRANSFER_TAB_QUESTION_ID, questionText);
            if (clearTab) {
                // NOTE: Can't replace with new instance because Foreign Draft uses same object
                clearWireTransferValues(preqWireTransfer);
            }
            else {
                // return to document if the user doesn't want to clear the Wire Transfer tab
                super.event.setActionForwardName(KFSConstants.MAPPING_BASIC);
                tabStatesOK = false;
            }
        }

        return tabStatesOK;
    }

    protected boolean hasWireTransferValues(PaymentRequestWireTransfer preqWireTransfer) {
        boolean hasValues = false;

        // Checks each explicit field in the tab for user entered values
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqAutomatedClearingHouseProfileNumber());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankName());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankRoutingNumber());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankCityName());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankStateCode());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqBankCountryCode());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqPayeeAccountNumber());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqAttentionLineText());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqCurrencyTypeName());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqAdditionalWireText());
        hasValues |= StringUtils.isNotBlank(preqWireTransfer.getPreqPayeeAccountName());

        return hasValues;
    }
    protected void clearWireTransferValues(PaymentRequestWireTransfer preqWireTransfer) {
        preqWireTransfer.setPreqAutomatedClearingHouseProfileNumber(null);
        preqWireTransfer.setPreqBankName(null);
        preqWireTransfer.setPreqBankRoutingNumber(null);
        preqWireTransfer.setPreqBankCityName(null);
        preqWireTransfer.setPreqBankStateCode(null);
        preqWireTransfer.setPreqBankCountryCode(null);
        preqWireTransfer.setPreqPayeeAccountNumber(null);
        preqWireTransfer.setPreqAttentionLineText(null);
        preqWireTransfer.setPreqCurrencyTypeName(null);
        preqWireTransfer.setPreqAdditionalWireText(null);
        preqWireTransfer.setPreqPayeeAccountName(null);
    }
}