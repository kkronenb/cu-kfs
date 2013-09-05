package edu.cornell.kfs.vnd.service;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.bo.Note;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.NoteService;
import org.kuali.rice.kns.util.ErrorMessage;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;
import edu.cornell.kfs.vnd.document.service.CUVendorService;
import edu.cornell.kfs.vnd.service.params.VendorAddressParam;
import edu.cornell.kfs.vnd.service.params.VendorContactParam;
import edu.cornell.kfs.vnd.service.params.VendorPhoneNumberParam;
import edu.cornell.kfs.vnd.service.params.VendorSupplierDiversityParam;


/**
 *
 * <p>Title: KFSVendorWebServiceImpl</p>
 * <p>Description: Implements the webservice for KFS vendor management</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Cornell University: Kuali Financial Systems</p>
 * @author Dennis Friends
 * @version 1.0
 */
@MTOM
@WebService(endpointInterface = "edu.cornell.kfs.vnd.service.KFSVendorWebService")
public class KFSVendorWebServiceImpl implements KFSVendorWebService {

	private static final String VENDOR_NOT_FOUND ="Vendor Not Found";
    private static Logger LOG = Logger.getLogger(KFSVendorWebServiceImpl.class);
    private NoteService noteService;
    private VendorService vendorService;
    
	/**
	 * 
	 */
	public String addVendor(String vendorName, String vendorTypeCode, boolean isForeign, String taxNumber, String taxNumberType, String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
			                 List<VendorAddressParam> addresses,List<VendorContactParam> contacts, List<VendorPhoneNumberParam> phoneNumbers, List<VendorSupplierDiversityParam> supplierDiversitys) throws Exception {
        UserSession actualUserSession = GlobalVariables.getUserSession();
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();
        
        // create and route doc as system user
        GlobalVariables.setUserSession(new UserSession("kfs"));
        LOG.info("addVendor "+vendorName);       
        try {
        	DocumentService docService = SpringContext.getBean(DocumentService.class);
        	
            MaintenanceDocument vendorDoc = (MaintenanceDocument)docService.getNewDocument("PVEN");
            String docDesc = "New vendor - PT -'" +vendorName + "'";
            if (docDesc.length() > 40) {
            	docDesc = docDesc.substring(0,40);
            }
			vendorDoc.getDocumentHeader().setDocumentDescription(docDesc);
           
        	VendorMaintainableImpl vImpl = (VendorMaintainableImpl)vendorDoc.getNewMaintainableObject();

        	VendorDetail vDetail = (VendorDetail)vImpl.getBusinessObject();
        	
        	vDetail.setVendorName(vendorName);
        	vDetail.setActiveIndicator(true);
        	vDetail.setTaxableIndicator(isTaxable);
        	((VendorDetailExtension)vDetail.getExtension()).setEinvoiceVendorIndicator(isEInvoice);
        	vDetail.setVendorAddresses(getVendorAddresses(addresses, vDetail));
        	vDetail.setVendorContacts(getVendorContacts(contacts));
        	vDetail.setVendorPhoneNumbers(getVendorPhoneNumbers(phoneNumbers));

            VendorHeader vHeader = vDetail.getVendorHeader();
        	
        	vHeader.setVendorTypeCode(vendorTypeCode);
        	vHeader.setVendorTaxNumber(taxNumber);
        	vHeader.setVendorTaxTypeCode(taxNumberType);
        	vHeader.setVendorForeignIndicator(isForeign);
        	vHeader.setVendorOwnershipCode(ownershipTypeCode);

        	vHeader.setVendorSupplierDiversities(getVendorSupplierDiversitys(supplierDiversitys));
        	vDetail.setVendorHeader(vHeader);
        	vImpl.setBusinessObject(vDetail);
        	vendorDoc.setNewMaintainableObject(vImpl);

        	addNoteToVendor(vImpl, vendorDoc.getDocumentNumber());
          	docService.routeDocument(vendorDoc, "", null);
            return vendorDoc.getDocumentNumber();
        } catch (Exception e) {
            LOG.info("addVendor Failed request : "+ e.getMessage() + KFSConstants.NEWLINE + getValidationErrors(e));       
         	return "Failed request : "+ e.getMessage() + KFSConstants.NEWLINE + getValidationErrors(e);
        } finally {
            GlobalVariables.setUserSession(actualUserSession);
            GlobalVariables.setMessageMap(globalErrorMap);
		}        
	}
  
