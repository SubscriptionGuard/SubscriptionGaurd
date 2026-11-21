package com.tfmhntspn.subguard.models;

public class Connection {

    private String connectionId;
    private String senderId;
    private String receiverId;
    private String status;

    public Connection() {}

    public Connection(String connectionId, String senderId, String receiverId, String status) {
        this.connectionId = connectionId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

