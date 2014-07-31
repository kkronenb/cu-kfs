/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import edu.cornell.kfs.sys.businessobject.YearEndPersistableBusinessObjectExtensionBase;

/**
 * @author kco26
 *
 */
public class SubObjectCodeExtendedAttribute extends YearEndPersistableBusinessObjectExtensionBase {

    /**
     * Primary Keys!
     */
	private String chartOfAccountsCode;
	private String accountNumber;
	private String financialObjectCode;
	private String financialSubObjectCode;
	
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Sub Object Code Description
	 */
    private String subObjectCodeDescr;
        
    
    /*
     * Class constructor
     */
    public SubObjectCodeExtendedAttribute() {
    	
    }
    
	/**
	 * @return the subObjectCodeDescr
	 */
	public String getSubObjectCodeDescr() {
		return subObjectCodeDescr;
	}
	
	/**
	 * @param subObjectCodeDescr the subObjectCodeDescr to set
	 */
	public void setSubObjectCodeDescr(String subObjectCodeDescr) {
		this.subObjectCodeDescr = subObjectCodeDescr;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * @return the financialObjectCode
	 */
	public String getFinancialObjectCode() {
		return financialObjectCode;
	}

	/**
	 * @param financialObjectCode the financialObjectCode to set
	 */
	public void setFinancialObjectCode(String financialObjectCode) {
		this.financialObjectCode = financialObjectCode;
	}

	/**
	 * @return the chartOfAccountsCode
	 */
	public String getChartOfAccountsCode() {
		return chartOfAccountsCode;
	}

	/**
	 * @param chartOfAccountsCode the chartOfAccountsCode to set
	 */
	public void setChartOfAccountsCode(String chartOfAccountsCode) {
		this.chartOfAccountsCode = chartOfAccountsCode;
	}

	/**
	 * @return the financialSubObjectCode
	 */
	public String getFinancialSubObjectCode() {
		return financialSubObjectCode;
	}

	/**
	 * @param financialSubObjectCode the financialSubObjectCode to set
	 */
	public void setFinancialSubObjectCode(String financialSubObjectCode) {
		this.financialSubObjectCode = financialSubObjectCode;
	}
		
}