	/*
	 * get more helpful error message to return for business rule validation exception
	 */
	private String getValidationErrors(Exception e) {
        MessageMap globalErrorMap = GlobalVariables.getMessageMap();
        String errors = "";
    	if (e instanceof ValidationException) {
    		for (String key : globalErrorMap.getErrorMessages().keySet()) {
    			for (Object errorMsg : globalErrorMap.getErrorMessages().get(key)) {
    				String messageText = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(((ErrorMessage) errorMsg).getErrorKey());
       				errors = errors + replaceParams(messageText, ((ErrorMessage) errorMsg).getMessageParameters()) +  KFSConstants.NEWLINE;
    			}
    		}
    	}
    	return errors;
	
	}
	
	/*
	 * replace parameter holder with proper name or description
	 */
	private String replaceParams (String message, String[] params) {
		String retStr = message;
		for (int i =0; i < params.length; i++) {
			retStr = retStr.replace(CUKFSConstants.CURLY_BRACKET_LEFT + i + CUKFSConstants.CURLY_BRACKET_RIGHT, params[i]);
		}
		return retStr;
	}
	
	/*
	 * populate vendor address list from request address parameter
	 */
	private List<VendorAddress> getVendorAddresses(List<VendorAddressParam> addresses, VendorDetail vDetail) {
    	List<VendorAddress> vAddrs = new ArrayList<VendorAddress>();
    	if (CollectionUtils.isNotEmpty(addresses)) {
			for (VendorAddressParam address : addresses) {
				LOG.info("addVendor address " + address);
				VendorAddress vendorAddr = new VendorAddress();
				populateVendorAddress(address, vendorAddr, vDetail);
				vAddrs.add(vendorAddr);
			}
    	}
    	return vAddrs;
	}
	
	/*
	 * populate vendor contact list from request contact parameter
	 */
	private List<VendorContact> getVendorContacts(List<VendorContactParam> contacts) {
    	ArrayList<VendorContact> vendorContacts = new ArrayList<VendorContact>();
    	if (CollectionUtils.isNotEmpty(contacts)) {
			for (VendorContactParam contact : contacts) {
				LOG.info("addVendor contact " + contact);
				VendorContact vContact = new VendorContact();
				populateVendorContact(contact, vContact);
				vendorContacts.add(vContact);
			}
    	}
    	return vendorContacts;
	}
	
	/*
	 * populate vendor phonenumber list from request phonenumber parameter
	 */
	private List<VendorPhoneNumber> getVendorPhoneNumbers(List<VendorPhoneNumberParam> phoneNumbers) {
    	List<VendorPhoneNumber> vendorPhoneNumbers = new ArrayList<VendorPhoneNumber>();
    	if (CollectionUtils.isNotEmpty(phoneNumbers)) {
    	for (VendorPhoneNumberParam phoneNumber : phoneNumbers) {
	        LOG.info("addVendor phoneNumber "+phoneNumber);       
    		VendorPhoneNumber vPhoneNumber = new VendorPhoneNumber();
    		populateVendorPhoneNumber(phoneNumber, vPhoneNumber);
        	vendorPhoneNumbers.add(vPhoneNumber);
   		
    	}
    	}
    	return vendorPhoneNumbers;
	}
	
