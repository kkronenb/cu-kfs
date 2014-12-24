package edu.cornell.kfs.module.purap.businessobject;

public class BatchIWantAttachment {

    private String attachmentType;
    private String attachmentFileName;

    public BatchIWantAttachment() {}

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }

}
