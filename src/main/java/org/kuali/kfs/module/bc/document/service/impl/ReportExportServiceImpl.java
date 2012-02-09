/*
 * Copyright 2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.bc.document.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.BCPropertyConstants;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAccountDump;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAccountReports;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAdministrativePost;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionAppointmentFundingReason;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionCalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionIntendedIncumbent;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionMonthly;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionOrganizationReports;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPosition;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionAppointmentFunding;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionGeneralLedger;
import org.kuali.kfs.module.bc.document.dataaccess.ReportDumpDao;
import org.kuali.kfs.module.bc.document.service.ReportExportService;
import org.kuali.kfs.sys.DynamicCollectionComparator;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.businessobject.AccountExtendedAttribute;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;

import org.kuali.kfs.module.bc.document.dataaccess.BudgetConstructionOrganizationReportsDao;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionSipDao;
import edu.cornell.kfs.module.bc.document.dataaccess.impl.BudgetConstructionSipDaoJdbc.SIPExportData;

import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionBudgetedSalaryLineExportDao;
import edu.cornell.kfs.module.bc.document.dataaccess.impl.BudgetConstructionBudgetedSalaryLineExportDaoJdbc.BSLExportData;

/**
 * @see org.kuali.kfs.module.bc.document.service.ReportExportService
 */