	/*
	 * populate vendor supplierdiversity list from request supplierdiversity parameter
	 */
	private List<VendorSupplierDiversity> getVendorSupplierDiversitys(List<VendorSupplierDiversityParam> supplierDiversitys) {
    	List<VendorSupplierDiversity> vendorSupplierDiversitys = new ArrayList<VendorSupplierDiversity>();
    	if (CollectionUtils.isNotEmpty(supplierDiversitys)) {
    	for (VendorSupplierDiversityParam diversity : supplierDiversitys) {
	        LOG.info("addVendor diversity "+diversity+"~"+diversity.getVendorSupplierDiversityCode()+"~"+diversity.getVendorSupplierDiversityExpirationDate());       
    		VendorSupplierDiversity vDiversity = new VendorSupplierDiversity();

            vDiversity.setVendorSupplierDiversityCode(diversity.getVendorSupplierDiversityCode());
            vDiversity.setVendorSupplierDiversityExpirationDate(new java.sql.Date(diversity.getVendorSupplierDiversityExpirationDate().getTime()));
            vDiversity.setActive(diversity.isActive());
            vendorSupplierDiversitys.add(vDiversity);
   		
    	}
    	}
    	return vendorSupplierDiversitys;
	}
	
	/*
	 * populate vendor address from request data
	 */
	private void populateVendorAddress(VendorAddressParam address,VendorAddress vendorAddr, VendorDetail vDetail) {
		vendorAddr.setVendorAddressTypeCode(address.getVendorAddressTypeCode());
		vendorAddr.setVendorLine1Address(address.getVendorLine1Address());
		vendorAddr.setVendorCityName(address.getVendorCityName());
		vendorAddr.setVendorStateCode(address.getVendorStateCode());
		vendorAddr.setVendorZipCode(address.getVendorZipCode());
		vendorAddr.setVendorCountryCode(address.getVendorCountryCode());
		vendorAddr.setVendorDefaultAddressIndicator(true);
		vendorAddr.setVendorDefaultAddressIndicator(address.isVendorDefaultAddressIndicator());
		if (address.isVendorDefaultAddressIndicator()) {
			// These vdetail's default address is not presisted in DB
        	vDetail.setDefaultAddressLine1(address.getVendorLine1Address());
        	vDetail.setDefaultAddressCity(address.getVendorCityName());
        	vDetail.setDefaultAddressStateCode(address.getVendorStateCode());
        	vDetail.setDefaultAddressPostalCode(address.getVendorZipCode());
        	vDetail.setDefaultAddressCountryCode(address.getVendorCountryCode());
			
		}
		vendorAddr.setPurchaseOrderTransmissionMethodCode(address.getPurchaseOrderTransmissionMethodCode());
		vendorAddr.setVendorAddressEmailAddress(address.getVendorAddressEmailAddress());						
		vendorAddr.setVendorFaxNumber(address.getVendorFaxNumber());
		vendorAddr.setActive(address.isActive());
	}
	
	/*
	 * populate vendor contact from request data
	 */
	private void populateVendorContact(VendorContactParam contact,VendorContact vContact) {
    	vContact.setVendorContactTypeCode(contact.getVendorContactTypeCode());
    	vContact.setVendorContactName(contact.getVendorContactName());
    	vContact.setVendorContactEmailAddress(contact.getVendorContactEmailAddress());
    	vContact.setVendorContactCommentText(contact.getVendorContactCommentText());
    	vContact.setVendorLine1Address(contact.getVendorLine1Address());
    	vContact.setVendorLine2Address(contact.getVendorLine2Address());
    	vContact.setVendorCityName(contact.getVendorCityName());
    	vContact.setVendorCountryCode(contact.getVendorCountryCode());
    	vContact.setVendorStateCode(contact.getVendorStateCode());
    	vContact.setVendorZipCode(contact.getVendorZipCode());
    	vContact.setActive(contact.isActive());

	}
	
	/*
	 * populate Vendor Phone Number from request data
	 */
	private void populateVendorPhoneNumber(VendorPhoneNumberParam phoneNumber,VendorPhoneNumber vPhoneNumber) {
    	vPhoneNumber.setVendorPhoneTypeCode(phoneNumber.getVendorPhoneTypeCode());
    	vPhoneNumber.setVendorPhoneNumber(phoneNumber.getVendorPhoneNumber());
    	vPhoneNumber.setVendorPhoneExtensionNumber(phoneNumber.getVendorPhoneExtensionNumber());
    	vPhoneNumber.setActive(phoneNumber.isActive());
	}
	
