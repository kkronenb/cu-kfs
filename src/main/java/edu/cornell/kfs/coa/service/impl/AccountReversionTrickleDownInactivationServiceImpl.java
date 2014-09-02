package edu.cornell.kfs.coa.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.service.impl.SubAccountTrickleDownInactivationServiceImpl;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.service.MaintenanceDocumentDictionaryService;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.bo.PersistableBusinessObject;
import org.kuali.rice.krad.dao.MaintenanceDocumentDao;
import org.kuali.rice.krad.maintenance.MaintenanceLock;
import org.kuali.rice.krad.service.DocumentHeaderService;
import org.kuali.rice.krad.service.NoteService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.dataaccess.AccountReversionDao;
import edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

@Transactional
public class AccountReversionTrickleDownInactivationServiceImpl implements AccountReversionTrickleDownInactivationService {
    
    private static final Logger LOG = Logger.getLogger(AccountReversionTrickleDownInactivationServiceImpl.class);

    protected AccountReversionDao accountReversionDao;
    protected MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService;
    protected MaintenanceDocumentDao maintenanceDocumentDao;
    protected NoteService noteService;
    protected ConfigurationService kualiConfigurationService;
    protected DocumentHeaderService documentHeaderService;
    protected UniversityDateService universityDateService;
    
