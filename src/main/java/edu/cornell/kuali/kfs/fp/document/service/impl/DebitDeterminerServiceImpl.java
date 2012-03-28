package edu.cornell.kfs.fp.document.service.impl;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.NonCheckDisbursementDocument;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.rice.kns.util.KualiDecimal;

public class DebitDeterminerServiceImpl extends	org.kuali.kfs.sys.document.service.impl.DebitDeterminerServiceImpl {

	/**
	 * Override to check for negatives on DVs
	 * 
	 * @see org.kuali.kfs.sys.document.service.impl.DebitDeterminerServiceImpl#isDebitConsideringNothingPositiveOnly(org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource, org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail)
	 */
	@Override
	public boolean isDebitConsideringNothingPositiveOnly(	GeneralLedgerPendingEntrySource poster,	GeneralLedgerPendingEntrySourceDetail postable) {
        KualiDecimal amount = postable.getAmount();
        if (amount.isNegative() && poster instanceof DisbursementVoucherDocument) {
        	return false;
        }

        if (amount.isNegative() && poster instanceof NonCheckDisbursementDocument) {
        	return false;
        }
		
		return super.isDebitConsideringNothingPositiveOnly(poster, postable);
	}

}