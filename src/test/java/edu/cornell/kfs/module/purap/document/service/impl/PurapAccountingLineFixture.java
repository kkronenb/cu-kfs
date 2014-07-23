package edu.cornell.kfs.module.purap.document.service.impl;

import java.math.BigDecimal;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.RequisitionAccount;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public enum PurapAccountingLineFixture {
	
	REQ_ITEM_ACCT_LINE(new BigDecimal(100), "1000710", "IT", "6100", new KualiDecimal(1));
	
	public final BigDecimal accountLinePercent;
	public final String accountNumber;
	public final String chartOfAccountsCode;
	public final String financialObjectCode;
	public final KualiDecimal amount;
	
	private PurapAccountingLineFixture(BigDecimal accountLinePercent, String accountNumber, String chartOfAccountsCode, String financialObjectCode, KualiDecimal amount) {
		this.accountLinePercent = accountLinePercent;
		this.accountNumber = accountNumber;
		this.chartOfAccountsCode = chartOfAccountsCode;
		this.financialObjectCode = financialObjectCode;
		this.amount = amount;
	}
	
	public PurApAccountingLine createRequisitionAccount(){
		PurApAccountingLine purapAcctLine = new RequisitionAccount();
		purapAcctLine.setAccountLinePercent(accountLinePercent);
		purapAcctLine.setAccountNumber(accountNumber);
		purapAcctLine.setChartOfAccountsCode(chartOfAccountsCode);
		purapAcctLine.setFinancialObjectCode(financialObjectCode);
		purapAcctLine.setAmount(amount);
		
		return purapAcctLine;
	}

}
