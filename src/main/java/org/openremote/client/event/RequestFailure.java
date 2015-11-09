package org.openremote.client.event;

import jsinterop.annotations.JsType;

@JsType
public class RequestFailure {

    public final String requestText;
    public final int statusCode;
    public final String statusText;
    public final String errorText;

    public RequestFailure(String requestText, int statusCode, String statusText, String errorText) {
        this.requestText = requestText;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.errorText = errorText;
    }

    public String getFailureMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Request \"").append(requestText).append("\" failed.");
        if (statusCode > 0) {
            sb.append(" Response was (").append(statusCode).append(") ");
            sb.append(statusText).append(".");
        } else {
            sb.append(" No response.");
        }
        if (errorText != null && !errorText.equals(statusText)) {
            sb.append(" ").append(errorText);
            if (!errorText.substring(errorText.length()-1).equals("."))
                sb.append(".");
        }
        return sb.toString();
    }
}
