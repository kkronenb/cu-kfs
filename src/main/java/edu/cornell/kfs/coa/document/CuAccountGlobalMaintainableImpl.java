package edu.cornell.kfs.coa.document;

import java.util.HashMap;

import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.document.AccountGlobalMaintainableImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.coa.businessobject.AccountGlobalExtendedAttribute;
import edu.cornell.kfs.coa.businessobject.AppropriationAccount;
import edu.cornell.kfs.coa.businessobject.SubFundProgram;

public class CuAccountGlobalMaintainableImpl extends AccountGlobalMaintainableImpl {

    private static final long serialVersionUID = 1L;

    @Override
    public void prepareForSave() {   
        AccountGlobal accountGlobal = (AccountGlobal) getBusinessObject();
        AccountGlobalExtendedAttribute accountGlobalExtension = (AccountGlobalExtendedAttribute) (accountGlobal.getExtension());
        accountGlobalExtension.setDocumentNumber(accountGlobal.getDocumentNumber());
        accountGlobalExtension.setSubFundGroupCode(accountGlobal.getSubFundGroupCode());

        super.prepareForSave();
    }

}
