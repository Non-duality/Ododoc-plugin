package com.ssafy.ododocintellij.login.manager;

public class TokenManager {

    private String accessToken;
    private String refreshToken;

    private TokenManager() {}

    private static class TokenManagerHolder {
        private static final TokenManager INSTANCE = new TokenManager();
    }

    public static TokenManager getInstance() {
        return TokenManagerHolder.INSTANCE;
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }

    public synchronized String getRefreshToken() {
        return refreshToken;
    }

    public synchronized void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public synchronized void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
