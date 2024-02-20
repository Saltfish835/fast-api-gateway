package org.example.gateway.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.*;
import org.asynchttpclient.Response;
import org.example.gateway.common.enums.ResponseCode;
import org.example.gateway.common.utils.JSONUtil;

public class GatewayResponse {

    /**
     * 响应头
     */
    private HttpHeaders responseHeaders = new DefaultHttpHeaders();

    /**
     * 额外的响应头
     */
    private HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    /**
     * 响应内容
     */
    private String content;

    /**
     * 响应码
     */
    private HttpResponseStatus httpResponseStatus;

    /**
     * 异步响应对象
     */
    private Response futureResponse;

    public GatewayResponse() {

    }

    public HttpHeaders getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(HttpHeaders responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public HttpHeaders getExtraResponseHeaders() {
        return extraResponseHeaders;
    }

    public void setExtraResponseHeaders(HttpHeaders extraResponseHeaders) {
        this.extraResponseHeaders = extraResponseHeaders;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return httpResponseStatus;
    }

    public void setHttpResponseStatus(HttpResponseStatus httpResponseStatus) {
        this.httpResponseStatus = httpResponseStatus;
    }

    public Response getFutureResponse() {
        return futureResponse;
    }

    public void setFutureResponse(Response futureResponse) {
        this.futureResponse = futureResponse;
    }

    /**
     * 设置响应头
     * @param key
     * @param value
     */
    public void putHeader(CharSequence key, CharSequence value) {
        responseHeaders.add(key, value);
    }

    /**
     * 构建异步响应对象
     * @param futureResponse
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Response futureResponse) {
        final GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return response;
    }

    /**
     * 失败时的响应内容
     * @param code
     * @param args
     * @return
     */
    public static GatewayResponse buildGatewayResponse(ResponseCode code, Object... args) {
        final ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, code.getStatus().code());
        objectNode.put(JSONUtil.CODE, code.getCode());
        objectNode.put(JSONUtil.MESSAGE, code.getMessage());
        final GatewayResponse res = new GatewayResponse();
        res.setHttpResponseStatus(code.getStatus());
        res.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        return res;
    }

    /**
     * 成功时的响应内容
     * @param data
     * @return
     */
    public static GatewayResponse buildGatewayResponse(Object data) {
        final ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.CODE, ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA, data);
        final GatewayResponse res = new GatewayResponse();
        res.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        res.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        return res;
    }

}
