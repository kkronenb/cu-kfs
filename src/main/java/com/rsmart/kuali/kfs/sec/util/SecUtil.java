/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.sec.util;

import java.util.List;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.GlobalVariables;

import com.rsmart.kuali.kfs.sec.service.AccessSecurityService;

public class SecUtil {

    /**
     * Compares the size of the given list against the given previous size and if different adds an info message
     * 
     * @param previousListSize int giving previous size of list to compare to
     * @param results List to get size for and compare
     * @param messageKey String key of message that should be added
     */
    public static void compareListSizeAndAddMessageIfChanged(int previousListSize, List results, String messageKey) {
        int currentListSize = results.size();

        if (previousListSize != currentListSize) {
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, messageKey, (String) null);
        }
    }

    /**
     * Calls access security service to check view access on given GLPE for current user. Access to view the GLPE on the document should be related to the view permissions for an
     * accounting line with the same account attributes. Called from generalLedgerPendingEntries.tag
     * 
     * @param pendingEntry GeneralLedgerPendingEntry to check access for
     * @return boolean true if current user has view permission, false otherwise
     */
    public static boolean canViewGLPE(Document document, GeneralLedgerPendingEntry pendingEntry) {
        boolean canView = true;

        if (document instanceof AccountingDocument) {
            AccountingLine line = new SourceAccountingLine();

            line.setPostingYear(pendingEntry.getUniversityFiscalYear());
            line.setChartOfAccountsCode(pendingEntry.getChartOfAccountsCode());
            line.setAccountNumber(pendingEntry.getAccountNumber());
            line.setSubAccountNumber(pendingEntry.getSubAccountNumber());
            line.setFinancialObjectCode(pendingEntry.getFinancialObjectCode());
            line.setFinancialSubObjectCode(pendingEntry.getFinancialSubObjectCode());
            line.setProjectCode(pendingEntry.getProjectCode());

            line.refreshNonUpdateableReferences();

            canView = SpringContext.getBean(AccessSecurityService.class).canViewDocumentAccountingLine((AccountingDocument) document, line, GlobalVariables.getUserSession().getPerson());
        }

        return canView;
    }
}