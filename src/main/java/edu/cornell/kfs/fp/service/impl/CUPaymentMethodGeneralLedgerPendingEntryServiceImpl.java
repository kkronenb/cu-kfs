/*
 * Copyright 2010 The Kuali Foundation.
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
package edu.cornell.kfs.fp.service.impl;

import static org.kuali.kfs.sys.KFSConstants.GL_CREDIT_CODE;
import static org.kuali.kfs.sys.KFSConstants.GL_DEBIT_CODE;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.document.service.AccountingDocumentRuleHelperService;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.GENERAL_LEDGER_PENDING_ENTRY_CODE;
import org.kuali.kfs.sys.service.BankService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.spring.CacheNoCopy;

import edu.cornell.kfs.fp.businessobject.PaymentMethod;
import edu.cornell.kfs.fp.businessobject.PaymentMethodChart;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;

@NonTransactional
public class CUPaymentMethodGeneralLedgerPendingEntryServiceImpl implements CUPaymentMethodGeneralLedgerPendingEntryService {
    private static Logger LOG = Logger.getLogger(CUPaymentMethodGeneralLedgerPendingEntryServiceImpl.class);
    protected static final String DEFAULT_PAYMENT_METHOD_IF_MISSING = "A"; // check/ACH

    // not sure why these are not injected ?
    private GeneralLedgerPendingEntryService generalLedgerPendingEntryService;
    private ObjectCodeService objectCodeService;
    private ParameterService parameterService;
    private BusinessObjectService businessObjectService;
    private BankService bankService;
    

    @CacheNoCopy
    public boolean isPaymentMethodProcessedUsingPdp(String paymentMethodCode) {
        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        if ( pm != null ) {
            return pm.isProcessedUsingPdp();
        }
        return false;
    }
    
    /**
     * This implementation will also return null if the bank code on the payment method record does not exist in the bank table.
     * 
     */
    public Bank getBankForPaymentMethod(String paymentMethodCode) {
        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        if ( pm != null ) {
            // if no bank code, short circuit and return null
            if ( pm.getBankCode() != null ) {
                return pm.getBank();
            }
        }
        return null;
    }
    
    /**
     * Generates additional document-level GL entries for the DV, depending on the payment method code. 
     * 
     * Return true if GLPE's are generated successfully (i.e. there are either 0 GLPE's or 1 GLPE in disbursement voucher document)
     * 
     * @param financialDocument submitted financial document
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * @return true if GLPE's are generated successfully
     */
    public boolean generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(
            AccountingDocument document, String paymentMethodCode, String bankCode, String bankCodePropertyName, 
            GeneralLedgerPendingEntry templatePendingEntry, 
            boolean feesWaived, boolean reverseCharge, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(document, paymentMethodCode, bankCode, bankCodePropertyName, templatePendingEntry, feesWaived, reverseCharge, sequenceHelper, null, null);
    }

    public boolean generatePaymentMethodSpecificDocumentGeneralLedgerPendingEntries(AccountingDocument document, 
            String paymentMethodCode, 
            String bankCode, 
            String bankCodePropertyName, 
            GeneralLedgerPendingEntry templatePendingEntry, 
            boolean feesWaived, 
            boolean reverseCharge, 
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper, 
            KualiDecimal bankOffsetAmount, 
            Map<String, KualiDecimal> actualTotalsByChart) {

        if ( StringUtils.isBlank(paymentMethodCode) ) {
            paymentMethodCode = DEFAULT_PAYMENT_METHOD_IF_MISSING;
        }
        PaymentMethod pm = getBusinessObjectService().findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCode);
        // no payment method? abort.
        if ( pm == null ) {
            return false;
        }
        
        if ( pm.isAssessedFees() ) {
            if ( !feesWaived ) {
                generateFeeAssessmentEntries(pm, document, templatePendingEntry, sequenceHelper, reverseCharge);
            }                        
        }
        
        if ( pm.isOffsetUsingClearingAccount() ) {
            generateClearingAccountOffsetEntries(pm, document, sequenceHelper, actualTotalsByChart);
        }
        
        if ( !pm.isProcessedUsingPdp() && StringUtils.isNotBlank( bankCode ) ) {
            generateDocumentBankOffsetEntries(document,bankCode,bankCodePropertyName,templatePendingEntry.getFinancialDocumentTypeCode(), sequenceHelper, bankOffsetAmount );
        }
        
        return true;
    }
    
    /**
     * Generates the GL entries to charge the department for the foreign draft and credit the Wire Charge
     * Fee Account as specified by system parameters.
     * 
     * @param document Document into which to add the generated GL Entries.
     * 
     */
    protected boolean generateFeeAssessmentEntries(PaymentMethod pm, AccountingDocument document, GeneralLedgerPendingEntry templatePendingEntry, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, boolean reverseEntries) {
        LOG.debug("generateForeignDraftChargeEntries started");
        
        PaymentMethodChart pmc = pm.getPaymentMethodChartInfo(templatePendingEntry.getChartOfAccountsCode(), new java.sql.Date( document.getDocumentHeader().getWorkflowDocument().getCreateDate().getTime() ) );
        if ( pmc == null ) {
            LOG.warn( "No Applicable PaymentMethodChart found for chart: " + templatePendingEntry.getChartOfAccountsCode() + " and date: " + document.getDocumentHeader().getWorkflowDocument().getCreateDate() );
            return false;
        }
        // Get all the parameters which control these entries
        String feeIncomeChartCode = pmc.getFeeIncomeChartOfAccountsCode();
        String feeIncomeAccountNumber = pmc.getFeeIncomeAccountNumber();
        String feeExpenseObjectCode = pmc.getFeeExpenseFinancialObjectCode();
        String feeIncomeObjectCode = pmc.getFeeIncomeFinancialObjectCode();
        KualiDecimal feeAmount = pmc.getFeeAmount();

        // skip creation if the fee has been set to zero
        if ( !KualiDecimal.ZERO.equals(feeAmount) ) {
            // grab the explicit entry for the first accounting line and adjust for the foreign draft fee
            GeneralLedgerPendingEntry chargeEntry = new GeneralLedgerPendingEntry(document.getGeneralLedgerPendingEntry(0));        
            chargeEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            
            // change the object code (expense to the department)
            chargeEntry.setFinancialObjectCode(feeExpenseObjectCode);
            chargeEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
            chargeEntry.setTransactionLedgerEntryDescription( StringUtils.left( "Automatic debit for " + pm.getPaymentMethodName() + " fee", 40 ));
            chargeEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
    
            // retrieve object type
            ObjectCode objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(chargeEntry.getChartOfAccountsCode(), chargeEntry.getFinancialObjectCode());
            if ( objectCode == null ) {
                LOG.fatal("Specified offset object code: " + chargeEntry.getChartOfAccountsCode() + "-" + chargeEntry.getFinancialObjectCode() + " does not exist - failed to generate foreign draft fee entries", new RuntimeException() );
                return false;
            }       
            chargeEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
            
            // Set the amount from the parameter
            chargeEntry.setTransactionLedgerEntryAmount(feeAmount);
            chargeEntry.setTransactionDebitCreditCode(reverseEntries?GL_CREDIT_CODE:GL_DEBIT_CODE);
    
            document.addPendingEntry(chargeEntry);
            sequenceHelper.increment();
    
            // handle the offset entry
            GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(chargeEntry);
            getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), chargeEntry, sequenceHelper, offsetEntry);
    
            document.addPendingEntry(offsetEntry);
            sequenceHelper.increment();
            
            // Now, create the income entry in the AP Foreign draft fee account
            
            GeneralLedgerPendingEntry feeIncomeEntry = new GeneralLedgerPendingEntry(document.getGeneralLedgerPendingEntry(0));
            feeIncomeEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
    
            feeIncomeEntry.setChartOfAccountsCode(feeIncomeChartCode);
            feeIncomeEntry.setAccountNumber(feeIncomeAccountNumber);
            feeIncomeEntry.setFinancialObjectCode(feeIncomeObjectCode);
            feeIncomeEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
            feeIncomeEntry.setSubAccountNumber(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber());
            feeIncomeEntry.setProjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode());
    
            // retrieve object type
            objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(feeIncomeChartCode, feeIncomeObjectCode);
            if ( objectCode == null ) {
                LOG.fatal("Specified income object code: " + feeIncomeChartCode + "-" + feeIncomeObjectCode + " does not exist - failed to generate foreign draft income entries", new RuntimeException() );
                return false;
            }
            feeIncomeEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());       
            feeIncomeEntry.setTransactionLedgerEntryAmount(feeAmount);
            feeIncomeEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            feeIncomeEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
    
            document.addPendingEntry(feeIncomeEntry);
            sequenceHelper.increment();
            
            // create the offset entry
            offsetEntry = new GeneralLedgerPendingEntry(feeIncomeEntry);
            getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), feeIncomeEntry, sequenceHelper, offsetEntry);
    
            document.addPendingEntry(offsetEntry);
            sequenceHelper.increment();
        }
        return true;
    }
    
    /**
     * Adds up the amounts of all cash to offset GeneralLedgerPendingEntry records on the given AccountingDocument
     * 
     * MOD-PA2000-01 : Copied from the GL Pending entry service since that one does not make any distinction between
     * expense and encumbrance balance types
     * 
     * @author jonathan
     * 
     * @param glPostingDocument the accounting document total the offset to cash amount for
     * @return the offset to cash amount, where debited values have been subtracted and credited values have been added
     */
    protected Map<String,KualiDecimal> getNonOffsetActualTotalsByChart(GeneralLedgerPostingDocument glPostingDocument) {
        Map<String,KualiDecimal> totals = new HashMap<String, KualiDecimal>();
        for (GeneralLedgerPendingEntry glpe : glPostingDocument.getGeneralLedgerPendingEntries()) {
            if ( KFSConstants.BALANCE_TYPE_ACTUAL.equals(glpe.getFinancialBalanceTypeCode()) ) {
                if ( !glpe.isTransactionEntryOffsetIndicator() ) {
                    if ( !totals.containsKey(glpe.getChartOfAccountsCode() ) ) {
                        totals.put(glpe.getChartOfAccountsCode(), KualiDecimal.ZERO);
                    }
                    if (glpe.getTransactionDebitCreditCode().equals(KFSConstants.GL_DEBIT_CODE)) {
                        totals.put(glpe.getChartOfAccountsCode(),totals.get(glpe.getChartOfAccountsCode()).add(glpe.getTransactionLedgerEntryAmount()));
                    } else if (glpe.getTransactionDebitCreditCode().equals(KFSConstants.GL_CREDIT_CODE)) {
                        totals.put(glpe.getChartOfAccountsCode(),totals.get(glpe.getChartOfAccountsCode()).subtract(glpe.getTransactionLedgerEntryAmount()));
                    }
                }
            }
        }
        return totals;
    }    

    /**
     * When the "A" payment method is used for AP Credit Cards - generate the needed entries in the clearing account.
     * 
     * @param document Document into which to add the generated GL Entries.
     * @param sequenceHelper helper class to keep track of GLPE sequence
     * 
     */
    public boolean generateClearingAccountOffsetEntries(PaymentMethod pm, AccountingDocument document, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, Map<String,KualiDecimal> actualTotalsByChart) {
        if ( actualTotalsByChart == null ) {
            actualTotalsByChart = getNonOffsetActualTotalsByChart(document);
        }

        for ( String chart : actualTotalsByChart.keySet() ) {
            KualiDecimal offsetAmount = actualTotalsByChart.get(chart);
            if ( !KualiDecimal.ZERO.equals(offsetAmount) ) {
                PaymentMethodChart pmc = pm.getPaymentMethodChartInfo(chart, new java.sql.Date( document.getDocumentHeader().getWorkflowDocument().getCreateDate().getTime() ) );
                if ( pmc == null ) {
                    LOG.warn( "No Applicable PaymentMethodChart found for chart: " + chart + " and date: " + document.getDocumentHeader().getWorkflowDocument().getCreateDate() );
                    // skip this line - still attempt for other charts
                    continue;
                }
                String clearingChartCode = pmc.getClearingChartOfAccountsCode();
                String clearingAccountNumber = pmc.getClearingAccountNumber();
                String clearingObjectCode = pmc.getClearingFinancialObjectCode(); // liability object code
                
                GeneralLedgerPendingEntry apOffsetEntry = new GeneralLedgerPendingEntry(document.getGeneralLedgerPendingEntry(0));
                apOffsetEntry.setTransactionLedgerEntrySequenceNumber(new Integer(sequenceHelper.getSequenceCounter()));
    
                apOffsetEntry.setChartOfAccountsCode(clearingChartCode);
                apOffsetEntry.setAccountNumber(clearingAccountNumber);
                apOffsetEntry.setFinancialObjectCode(clearingObjectCode);
                // if internal billing
                if (StringUtils.equals(PaymentMethod.PM_CODE_INTERNAL_BILLING, pm.getPaymentMethodCode())) {
                    apOffsetEntry.setFinancialSubObjectCode(pmc.getClearingFinancialSubObjectCode());
                    apOffsetEntry.setSubAccountNumber(pmc.getClearingSubAccountNumber());
                } else {
                apOffsetEntry.setFinancialSubObjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankFinancialSubObjectCode());
                apOffsetEntry.setSubAccountNumber(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankSubAccountNumber());
                }
                apOffsetEntry.setProjectCode(GENERAL_LEDGER_PENDING_ENTRY_CODE.getBlankProjectCode());
    
                // retrieve object type
                ObjectCode objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(clearingChartCode, clearingObjectCode);
                if ( objectCode == null ) {
                    LOG.fatal("Specified offset object code: " + clearingChartCode + "-" + clearingObjectCode + " does not exist - failed to generate CC offset entries", new RuntimeException() );
                    return false;
                }
                apOffsetEntry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());       
                apOffsetEntry.setTransactionLedgerEntryAmount(offsetAmount.abs());
                apOffsetEntry.setTransactionDebitCreditCode(offsetAmount.isNegative()?KFSConstants.GL_DEBIT_CODE:KFSConstants.GL_CREDIT_CODE);
                apOffsetEntry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
    
                document.addPendingEntry(apOffsetEntry);
                sequenceHelper.increment();
                
                // handle the offset entry
                GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(apOffsetEntry);
                getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), apOffsetEntry, sequenceHelper, offsetEntry);
    
                document.addPendingEntry(offsetEntry);
                sequenceHelper.increment();
            }
        }
        
        return true;
    }

    /**
     * If bank specification is enabled generates bank offsetting entries for the document amount
     * 
     */
    public boolean generateDocumentBankOffsetEntries(AccountingDocument document, String bankCode, String bankCodePropertyName, String documentTypeCode, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, KualiDecimal bankOffsetAmount ) {
        boolean success = true;

        if (!getBankService().isBankSpecificationEnabled()) {
            return success;
        }
        Bank bank = getBankService().getByPrimaryId(bankCode);

        if ( bankOffsetAmount == null ) {
            bankOffsetAmount = getGeneralLedgerPendingEntryService().getOffsetToCashAmount(document).negated();
        }
        if ( !KualiDecimal.ZERO.equals(bankOffsetAmount) ) {
            GeneralLedgerPendingEntry bankOffsetEntry = new GeneralLedgerPendingEntry();
            success &= getGeneralLedgerPendingEntryService()
                    .populateBankOffsetGeneralLedgerPendingEntry(bank, bankOffsetAmount, document, 
                            document.getPostingYear(), sequenceHelper, bankOffsetEntry, bankCodePropertyName);
    
            if (success) {
                AccountingDocumentRuleHelperService accountingDocumentRuleUtil = SpringContext.getBean(AccountingDocumentRuleHelperService.class);
                bankOffsetEntry.setTransactionLedgerEntryDescription(accountingDocumentRuleUtil.formatProperty(KFSKeyConstants.Bank.DESCRIPTION_GLPE_BANK_OFFSET));
                bankOffsetEntry.setFinancialDocumentTypeCode(documentTypeCode);
                document.addPendingEntry(bankOffsetEntry);
                sequenceHelper.increment();
    
                GeneralLedgerPendingEntry offsetEntry = new GeneralLedgerPendingEntry(bankOffsetEntry);
                success &= getGeneralLedgerPendingEntryService().populateOffsetGeneralLedgerPendingEntry(document.getPostingYear(), bankOffsetEntry, sequenceHelper, offsetEntry);
                bankOffsetEntry.setFinancialDocumentTypeCode(documentTypeCode);
                document.addPendingEntry(offsetEntry);
                sequenceHelper.increment();
            }
        }

        return success;
    }
    
    protected GeneralLedgerPendingEntryService getGeneralLedgerPendingEntryService() {
        if ( generalLedgerPendingEntryService == null ) {
            generalLedgerPendingEntryService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        }
        return generalLedgerPendingEntryService;
    }
    
    protected ObjectCodeService getObjectCodeService() {
        if ( objectCodeService == null ) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }
        return objectCodeService;
    }
    
    protected ParameterService getParameterService() {
        if ( parameterService == null ) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if ( businessObjectService == null ) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }
    
    protected BankService getBankService() {
        if ( bankService == null ) {
            bankService = SpringContext.getBean(BankService.class);
        }
        return bankService;
    }

}