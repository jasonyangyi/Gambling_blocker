package com.example.gambling_blocker;

public class Website_post {
    private String website_address;
    private String Message;
    public Website_post(String website_address, String message) {
        this.website_address = website_address;
        Message = message;
    }

    public String getWebsite_address() {
        return website_address;
    }

    public void setWebsite_address(String website_address) {
        this.website_address = website_address;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
