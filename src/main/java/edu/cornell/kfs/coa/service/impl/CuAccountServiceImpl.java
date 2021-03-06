package edu.cornell.kfs.coa.service.impl;

import java.util.HashMap;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.impl.AccountServiceImpl;

public class CuAccountServiceImpl extends AccountServiceImpl {
    
    private static final String DEFAULT_BENEFIT_RATE_CATEGORY_CODE_BY_ACCOUNT_TYPE = "DEFAULT_BENEFIT_RATE_CATEGORY_CODE_BY_ACCOUNT_TYPE";
    private static final String DEFAULT_BENEFIT_RATE_CATEGORY_CODE = "DEFAULT_BENEFIT_RATE_CATEGORY_CODE";

    public String getDefaultLaborBenefitRateCategoryCodeForAccountType(String accountTypeCode) {
        String value = "";
        
        //TODO rewrite to use parameter evaluator service
        
        // make sure the parameter exists
        if (parameterService.parameterExists(Account.class, DEFAULT_BENEFIT_RATE_CATEGORY_CODE_BY_ACCOUNT_TYPE)) {
            // retrieve the value(s) for the parameter
            String paramValues = parameterService.getParameterValueAsString(Account.class, DEFAULT_BENEFIT_RATE_CATEGORY_CODE_BY_ACCOUNT_TYPE);

            // split the values of the parameter on the semi colon
            String[] paramValuesArray = paramValues.split(";");

            // load the array into a HashMap
            HashMap<String, String> paramValuesMap = new HashMap<String, String>();
            for (int i = 0; i < paramValuesArray.length; i++) {
                // create a new array to split on equals sign
                String[] tempArray = paramValuesArray[i].split("=");
                paramValuesMap.put(tempArray[0], tempArray[1]);
            }

            // check to see if the map has the account type code in it
            if (paramValuesMap.containsKey(accountTypeCode)) {
                value = paramValuesMap.get(accountTypeCode);
            } else {
                // make sure the system parameter exists
                if (parameterService.parameterExists(Account.class, DEFAULT_BENEFIT_RATE_CATEGORY_CODE)) {
                    value = parameterService.getParameterValueAsString(Account.class, DEFAULT_BENEFIT_RATE_CATEGORY_CODE);
                }
            }
        }
        return value;
    }

}