	public String updateVendor(String vendorName, String vendorTypeCode, boolean isForeign, String vendorNumber, 
			String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
			List<VendorAddressParam> addresses,List<VendorContactParam> contacts, List<VendorPhoneNumberParam> phoneNumbers,List<VendorSupplierDiversityParam> supplierDiversitys) throws Exception {
		UserSession actualUserSession = GlobalVariables.getUserSession();
		MessageMap globalErrorMap = GlobalVariables.getMessageMap();

		// create and route doc as system user
		GlobalVariables.setUserSession(new UserSession("kfs"));

		try {
			DocumentService docService = SpringContext.getBean(DocumentService.class);

			MaintenanceDocument vendorDoc = (MaintenanceDocument) docService.getNewDocument("PVEN");
            String docDesc = "Update vendor - PT -'" +vendorName + "'";
            if (docDesc.length() > 40) {
            	docDesc = docDesc.substring(0,40);
            }
			vendorDoc.getDocumentHeader().setDocumentDescription(docDesc);

			LOG.info("updateVendor " + vendorNumber);
				VendorDetail vendor = retrieveVendor(vendorNumber, "VENDORID");
				if (vendor != null) {
					// Vendor does not exist
				    VendorMaintainableImpl oldVendorImpl = (VendorMaintainableImpl) vendorDoc.getOldMaintainableObject();
				    oldVendorImpl.setBusinessObject(vendor);

				} else {
					// Vendor does not eist
					return "Vendor " + vendorNumber + " Not Found.";
				}
				
			VendorMaintainableImpl vImpl = (VendorMaintainableImpl) vendorDoc.getNewMaintainableObject();

			vImpl.setMaintenanceAction(KFSConstants.MAINTENANCE_EDIT_ACTION);
//			VendorDetail vendorCopy = (VendorDetail)ObjectUtils.deepCopy(vendor);
			vImpl.setBusinessObject((VendorDetail)ObjectUtils.deepCopy(vendor));
			VendorDetail vDetail = (VendorDetail) vImpl.getBusinessObject();

			vDetail.setVendorName(vendorName);
			vDetail.setActiveIndicator(true);
			vDetail.setTaxableIndicator(isTaxable);

			((VendorDetailExtension) vDetail.getExtension()).setEinvoiceVendorIndicator(isEInvoice);

			
        	updateVendorAddresses(addresses, vendor, vDetail);
//			vDetail.setVendorAddresses(vAddrs);

             updateVendorContacts(contacts, vendor, vDetail);
        	updateVendorPhoneNumbers(phoneNumbers, vendor, vDetail);

        	updateVendorSupplierDiversitys(supplierDiversitys, vendor, vDetail);

			VendorHeader vHeader = vDetail.getVendorHeader();

			vHeader.setVendorTypeCode(vendorTypeCode);
			vHeader.setVendorForeignIndicator(isForeign);
			vHeader.setVendorOwnershipCode(ownershipTypeCode);

//			vDetail.setVendorHeader(vHeader);
			vImpl.setBusinessObject(vDetail);
			vendorDoc.setNewMaintainableObject(vImpl);

        	addNoteToVendor(vImpl, vendorDoc.getDocumentNumber());
        	addNoteForEditToVendor(vImpl, vendor, vDetail);
			docService.routeDocument(vendorDoc, "", null);
			return vendorDoc.getDocumentNumber();
        } catch (Exception e) {
        	LOG.info("updateVendor STE " + e.getMessage() + KFSConstants.NEWLINE + getValidationErrors(e));
        	return "Failed request : "+ e.getMessage() + KFSConstants.NEWLINE + getValidationErrors(e);
		} finally {
			GlobalVariables.setUserSession(actualUserSession);
			GlobalVariables.setMessageMap(globalErrorMap);
		}
	}	
	
