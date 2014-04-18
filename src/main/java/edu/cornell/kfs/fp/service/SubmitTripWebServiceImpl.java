package edu.cornell.kfs.fp.service;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.DistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.affiliation.EntityAffiliationContract;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.kuali.rice.kns.service.DocumentHelperService;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.document.DocumentAuthorizer;
import org.kuali.rice.krad.service.DataDictionaryService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

/**
 *
 * <p>Title: SubmitTripWebServiceImpl</p>
 * <p>Description: Implements the webservice for travel's trip submission</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Dennis Friends
 * @author Sandy Eccleston
 * @version 1.0
 */
@WebService(endpointInterface = "edu.cornell.kfs.fp.service.SubmitTripWebService")
public class SubmitTripWebServiceImpl implements SubmitTripWebService {

	public static final String DISBURSEMENT_VOUCHER = "Disbursement Voucher";
	public static final String DISTRIBUTION_INCOME_EXPENSE = "Distribution of Income and Expense";
	
	/**
	 * 
	 */
	public String submitTrip(String dvDescription, String dvExplanation, String tripNumber, String travelerNetId, String initiatorNetId, double totalAmount, String checkStubText) throws Exception {
        UserSession actualUserSession = GlobalVariables.getUserSession();
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();
        
        try {
	        if(totalAmount > 0.00) {
	        	return buildDisbursementVoucher(dvDescription, dvExplanation, tripNumber, travelerNetId, initiatorNetId, totalAmount, checkStubText);
	        } else {
	        	return buildDistributionIncomeExpenseDocument(dvDescription, dvExplanation, tripNumber, initiatorNetId);
	        }
		} finally {
            GlobalVariables.setUserSession(actualUserSession);
            GlobalVariables.setMessageMap(globalErrorMap);
		}        
	}
  
	/**
	 * NOTE: The current document names that are supported by KFS are 
	 * - Disbursement Voucher
	 * - Distribution of Income and Expense
	 * 
	 * 
	 */
	public boolean isValidDocumentInitiator(String initiatorNetId, String documentName) throws Exception {
		Person initiator = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(initiatorNetId);
		Class docClass;
		if(StringUtils.equalsIgnoreCase(documentName, "Disbursement Voucher")) {
			docClass = DisbursementVoucherDocument.class;
		} else if(StringUtils.equalsIgnoreCase(documentName, "Distribution of Income and Expense")) {
			docClass = DistributionOfIncomeAndExpenseDocument.class;
		}
        String documentTypeName = SpringContext.getBean(DataDictionaryService.class).getDocumentTypeNameByClass(DisbursementVoucherDocument.class);
        DocumentAuthorizer documentAuthorizer = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(documentTypeName);

		return documentAuthorizer.canInitiate(documentTypeName, initiator);
	}

	/**
	 * 
	 * @param viewerNetId
	 * @param docID
	 * @return
	 * @throws Exception
	 */
	public boolean canViewKfsDocument(String viewerNetId, String docID) throws Exception {
		Person viewer = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(viewerNetId);
        Document document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderIdSessionless(docID);
        DocumentAuthorizer documentAuthorizer = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(document);

		return documentAuthorizer.canOpen(document, viewer);
	}

