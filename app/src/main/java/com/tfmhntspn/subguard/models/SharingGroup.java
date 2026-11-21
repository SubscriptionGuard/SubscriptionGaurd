package com.tfmhntspn.subguard.models;

public class SharingGroup {

    private String groupId;
    private String groupName;
    private String ownerId;
    private long createdAt;

    public SharingGroup() {}

    public SharingGroup(String groupId, String groupName, String ownerId, Long createdAt){
        this.groupId = groupId;
        this.groupName = groupName;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
