package edu.cornell.kfs.vnd.businessobject;




public class VendorInactivateConvertBatch  {

    /**
     * @author cab379
     */
    
    private String vendorId;
    private String vendorName;
    private String action;
    private String children;
    
	public String getVendorId() {
		return vendorId;
	}
	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
    public String getVendorName() {
        return vendorName;
    }
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    public String getChildren() {
        return children;
    }
    public void setChildren(String children) {
        this.children = children;
    }
   
        

    

}
