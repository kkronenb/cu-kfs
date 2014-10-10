package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;
import org.kuali.rice.krad.service.BusinessObjectService;

public class AccountGlobalExtendedAttribute  extends PersistableBusinessObjectExtensionBase {

    // TODO : Should consider refactoring to create a super class for AccountExtendedAttribute & AccountGlobalExtendedAttribute 
    private static final long serialVersionUID = 1L;
    private String documentNumber;
    private String programCode;
    private String subFundGroupCode;
    private String appropriationAccountNumber;
    private SubFundProgram subFundProgram;
    private AppropriationAccount appropriationAccount;

    
    public AccountGlobalExtendedAttribute() {
        
    }
    
    /**
     * @return the subFundProgram
     */
    public SubFundProgram getSubFundProgram() {
        return subFundProgram;
    }


    /**
     * @param subFundProgram the subFundProgram to set
     */
    public void setSubFundProgram(SubFundProgram subFundProgram) {
        this.subFundProgram = subFundProgram;
    }


    /**
     * @return the appropriationAccount
     */
    public AppropriationAccount getAppropriationAccount() {
        return appropriationAccount;
    }


    /**
     * @param appropriationAccount the appropriationAccount to set
     */
    public void setAppropriationAccount(AppropriationAccount appropriationAccount) {
        this.appropriationAccount = appropriationAccount;
    }

    /**
     * @return the programCode
     */
    public String getProgramCode() {
        return programCode;
    }


    /**
     * @param programCode the programCode to set
     */
    public void setProgramCode(String programCode) {
        this.programCode = programCode;
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        HashMap<String,String> keys = new HashMap<String,String>();
        keys.put("programCode", programCode);
        keys.put("subFundGroupCode", subFundGroupCode);
        subFundProgram = (SubFundProgram) bos.findByPrimaryKey(SubFundProgram.class, keys );
    }


    /**
     * @return the appropriationAccountNumber
     */
    public String getAppropriationAccountNumber() {
        return appropriationAccountNumber;
    }


    /**
     * @param appropriationAccountNumber the appropriationAccountNumber to set
     */
    public void setAppropriationAccountNumber(String appropriationAccountNumber) {
        this.appropriationAccountNumber = appropriationAccountNumber;
        BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
        HashMap<String,String> keys = new HashMap<String,String>();
        keys.put("appropriationAccountNumber", appropriationAccountNumber);
        keys.put("subFundGroupCode", subFundGroupCode);
        appropriationAccount = (AppropriationAccount) bos.findByPrimaryKey(AppropriationAccount.class, keys );
    }
    /**
     * @return the subFundGroupCode
     */
    public String getSubFundGroupCode() {
    
        return subFundGroupCode;
    }
    /**
     * @param subFundGroupCode the subFundGroupCode to set
     */
    public void setSubFundGroupCode(String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
        
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}
