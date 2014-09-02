package edu.cornell.kfs.coa.service;

import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.rice.krad.maintenance.MaintenanceLock;

public interface AccountReversionTrickleDownInactivationService {
    
    public List<MaintenanceLock> generateTrickleDownMaintenanceLocks(Account inactivatedAccount, String documentNumber);
    
    public void trickleDownInactivateAccountReversions(Account inactivatedAccount, String documentNumber); 

}