@Transactional
public class ReportExportServiceImpl implements ReportExportService {
    ReportDumpDao reportDumpDao;
    BudgetConstructionSipDao budgetConstructionSipDao;
    BudgetConstructionBudgetedSalaryLineExportDao budgetConstructionBudgetedSalaryLineExportDao;
	BusinessObjectService businessObjectService;
    protected DataDictionaryService dataDictionaryService;

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#updateAccountDump(java.lang.String)
     */
    public void updateAccountDump(String principalId) {
        reportDumpDao.updateAccountDump(principalId);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#buildAccountDumpFile(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public StringBuilder buildOrganizationAccountDumpFile(String principalId, String fieldSeperator, String textDelimiter) {

        // read u_where %\
        // (univ_fiscal_yr.ld_pnd_bcnstr_gl_t = univ_fiscal_yr.ld_bcn_acct_dump_t & %\
        // fin_coa_cd.ld_pnd_bcnstr_gl_t = fin_coa_cd.ld_bcn_acct_dump_t & %\
        // account_nbr.ld_pnd_bcnstr_gl_t = account_nbr.ld_bcn_acct_dump_t & %\
        // sub_acct_nbr.ld_pnd_bcnstr_gl_t = sub_acct_nbr.ld_bcn_acct_dump_t) %\
        // order by "fin_object_cd, fin_sub_obj_cd"

        /*
         * Find all BudgetConstructionAccountDump objects for principalId and iterate through returned collection
         * then, retrieve all PendingBudgetConstructionGeneralLedger objects for fiscal year, chart, account and sub-account of
         * account dump record (order by object and sub object code). Iterate through this collection. Build up a String with the
         * fields below and add new line character and then add built up String to StringBuilder. Finally return StringBuilder.
         */

        //
        //
        // 
        // $line$ = "%%$line$%%$dlm$%%fdoc_nbr.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $line$ = "%%$line$%%univ_fiscal_yr.ld_pnd_bcnstr_gl_t%%$sep$"
        // $line$ = "%%$line$%%$dlm$%%fin_coa_cd.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $line$ = "%%$line$%%$dlm$%%account_nbr.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // ;add in org code field
        // $line$ = "%%$line$%%$dlm$%%$org_cd$%%$dlm$%%$sep$"
        // $line$ = "%%$line$%%$dlm$%%sub_acct_nbr.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $line$ = "%%$line$%%$dlm$%%fin_object_cd.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $line$ = "%%$line$%%$dlm$%%fin_sub_obj_cd.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $line$ = %\
        // "%%$line$%%$dlm$%%fin_balance_typ_cd.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $line$ = "%%$line$%%$dlm$%%fin_obj_typ_cd.ld_pnd_bcnstr_gl_t%%$dlm$%%$sep$"
        // $gennum$ = fin_beg_bal_ln_amt.ld_pnd_bcnstr_gl_t
        // $line$ = "%%$line$%%$gennum$%%$sep$"
        // $gennum$ = acln_annl_bal_amt.ld_pnd_bcnstr_gl_t
        // $line$ = "%%$line$%%$gennum$%%$sep$"
        // ;rc code added 12/20/2004 - gwp
        // $line$ = "%%$line$%%$dlm$%%$rc_cd$%%$dlm$"
        // $line$ = "%%$line$%%^"

        /*
         * NOTE: org code and rc code above come from BudgetConstructionAccountReports and BudgetConstructionOrganizationReports As
         * you iterate, retrieve BudgetConstructionAccountReports based on the ld_pnd_bcnstr_gl_t chart and account, the org code is
         * reportsToOrganizationCode then, the rc code come from budgetConstructionOrganizationReports.responsibilityCenterCode (a
         * reference to BudgetConstructionAccountReports)
         */

        // update account dump table
        updateAccountDump(principalId);

        StringBuilder results = new StringBuilder();
        //build and add report header row
        results.append(constructAccountDumpHeaderLine(fieldSeperator));

        List<BudgetConstructionAccountDump> accountDumpRecords = getBudgetConstructionAccountDump(principalId);
        for (BudgetConstructionAccountDump accountRecord : accountDumpRecords) {
            List<PendingBudgetConstructionGeneralLedger> pendingEntryList = getPendingBudgetConstructionGeneralLedgerRecords(accountRecord);

            for (PendingBudgetConstructionGeneralLedger pendingEntry : pendingEntryList) {
                results.append(constructAccountDumpLine(pendingEntry, textDelimiter, fieldSeperator));
            }
        }
        reportDumpDao.cleanAccountDump(principalId);

        return results;
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#buildFundingDumpFile(java.lang.String, java.lang.String,
     *      java.lang.String) // read u_where %\ // (univ_fiscal_yr.ld_pndbc_apptfnd_t = univ_fiscal_yr.ld_bcn_acct_dump_t & %\ //
     *      fin_coa_cd.ld_pndbc_apptfnd_t = fin_coa_cd.ld_bcn_acct_dump_t & %\ // account_nbr.ld_pndbc_apptfnd_t =
     *      account_nbr.ld_bcn_acct_dump_t & %\ // sub_acct_nbr.ld_pndbc_apptfnd_t = sub_acct_nbr.ld_bcn_acct_dump_t) %\ // order by
     *      "fin_object_cd , fin_sub_obj_cd, position_nbr, emplid" // // // if ($empty(ld_pndbc_apptfnd_t) = 0) // repeat // ;build
     *      the output line // ;note that gennum and genpct are used to strip numbers of commas // ;bcpct and bcfte are used also to
     *      force the display of decimals // $line$ = "%%univ_fiscal_yr.ld_pndbc_apptfnd_t%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%fin_coa_cd.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%account_nbr.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // ;add in org code field // $line$ =
     *      "%%$line$%%$dlm$%%$org_cd$%%$dlm$%%$sep$" // $line$ = "%%$line$%%$dlm$%%sub_acct_nbr.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" //
     *      $line$ = "%%$line$%%$dlm$%%fin_object_cd.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%fin_sub_obj_cd.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%position_nbr.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // ;add in position and job rank fields // $line$ =
     *      "%%$line$%%$dlm$%%pos_descr.ld_bcn_pos_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%setid_salary.ld_bcn_pos_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%pos_sal_plan_dflt.ld_bcn_pos_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%pos_grade_dflt.ld_bcn_pos_t%%$dlm$%%$sep$" // ;add in work months and pay months // $line$ =
     *      "%%$line$%%iu_norm_work_months.ld_bcn_pos_t%%$sep$" // $line$ = "%%$line$%%iu_pay_months.ld_bcn_pos_t%%$sep$" // ;add in
     *      incumbent fields // $line$ = "%%$line$%%$dlm$%%emplid.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%person_nm.ld_bcn_intincbnt_t%%$dlm$%%$sep$" // $line$ = %\ //
     *      "%%$line$%%$dlm$%%iu_classif_level.ld_bcn_intincbnt_t%%$dlm$%%$sep$" // ;add in the admin post // $line$ = %\ //
     *      "%%$line$%%$dlm$%%admin_post.ld_bcn_adm_post_t%%$dlm$%%$sep$" // ;add in the csf info // $gennum$ =
     *      pos_csf_amt.ld_bcn_csf_trckr_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $bcfte$ = pos_csf_fte_qty.ld_bcn_csf_trckr_t //
     *      $line$ = "%%$line$%%$bcfte$%%$sep$" // $bcpct$ = pos_csf_tm_pct.ld_bcn_csf_trckr_t // $line$ =
     *      "%%$line$%%$bcpct$%%$sep$" // ;rest of bcaf // $line$ =
     *      "%%$line$%%$dlm$%%appt_fnd_dur_cd.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // $gennum$ = appt_rqst_csf_amt.ld_pndbc_apptfnd_t //
     *      $line$ = "%%$line$%%$gennum$%%$sep$" // $bcfte$ = appt_rqcsf_fte_qty.ld_pndbc_apptfnd_t // $line$ =
     *      "%%$line$%%$bcfte$%%$sep$" // $bcpct$ = appt_rqcsf_tm_pct.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$bcpct$%%$sep$" //
     *      $gennum$ = appt_tot_intnd_amt.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $bcfte$ =
     *      appt_totintfte_qty.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$bcfte$%%$sep$" // $gennum$ =
     *      appt_rqst_amt.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $bcpct$ =
     *      appt_rqst_tm_pct.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$bcpct$%%$sep$" // $bcfte$ =
     *      appt_rqst_fte_qty.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$bcfte$%%$sep$" // $gennum$ =
     *      appt_rqst_pay_rt.ld_pndbc_apptfnd_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%appt_fnd_dlt_cd.ld_pndbc_apptfnd_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%appt_fnd_mo.ld_pndbc_apptfnd_t%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%appt_fnd_reason_cd.ld_bcn_af_reason_t%%$dlm$%%$sep$" // ; rc_cd added 12/20/2004 - gwp // $line$ =
     *      "%%$line$%%$dlm$%%$rc_cd$%%$dlm$" // // $line$ = "%%$line$%%^"
     */
    public StringBuilder buildOrganizationFundingDumpFile(String principalId, String fieldSeperator, String textDelimiter) {
        // update account dump table
        // updateAccountDump(principalId);  // Not needed as it is being performed in the SQL in the Jdbc class

        StringBuilder results = new StringBuilder();
        
        // construct and append the header line
        results.append(constructFundingDumpHeaderLine(fieldSeperator));
        Collection<BSLExportData> bslExportRecords = budgetConstructionBudgetedSalaryLineExportDao.getBSLExtractByPersonUnivId(principalId);

        if (ObjectUtils.isNotNull(bslExportRecords))
	        for (BSLExportData bslRecord : bslExportRecords) {
	           results.append(this.constructBSLDumpLine(bslRecord, fieldSeperator, textDelimiter));
	        }
        
//        List<BudgetConstructionAccountDump> accountDumpRecords = getBudgetConstructionAccountDump(principalId);
//        for (BudgetConstructionAccountDump accountRecord : accountDumpRecords) {
//            List<PendingBudgetConstructionAppointmentFunding> pendingBudgetConstructionAppointmentFundingList = getPendingBudgetConstructionAppointmentFundingRecords(accountRecord);
//            for (PendingBudgetConstructionAppointmentFunding fundingRecord : pendingBudgetConstructionAppointmentFundingList) {
//                results.append(this.constructFundingDumpLine(fundingRecord, fieldSeperator, textDelimiter));
//            }
//        }
//		  reportDumpDao.cleanAccountDump(principalId);  // Not needed as the updateAccountDump(principalId) is commented out above

        return results;
    }

