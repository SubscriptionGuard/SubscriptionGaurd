
package com.tfmhntspn.subguard.models;

public class Subscription {

    private String id;
    private String name;
    private double cost;
    private String currency;
    private String billingCycle;
    private String category;
    private String nextRenewalDate;
    private boolean reminderEnabled;

    // Required for Firebase
    public Subscription() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(String billingCycle) {
        this.billingCycle = billingCycle;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNextRenewalDate() {
        return nextRenewalDate;
    }

    public void setNextRenewalDate(String nextRenewalDate) {
        this.nextRenewalDate = nextRenewalDate;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }
}
