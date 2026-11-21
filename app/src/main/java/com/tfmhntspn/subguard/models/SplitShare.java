package com.tfmhntspn.subguard.models;

public class SplitShare {

    private String shareId;
    private String billId;
    private String userId;
    private long amountCents;

    public SplitShare() {}
    public SplitShare(String shareId, String billId, String userId, long amountCents) {
        this.shareId = shareId;
        this.billId = billId;
        this.userId = userId;
        this.amountCents = amountCents;
    }
    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

}