    private Object constructBSLDumpLine(BSLExportData bslRecord, String fieldSeperator, String textDelimiter) {
		// Generate each line as a single String and return it
        String line = "";
        
        line = textDelimiter + bslRecord.getUnivFiscalYear() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getFinCoaCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getAccountNbr() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getRptsToOrgCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getSubAcctNbr() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getFinObjectCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getFinSubObjCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPositionNbr() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPosDescr() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getSetidSalary() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPosSalPlanDflt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPosGradeDflt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getIuNormWorkMonths() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getIuPayMonths() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getEmplId() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPersonNm() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getIuClassifLevel() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getAdminPost() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPosCsfAmt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPosCsfFteQty() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getPosCsfTmPct() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptFndDurCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptDurDesc() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqstCsfAmt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqcsfFteQty() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqcsfTmPct() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptTotIntndAmt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptTotintfteQty() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqstAmt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqstTmPct() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqstFteQty() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptRqstPayRt() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptFndDltCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptFndMo() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getApptFndReasonCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getSubFundGrpCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getRcCd() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + bslRecord.getProgramCd() + textDelimiter + fieldSeperator;
        
        line = line + "\r\n";
    	return line;
    	
    	
	}
    
    public StringBuilder buildSIPExportDumpFile(String principalId, String fieldSeperator, String textDelimiter, boolean executivesOnly) {
        // update account dump table
        // updateAccountDump(principalId);

        StringBuilder results = new StringBuilder();
        
        // construct and append the header line
        results.append(constructSIPExportHeaderLine(fieldSeperator));
        Collection<SIPExportData> sipExportRecords = budgetConstructionSipDao.getSIPExtractByPersonUnivId(principalId, executivesOnly);
        
        if (ObjectUtils.isNotNull(sipExportRecords))
	        for (SIPExportData sipRecord : sipExportRecords) {
	           results.append(this.constructSipDumpLine(sipRecord, fieldSeperator, textDelimiter));
	        }
        
        //Returns results of the extract to the user via the browser.
        return results;
    }

    private Object constructSipDumpLine(SIPExportData sipRecord, String fieldSeperator, String textDelimiter) {
		// Generate each line as a single String and return it
        String line = "";
        line = textDelimiter + sipRecord.getC_Level_Name() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getHR_DEPTID() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getPOS_DEPTID() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getD_Level_Name() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getPOSITION_NBR() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getPOS_DESCR() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getEMPLID() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getPERSON_NM() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getSIP_Eligibility() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getSIP_Employee_Type() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getEMPL_RCD() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getJOBCODE() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getJOB_CD_DESC_SHRT() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getJOB_FAMILY() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getCU_PLANNED_FTE() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getPOS_GRADE_DFLT() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getCU_STATE_CERT() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getCOMP_FREQ() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getANNL_RT() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getCOMP_RT() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getJOB_STD_HRS() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getWRK_MNTHS() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getJOB_FUNC() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getJOB_FUNC_DESC() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getIncrease_To_Minimum() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getEquity() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getMerit() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getNote() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getDeferred() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getCU_ABBR_FLAG() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getAPPT_TOT_INTND_AMT() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getAPPT_RQST_FTE_QTY() + textDelimiter + fieldSeperator;
//        line = line + textDelimiter + sipRecord.getLeave_Code() + textDelimiter + fieldSeperator;
//        line = line + textDelimiter + sipRecord.getLeave_Description() + textDelimiter + fieldSeperator;
//        line = line + textDelimiter + sipRecord.getLeave_Amount() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipRecord.getIU_POSITION_TYPE() + textDelimiter + fieldSeperator;
        line = line + "\r\n";
    	return line;
	}

