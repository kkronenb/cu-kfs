package edu.cornell.kfs.module.cg.document.validation.impl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.document.validation.impl.AwardRule;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.document.MaintenanceDocument;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

@SuppressWarnings("deprecation")
public class AwardExtensionRule extends AwardRule {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
    	boolean success = true;
    	success &= super.processCustomRouteDocumentBusinessRules(document);
    	success &= checkFinalFinancialReportRequired();
        success &= checkForDuplicateAccoutnts();
        success &= checkForDuplicateAwardProjectDirector();
        success &= checkForDuplicateAwardOrganization();
    	
    	return success;
    }
    
    
	protected boolean checkFinalFinancialReportRequired() {
    	boolean success = true;
    	

    	AwardExtendedAttribute awardExtendedAttributeNew = (AwardExtendedAttribute) ( (Award) super.getNewBo()).getExtension();
    	AwardExtendedAttribute awardExtendedAttributeOld = (AwardExtendedAttribute) ( (Award) super.getOldBo()).getExtension();

    	if (awardExtendedAttributeNew.isFinalFinancialReportRequired() && null==awardExtendedAttributeNew.getFinalFiscalReportDate()) {
    		success = false;
    		putFieldError("extension.finalFiscalReportDate", CUKFSKeyConstants.ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED);
    	}

    	if (awardExtendedAttributeOld.isFinalFinancialReportRequired() && null==awardExtendedAttributeOld.getFinalFiscalReportDate()) {
    		success = false;
    		putFieldError("extension.finalFiscalReportDate", CUKFSKeyConstants.ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED);
    	}

    	
    	return success;
    }
	
	/**
     * @see org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase#setupBaseConvenienceObjects(org.kuali.rice.kns.document.MaintenanceDocument)
     */
	@Override
    public void setupBaseConvenienceObjects(MaintenanceDocument document) {
        newAwardCopy = (Award) document.getNewMaintainableObject().getBusinessObject();
        Proposal tempProposal = newAwardCopy.getProposal();
        super.setupBaseConvenienceObjects(document);
        newAwardCopy = (Award) document.getNewMaintainableObject().getBusinessObject();
        newAwardCopy.setProposal(tempProposal);
        super.setNewBo(newAwardCopy);
    }
	
	protected boolean checkForDuplicateAccoutnts() {
        boolean success = true;
        String accountNumber;
        String accountChart;
        
        Collection<AwardAccount> awardAccounts = newAwardCopy.getAwardAccounts();
        HashSet<String> accountHash = new HashSet<String>();

        //validate if the newly entered award account is already on that award
        for(AwardAccount account: awardAccounts){
            if(account!=null && StringUtils.isNotEmpty(account.getAccountNumber())){
                accountNumber = account.getAccountNumber();
                accountChart  = account.getChartOfAccountsCode();
                if (!accountHash.add(accountChart+accountNumber)){
                   putFieldError(KFSPropertyConstants.AWARD_ACCOUNTS, CUKFSKeyConstants.ERROR_DUPLICATE_AWARD_ACCOUNT, accountChart + "-" + accountNumber);
                   return false;
                }
            }    
         }        
         return success;     
    }
   
    protected boolean checkForDuplicateAwardProjectDirector() {
        boolean success = true;
        String principalId;
        Collection<AwardProjectDirector> awardProjectDirectors = newAwardCopy.getAwardProjectDirectors();
        HashSet<String> principalIdHash = new HashSet<String>();

        //validate if the newly entered AwardProjectDirector is already on that award
        for(AwardProjectDirector projectDirector: awardProjectDirectors){
             if(projectDirector!=null && StringUtils.isNotEmpty(projectDirector.getPrincipalId())){
                principalId = projectDirector.getPrincipalId();
                if (!principalIdHash.add(principalId)){
                   putFieldError(KFSPropertyConstants.AWARD_PROJECT_DIRECTORS, CUKFSKeyConstants.ERROR_DUPLICATE_AWARD_PROJECT_DIRECTOR, principalId);
                   return false;
                }
            }   
         }        
         return success;
    }     
  
    protected boolean checkForDuplicateAwardOrganization() {
        boolean success = true;
        String organizationCode;
        String organizationChart;
        Collection<AwardOrganization> awardOrganizations = newAwardCopy.getAwardOrganizations();
        HashSet<String> orgaizationHash = new HashSet<String>();
        
        //validate if the newly entered awardOrganization is already on that award
        for(AwardOrganization awardOrganization: awardOrganizations){
            if(awardOrganization!=null && StringUtils.isNotEmpty(awardOrganization.getOrganizationCode())){
                organizationCode = awardOrganization.getOrganizationCode();
                organizationChart  = awardOrganization.getChartOfAccountsCode();
                if (!orgaizationHash.add(organizationChart+organizationCode)){
                   putFieldError(KFSPropertyConstants.AWARD_ORGRANIZATIONS, CUKFSKeyConstants.ERROR_DUPLICATE_AWARD_ORGANIZATION, organizationChart + "-" + organizationCode);
                   return false;
                }
            }
        }
        return success;
    }
    
}
