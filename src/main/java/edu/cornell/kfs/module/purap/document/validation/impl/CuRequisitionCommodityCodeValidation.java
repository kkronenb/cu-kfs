/**
 * 
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PurchasingCommodityCodeValidation;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;


/**
 * @author dwf5
 *
 */
public class CuRequisitionCommodityCodeValidation extends PurchasingCommodityCodeValidation {

    /**
     * This method overrides the parent method to implement some parameter checking that is unique
     * to the Requisition.
     * 
     * @return
     */
	protected boolean commodityCodeIsRequired(PurApItem item) {
    	// commodity code is not an attribute for below the line items.
    	if (!item.getItemType().isLineItemIndicator()) {
        	return false;
        }

        // if the ENABLE_COMMODITY_CODE_IND parameter is  N then we don't
        // need to check for the ITEMS_REQUIRE_COMMODITY_CODE_IND parameter anymore, just return false. 
        boolean enableCommodityCode = SpringContext.getBean(ParameterService.class)
                .getParameterValueAsBoolean(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_COMMODITY_CODE_IND);
        if (!enableCommodityCode) {
            return false;
        } else {        
            return SpringContext.getBean(ParameterService.class).getParameterValueAsBoolean(RequisitionDocument.class,
                    PurapRuleConstants.ITEMS_REQUIRE_COMMODITY_CODE_IND);
        }
    }

}
