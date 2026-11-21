package com.tfmhntspn.subguard.models;

public class SplitParticipant {

    private String userId;
    private String displayName;
    private long amountCents;

    public SplitParticipant(String userId, String displayName,
                            long amountCents) {
        this.userId = userId;
        this.displayName = displayName;
        this.amountCents = amountCents;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }
}