	/*
	 * update vendor addresses from request address data
	 */
	private void updateVendorAddresses(List<VendorAddressParam> addresses, VendorDetail vendor, VendorDetail vDetail) {
    	if (CollectionUtils.isNotEmpty(addresses)) {
		for (VendorAddressParam address : addresses) {
			VendorAddress vendorAddr = new VendorAddress();
			LOG.info("updateVendor ADDRESS " + address+ "~" +address.getVendorAddressTypeCode()+ "~" + address.getVendorAddressGeneratedIdentifier());
			if (address.getVendorAddressGeneratedIdentifier() != null) {
				vendorAddr = getExistingVendorAddress(vDetail, address.getVendorAddressGeneratedIdentifier());
			}
			populateVendorAddress(address, vendorAddr, vDetail);
			
			if (vendorAddr.getVendorAddressGeneratedIdentifier() == null) {
			     vDetail.getVendorAddresses().add(vendorAddr);
			     vendor.getVendorAddresses().add(new VendorAddress()); // oldobj
			}
		}        	
    	}
	}

	/*
	 * update vendor contacts from request contact data
	 */
	private void updateVendorContacts(List<VendorContactParam> contacts, VendorDetail vendor, VendorDetail vDetail) {
		if (CollectionUtils.isNotEmpty(contacts)) {
			for (VendorContactParam contact : contacts) {
				LOG.info("updateVendor contact " + contact + "~" + contact.getVendorContactGeneratedIdentifier() + "~"
						+ contact.getVendorContactName());
				VendorContact vContact = new VendorContact();
				if (contact.getVendorContactGeneratedIdentifier() != null) {
					vContact = getExistingVendorContact(vDetail, contact.getVendorContactGeneratedIdentifier());
				}
				populateVendorContact(contact, vContact);
				if (vContact.getVendorContactGeneratedIdentifier() == null) {
					vDetail.getVendorContacts().add(vContact);
					vendor.getVendorContacts().add(new VendorContact()); // oldobj

				}

			}
		}
	}

	
	/*
	 * update vendor Phone numbers from request Phone numbers data
	 */
	private void updateVendorPhoneNumbers(List<VendorPhoneNumberParam> phoneNumbers, VendorDetail vendor, VendorDetail vDetail) {
		if (CollectionUtils.isNotEmpty(phoneNumbers)) {
			for (VendorPhoneNumberParam phoneNumber : phoneNumbers) {
				LOG.info("updateVendor phoneNumber " + phoneNumber + "~" + phoneNumber.getVendorPhoneGeneratedIdentifier() + "~"
						+ phoneNumber.getVendorPhoneTypeCode());
				VendorPhoneNumber vPhoneNumber = new VendorPhoneNumber();
				if (phoneNumber.getVendorPhoneGeneratedIdentifier() != null) {
					vPhoneNumber = getExistingVendorPhoneNumber(vDetail, phoneNumber.getVendorPhoneGeneratedIdentifier());
				}
				populateVendorPhoneNumber(phoneNumber, vPhoneNumber);
				if (vPhoneNumber.getVendorPhoneGeneratedIdentifier() == null) {
					vDetail.getVendorPhoneNumbers().add(vPhoneNumber);
					vendor.getVendorPhoneNumbers().add(new VendorPhoneNumber()); // oldobj

				}

			}
		}
	}

	/*
	 * update vendor supplier diversity from request supplier diversity data
	 */
	private void updateVendorSupplierDiversitys(List<VendorSupplierDiversityParam> supplierDiversitys, VendorDetail vendor, VendorDetail vDetail) {
    	if (CollectionUtils.isNotEmpty(supplierDiversitys)) {
    	for (VendorSupplierDiversityParam diversity : supplierDiversitys) {
			LOG.info("updateVendor diversity " + diversity+"~"+diversity.getVendorSupplierDiversityCode()+"~"+diversity.getVendorSupplierDiversityExpirationDate());
    		VendorSupplierDiversity vDiversity = getExistingVendorSupplierDiversity(vDetail.getVendorHeader(), diversity.getVendorSupplierDiversityCode());
            boolean isExist = StringUtils.isNotBlank(vDiversity.getVendorSupplierDiversityCode());
            vDiversity.setVendorSupplierDiversityCode(diversity.getVendorSupplierDiversityCode());
            vDiversity.setVendorSupplierDiversityExpirationDate(new java.sql.Date(diversity.getVendorSupplierDiversityExpirationDate().getTime()));
            vDiversity.setActive(diversity.isActive());
            if (!isExist) {
            	vDetail.getVendorHeader().getVendorSupplierDiversities().add(vDiversity);
            	vendor.getVendorHeader().getVendorSupplierDiversities().add(new VendorSupplierDiversity());
            }
   		
    	}
    	}
	}

