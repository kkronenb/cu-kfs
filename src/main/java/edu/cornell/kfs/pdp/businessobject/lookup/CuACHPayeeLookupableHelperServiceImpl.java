package edu.cornell.kfs.pdp.businessobject.lookup;

import java.util.Map;

import org.kuali.kfs.pdp.businessobject.lookup.ACHPayeeLookupableHelperServiceImpl;
import org.kuali.rice.kim.impl.KIMPropertyConstants;

public class CuACHPayeeLookupableHelperServiceImpl extends ACHPayeeLookupableHelperServiceImpl {
	

    @Override
    protected Map<String, String> getPersonFieldValues(Map<String, String> fieldValues) {
        // TODO Auto-generated method stub
        Map<String, String> personFieldValues = super.getPersonFieldValues(fieldValues);

        // add entity
        personFieldValues.put(KIMPropertyConstants.Person.ENTITY_ID, fieldValues.get(KIMPropertyConstants.Person.ENTITY_ID));

        return personFieldValues;
    }

}
