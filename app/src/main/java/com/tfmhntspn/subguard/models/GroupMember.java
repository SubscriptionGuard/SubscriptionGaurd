package com.tfmhntspn.subguard.models;

public class GroupMember {

    private String groupId;
    private String userId;
    private String role;

    public GroupMember() {}

    public GroupMember(String groupId, String userId, String role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