	/**
	 * 
	 * @param dvDescription
	 * @param dvExplanation
	 * @param tripNumber
	 * @param travelerNetId
	 * @param initiatorNetId
	 * @param totalAmount
	 * @param checkStubText
	 * @return
	 * @throws Exception
	 */
	private String buildDisbursementVoucher(String dvDescription, String dvExplanation, String tripNumber, String travelerNetId, String initiatorNetId, double totalAmount, String checkStubText) throws Exception {
        try {
        	if(!isValidDocumentInitiator(initiatorNetId, DISBURSEMENT_VOUCHER)) {
        		throw new RuntimeException("Initiator identified does not have permission to create a DV.");
        	}
        } catch (Exception ex) {
    		throw new RuntimeException("Initiator identified does not have permission to create a DV.", ex);
        }
        
        // create and route doc as system user
        GlobalVariables.setUserSession(new UserSession(initiatorNetId));

        MessageMap documentErrorMap = new MessageMap();
        GlobalVariables.setMessageMap(documentErrorMap);

        // Create document with description provided
		CuDisbursementVoucherDocument dvDoc = null;
		
        try {
            dvDoc = (CuDisbursementVoucherDocument) SpringContext.getBean(DocumentService.class).getNewDocument(DisbursementVoucherDocument.class);
        }
        catch (WorkflowException e) {
            throw new RuntimeException("Error creating new disbursement voucher document: " + e.getMessage(), e);
        }
	  
        if(dvDoc != null) {
			dvDoc.getDocumentHeader().setDocumentDescription(dvDescription);
			dvDoc.getDocumentHeader().setExplanation(dvExplanation);
			dvDoc.getDocumentHeader().setOrganizationDocumentNumber(tripNumber);
			
			dvDoc.initiateDocument();

			// Set vendor to traveler using netID provided
			Person traveler = SpringContext.getBean(PersonService.class).getPersonByPrincipalName(travelerNetId);

	        for(EntityAffiliationContract entityAffiliation : ((PersonImpl)traveler).getAffiliations()) {
	        	if(entityAffiliation.isDefaultValue()) {
	       
		    		if(StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.STUDENT)) {
		        		dvDoc.templateStudent(traveler);
		    		}
		    		else if(StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.ALUMNI)) {
		        		dvDoc.templateAlumni(traveler);
		    		}
		    		else if(StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.FACULTY) ||
		    				StringUtils.equalsIgnoreCase(entityAffiliation.getAffiliationType().getCode(), CuDisbursementVoucherConstants.PayeeAffiliations.STAFF)) {
		        		dvDoc.templateEmployee(traveler);
		    		}
	        	}
	        }
			dvDoc.setPayeeAssigned(true);
			
	        dvDoc.getDvPayeeDetail().setDisbVchrPaymentReasonCode("J");
			
			dvDoc.setDisbVchrCheckTotalAmount(new KualiDecimal(totalAmount));
			dvDoc.setDisbVchrPaymentMethodCode("P");

			dvDoc.setDisbVchrCheckStubText(checkStubText);
			
			// Persist document
			SpringContext.getBean(DocumentService.class).saveDocument(dvDoc);
			
			return dvDoc.getDocumentNumber();
        } else {
        	return "";
        }
	}

	/**
	 * 
	 * @param diDescription
	 * @param diExplanation
	 * @param tripNumber
	 * @param initiatorNetId
	 * @return
	 * @throws Exception
	 */
	private String buildDistributionIncomeExpenseDocument(String diDescription, String diExplanation, String tripNumber, String initiatorNetId) throws Exception {
        try {
        	if(!isValidDocumentInitiator(initiatorNetId, DISTRIBUTION_INCOME_EXPENSE)) {
        		throw new RuntimeException("Initiator identified does not have permission to create a DI.");
        	}
        } catch (Exception ex) {
    		throw new RuntimeException("Initiator identified does not have permission to create a DI.", ex);
        }
        
        // create and route doc as system user
        GlobalVariables.setUserSession(new UserSession(initiatorNetId));

        MessageMap documentErrorMap = new MessageMap();
        GlobalVariables.setMessageMap(documentErrorMap);

        // Create document with description provided
		DistributionOfIncomeAndExpenseDocument diDoc = null;
		
        try {
            diDoc = (DistributionOfIncomeAndExpenseDocument) SpringContext.getBean(DocumentService.class).getNewDocument(DistributionOfIncomeAndExpenseDocument.class);
        }
        catch (WorkflowException e) {
            throw new RuntimeException("Error creating new disbursement voucher document: " + e.getMessage(), e);
        }
	  
        if(diDoc != null) {
        	diDoc.getDocumentHeader().setDocumentDescription(diDescription);
        	diDoc.getDocumentHeader().setExplanation(diExplanation);
        	diDoc.getDocumentHeader().setOrganizationDocumentNumber(tripNumber);
			
			// Persist document
			SpringContext.getBean(DocumentService.class).saveDocument(diDoc);
			
			return diDoc.getDocumentNumber();
        } else {
        	return "";
        }
	}

}