    /**
     * Will generate Maintenance Locks for all (active or not) sub-accounts in the system related to the inactivated account using the sub-account
     * maintainable registered for the sub-account maintenance document
     * 
     * This version of the method assumes that the sub-account maintainable only requires that the SubAccount BOClass, document number, and SubAccount
     * instance only needs to be passed into it
     * @see org.kuali.kfs.gl.service.SubAccountTrickleDownInactivationService#generateTrickleDownMaintenanceLocks(org.kuali.kfs.coa.businessobject.Account, java.lang.String)
     */
    public List<MaintenanceLock> generateTrickleDownMaintenanceLocks(Account inactivatedAccount, String documentNumber) {
        List<MaintenanceLock> maintenanceLocks = new ArrayList<MaintenanceLock>();
        
        Maintainable accountReversionMaintainable;
        try {
            accountReversionMaintainable = (Maintainable) maintenanceDocumentDictionaryService.getMaintainableClass(AccountReversion.class.getName()).newInstance();
            accountReversionMaintainable.setBoClass(AccountReversion.class);
            accountReversionMaintainable.setDocumentNumber(documentNumber);
        }
        catch (Exception e) {
            LOG.error("Unable to instantiate Account Reversion Maintainable" , e);
            throw new RuntimeException("Unable to instantiate Account Reversion Maintainable" , e);
        }
        
        Integer universityFiscalYear = universityDateService.getCurrentFiscalYear() - 1;
        List<AccountReversion> accountReversionRules = new ArrayList<AccountReversion>();
        AccountReversion accountReversionRule = accountReversionDao.getByPrimaryId(universityFiscalYear, inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        if(ObjectUtils.isNotNull(accountReversionRule)){
            accountReversionRules.add(accountReversionRule);
        }
        
        List<AccountReversion> cashAccountReversionRules = accountReversionDao.getByCashReversionAcount(universityFiscalYear, inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(cashAccountReversionRules) && cashAccountReversionRules.size() > 0){
            accountReversionRules.addAll(cashAccountReversionRules);
        }
        
        List<AccountReversion> budgetAccountReversionRules = accountReversionDao.getByBudgetReversionAcount(universityFiscalYear, inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(budgetAccountReversionRules) && budgetAccountReversionRules.size() > 0){
            accountReversionRules.addAll(budgetAccountReversionRules);
        }
        
        if (ObjectUtils.isNotNull(accountReversionRules) && !accountReversionRules.isEmpty()) {
            for (AccountReversion accountReversion : accountReversionRules) {         
                accountReversionMaintainable.setBusinessObject(accountReversion);
                maintenanceLocks.addAll(accountReversionMaintainable.generateMaintenanceLocks());
            }
        }
        return maintenanceLocks;
    }
    
    public void trickleDownInactivateAccountReversions(Account inactivatedAccount, String documentNumber) {
        List<AccountReversion> inactivatedAccountReversions = new ArrayList<AccountReversion>();
        Map<AccountReversion, String> alreadyLockedAccountReversions = new HashMap<AccountReversion, String>();
        List<AccountReversion> errorPersistingAccountReversions = new ArrayList<AccountReversion>();
        
        Maintainable accountReversionMaintainable;
        try {
            accountReversionMaintainable = (Maintainable) maintenanceDocumentDictionaryService.getMaintainableClass(AccountReversion.class.getName()).newInstance();
            accountReversionMaintainable.setBoClass(AccountReversion.class);
            accountReversionMaintainable.setDocumentNumber(documentNumber);
        }
        catch (Exception e) {
            LOG.error("Unable to instantiate accountReversionMaintainable Maintainable" , e);
            throw new RuntimeException("Unable to instantiate accountReversionMaintainable Maintainable" , e);
        }
        

        Integer universityFiscalYear = universityDateService.getCurrentFiscalYear() - 1;
        List<AccountReversion> accountReversionRules = new ArrayList<AccountReversion>();
        AccountReversion accountReversionRule = accountReversionDao.getByPrimaryId(universityFiscalYear, inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        if(ObjectUtils.isNotNull(accountReversionRule)){
            accountReversionRules.add(accountReversionRule);
        }
        
        List<AccountReversion> cashAccountReversionRules = accountReversionDao.getByCashReversionAcount(universityFiscalYear, inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(cashAccountReversionRules) && cashAccountReversionRules.size() > 0){
            accountReversionRules.addAll(cashAccountReversionRules);
        }
        
        List<AccountReversion> budgetAccountReversionRules = accountReversionDao.getByBudgetReversionAcount(universityFiscalYear, inactivatedAccount.getChartOfAccountsCode(), inactivatedAccount.getAccountNumber());
        
        if(ObjectUtils.isNotNull(budgetAccountReversionRules) && budgetAccountReversionRules.size() > 0){
            accountReversionRules.addAll(budgetAccountReversionRules);
        }
        
        if (ObjectUtils.isNotNull(accountReversionRules) && !accountReversionRules.isEmpty()) {
            for (AccountReversion accountReversion : accountReversionRules ) {
               
                if (accountReversion.isActive()) {
                    accountReversionMaintainable.setBusinessObject(accountReversion);
                    List<MaintenanceLock> accountReversionLocks = accountReversionMaintainable.generateMaintenanceLocks();
                    
                    MaintenanceLock failedLock = verifyAllLocksFromThisDocument(accountReversionLocks, documentNumber);
                    if (failedLock != null) {
                        // another document has locked this sub account, so we don't try to inactivate the account
                        alreadyLockedAccountReversions.put(accountReversion, failedLock.getDocumentNumber());
                    }
                    else {
                        // no locks other than our own (but there may have been no locks at all), just go ahead and try to update
                        accountReversion.setActive(false);
                        
                        try {
                            accountReversionMaintainable.saveBusinessObject();
                            inactivatedAccountReversions.add(accountReversion);
                        }
                        catch (RuntimeException e) {
                            LOG.error("Unable to trickle-down inactivate accountReversion " + accountReversion.toString(), e);
                            errorPersistingAccountReversions.add(accountReversion);
                        }
                    }
                }
            }
            
            addNotesToDocument(documentNumber, inactivatedAccountReversions, alreadyLockedAccountReversions, errorPersistingAccountReversions);
        }
    }

    protected void addNotesToDocument(String documentNumber, List<AccountReversion> inactivatedAccountReversions, Map<AccountReversion, String> alreadyLockedAccountReversions, List<AccountReversion> errorPersistingAccountReversions) {
        if (inactivatedAccountReversions.isEmpty() && alreadyLockedAccountReversions.isEmpty() && errorPersistingAccountReversions.isEmpty()) {
            // if we didn't try to inactivate any sub-accounts, then don't bother
            return;
        }
        DocumentHeader noteParent = documentHeaderService.getDocumentHeaderById(documentNumber);
        Note newNote = new Note();
        
        addNotes(documentNumber, inactivatedAccountReversions, CUKFSKeyConstants.ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION, noteParent, newNote);
        addNotes(documentNumber, errorPersistingAccountReversions, CUKFSKeyConstants.ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE, noteParent, newNote);
        addMaintenanceLockedNotes(documentNumber, alreadyLockedAccountReversions, CUKFSKeyConstants.ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED, noteParent, newNote);
    }
    
    protected void addMaintenanceLockedNotes(String documentNumber, Map<AccountReversion, String> lockedAccountReversions, String messageKey, PersistableBusinessObject noteParent, Note noteTemplate) {
        for (Map.Entry<AccountReversion, String> entry : lockedAccountReversions.entrySet()) {
            try {
                AccountReversion accountReversion = entry.getKey();
                String accountReversionString = accountReversion.getUniversityFiscalYear() + " - " + accountReversion.getChartOfAccountsCode() + " - " + accountReversion.getAccountNumber();
                if (StringUtils.isNotBlank(accountReversionString)) {
                    String noteTextTemplate = kualiConfigurationService.getPropertyValueAsString(messageKey);
                    String noteText = MessageFormat.format(noteTextTemplate, accountReversionString, entry.getValue());
                    Note note = noteService.createNote(noteTemplate, noteParent, GlobalVariables.getUserSession().getPrincipalId());
                    note.setNoteText(noteText);
                    noteService.save(note);
                }
            }
            catch (Exception e) {
                LOG.error("Unable to create/save notes for document " + documentNumber, e);
                throw new RuntimeException("Unable to create/save notes for document " + documentNumber, e);
            }
        }
    }

    protected void addNotes(String documentNumber, List<AccountReversion> listOfAccountReversions, String messageKey, PersistableBusinessObject noteParent, Note noteTemplate) {
        for (int i = 0; i < listOfAccountReversions.size(); i += getNumSubAccountsPerNote()) {
            try {
                String accountReversionString = createAccountReversionChunk(listOfAccountReversions, i, i + getNumSubAccountsPerNote());
                if (StringUtils.isNotBlank(accountReversionString)) {
                    String noteTextTemplate = kualiConfigurationService.getPropertyValueAsString(messageKey);
                    String noteText = MessageFormat.format(noteTextTemplate, accountReversionString);
                    Note note = noteService.createNote(noteTemplate, noteParent, GlobalVariables.getUserSession().getPrincipalId());
                    note.setNoteText(noteText);
                    note.setNotePostedTimestampToCurrent();
                    noteService.save(note);
                }
            }
            catch (Exception e) {
                LOG.error("Unable to create/save notes for document " + documentNumber, e);
                throw new RuntimeException("Unable to create/save notes for document " + documentNumber, e);
            }
        }
    }
    
    protected String createAccountReversionChunk(List<AccountReversion> listOfAccountReversions, int startIndex, int endIndex) {
        StringBuilder buf = new StringBuilder(); 
        for (int i = startIndex; i < endIndex && i < listOfAccountReversions.size(); i++) {
            AccountReversion accountReversion = listOfAccountReversions.get(i);
            buf.append(accountReversion.getUniversityFiscalYear()).append(" - ").append(accountReversion.getChartOfAccountsCode()).append(" - ")
                    .append(accountReversion.getAccountNumber());
            if (i + 1 < endIndex && i + 1 < listOfAccountReversions.size()) {
                buf.append(", ");
            }
        }
        return buf.toString();
    }
    
    protected int getNumSubAccountsPerNote() {
        return 20;
    }
    
    protected MaintenanceLock verifyAllLocksFromThisDocument(List<MaintenanceLock> maintenanceLocks, String documentNumber) {
        for (MaintenanceLock maintenanceLock : maintenanceLocks) {
            String lockingDocNumber = maintenanceDocumentDao.getLockingDocumentNumber(maintenanceLock.getLockingRepresentation(), documentNumber);
            if (StringUtils.isNotBlank(lockingDocNumber)) {
                return maintenanceLock;
            }
        }
        return null;
    }

    public void setMaintenanceDocumentDictionaryService(MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService) {
        this.maintenanceDocumentDictionaryService = maintenanceDocumentDictionaryService;
    }



    public void setMaintenanceDocumentDao(MaintenanceDocumentDao maintenanceDocumentDao) {
        this.maintenanceDocumentDao = maintenanceDocumentDao;
    }



    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    public void setDocumentHeaderService(DocumentHeaderService documentHeaderService) {
        this.documentHeaderService = documentHeaderService;
    }

    public AccountReversionDao getAccountReversionDao() {
        return accountReversionDao;
    }

    public void setAccountReversionDao(AccountReversionDao accountReversionDao) {
        this.accountReversionDao = accountReversionDao;
    }

    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

}
