package edu.cornell.kfs.fp.document.service.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.kuali.kfs.fp.businessobject.PaymentReasonCode;
import org.kuali.kfs.fp.document.service.DisbursementVoucherPaymentReasonService;
import org.kuali.kfs.fp.document.service.impl.DisbursementVoucherPaymentReasonServiceImpl;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.pdp.businessobject.PayeeType;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.kns.util.MessageList;

import edu.cornell.kfs.fp.service.SubmitTripWebServiceImpl;
import edu.cornell.kfs.module.cam.businessobject.lookup.CuAssetLookupableHelperServiceImpl;

@ConfigureContext(session = ccs1)
public class CuDisbursementVoucherPaymentReasonServiceImplTest extends KualiTestBase {
	private CuDisbursementVoucherPaymentReasonServiceImpl cuDisbursementVoucherPaymentReasonService;
	private DisbursementVoucherPaymentReasonServiceImpl disbursementVoucherPaymentReasonService;
	private Class cuDisbursementVoucherPaymentReasonServiceImplClass;
    private Class DisbursementVoucherPaymentReasonServiceImplClass;
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        disbursementVoucherPaymentReasonService = DisbursementVoucherPaymentReasonServiceImpl.class.newInstance();
        cuDisbursementVoucherPaymentReasonService = CuDisbursementVoucherPaymentReasonServiceImpl.class.newInstance();
        cuDisbursementVoucherPaymentReasonServiceImplClass = CuDisbursementVoucherPaymentReasonServiceImpl.class;  
        DisbursementVoucherPaymentReasonServiceImplClass = DisbursementVoucherPaymentReasonServiceImpl.class;
	}          
	
                  
	public void test(){
		
		Collection<PayeeType> payees = SpringContext.getBean(BusinessObjectService.class).findAll(PayeeType.class);
		Collection<PayeeType> payees2 = payees;		
       
       Collection<String> payeeTypeCds = new ArrayList<String>();
               
       payeeTypeCds.add("E");
       payeeTypeCds.add("F");
       payeeTypeCds.add("T");
       payeeTypeCds.add("V");
       payeeTypeCds.add("E");
       payeeTypeCds.add("F");
       payeeTypeCds.add("X");
       payeeTypeCds.add("S");
       
       


        
        Method eduMethod = null;
        Method orgMethod = null;
		try {
				eduMethod = cuDisbursementVoucherPaymentReasonServiceImplClass.getDeclaredMethod("getDescriptivePayeeTypesAsString", Collection.class);
				orgMethod = DisbursementVoucherPaymentReasonServiceImpl.class.getDeclaredMethod("getDescriptivePayeeTypesAsString", Collection.class);
				eduMethod.setAccessible(true);
				orgMethod.setAccessible(true);
				
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Object eduResult = null;
		Object orgResult = null;
        
		try {
			eduResult = eduMethod.invoke(cuDisbursementVoucherPaymentReasonService, payeeTypeCds);
			orgResult = orgMethod.invoke(disbursementVoucherPaymentReasonService, payeeTypeCds);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		if (eduResult.toString().length() >= orgResult.toString().length()) {
			fail("The edo method should return a smaller string");
		}
		

	}
    

}