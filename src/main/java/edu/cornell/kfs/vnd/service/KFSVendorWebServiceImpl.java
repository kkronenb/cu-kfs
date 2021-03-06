package edu.cornell.kfs.vnd.service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.common.util.Base64Utility;
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
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.bo.Attachment;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.maintenance.MaintenanceDocument;
import org.kuali.rice.krad.service.AttachmentService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.NoteService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;
import org.kuali.rice.krad.util.ObjectUtils;

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
	/**
	 * 
	 */
	// TODO : need to add poTransmissionMethodCode in web service params. 'name' in contact is also required
    //
    //  KFSUPGRADE-1017 method needs to be remediated so that document created will route successfully
    //
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
            
            vendorDoc.getDocumentHeader().setDocumentDescription("New vendor from Procurement tool");
                        
        	VendorMaintainableImpl vImpl = (VendorMaintainableImpl)vendorDoc.getNewMaintainableObject();

        	VendorDetail vDetail = (VendorDetail)vImpl.getBusinessObject();
        	
        	vDetail.setVendorName(vendorName);
        	vDetail.setActiveIndicator(true);
        	vDetail.setTaxableIndicator(isTaxable);

        	((VendorDetailExtension)vDetail.getExtension()).setEinvoiceVendorIndicator(isEInvoice);

        	// how should address be handled.  If no addres type code matched, then create, otherwise change ?
        	// how do we know that this is just to change the address type code.  should there is another filed, say 'oldAddressTypeCode' ?
        	vDetail.setVendorAddresses(getVendorAddresses(addresses, vDetail));

        	// also, question for contact, are we assume, the contact type is always "VI" ?
        	// should we handle like address ?
        	// Contact type does not have "VT" which is originally set up
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

        	docService.routeDocument(vendorDoc, "", null);
        	
            return vendorDoc.getDocumentNumber();
        } catch (Exception e) {
        	return "Failed request : "+ e.getMessage();
        } finally {
            GlobalVariables.setUserSession(actualUserSession);
            GlobalVariables.setMessageMap(globalErrorMap);
		}        
	}
  
	private List<VendorAddress> getVendorAddresses(List<VendorAddressParam> addresses, VendorDetail vDetail) {
    	List<VendorAddress> vAddrs = new ArrayList<VendorAddress>();
    	if (CollectionUtils.isNotEmpty(addresses)) {
			for (VendorAddressParam address : addresses) {
				LOG.info("addVendor address " + address);
				VendorAddress vendorAddr = new VendorAddress();
				setVendorAddress(address, vendorAddr, vDetail);
				vAddrs.add(vendorAddr);
			}
    	}
    	return vAddrs;
	}
	
	private List<VendorContact> getVendorContacts(List<VendorContactParam> contacts) {
    	ArrayList<VendorContact> vendorContacts = new ArrayList<VendorContact>();
    	if (CollectionUtils.isNotEmpty(contacts)) {
			for (VendorContactParam contact : contacts) {
				LOG.info("addVendor contact " + contact);
				VendorContact vContact = new VendorContact();
				setVendorContact(contact, vContact);
				vendorContacts.add(vContact);
			}
    	}
    	return vendorContacts;
	}
	
	private List<VendorPhoneNumber> getVendorPhoneNumbers(List<VendorPhoneNumberParam> phoneNumbers) {
    	List<VendorPhoneNumber> vendorPhoneNumbers = new ArrayList<VendorPhoneNumber>();
    	if (CollectionUtils.isNotEmpty(phoneNumbers)) {
	    	for (VendorPhoneNumberParam phoneNumber : phoneNumbers) {
		        LOG.info("addVendor phoneNumber " + phoneNumber);       
				VendorPhoneNumber vPhoneNumber = new VendorPhoneNumber();
				setVendorPhoneNumber(phoneNumber, vPhoneNumber);
		    	vendorPhoneNumbers.add(vPhoneNumber);
	   		
	    	}
    	}
    	return vendorPhoneNumbers;
	}
	
	private List<VendorSupplierDiversity> getVendorSupplierDiversitys(List<VendorSupplierDiversityParam> supplierDiversitys) {
		
		//
		//  method will need to be remediated to set vendorSupplierDiversityExpriationDate
		//  see KFSUPGRADE-1017
		//
		
    	List<VendorSupplierDiversity> vendorSupplierDiversitys = new ArrayList<VendorSupplierDiversity>();
    	if (CollectionUtils.isNotEmpty(supplierDiversitys)) {
	    	for (VendorSupplierDiversityParam diversity : supplierDiversitys) {
		        LOG.info("addVendor diversity " + diversity);       
	    		VendorSupplierDiversity vDiversity = new VendorSupplierDiversity();
	
	            vDiversity.setVendorSupplierDiversityCode(diversity.getVendorSupplierDiversityCode());
	            //TODO UPGRADE-911
	            //vDiversity.setVendorSupplierDiversityExpirationDate(new java.sql.Date(diversity.getVendorSupplierDiversityExpirationDate().getTime()));
	            vDiversity.setActive(diversity.isActive());
	            vendorSupplierDiversitys.add(vDiversity);
	   		
	    	}
    	}
    	return vendorSupplierDiversitys;
	}
	
	private void setVendorAddress(VendorAddressParam address,VendorAddress vendorAddr, VendorDetail vDetail) {
		vendorAddr.setVendorAddressTypeCode(address.getVendorAddressTypeCode());
		vendorAddr.setVendorLine1Address(address.getVendorLine1Address());
		vendorAddr.setVendorCityName(address.getVendorCityName());
		vendorAddr.setVendorStateCode(address.getVendorStateCode());
		vendorAddr.setVendorZipCode(address.getVendorZipCode());
		vendorAddr.setVendorCountryCode(address.getVendorCountryCode());
		vendorAddr.setVendorDefaultAddressIndicator(true);
		vendorAddr.setVendorDefaultAddressIndicator(address.isVendorDefaultAddressIndicator());
		if (address.isVendorDefaultAddressIndicator()) {
			// TODO : which one should be the vdetail's default address because there are different type address ???
        	vDetail.setDefaultAddressLine1(address.getVendorLine1Address());
        	vDetail.setDefaultAddressCity(address.getVendorCityName());
        	vDetail.setDefaultAddressStateCode(address.getVendorStateCode());
        	vDetail.setDefaultAddressPostalCode(address.getVendorZipCode());
        	vDetail.setDefaultAddressCountryCode(address.getVendorCountryCode());
			
		}
		// TODO : need to add poTransmissionMethodCode because it is
		// required if PO type
		//TODO UPGRADE-911
		//vendorAddr.setPurchaseOrderTransmissionMethodCode(address.getPurchaseOrderTransmissionMethodCode());
		vendorAddr.setVendorAddressEmailAddress(address.getVendorAddressEmailAddress());						
		vendorAddr.setVendorFaxNumber(address.getVendorFaxNumber());
		vendorAddr.setActive(address.isActive());
	}
	
	private void setVendorContact(VendorContactParam contact,VendorContact vContact) {
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
	
	private void setVendorPhoneNumber(VendorPhoneNumberParam phoneNumber,VendorPhoneNumber vPhoneNumber) {
    	vPhoneNumber.setVendorPhoneTypeCode(phoneNumber.getVendorPhoneTypeCode());
    	vPhoneNumber.setVendorPhoneNumber(phoneNumber.getVendorPhoneNumber());
    	vPhoneNumber.setVendorPhoneExtensionNumber(phoneNumber.getVendorPhoneExtensionNumber());
    	vPhoneNumber.setActive(phoneNumber.isActive());
	}
	
    //
    //  KFSUPGRADE-1017 method needs to be remediated so that document created will route successfully
    //	
	public String updateVendor(String vendorName, String vendorTypeCode, boolean isForeign, 
			String vendorNumber,  String ownershipTypeCode, boolean isTaxable, boolean isEInvoice,
			List<VendorAddressParam> addresses,List<VendorContactParam> contacts, List<VendorPhoneNumberParam> phoneNumbers,List<VendorSupplierDiversityParam> supplierDiversitys) throws Exception {
		UserSession actualUserSession = GlobalVariables.getUserSession();
		MessageMap globalErrorMap = GlobalVariables.getMessageMap();

		// create and route doc as system user
		GlobalVariables.setUserSession(new UserSession("kfs"));

		try {
			DocumentService docService = SpringContext.getBean(DocumentService.class);

			MaintenanceDocument vendorDoc = (MaintenanceDocument) docService.getNewDocument("PVEN");

			vendorDoc.getDocumentHeader().setDocumentDescription("Update vendor from Procurement tool");

			LOG.info("updateVendor " + vendorNumber);
			VendorDetail vendor = retrieveVendor(vendorNumber, "VENDORID");
			if (vendor != null) {
				// Vendor does not eist
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

			vDetail.setVendorHeader(vHeader);
			vImpl.setBusinessObject(vDetail);
			vendorDoc.setNewMaintainableObject(vImpl);

			docService.routeDocument(vendorDoc, "", null);

			return vendorDoc.getDocumentNumber();
        } catch (Exception e) {
        	LOG.info("updateVendor STE " + e.getStackTrace()+e.toString());
        	return "Failed request : "+ e.getMessage() + " " + e.toString();
		} finally {
			GlobalVariables.setUserSession(actualUserSession);
			GlobalVariables.setMessageMap(globalErrorMap);
		}
	}	
	
	private void updateVendorAddresses(List<VendorAddressParam> addresses, VendorDetail vendor, VendorDetail vDetail) {
    	if (CollectionUtils.isNotEmpty(addresses)) {
			for (VendorAddressParam address : addresses) {
				VendorAddress vendorAddr = new VendorAddress();
				LOG.info("updateVendor ADDRESS " + address +  "~"  + address.getVendorAddressTypeCode() + "~" + address.getVendorAddressGeneratedIdentifier());
				if (address.getVendorAddressGeneratedIdentifier() != null) {
					vendorAddr = getVendorAddress(vDetail, address.getVendorAddressGeneratedIdentifier());
				}
				setVendorAddress(address, vendorAddr, vDetail);
				
				if (vendorAddr.getVendorAddressGeneratedIdentifier() == null) {
					vDetail.getVendorAddresses().add(vendorAddr);
					vendor.getVendorAddresses().add(new VendorAddress()); 
				}
				// TODO : how about those existing addr, but not passed from request, should they be 'inactivated' ?
			}        	
    	}
	}

	private void updateVendorContacts(List<VendorContactParam> contacts, VendorDetail vendor, VendorDetail vDetail) {
    	if (CollectionUtils.isNotEmpty(contacts)) {
	    	for (VendorContactParam contact : contacts) {
				LOG.info("updateVendor contact " + contact +  "~" + contact.getVendorContactGeneratedIdentifier() + "~" + contact.getVendorContactName());
	        	VendorContact vContact = new VendorContact();
	        	if (contact.getVendorContactGeneratedIdentifier() != null) {
	        		vContact = getVendorContact(vDetail, contact.getVendorContactGeneratedIdentifier());
	        	}
				setVendorContact(contact, vContact);
	        	if (vContact.getVendorContactGeneratedIdentifier() == null) {
	            	vDetail.getVendorContacts().add(vContact);
				     vendor.getVendorContacts().add(new VendorContact());
	      		
	        	}
	        	// TODO : what to do with those existing contacts, but not passed from request
	   		
	    	}
    	}
	}

	private void updateVendorPhoneNumbers(List<VendorPhoneNumberParam> phoneNumbers, VendorDetail vendor, VendorDetail vDetail) {
    	if (CollectionUtils.isNotEmpty(phoneNumbers)) {
	    	for (VendorPhoneNumberParam phoneNumber : phoneNumbers) {
				LOG.info("updateVendor phoneNumber " + phoneNumber + "~" + phoneNumber.getVendorPhoneGeneratedIdentifier() + "~" 
						+ phoneNumber.getVendorPhoneTypeCode());
	    		VendorPhoneNumber vPhoneNumber = new VendorPhoneNumber();
	        	if (phoneNumber.getVendorPhoneGeneratedIdentifier() != null) {
	        		vPhoneNumber = getVendorPhoneNumber(vDetail, phoneNumber.getVendorPhoneGeneratedIdentifier());
	        	}
	        	setVendorPhoneNumber(phoneNumber, vPhoneNumber);
	        	if (vPhoneNumber.getVendorPhoneGeneratedIdentifier() == null) {
	            	vDetail.getVendorPhoneNumbers().add(vPhoneNumber);
	            	vendor.getVendorPhoneNumbers().add(new VendorPhoneNumber()); 
	      		
	        	}
        	// TODO : what to do with those existing contacts, but not passed from request
   		
	    	}
    	}
	}

	private void updateVendorSupplierDiversitys(List<VendorSupplierDiversityParam> supplierDiversitys, VendorDetail vendor, VendorDetail vDetail) {
    	ArrayList<VendorSupplierDiversity> vendorSupplierDiversitys = new ArrayList<VendorSupplierDiversity>();
    	if (CollectionUtils.isNotEmpty(supplierDiversitys)) {
	    	for (VendorSupplierDiversityParam diversity : supplierDiversitys) {
				LOG.info("updateVendor diversity " + diversity);
	    		VendorSupplierDiversity vDiversity = getVendorSupplierDiversity(vDetail.getVendorHeader(), diversity.getVendorSupplierDiversityCode());
	            boolean isExist = StringUtils.isNotBlank(vDiversity.getVendorSupplierDiversityCode());
	            vDiversity.setVendorSupplierDiversityCode(diversity.getVendorSupplierDiversityCode());
	            //TODO UPGRADE-911
	            //vDiversity.setVendorSupplierDiversityExpirationDate(new java.sql.Date(diversity.getVendorSupplierDiversityExpirationDate().getTime()));
	            vDiversity.setActive(diversity.isActive());
	            if (!isExist) {
	            	vDetail.getVendorHeader().getVendorSupplierDiversities().add(vDiversity);
	            	vendor.getVendorHeader().getVendorSupplierDiversities().add(new VendorSupplierDiversity());
	            }
	   		
	    	}
    	}
	}

	/**
	 * Return caret (^) delineated string of Vendor values
	 */
	public String retrieveKfsVendor(String vendorId, String vendorIdType) throws Exception {
		VendorDetail vendor = retrieveVendor(vendorId, vendorIdType);
				
		// TODO : this is not quite right because vendor may not be found
		//return vendor.getVendorNumber();
		if(StringUtils.equalsIgnoreCase(vendorIdType, "VENDORID")) {
			// TODO : this is for testing to get the list of vendorcontactgenerateddetailid
			String retVal = vendor != null ? vendor.getVendorNumber() : VENDOR_NOT_FOUND;
			if (vendor != null && CollectionUtils.isNotEmpty(vendor.getVendorContacts())) {
				for (VendorContact contact :vendor.getVendorContacts()) {
					retVal = retVal + "~" + contact.getVendorContactGeneratedIdentifier();
				}
				retVal = retVal + "p~";
				for (VendorPhoneNumber phoneNumber :vendor.getVendorPhoneNumbers()) {
					retVal = retVal + "~" + phoneNumber.getVendorPhoneGeneratedIdentifier();
				}
		    }
			return retVal;
		}
		return vendor != null ? vendor.getVendorNumber() : VENDOR_NOT_FOUND;
	}

	private VendorAddress getVendorAddress(VendorDetail vDetail, Integer vendorAddressGeneratedIdentifier) {
		for (VendorAddress vAddress : vDetail.getVendorAddresses()) {
			if (vendorAddressGeneratedIdentifier.equals(vAddress.getVendorAddressGeneratedIdentifier())) {
				return vAddress;
			}
		}
		return new VendorAddress();
	}

	private VendorContact getVendorContact(VendorDetail vDetail, Integer vendorContactGeneratedIdentifier) {
    	if (CollectionUtils.isNotEmpty(vDetail.getVendorContacts())) {
			for (VendorContact vContact : vDetail.getVendorContacts()) {
				if (vendorContactGeneratedIdentifier.equals(vContact.getVendorContactGeneratedIdentifier())) {
					return vContact;
				}
			}
		}
		return new VendorContact();
	}
	
	private VendorPhoneNumber getVendorPhoneNumber(VendorDetail vDetail, Integer vendorPhoneNumberGeneratedIdentifier) {
    	if (CollectionUtils.isNotEmpty(vDetail.getVendorPhoneNumbers())) {
		for (VendorPhoneNumber vPhoneNumber : vDetail.getVendorPhoneNumbers()) {
			if (vendorPhoneNumberGeneratedIdentifier.equals(vPhoneNumber.getVendorPhoneGeneratedIdentifier())) {
				return vPhoneNumber;
			}
		}
    	}
		return new VendorPhoneNumber();
	}
	
	private VendorSupplierDiversity getVendorSupplierDiversity(VendorHeader vHeader, String supplierDiversityCode) {
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
		// TODO : this is not quite right because vendor may not be found
		//return vendor.getVendorNumber();
		return vendor != null ? vendor.getVendorNumber() : VENDOR_NOT_FOUND;
	}
	
	/**
	 * 
	 * @param vendorId
	 * @param vendorIdType - DUNS, VENDORID, SSN, FEIN
	 * @return
	 * @throws Exception
	 */
	public boolean vendorExists(String vendorId, String vendorIdType) throws Exception {
		VendorDetail vendor = retrieveVendor(vendorId, vendorIdType);
		return ObjectUtils.isNotNull(vendor);
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
		if (StringUtils.equalsIgnoreCase(vendorIdType, "DUNS")) {
			vendor = vendorService.getVendorByDunsNumber(vendorId);
		} else if (StringUtils.equalsIgnoreCase(vendorIdType, "VENDORID")) {
			vendor = vendorService.getByVendorNumber(vendorId);
		} else if (StringUtils.equalsIgnoreCase(vendorIdType, "VENDORNAME")) {
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
		final String CARET = "^";

		if (ObjectUtils.isNotNull(vendor)) {
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

	public String uploadAttachment(String vendorId, String fileData, String fileName, String noteText) throws Exception {
		try {
			LOG.info("Starting uploadAttachment");
	        GlobalVariables.setUserSession(new UserSession("kfs"));
			VendorDetail vendor = SpringContext.getBean(VendorService.class).getVendorDetail(vendorId);
			if (vendor == null) {
				return VENDOR_NOT_FOUND;
			}
			byte[] fileDataBytes = Base64Utility.decode(fileData);
			LOG.info("call service to create attachment");
	        Attachment attachment = SpringContext.getBean(AttachmentService.class)
	        		.createAttachment(vendor, fileName, "application/pdf", fileDataBytes.length, new ByteArrayInputStream(fileDataBytes), null);
	       
	        Note newNote = new Note();
	        newNote.setNoteText(noteText);
	        newNote.setAttachment(attachment);
			LOG.info("create tempnote");
	
	        Note tmpNote = SpringContext.getBean(NoteService.class).createNote(newNote, vendor, GlobalVariables.getUserSession().getPrincipalId());
			LOG.info("save note");
	
	        SpringContext.getBean(NoteService.class).save(tmpNote);
	//		FileOutputStream bas64Out = new FileOutputStream("C:\\temp\\testBase64.pdf");
	//		bas64Out.write(Base64Utility.decode(vendorEin));
	//		bas64Out.close();
			return "upload Attachment ok";
		} catch (Exception e) {
			return "Failed request : " + e.getMessage();		}
	}
	
	public String uploadAtt(String vendorId,  @XmlMimeType("application/octet-stream")DataHandler fileData, String fileName, String noteText) throws Exception {
		try {
			LOG.info("Starting uploadAtt");
	        GlobalVariables.setUserSession(new UserSession("kfs"));
			VendorDetail vendor = SpringContext.getBean(VendorService.class).getVendorDetail(vendorId);
			if (vendor == null) {
				return VENDOR_NOT_FOUND;
			}
	        BufferedInputStream bin = new BufferedInputStream(fileData.getInputStream());
		 
	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	        int c;
	        while ((c = bin.read()) != -1) {
	        	buffer.write(c);
	        }
		                   
	        bin.close();
	        Attachment attachment = SpringContext.getBean(AttachmentService.class)
	        		.createAttachment(vendor, fileName, "application/pdf", buffer.toByteArray().length, fileData.getInputStream(), null);
	       
	        Note newNote = new Note();
	        newNote.setNoteText(noteText);
	        newNote.setAttachment(attachment);
			LOG.info("create tempnote");
	  
	        Note tmpNote = SpringContext.getBean(NoteService.class).createNote(newNote, vendor, GlobalVariables.getUserSession().getPrincipalId());
			LOG.info("save note");
	
	        SpringContext.getBean(NoteService.class).save(tmpNote);
	//		FileOutputStream bas64Out = new FileOutputStream("C:\\temp\\testBase64.pdf");
	//		bas64Out.write(Base64Utility.decode(vendorEin));
	//		bas64Out.close();
			return "upload Attachment ok";
		} catch (Exception e) {
			return "Failed request : " + e.getMessage();		
		}
	}

	public String retrieveKfsVendorByEin(String vendorEin) throws Exception {
		VendorHeader vendor = SpringContext.getBean(CUVendorService.class).getVendorByEin(vendorEin);
		if (vendor != null) {
			return vendor.getVendorHeaderGeneratedIdentifier() + "-0";
		} 
		return VENDOR_NOT_FOUND;
	}
	
}