	/*
	 * get existing vendor address that matched the address generated id from parameter
	 */
	private VendorAddress getExistingVendorAddress(VendorDetail vDetail, Integer vendorAddressGeneratedIdentifier) {
		for (VendorAddress vAddress : vDetail.getVendorAddresses()) {
			if (vendorAddressGeneratedIdentifier.equals(vAddress.getVendorAddressGeneratedIdentifier())) {
				return vAddress;
			}
		}
		return new VendorAddress();
	}

	/*
	 * get existing vendor contact that matched the contact generated id from parameter
	 */
	private VendorContact getExistingVendorContact(VendorDetail vDetail, Integer vendorContactGeneratedIdentifier) {
    	if (CollectionUtils.isNotEmpty(vDetail.getVendorContacts())) {
		for (VendorContact vContact : vDetail.getVendorContacts()) {
			if (vendorContactGeneratedIdentifier.equals(vContact.getVendorContactGeneratedIdentifier())) {
				return vContact;
			}
		}}
		return new VendorContact();
	}
	
	/*
	 * get existing vendor phonenumber that matched the phonenumber generated id from parameter
	 */
	private VendorPhoneNumber getExistingVendorPhoneNumber(VendorDetail vDetail, Integer vendorPhoneNumberGeneratedIdentifier) {
    	if (CollectionUtils.isNotEmpty(vDetail.getVendorPhoneNumbers())) {
		for (VendorPhoneNumber vPhoneNumber : vDetail.getVendorPhoneNumbers()) {
			if (vendorPhoneNumberGeneratedIdentifier.equals(vPhoneNumber.getVendorPhoneGeneratedIdentifier())) {
				return vPhoneNumber;
			}
		}
    	}
		return new VendorPhoneNumber();
	}
	
	/*
	 * get existing vendor supplierdiversity that matched the vendor supplier diversity code from parameter
	 */
	private VendorSupplierDiversity getExistingVendorSupplierDiversity(VendorHeader vHeader, String supplierDiversityCode) {
    	if (CollectionUtils.isNotEmpty(vHeader.getVendorSupplierDiversities())) {
		for (VendorSupplierDiversity vSupplierDiversity : vHeader.getVendorSupplierDiversities()) {
			if (StringUtils.equals(vSupplierDiversity.getVendorSupplierDiversityCode(), supplierDiversityCode)) {
				return vSupplierDiversity;
			}
		}
    	}
		return new VendorSupplierDiversity();
	}

	/**
	 * 
	 * @param vendorName
	 * @param lastFour
	 * @return
	 * @throws Exception
	 */
	public String retrieveKfsVendorByNamePlusLastFour(String vendorName, String lastFour) throws Exception {
		VendorDetail vendor = SpringContext.getBean(CUVendorService.class).getVendorByNamePlusLastFourOfTaxID(vendorName, lastFour);
		return vendor != null ? vendor.getVendorNumber() : VENDOR_NOT_FOUND;
	}
	
	/**
	 * 
	 * @param vendorId
	 * @param vendorIdType
	 * @return
	 * @throws Exception
	 */
	private VendorDetail retrieveVendor(String vendorId, String vendorIdType) throws Exception {
		VendorDetail vendor = null;
		CUVendorService vendorService = SpringContext.getBean(CUVendorService.class);
		if(StringUtils.equalsIgnoreCase(vendorIdType, "DUNS")) {
			vendor = vendorService.getVendorByDunsNumber(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORID")) {
			vendor = vendorService.getByVendorNumber(vendorId);
		} else if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORNAME")) {
			vendor = vendorService.getVendorByVendorName(vendorId);
		}
		return vendor;
	}

	
	
