package com.autoprov.autoprov.dto;

public class AbsTokenResponse {
    private String access_token;

    // Default constructor
    public AbsTokenResponse() {
    }

    // Constructor to initialize the accessToken
    public AbsTokenResponse(String access_token) {
        this.access_token = access_token;
    }

    // Getter for accessTokentoken
    public String getAccess_token() {
        return access_token;
    }

    // Setter for accessToken
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
