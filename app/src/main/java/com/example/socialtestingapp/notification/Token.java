package com.example.socialtestingapp.notification;

public class Token {
    /* an FCM token, or commonly know as a registrationToken.
        an ID issued by the GCM connection servers to client app
        that allows it to receive messages
     */
    String token;

    public Token() {
    }

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
