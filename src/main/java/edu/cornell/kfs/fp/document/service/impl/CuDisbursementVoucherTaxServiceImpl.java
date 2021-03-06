package edu.cornell.kfs.fp.document.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherTaxServiceImpl;
import org.kuali.kfs.fp.document.validation.impl.DisbursementVoucherNonResidentAlienInformationValidation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

import edu.cornell.kfs.fp.document.validation.impl.CuDisbursementVoucherNonResidentAlienInformationValidation;

public class CuDisbursementVoucherTaxServiceImpl extends DisbursementVoucherTaxServiceImpl{

    public void processNonResidentAlienTax(DisbursementVoucherDocument document) {
        if (validateNRATaxInformation(document)) {
            generateNRATaxLines(document);
        }
    }
    
    @Override
    protected boolean validateNRATaxInformation(DisbursementVoucherDocument document) {
        MessageMap errors = GlobalVariables.getMessageMap();

        CuDisbursementVoucherNonResidentAlienInformationValidation dvNRA = new CuDisbursementVoucherNonResidentAlienInformationValidation();
        dvNRA.setAccountingDocumentForValidation(document);
        dvNRA.setValidationType("GENERATE");
                    
        if(!dvNRA.validate(null)) {
            return false;
        }

        if (GlobalVariables.getMessageMap().hasErrors()) {
            return false;
        }

        /* make sure vendor is nra */
        if (!document.getDvPayeeDetail().isDisbVchrAlienPaymentCode()) {
            errors.putErrorWithoutFullErrorPath("DVNRATaxErrors", KFSKeyConstants.ERROR_DV_GENERATE_TAX_NOT_NRA);
            return false;
        }

        /* don't generate tax if reference doc is given */
        if (StringUtils.isNotBlank(document.getDvNonResidentAlienTax().getReferenceFinancialDocumentNumber())) {
            errors.putErrorWithoutFullErrorPath("DVNRATaxErrors", KFSKeyConstants.ERROR_DV_GENERATE_TAX_DOC_REFERENCE);
            return false;
        }

        // check attributes needed to generate lines
        /* need at least 1 line */
        if (!(document.getSourceAccountingLines().size() >= 1)) {
            errors.putErrorWithoutFullErrorPath("DVNRATaxErrors", KFSKeyConstants.ERROR_DV_GENERATE_TAX_NO_SOURCE);
            return false;
        }

        /* make sure both fed and state tax percents are not 0, in which case there is no need to generate lines */
        if (KualiDecimal.ZERO.equals(document.getDvNonResidentAlienTax().getFederalIncomeTaxPercent()) && KualiDecimal.ZERO.equals(document.getDvNonResidentAlienTax().getStateIncomeTaxPercent())) {
            errors.putErrorWithoutFullErrorPath("DVNRATaxErrors", KFSKeyConstants.ERROR_DV_GENERATE_TAX_BOTH_0);
            return false;
        }
        
        /* check total cannot be negative */
        if (KualiDecimal.ZERO.compareTo(document.getDisbVchrCheckTotalAmount()) == 1) {
            errors.putErrorWithoutFullErrorPath("document.disbVchrCheckTotalAmount", KFSKeyConstants.ERROR_NEGATIVE_OR_ZERO_CHECK_TOTAL);
            return false;
        }

        /* total accounting lines cannot be negative */
        if (KualiDecimal.ZERO.compareTo(document.getSourceTotal()) == 1) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.ACCOUNTING_LINE_ERRORS, KFSKeyConstants.ERROR_NEGATIVE_ACCOUNTING_TOTAL);
            return false;
        }

        /* total of accounting lines must match check total */
        if (document.getDisbVchrCheckTotalAmount().compareTo(document.getSourceTotal()) != 0) {
            errors.putErrorWithoutFullErrorPath(KFSConstants.ACCOUNTING_LINE_ERRORS, KFSKeyConstants.ERROR_CHECK_ACCOUNTING_TOTAL);
            return false;
        }
        return true;
    }
}
