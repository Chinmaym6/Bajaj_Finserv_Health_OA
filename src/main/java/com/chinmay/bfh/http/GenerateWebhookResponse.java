package com.chinmay.bfh.http;

import com.fasterxml.jackson.annotation.JsonAlias;

public class GenerateWebhookResponse {
    @JsonAlias({"webhook", "webhookUrl", "url"})
    private String webhook;

    @JsonAlias({"accessToken", "token", "jwt"})
    private String accessToken;

    private String message;

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
