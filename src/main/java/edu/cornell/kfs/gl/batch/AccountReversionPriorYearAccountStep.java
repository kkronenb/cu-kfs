

/*
 * Copyright 2006 The Kuali Foundation
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
package edu.cornell.kfs.gl.batch;

import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.gl.batch.service.OrganizationReversionProcessService;
import org.kuali.kfs.gl.batch.service.YearEndService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractWrappedBatchStep;
import org.kuali.kfs.sys.batch.service.WrappedBatchExecutorService.CustomBatchExecutor;
import org.springframework.util.StopWatch;


import edu.cornell.kfs.gl.batch.service.ReversionProcessService;
/**
 * A step that runs the reversion and carry forward process. The end of year version of the process is supposed to be run before the
 * end of a fiscal year for reporting purposes; therefore, it uses current year accounts instead of prior year accounts.
 */
public class AccountReversionPriorYearAccountStep extends AbstractWrappedBatchStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionPriorYearAccountStep.class);
    private ReversionProcessService accountReversionProcessService;
    private YearEndService yearEndService;

    /**
     * @see org.kuali.kfs.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
    @Override
    protected CustomBatchExecutor getCustomBatchExecutor() {
        return new CustomBatchExecutor() {
            /**
             * Runs the account reversion process, retrieving parameter, creating the origin entry group for output entries, and
             * generating the reports on the process.
             * @return true if the job completed successfully, false if otherwise
             * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String)
             */
            public boolean execute() {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("AccountReversionPriorYearAccountStep");

                Map jobParameters = accountReversionProcessService.getJobParameters();
                Map<String, Integer> accountReversionCounts = new HashMap<String, Integer>();
                
                getYearEndService().logAllMissingPriorYearAccounts((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));
                getYearEndService().logAllMissingSubFundGroups((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));

                getAccountReversionProcessService().reversionPriorYearAccountProcess(jobParameters, accountReversionCounts);
                
                stopWatch.stop();
                LOG.info("Account ReversionPriorYearAccountStep took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");
                return true;
            }
        };
    }

    /**
     * Sets the accountReversionProcessService (not to be confused with the AccountReversionService, which doesn't do a
     * process, but which does all the database stuff associated with AccountReversion records; it's off in Chart), which
     * allows the injection of an implementation of the service.
     * 
     * @param accountReversionProcessService the implementation of the accountReversionProcessService to set
     * @see org.kuali.kfs.gl.batch.service.AccountReversionProcessService
     */
    public void setAccountReversionProcessService(ReversionProcessService accountReversionProcessService) {
        this.accountReversionProcessService = accountReversionProcessService;
    }

    /**
     * Gets the yearEndService attribute. 
     * @return Returns the yearEndService.
     */
    public YearEndService getYearEndService() {
        return yearEndService;
    }

    /**
     * Sets the yearEndService attribute value.
     * @param yearEndService The yearEndService to set.
     */
    public void setYearEndService(YearEndService yearEndService) {
        this.yearEndService = yearEndService;
    }

    /**
     * Gets the organizationReversionProcessService attribute. 
     * @return Returns the organizationReversionProcessService.
     */
    public ReversionProcessService getAccountReversionProcessService() {
        return accountReversionProcessService;
    }
    
}
