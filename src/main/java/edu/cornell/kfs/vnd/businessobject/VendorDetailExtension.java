package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

public class VendorDetailExtension extends PersistableBusinessObjectExtensionBase {
	private static final long serialVersionUID = 2L;

	protected Integer vendorHeaderGeneratedIdentifier;
    protected Integer vendorDetailAssignedIdentifier;
    protected boolean einvoiceVendorIndicator;
    protected String  defaultB2BPaymentMethodCode;
    
    protected boolean insuranceRequiredIndicator;
    protected Boolean insuranceRequirementsCompleteIndicator;
    protected Boolean cornellAdditionalInsuredIndicator;
    protected KualiDecimal generalLiabilityCoverageAmount;
    protected Date generalLiabilityExpiration;
    protected KualiDecimal automobileLiabilityCoverageAmount;
    protected Date automobileLiabilityExpiration;
    protected KualiDecimal workmansCompCoverageAmount;
    protected Date workmansCompExpiration;
    protected KualiDecimal excessLiabilityUmbrellaAmount;
    protected Date excessLiabilityUmbExpiration;
    protected Boolean healthOffSiteCateringLicenseReq;
    protected Date healthOffSiteLicenseExpirationDate;
    protected String insuranceNotes;
    protected String merchantNotes;
    protected List<CuVendorCreditCardMerchant> vendorCreditCardMerchants;

    public VendorDetailExtension() {
        vendorCreditCardMerchants = new ArrayList<CuVendorCreditCardMerchant>();

    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public boolean isEinvoiceVendorIndicator() {
        return einvoiceVendorIndicator;
    }

    public void setEinvoiceVendorIndicator(boolean eInvoiceVendorIndicator) {
        this.einvoiceVendorIndicator = eInvoiceVendorIndicator;
    }
    
    public String getDefaultB2BPaymentMethodCode() {
        return defaultB2BPaymentMethodCode;
    }

    public void setDefaultB2BPaymentMethodCode(String defaultB2BPaymentMethodCode) {
        this.defaultB2BPaymentMethodCode = defaultB2BPaymentMethodCode;
    }

	public boolean isInsuranceRequiredIndicator() {
		return insuranceRequiredIndicator;
	}

	public void setInsuranceRequiredIndicator(boolean insuranceRequiredIndicator) {
		this.insuranceRequiredIndicator = insuranceRequiredIndicator;
	}

	public Boolean getInsuranceRequirementsCompleteIndicator() {
		return insuranceRequirementsCompleteIndicator;
	}

	public void setInsuranceRequirementsCompleteIndicator(
			Boolean insuranceRequirementsCompleteIndicator) {
		this.insuranceRequirementsCompleteIndicator = insuranceRequirementsCompleteIndicator;
	}

	public Boolean getCornellAdditionalInsuredIndicator() {
		return cornellAdditionalInsuredIndicator;
	}

	public void setCornellAdditionalInsuredIndicator(
			Boolean cornellAdditionalInsuredIndicator) {
		this.cornellAdditionalInsuredIndicator = cornellAdditionalInsuredIndicator;
	}

	public KualiDecimal getGeneralLiabilityCoverageAmount() {
		return generalLiabilityCoverageAmount;
	}

	public void setGeneralLiabilityCoverageAmount(
			KualiDecimal generalLiabilityCoverageAmount) {
		this.generalLiabilityCoverageAmount = generalLiabilityCoverageAmount;
	}

	public Date getGeneralLiabilityExpiration() {
		return generalLiabilityExpiration;
	}

	public void setGeneralLiabilityExpiration(Date generalLiabilityExpiration) {
		this.generalLiabilityExpiration = generalLiabilityExpiration;
	}

	public KualiDecimal getAutomobileLiabilityCoverageAmount() {
		return automobileLiabilityCoverageAmount;
	}

	public void setAutomobileLiabilityCoverageAmount(
			KualiDecimal automobileLiabilityCoverageAmount) {
		this.automobileLiabilityCoverageAmount = automobileLiabilityCoverageAmount;
	}

	public Date getAutomobileLiabilityExpiration() {
		return automobileLiabilityExpiration;
	}

	public void setAutomobileLiabilityExpiration(Date automobileLiabilityExpiration) {
		this.automobileLiabilityExpiration = automobileLiabilityExpiration;
	}

	public KualiDecimal getWorkmansCompCoverageAmount() {
		return workmansCompCoverageAmount;
	}

	public void setWorkmansCompCoverageAmount(
			KualiDecimal workmansCompCoverageAmount) {
		this.workmansCompCoverageAmount = workmansCompCoverageAmount;
	}

	public Date getWorkmansCompExpiration() {
		return workmansCompExpiration;
	}

	public void setWorkmansCompExpiration(Date workmansCompExpiration) {
		this.workmansCompExpiration = workmansCompExpiration;
	}

	public KualiDecimal getExcessLiabilityUmbrellaAmount() {
		return excessLiabilityUmbrellaAmount;
	}

	public void setExcessLiabilityUmbrellaAmount(
			KualiDecimal excessLiabilityUmbrellaAmount) {
		this.excessLiabilityUmbrellaAmount = excessLiabilityUmbrellaAmount;
	}

	public Date getExcessLiabilityUmbExpiration() {
		return excessLiabilityUmbExpiration;
	}

	public void setExcessLiabilityUmbExpiration(Date excessLiabilityUmbExpiration) {
		this.excessLiabilityUmbExpiration = excessLiabilityUmbExpiration;
	}

	public Boolean getHealthOffSiteCateringLicenseReq() {
		return healthOffSiteCateringLicenseReq;
	}

	public void setHealthOffSiteCateringLicenseReq(
			Boolean healthOffSiteCateringLicenseReq) {
		this.healthOffSiteCateringLicenseReq = healthOffSiteCateringLicenseReq;
	}

	public Date getHealthOffSiteLicenseExpirationDate() {
		return healthOffSiteLicenseExpirationDate;
	}

	public void setHealthOffSiteLicenseExpirationDate(
			Date healthOffSiteLicenseExpirationDate) {
		this.healthOffSiteLicenseExpirationDate = healthOffSiteLicenseExpirationDate;
	}

	public String getInsuranceNotes() {
		return insuranceNotes;
	}

	public void setInsuranceNotes(String insuranceNotes) {
		this.insuranceNotes = insuranceNotes;
	}

	public String getMerchantNotes() {
		return merchantNotes;
	}

	public void setMerchantNotes(String merchantNotes) {
		this.merchantNotes = merchantNotes;
	}

	public List<CuVendorCreditCardMerchant> getVendorCreditCardMerchants() {
		return vendorCreditCardMerchants;
	}

	public void setVendorCreditCardMerchants(
			List<CuVendorCreditCardMerchant> vendorCreditCardMerchants) {
		this.vendorCreditCardMerchants = vendorCreditCardMerchants;
	}

}