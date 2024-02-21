package org.example.gateway.common.exception;


import org.example.gateway.common.enums.ResponseCode;

public class ConnectException extends BaseException{

    private static final long serialVersionUID = -8503239867913964958L;

    private final String uniqueId;

    private final String requestUrl;

    public ConnectException(String uniqueId, String requestUrl) {
        this.uniqueId = uniqueId;
        this.requestUrl = requestUrl;
    }

    public ConnectException(Throwable cause, String uniqueId, String requestUrl, ResponseCode code) {
        super(code.getMessage(), cause, code);
        this.uniqueId = uniqueId;
        this.requestUrl = requestUrl;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getRequestUrl() {
        return requestUrl;
    }
}
