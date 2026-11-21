
package com.tfmhntspn.subguard.models;

public class SplitApproval {

    private String approvalId;
    private String billId;
    private String userId;
    private String status;
    private long respondedAt;

    public SplitApproval() {}

    public SplitApproval(String approvalId, String billId,
                         String userId, String status,
                         long respondedAt) {

        this.approvalId = approvalId;
        this.billId = billId;
        this.userId = userId;
        this.status = status;
        this.respondedAt = respondedAt;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(long respondedAt) {
        this.respondedAt = respondedAt;
    }
}
