package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.BudgetAdjustmentDocument;
import org.kuali.kfs.fp.document.authorization.BudgetAdjustmentDocumentPresentationController;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.KRADConstants;

public class CuBudgetAdjustmentDocumentPresentationController extends BudgetAdjustmentDocumentPresentationController {
    @Override
    public Set<String> getDocumentActions(Document document) {
        Set<String> documentActions = super.getDocumentActions(document);

        BudgetAdjustmentDocument budgetAdjustmentDocument = (BudgetAdjustmentDocument) document;
        String docInError = budgetAdjustmentDocument.getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber();
        
        if (StringUtils.isNotBlank(docInError)) {
            Boolean allowBlanketApproveNoRequest = getParameterService().getParameterValueAsBoolean(
                    KRADConstants.KNS_NAMESPACE, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                    KRADConstants.SystemGroupParameterNames.ALLOW_ENROUTE_BLANKET_APPROVE_WITHOUT_APPROVAL_REQUEST_IND);
            if (allowBlanketApproveNoRequest != null && allowBlanketApproveNoRequest.booleanValue()) {
                documentActions.add(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
            }
        }
        return documentActions;
    }


}