	/**
	 * 
	 * @param vendor
	 * @return
	 */
	private String buildVendorString(VendorDetail vendor) {
		StringBuffer vendorValues = new StringBuffer();
		String CARET = "^";

		if(ObjectUtils.isNotNull(vendor)) {
			VendorDetailExtension vdExtension = (VendorDetailExtension)vendor.getExtension();
			
			vendorValues.append(vendor.getVendorNumber()).append(CARET);
			vendorValues.append(vendor.getVendorName()).append(CARET);
			String taxID = vendor.getVendorHeader().getVendorTaxNumber();
			String maskedTaxID = "*****"+taxID.substring(taxID.length()-4, taxID.length());
			vendorValues.append(maskedTaxID).append(CARET);
			vendorValues.append(vendor.getVendorHeader().getVendorTaxTypeCode()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressLine1()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressLine2()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressCity()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressStateCode()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressPostalCode()).append(CARET);
			vendorValues.append(vendor.getDefaultAddressCountryCode()).append(CARET);
			vendorValues.append(vendor.getDefaultFaxNumber()).append(CARET);
			vendorValues.append(vdExtension.isEinvoiceVendorIndicator());
		}		
		return vendorValues.toString();
	}

	

	public String retrieveKfsVendorByEin(String vendorEin) throws Exception {
		VendorHeader vendor = SpringContext.getBean(CUVendorService.class).getVendorByEin(vendorEin);
		if (vendor != null) {
			return vendor.getVendorHeaderGeneratedIdentifier() + "-0";
		} 
		return VENDOR_NOT_FOUND;
	}
	
    private void addNoteToVendor(Maintainable maintainable, String documentNumber) {
        String noteText = StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, maintainable.getMaintenanceAction()) ?
        		"Change vendor document ID " + documentNumber : "Add vendor document ID " + documentNumber;
        addVendorNote(maintainable, noteText);;
    }

    private void addVendorNote(Maintainable maintainable, String noteText) {
        Note newBONote = new Note();
      newBONote.setNoteText(noteText);
        try {
            newBONote = getNoteService().createNote(newBONote, maintainable.getBusinessObject());

         //   getNoteService().save(newBONote);
        }
        catch (Exception e) {
            throw new RuntimeException("Caught Exception While Trying To Add Note to Vendor", e);
        }
        maintainable.getBusinessObject().getBoNotes().add(newBONote);      

    }
    
    private void addNoteForEditToVendor(Maintainable maintainable,VendorDetail oldVDtl, VendorDetail newVDtl) {
    	
    	StringBuffer sb = new StringBuffer();
    	 
    	if (!oldVDtl.getVendorHeader().isEqualForRouting(newVDtl.getVendorHeader())) {
    		sb.append("Vendor Header edited, ");
    	}
    	if (!oldVDtl.isEqualForRouting(newVDtl)) {
    		sb.append("Vendor detail edited, ");
    	}
     	if (!getVendorService().equalMemberLists(oldVDtl.getVendorAddresses(), newVDtl.getVendorAddresses())) {
    		sb.append("Address tab edited , ");
    	}
    	if (!getVendorService().equalMemberLists(oldVDtl.getVendorContacts(), newVDtl.getVendorContacts())) {
    		sb.append("Contact tab edited, ");
    	}
    	if (!getVendorService().equalMemberLists(oldVDtl.getVendorPhoneNumbers(), newVDtl.getVendorPhoneNumbers())) {
    		sb.append("Phone Number tab edited, ");
    	}
    	if (!getVendorService().equalMemberLists(oldVDtl.getVendorHeader().getVendorSupplierDiversities(), newVDtl.getVendorHeader().getVendorSupplierDiversities())) {
    		sb.append("Supplier Diversity tab edited, ");
    	}
	
    	if (StringUtils.isNotBlank(sb.toString())) {
            addVendorNote(maintainable, sb.toString().substring(0, sb.toString().lastIndexOf(",")));;    		
    	}
    }
    
	public NoteService getNoteService() {
		if (noteService == null) {
			noteService = SpringContext.getBean(NoteService.class);
		}
		return noteService;
	}

	public VendorService getVendorService() {
		if (vendorService == null) {
			vendorService = SpringContext.getBean(VendorService.class);
		}
		return vendorService;
	}

}
