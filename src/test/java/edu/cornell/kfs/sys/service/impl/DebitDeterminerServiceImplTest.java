package edu.cornell.kfs.fp.service.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.AccountingLineParser;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;

import com.rsmart.kuali.kfs.fp.FPPropertyConstants;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.service.impl.DebitDeterminerServiceImpl;
import edu.cornell.kfs.fp.service.SubmitTripWebServiceImpl;

@ConfigureContext(session = ccs1)
public class DebitDeterminerServiceImplTest extends KualiTestBase {
	private SubmitTripWebServiceImpl submitTripWebService;
	private DocumentService documentService;
	private DebitDeterminerServiceImpl debitDeterminerService;
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        documentService = SpringContext.getBean(DocumentService.class);
        debitDeterminerService = DebitDeterminerServiceImpl.class.newInstance();
	}
                  
	public void test(){
		
		CuDisbursementVoucherDocument dv = null;
		
        try {
            dv = (CuDisbursementVoucherDocument) SpringContext.getBean(DocumentService.class).getNewDocument(DisbursementVoucherDocument.class);
        }
        catch (WorkflowException e) {
            throw new RuntimeException("Error creating new disbursement voucher document: " + e.getMessage(), e);
        }
		
        if(dv != null) {
			dv.getDocumentHeader().setDocumentDescription("Test Document Description");
			dv.getDocumentHeader().setExplanation("Stuff");
			dv.getDocumentHeader().setOrganizationDocumentNumber("12345");
			
			dv.initiateDocument();
			
			Person traveler = SpringContext.getBean(PersonService.class).getPersonByPrincipalName("cab379");
			dv.templateEmployee(traveler);
			dv.setPayeeAssigned(true);
			
			dv.getDvPayeeDetail().setDisbVchrPaymentReasonCode("J");
			
			dv.setDisbVchrCheckTotalAmount(new KualiDecimal(11.00));
			dv.setDisbVchrPaymentMethodCode("P");

			dv.setDisbVchrCheckStubText("check text");
			
			SourceAccountingLine accountingLine = new SourceAccountingLine();								 
			
			Account account = new Account();
			Chart chart = new Chart();
			ObjectCode objectCode = new ObjectCode();
			
			//account.setAccountNumber("1000710");
			//chart.setCode("IT");
			//objectCode.setCode("6750");
			
			Map<String, String> pkValues = new HashMap<String, String>();
	        pkValues.put("accountNumber", "1000710");
	        pkValues.put("chartOfAccountsCode", "IT");
			
	        Map<String, String> pkValuesObj = new HashMap<String, String>();
	        pkValuesObj.put("financialObjectCode", "6750");
	        pkValuesObj.put("chartOfAccountsCode", "IT");
	        pkValuesObj.put("universityFiscalYear", "14");
	        
			account = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(Account.class, pkValues); 
			chart = SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(Chart.class, "IT");
			objectCode = SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(ObjectCode.class, pkValuesObj); 
			
			accountingLine.setAccount(account);
			accountingLine.setChart(chart);
			accountingLine.setObjectCode(objectCode);
			accountingLine.setAmount((new KualiDecimal(11.00)));
			
			dv.addSourceAccountingLine(accountingLine);
			
			try {
			documentService.saveDocument(dv);
			}
			catch (WorkflowException e) {
	            throw new RuntimeException("Error saving new disbursement voucher document: " + e.getMessage(), e);
	        }
			
			
			
			
		}			        
		
		List<GeneralLedgerPendingEntry> glpe = dv.getGeneralLedgerPendingEntries();
		List<GeneralLedgerPendingEntrySourceDetail> glpeS = dv.getGeneralLedgerPendingEntrySourceDetails();
		
		GeneralLedgerPendingEntry poster = glpe.get(0);
		GeneralLedgerPendingEntrySourceDetail postable = glpeS.get(0);
		
		System.out.println(poster.toString());
		System.out.println(postable.toString());
		
		debitDeterminerService.isDebitConsideringNothingPositiveOnly((GeneralLedgerPendingEntrySource) poster, postable);
		
	}
    

}
