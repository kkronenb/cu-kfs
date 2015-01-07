package edu.cornell.kfs.fp.document.web.struts;

import java.sql.Date;
import java.util.Calendar;

import org.kuali.kfs.fp.document.web.struts.AuxiliaryVoucherForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.krad.util.ObjectUtils;

public class CuAuxiliaryVoucherForm extends AuxiliaryVoucherForm {
	
	@Override
	protected Date getAvReversalDate() {
		 Date documentReveralDate = getAuxiliaryVoucherDocument().getReversalDate();      
	        if (ObjectUtils.isNotNull(documentReveralDate)) {
	            return documentReveralDate;
	        }
	        
	        java.sql.Date avReversalDate = getAuxiliaryVoucherDocument().getAccountingPeriod().getUniversityFiscalPeriodEndDate();

	        Calendar cal = Calendar.getInstance();
	        cal.setTimeInMillis(avReversalDate.getTime());
	        
	        int thisMonth;
	        
	        if (getAuxiliaryVoucherDocument().getAccountingPeriod().getUniversityFiscalPeriodCode().equals(KFSConstants.MONTH13)) {
	            thisMonth = cal.JULY;
	        } else
	            thisMonth = getAuxiliaryVoucherDocument().getAccountingPeriod().getMonth();

	        
	        cal.set(Calendar.MONTH, (thisMonth));
	        
	        //if today's day > 15 then set the month to next month.
	     //   if (cal.get(Calendar.DAY_OF_MONTH) > KFSConstants.AuxiliaryVoucher.ACCRUAL_DOC_DAY_OF_MONTH) {
	      //      cal.add(Calendar.MONTH, 1);
	      //  }
	        
	        int reversalDateDefaultDayOfMonth = this.getReversalDateDefaultDayOfMonth();
	        
	        cal.set(Calendar.DAY_OF_MONTH, reversalDateDefaultDayOfMonth);

	        avReversalDate.setTime(cal.getTimeInMillis());
	        
	        return avReversalDate;
	}

}
