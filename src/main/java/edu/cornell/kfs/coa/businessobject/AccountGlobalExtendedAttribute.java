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
    private String appropriationAccountNumber;

    
    public AccountGlobalExtendedAttribute() {
        
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
    }
 
    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}