	/**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#buildMonthlyDumpFile(java.lang.String, java.lang.String,
     *      java.lang.String) // read u_where %\ // (univ_fiscal_yr.ld_bcnstr_month_t = univ_fiscal_yr.ld_bcn_acct_dump_t & %\ //
     *      fin_coa_cd.ld_bcnstr_month_t = fin_coa_cd.ld_bcn_acct_dump_t & %\ // account_nbr.ld_bcnstr_month_t =
     *      account_nbr.ld_bcn_acct_dump_t & %\ // sub_acct_nbr.ld_bcnstr_month_t = sub_acct_nbr.ld_bcn_acct_dump_t) %\ // order by
     *      "fin_object_cd, fin_sub_obj_cd" // endif // // if ($empty(ld_bcnstr_month_t) = 0) // repeat // ;build the output line //
     *      ;note that gennum are used to strip numbers of commas // $line$ =
     *      "%%$dlm$%%fs_origin_cd.ld_bcnstr_month_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%fdoc_nbr.ld_bcnstr_month_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%univ_fiscal_yr.ld_bcnstr_month_t%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%fin_coa_cd.ld_bcnstr_month_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%account_nbr.ld_bcnstr_month_t%%$dlm$%%$sep$" // ;add in org code field // $line$ =
     *      "%%$line$%%$dlm$%%$org_cd$%%$dlm$%%$sep$" // $line$ = "%%$line$%%$dlm$%%sub_acct_nbr.ld_bcnstr_month_t%%$dlm$%%$sep$" //
     *      $line$ = "%%$line$%%$dlm$%%fin_object_cd.ld_bcnstr_month_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%fin_sub_obj_cd.ld_bcnstr_month_t%%$dlm$%%$sep$" // $line$ = %\ //
     *      "%%$line$%%$dlm$%%fin_balance_typ_cd.ld_bcnstr_month_t%%$dlm$%%$sep$" // $line$ =
     *      "%%$line$%%$dlm$%%fin_obj_typ_cd.ld_bcnstr_month_t%%$dlm$%%$sep$" // $gennum$ = fdoc_ln_mo1_amt.ld_bcnstr_month_t //
     *      $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ = fdoc_ln_mo2_amt.ld_bcnstr_month_t // $line$ =
     *      "%%$line$%%$gennum$%%$sep$" // $gennum$ = fdoc_ln_mo3_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" //
     *      $gennum$ = fdoc_ln_mo4_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo5_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo6_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo7_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo8_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo9_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo10_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo11_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // $gennum$ =
     *      fdoc_ln_mo12_amt.ld_bcnstr_month_t // $line$ = "%%$line$%%$gennum$%%$sep$" // ; rc_cd added 12/20/2004 - gwp // $line$ =
     *      "%%$line$%%$dlm$%%$rc_cd$%%$dlm$" // // $line$ = "%%$line$%%^"
     */
    public StringBuilder buildOrganizationMonthlyDumpFile(String principalId, String fieldSeperator, String textDelimiter) {
        // update account dump table
        updateAccountDump(principalId);

        StringBuilder results = new StringBuilder();
        
        results.append(constructMonthlyDumpHeaderLine(fieldSeperator));

        List<BudgetConstructionAccountDump> accountDumpRecords = getBudgetConstructionAccountDump(principalId);
        for (BudgetConstructionAccountDump accountRecord : accountDumpRecords) {
            List<BudgetConstructionMonthly> budgetConstructionMonthlyList = getBudgetConstructionMonthlyRecords(accountRecord);
            for (BudgetConstructionMonthly monthlyRecord : budgetConstructionMonthlyList) {
                results.append(this.constructMonthlyDumpLine(monthlyRecord, fieldSeperator, textDelimiter));
            }
        }
        reportDumpDao.cleanAccountDump(principalId);

        return results;
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#buildAccountDumpFile(java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
     */
    public StringBuilder buildAccountDumpFile(String principalId, String fieldSeperator, String textDelimiter, Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        StringBuilder results = new StringBuilder();
        Map searchFields = new HashMap();
        searchFields.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        searchFields.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        searchFields.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        searchFields.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, subAccountNumber);

        List<PendingBudgetConstructionGeneralLedger> pendingEntryList = new ArrayList<PendingBudgetConstructionGeneralLedger>(businessObjectService.findMatching(PendingBudgetConstructionGeneralLedger.class, searchFields));

        for (PendingBudgetConstructionGeneralLedger pendingEntry : pendingEntryList) {
            results.append(constructAccountDumpLine(pendingEntry, textDelimiter, fieldSeperator));
        }

        return results;
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#buildAccountFundingDumpFile(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
     */
    public StringBuilder buildAccountFundingDumpFile(String principalId, String fieldSeperator, String textDelimiter, Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        StringBuilder results = new StringBuilder();
        Map searchFields = new HashMap();
        searchFields.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        searchFields.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        searchFields.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        searchFields.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, subAccountNumber);

        List<PendingBudgetConstructionAppointmentFunding> pendingBudgetConstructionAppointmentFundingList = new ArrayList<PendingBudgetConstructionAppointmentFunding>(businessObjectService.findMatching(PendingBudgetConstructionAppointmentFunding.class, searchFields));
        for (PendingBudgetConstructionAppointmentFunding fundingRecord : pendingBudgetConstructionAppointmentFundingList) {
            results.append(this.constructFundingDumpLine(fundingRecord, fieldSeperator, textDelimiter));
        }

        return results;
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#buildAccountMonthlyDumpFile(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
     */
    public StringBuilder buildAccountMonthlyDumpFile(String principalId, String fieldSeperator, String textDelimiter, Integer universityFiscalYear, String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        StringBuilder results = new StringBuilder();
        Map searchFields = new HashMap();
        searchFields.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, universityFiscalYear);
        searchFields.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartOfAccountsCode);
        searchFields.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber);
        searchFields.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, subAccountNumber);

        List<BudgetConstructionMonthly> budgetConstructionMonthlyList = new ArrayList<BudgetConstructionMonthly>(businessObjectService.findMatching(BudgetConstructionMonthly.class, searchFields));
        for (BudgetConstructionMonthly monthlyRecord : budgetConstructionMonthlyList) {
            results.append(this.constructMonthlyDumpLine(monthlyRecord, fieldSeperator, textDelimiter));
        }

        return results;
    }

    /**
     * Sets the reportDumpDao attribute value.
     * 
     * @param reportDumpDao The reportDumpDao to set.
     */
    public void setReportDumpDao(ReportDumpDao reportDumpDao) {
        this.reportDumpDao = reportDumpDao;
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.ReportExportService#setBusinessObjectService(org.kuali.rice.kns.service.BusinessObjectService)
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Retrieves all PendingBudgetConstructionGeneralLedger sorted by financialObjectCode and financialSubObjectCode
     * 
     * @param accountRecord
     * @return
     */
    protected List<PendingBudgetConstructionGeneralLedger> getPendingBudgetConstructionGeneralLedgerRecords(BudgetConstructionAccountDump accountRecord) {
        Map searchParameters = new HashMap();
        searchParameters.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, accountRecord.getUniversityFiscalYear());
        searchParameters.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountRecord.getChartOfAccountsCode());
        searchParameters.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountRecord.getAccountNumber());
        searchParameters.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountRecord.getSubAccountNumber());

        ArrayList<PendingBudgetConstructionGeneralLedger> results = new ArrayList<PendingBudgetConstructionGeneralLedger>(this.businessObjectService.findMatchingOrderBy(PendingBudgetConstructionGeneralLedger.class, searchParameters, "financialObjectCode", true));

        DynamicCollectionComparator.sort(results, "financialObjectCode", "financialSubObjectCode");

        return results;
    }

    /**
     * Retrieves all PendingBudgetConstructionAppointmentFunding sorted by "financialObjectCode", "financialSubObjectCode",
     * "positionNumber", "emplid"
     * 
     * @param accountRecord
     * @return
     */
    protected List<PendingBudgetConstructionAppointmentFunding> getPendingBudgetConstructionAppointmentFundingRecords(BudgetConstructionAccountDump accountRecord) {
        Map searchParameters = new HashMap();
        searchParameters.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, accountRecord.getUniversityFiscalYear());
        searchParameters.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountRecord.getChartOfAccountsCode());
        searchParameters.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountRecord.getAccountNumber());
        searchParameters.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountRecord.getSubAccountNumber());

        ArrayList<PendingBudgetConstructionAppointmentFunding> results = 
        	new ArrayList<PendingBudgetConstructionAppointmentFunding>(this.businessObjectService.findMatchingOrderBy(PendingBudgetConstructionAppointmentFunding.class, searchParameters, "financialObjectCode", true));

        DynamicCollectionComparator.sort(results, "financialObjectCode", "financialSubObjectCode", "positionNumber", "emplid");

        return results;
    }

    /**
     * Retrieves all BudgetConstructionMonthly sorted by "financialObjectCode", "financialSubObjectCode"
     * 
     * @param accountRecord
     * @return
     */
    protected List<BudgetConstructionMonthly> getBudgetConstructionMonthlyRecords(BudgetConstructionAccountDump accountRecord) {
        Map searchParameters = new HashMap();
        searchParameters.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, accountRecord.getUniversityFiscalYear());
        searchParameters.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountRecord.getChartOfAccountsCode());
        searchParameters.put(KFSPropertyConstants.ACCOUNT_NUMBER, accountRecord.getAccountNumber());
        searchParameters.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountRecord.getSubAccountNumber());

        ArrayList<BudgetConstructionMonthly> results = new ArrayList<BudgetConstructionMonthly>(this.businessObjectService.findMatchingOrderBy(BudgetConstructionMonthly.class, searchParameters, "financialObjectCode", true));

        DynamicCollectionComparator.sort(results, "financialObjectCode", "financialSubObjectCode");

        return results;
    }

    /**
     * Retrieves all BudgetConstructionAccountDump by principalId
     * 
     * @param principalId
     * @return
     */
    protected List<BudgetConstructionAccountDump> getBudgetConstructionAccountDump(String principalId) {
        HashMap searchParameters = new HashMap();
        searchParameters.put(KFSPropertyConstants.KUALI_USER_PERSON_UNIVERSAL_IDENTIFIER, principalId);

        return new ArrayList<BudgetConstructionAccountDump>(this.businessObjectService.findMatching(BudgetConstructionAccountDump.class, searchParameters));

    }

    /**
     * Constructs a line for the Account Dump Report
     * 
     * @param pendingEntry
     * @param textDelimiter
     * @param fieldSeperator
     * @return
     */
    protected String constructAccountDumpLine(PendingBudgetConstructionGeneralLedger pendingEntry, String textDelimiter, String fieldSeperator) {
        HashMap accountReportSearchParameters = new HashMap();
        accountReportSearchParameters.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, pendingEntry.getChartOfAccountsCode());
        accountReportSearchParameters.put(KFSPropertyConstants.ACCOUNT_NUMBER, pendingEntry.getAccountNumber());
        BudgetConstructionAccountReports accountReport = (BudgetConstructionAccountReports) this.businessObjectService.findByPrimaryKey(BudgetConstructionAccountReports.class, accountReportSearchParameters);

        String line = "";
        line = textDelimiter + pendingEntry.getDocumentNumber() + textDelimiter + fieldSeperator;
        line = line + pendingEntry.getUniversityFiscalYear() + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getChartOfAccountsCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getAccountNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + accountReport.getReportsToOrganizationCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getSubAccountNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getFinancialObjectCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getFinancialSubObjectCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getFinancialBalanceTypeCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + pendingEntry.getFinancialObjectTypeCode() + textDelimiter + fieldSeperator;
        line = line + pendingEntry.getFinancialBeginningBalanceLineAmount() + fieldSeperator;
        line = line + pendingEntry.getAccountLineAnnualBalanceAmount() + fieldSeperator;
        line = line + textDelimiter + accountReport.getBudgetConstructionOrganizationReports().getResponsibilityCenterCode() + textDelimiter;
        line = line + "\r\n";

        return line;
    }

    /**
     * Constructs a line of the Funding Dump File
     * 
     * @param fundingRecord
     * @param fieldSeperator
     * @param textDelimiter
     * @return
     */
    protected String constructFundingDumpLine(PendingBudgetConstructionAppointmentFunding fundingRecord, String fieldSeperator, String textDelimiter) {
        HashMap accountReportSearchParameters = new HashMap();
        accountReportSearchParameters.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, fundingRecord.getChartOfAccountsCode());
        accountReportSearchParameters.put(KFSPropertyConstants.ACCOUNT_NUMBER, fundingRecord.getAccountNumber());
        BudgetConstructionAccountReports accountReport = (BudgetConstructionAccountReports) this.businessObjectService.findByPrimaryKey(BudgetConstructionAccountReports.class, accountReportSearchParameters);

        if (!fundingRecord.getEmplid().equals(BCConstants.VACANT_EMPLID)) {
            fundingRecord.refreshReferenceObject(BCPropertyConstants.BUDGET_CONSTRUCTION_INTENDED_INCUMBENT);
        }

        String line = "";
        line = line + fundingRecord.getUniversityFiscalYear() + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getChartOfAccountsCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getAccountNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + accountReport.getReportsToOrganizationCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getSubAccountNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getFinancialObjectCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getFinancialSubObjectCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getPositionNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getBudgetConstructionPosition().getPositionDescription() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getBudgetConstructionPosition().getSetidSalary() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getBudgetConstructionPosition().getPositionSalaryPlanDefault() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getBudgetConstructionPosition().getPositionGradeDefault() + textDelimiter + fieldSeperator;
        line = line + fundingRecord.getBudgetConstructionPosition().getIuNormalWorkMonths() + fieldSeperator;
        line = line + fundingRecord.getBudgetConstructionPosition().getIuPayMonths() + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getEmplid() + textDelimiter + fieldSeperator;

        if (ObjectUtils.isNotNull(fundingRecord.getBudgetConstructionIntendedIncumbent())) {
            line = line + textDelimiter + fundingRecord.getBudgetConstructionIntendedIncumbent().getName() + textDelimiter + fieldSeperator;
            line = line + textDelimiter + fundingRecord.getBudgetConstructionIntendedIncumbent().getIuClassificationLevel() + textDelimiter + fieldSeperator;
        }
        else {
            line = line + textDelimiter + textDelimiter + fieldSeperator;
            line = line + textDelimiter + textDelimiter + fieldSeperator;
        }
        if (ObjectUtils.isNotNull(fundingRecord.getBudgetConstructionAdministrativePost()))
            line = line + textDelimiter + fundingRecord.getBudgetConstructionAdministrativePost().getAdministrativePost() + textDelimiter + fieldSeperator;
        else
            line = line + textDelimiter + textDelimiter + fieldSeperator;

        // output blanks when no associated CSF row - relation is optional 1-1 configured in OJB as collection
        // and typed as TypedArrayList
        if (fundingRecord.getBcnCalculatedSalaryFoundationTracker().isEmpty()) {
            line = line + "" + fieldSeperator;
            line = line + "" + fieldSeperator;
            line = line + "" + fieldSeperator;

        }
        else {
            line = line + (new KualiDecimal(fundingRecord.getBcnCalculatedSalaryFoundationTracker().get(0).getCsfAmount().intValue())) + fieldSeperator;
            line = line + (new KualiDecimal(fundingRecord.getBcnCalculatedSalaryFoundationTracker().get(0).getCsfFullTimeEmploymentQuantity())) + fieldSeperator;
            line = line + (new KualiDecimal(fundingRecord.getBcnCalculatedSalaryFoundationTracker().get(0).getCsfTimePercent())) + fieldSeperator;

        }
        line = line + textDelimiter + fundingRecord.getAppointmentFundingDurationCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + fundingRecord.getBudgetConstructionDuration().getAppointmentDurationDescription() + textDelimiter + fieldSeperator;   //Apoint.Funding Dur Description
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedCsfAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedCsfFteQuantity()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedCsfTimePercent()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentTotalIntendedAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentTotalIntendedFteQuantity()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedTimePercent()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedFteQuantity()) + fieldSeperator;
        line = line + new KualiDecimal(fundingRecord.getAppointmentRequestedPayRate()) + fieldSeperator;
        line = line + textDelimiter + (fundingRecord.isAppointmentFundingDeleteIndicator() ? "Y" : "N") + textDelimiter + fieldSeperator;
        line = line + fundingRecord.getAppointmentFundingMonth() + fieldSeperator;
        
        List<BudgetConstructionAppointmentFundingReason> appointmentFundingReasonList = fundingRecord.getBudgetConstructionAppointmentFundingReason(); 
        if (ObjectUtils.isNotNull(appointmentFundingReasonList) && !appointmentFundingReasonList.isEmpty()){
            line = line + textDelimiter + ((appointmentFundingReasonList.get(0).getAppointmentFundingReasonCode() == null) ? "" : appointmentFundingReasonList.get(0).getAppointmentFundingReasonCode()) + textDelimiter + fieldSeperator;
        }
        else {
            line = line + textDelimiter + "" + textDelimiter + fieldSeperator;
        }
        line = line + textDelimiter + accountReport.getBudgetConstructionOrganizationReports().getResponsibilityCenterCode() + textDelimiter  + fieldSeperator;
        
        // Added lines to also provide the fund group code and the sub fund group code as per KITI-2989
        line = line + textDelimiter + fundingRecord.getAccount().getSubFundGroupCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + ((AccountExtendedAttribute)fundingRecord.getAccount().getExtension()).getProgramCode() +  textDelimiter + fieldSeperator;
        
        // Adds a carriage return and line feed character
        line = line + "\r\n";

        return line;
    }

    /**
     * Constructs a monthly dump file line
     * 
     * @param monthlyRecord
     * @param fieldSeperator
     * @param textDelimiter
     * @return
     */
    protected String constructMonthlyDumpLine(BudgetConstructionMonthly monthlyRecord, String fieldSeperator, String textDelimiter) {
        HashMap accountReportSearchParameters = new HashMap();
        accountReportSearchParameters.put("chartOfAccountsCode", monthlyRecord.getChartOfAccountsCode());
        accountReportSearchParameters.put("accountNumber", monthlyRecord.getAccountNumber());
        BudgetConstructionAccountReports accountReport = (BudgetConstructionAccountReports) this.businessObjectService.findByPrimaryKey(BudgetConstructionAccountReports.class, accountReportSearchParameters);

        String line = "";

        line = line + textDelimiter + monthlyRecord.getDocumentNumber() + textDelimiter + fieldSeperator;
        line = line + monthlyRecord.getUniversityFiscalYear() + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getChartOfAccountsCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getAccountNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + accountReport.getReportsToOrganizationCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getSubAccountNumber() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getFinancialObjectCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getFinancialSubObjectCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getFinancialBalanceTypeCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + monthlyRecord.getFinancialObjectTypeCode() + textDelimiter + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth1LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth2LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth3LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth4LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth5LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth6LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth7LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth8LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth9LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth10LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth11LineAmount().intValue()) + fieldSeperator;
        line = line + new KualiDecimal(monthlyRecord.getFinancialDocumentMonth12LineAmount().intValue()) + fieldSeperator;
        line = line + textDelimiter + accountReport.getBudgetConstructionOrganizationReports().getResponsibilityCenterCode() + textDelimiter;

        line = line + "\r\n";

        return line;
    }
    
    /**
     * Constructs header line for the Account Dump Report
     * 
     * @param fieldSeperator
     * @return
     */
    protected String constructAccountDumpHeaderLine(String fieldSeperator) {
      
        String line = "";
        line = dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.DOC_NBR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.UNIVERSITY_FISCAL_YEAR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.CHART_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.ACCOUNT_NBR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionAccountReports.class, CUBCPropertyConstants.BudgetConstructionAccountReportsProperties.REPORTS_TO_ORG_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.SUB_ACCOUNT_NBR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJECT_CODE) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_SUB_OBJECT_CODE) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_BALANCE_TYP_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FINANCIAL_OBJ_TYP_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.FIN_BEG_BAL_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(PendingBudgetConstructionGeneralLedger.class, CUBCPropertyConstants.PendingBudgetConstructionGeneralLedgerProperties.ACC_LINE_ANN_BAL_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionOrganizationReports.class, CUBCPropertyConstants.BudgetConstructionOrganizationReportsProperties.RESP_CENTER_CD);
        line = line + "\r\n";

        return line;
    }
    
    /**
     * Constructs a header line for the Funding Dump File
     * 
     * @param fieldSeperator
     * 
     * @return
     */
    protected String constructFundingDumpHeaderLine(String fieldSeperator) {

        String line = "";
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.UNIVERSITY_FISCAL_YEAR + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.CHART_CD + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.ACCOUNT_NBR + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.REPORTS_TO_ORG_CD  + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.SUB_ACCOUNT_NBR + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.FINANCIAL_OBJECT_CODE + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.FINANCIAL_SUB_OBJECT_CODE + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.POSITION_NBR + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.POSITION_DESC + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.SETID_SALARY + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.POSITION_SALARY_PLAN_DEFAULT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.POSITION_GRADE_DEFAULT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.NORMAL_WORK_MONTHS + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.PAY_MONTHS + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.EMPLID + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.NAME + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.CLASSIFICATION_LEVEL + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.ADMINISTRATIVE_POST + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.CSF_AMT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.CSF_FULL_TIME_EMPL_QUANTITY + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.CSF_TIME_PERCENT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_FUND_DURATION_CD + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_FUND_DURATION_DESC + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_CSF_AMT+ fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_CSF_FTE_QUANTITY + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_CSF_TIME_PERCENT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_TOTAL_INTENDED_AMT  + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_TOTAL_INTENDED_FTE_QUANTITY + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_AMT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_TIME_PERCENT + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_FTE_QUANTITY + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_REQ_PAY_RATE + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_FUNDING_DELETE_IND + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_FUNDING_MONTH + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.APPT_FUNDING_REASON_CD + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.RESP_CENTER_CD + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.SUB_FUND_GRP_CD + fieldSeperator;
        line = line + CUBCPropertyConstants.BudgetConstructionFundingExportProperties.PROGRAM_CD + fieldSeperator;

        line = line + "\r\n";

        return line;
    }
    

    /**
     * Constructs a monthly dump header line
     * 
     * @param fieldSeperator
     * @return
     */
    protected String constructMonthlyDumpHeaderLine( String fieldSeperator) {
        String line = "";

        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.DOC_NBR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.UNIVERSITY_FISCAL_YEAR)  + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.CHART_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.ACCOUNT_NBR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionAccountReports.class, CUBCPropertyConstants.BudgetConstructionAccountReportsProperties.REPORTS_TO_ORG_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.SUB_ACCOUNT_NBR) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_OBJ_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_SUB_OBJ_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_BAL_TYP_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_OBJ_TYP_CD) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_1_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_2_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_3_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_4_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_5_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_6_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_7_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_8_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_9_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_10_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_11_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionMonthly.class, CUBCPropertyConstants.BudgetConstructionMonthlyProperties.FINANCIAL_DOC_MONTH_12_LINE_AMT) + fieldSeperator;
        line = line + dataDictionaryService.getAttributeLabel(BudgetConstructionOrganizationReports.class, CUBCPropertyConstants.BudgetConstructionOrganizationReportsProperties.RESP_CENTER_CD);

        line = line + "\r\n";

        return line;
    }
    
    /**
     * Constructs a header line for the Funding Dump File
     * 
     * @param fieldSeparator
     * 
     * @return
     */
    protected String constructSIPExportHeaderLine(String fieldSeparator) {

        String line = "";
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.UNITID + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.HRDEPT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.DEPTID + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.DEPT_NAME + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.POSITION_NBR + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.POSITION_DESC + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.EMPLID + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.PERSON_NAME + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.SIP_ELIGIBILITY + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.EMPLOYEE_TYPE + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.EMPL_RCD + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.JOBCODE + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.JOBCODE_SHORT_DESC + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.JOB_FAMILY + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.CU_PLANNED_FTE + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.POS_GRADE_DFLT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.CU_STATE_CERT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.COMP_FREQ + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.ANNL_RT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.COMP_RT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.JOB_STD_HRS + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.WORK_MONTHS + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.JOB_FUNC + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.JOB_FUNC_DESC + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.INCREASE_TO_MINIMUM + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.EQUITY + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.MERIT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.NOTE + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.DEFERRED + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.CU_ABBR_FLAG + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.APPT_TOT_INTND_AMT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.APPT_RQST_FTE_QTY + fieldSeparator;
//        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.LEAVE_CODE + fieldSeparator;
//        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.LEAVE_DESC + fieldSeparator;
//        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.LEAVE_AMT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSIPExportProperties.BGP_FLSA + fieldSeparator;
        line = line + "\r\n";

        return line;
    }

    /**
     * Gets the dataDictionaryService.
     * 
     * @param dataDictionaryService
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public BudgetConstructionSipDao getBudgetConstructionSipDao() {
		return budgetConstructionSipDao;
	}

	public void setBudgetConstructionSipDao(
			BudgetConstructionSipDao budgetConstructionSipDao) {
		this.budgetConstructionSipDao = budgetConstructionSipDao;
	}

	public BudgetConstructionBudgetedSalaryLineExportDao getBudgetConstructionBudgetedSalaryLineExportDao() {
		return budgetConstructionBudgetedSalaryLineExportDao;
	}

	public void setBudgetConstructionBudgetedSalaryLineExportDao(
			BudgetConstructionBudgetedSalaryLineExportDao budgetConstructionBudgetedSalaryLineExportDao) {
		this.budgetConstructionBudgetedSalaryLineExportDao = budgetConstructionBudgetedSalaryLineExportDao;
	}
}

