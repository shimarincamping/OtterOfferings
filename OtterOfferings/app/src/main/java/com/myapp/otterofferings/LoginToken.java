package com.myapp.otterofferings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginToken {

    @JsonProperty("loginEmail")
    private String loginEmail;
    @JsonProperty("loginToken")
    private String loginToken;

    public LoginToken() {

    }
    public LoginToken(String loginEmail, String loginToken) {
        this.loginEmail = loginEmail;
        this.loginToken = loginToken;
    }

    public String getLoginEmail() {
        return loginEmail;
    }

    public void setLoginEmail(String loginEmail) {
        this.loginEmail = loginEmail;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }
}
