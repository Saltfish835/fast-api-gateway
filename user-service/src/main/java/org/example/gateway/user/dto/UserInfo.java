package org.example.gateway.user.dto;

public class UserInfo {

    private long id;
    private String nikeName;
    private String phoneNumber;

    public UserInfo(long id, String nikeName, String phoneNumber) {
        this.id = id;
        this.nikeName = nikeName;
        this.phoneNumber = phoneNumber;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNikeName() {
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
