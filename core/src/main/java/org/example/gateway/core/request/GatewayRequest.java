package org.example.gateway.core.request;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.example.gateway.common.constants.BasicConst;
import org.example.gateway.common.utils.TimeUtil;

import java.nio.charset.Charset;
import java.util.*;

public class GatewayRequest implements IGatewayRequest{


    private final String uniqueId;

    /**
     * 请求进入网关的开始时间
     */
    private final long beginTime;

    /**
     * 请求进入网关的结束时间
     */
    private final long endTime;

    /**
     * 字符集
     */
    private final Charset charset;

    /**
     * 客户端ip
     */
    private final String clientIp;

    /**
     * 服务端主机
     */
    private final String host;

    /**
     * 服务端请求路径
     * eg: /xxx/xx
     */
    private final String path;

    /**
     * 统一资源标识符
     * eg: /xxx/xx?attr1=x&attr2=x
     */
    private final String uri;

    /**
     * 请求方式
     * eg POST/GET/PUT
     */
    private final HttpMethod httpMethod;

    /**
     * 请求格式
     */
    private final String contentType;

    /**
     * 请求头
     */
    private final HttpHeaders httpHeaders;

    /**
     * 参数解析器
     */
    private final QueryStringDecoder queryStringDecoder;

    /**
     *
     */
    private final FullHttpRequest fullHttpRequest;

    /**
     * 请求体
     */
    private String body;

    /**
     * Cookie
     */
    private Map<String, io.netty.handler.codec.http.cookie.Cookie> cookieMap;

    /**
     * POST方式的请求参数
     */
    private Map<String, List<String>> postParameters;

    /**
     * 修改scheme，默认为http
     */
    private String modifyScheme;

    /**
     * 可修改的主机名
     */
    private String modifyHost;

    /**
     * 可修改的请求路径
     */
    private String modifyPath;

    /**
     * 构建下游http请求的构建器
     */
    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId,
                          long endTime,
                          Charset charset,
                          String clientIp,
                          String host,
                          String path,
                          String uri,
                          HttpMethod httpMethod,
                          String contentType,
                          HttpHeaders httpHeaders,
                          QueryStringDecoder queryStringDecoder,
                          FullHttpRequest fullHttpRequest,
                          RequestBuilder requestBuilder) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.endTime = endTime;
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.path = queryStringDecoder.path();
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.contentType = contentType;
        this.httpHeaders = httpHeaders;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.fullHttpRequest = fullHttpRequest;
        this.requestBuilder = new RequestBuilder();

        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;
        this.requestBuilder.setMethod(getHttpMethod().name());
        this.requestBuilder.setHeaders(getHttpHeaders());
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());

        ByteBuf contentBuffer = fullHttpRequest.content();
        if(Objects.nonNull(contentBuffer)) {
            this.requestBuilder.setBody(contentBuffer.nioBuffer());
        }
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        return uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public QueryStringDecoder getQueryStringDecoder() {
        return queryStringDecoder;
    }

    public FullHttpRequest getFullHttpRequest() {
        return fullHttpRequest;
    }

    public String getBody() {
        if(StringUtils.isEmpty(body)) {
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }


    public io.netty.handler.codec.http.cookie.Cookie getCookie(String name) {
        if(cookieMap == null) {
            cookieMap = new HashMap<>();
            final String cookieStr = getHttpHeaders().get(HttpHeaderNames.COOKIE);
            final Set<io.netty.handler.codec.http.cookie.Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieStr);
            for(io.netty.handler.codec.http.cookie.Cookie cookie : cookieSet) {
                cookieMap.put(name, cookie);
            }
        }
        return cookieMap.get(name);
    }


    /**
     * 获取指定名称的参数值
     * @param name
     * @return
     */
    public List<String> getQueryParametersMultiple(String name) {
        return queryStringDecoder.parameters().get(name);
    }

    /**
     * 获取Post请求指定名称的参数值
     * @param name
     * @return
     */
    public List<String> getPostParameterMultiple(String name) {
        String body = getBody();
        // 如果post请求的请求体是表单
        if(isFormPost()) {
            if(postParameters == null) {
                final QueryStringDecoder paramDecoder = new QueryStringDecoder(body, false);
                postParameters = paramDecoder.parameters();
            }
            if(postParameters == null || postParameters.isEmpty()) {
                return null;
            }else {
                return postParameters.get(name);
            }
        }else if(isJsonPost()){ // 如果post请求体是json
            return Lists.newArrayList(JsonPath.read(body,name).toString());
        }
        return null;
    }


    /**
     * 判断请求体是不是form表单
     * @return
     */
    public boolean isFormPost() {
        return HttpMethod.POST.equals(httpMethod) && (
                contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }


    /**
     * 判断请求体是不是json
     * @return
     */
    public boolean isJsonPost() {
        return HttpMethod.POST.equals(httpMethod) && contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }


    public Map<String, io.netty.handler.codec.http.cookie.Cookie> getCookieMap() {
        return cookieMap;
    }

    public Map<String, List<String>> getPostParameters() {
        return postParameters;
    }

    public String getModifyScheme() {
        return modifyScheme;
    }

    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    @Override
    public void setModifyHost(String host) {
        this.modifyHost = host;
    }

    @Override
    public String getModifyHost() {
        return this.modifyHost;
    }

    @Override
    public void setModifyPath(String path) {
        this.modifyPath = path;
    }

    @Override
    public String getModifyPath() {
        return this.modifyPath;
    }

    @Override
    public void addHeader(CharSequence name, String value) {
        requestBuilder.addHeader(name, value);
    }

    @Override
    public void setHeader(CharSequence name, String value) {
        requestBuilder.setHeader(name, value);
    }

    @Override
    public void addQueryParam(String name, String value) {
        requestBuilder.addQueryParam(name, value);
    }

    @Override
    public void addFormParam(String name, String value) {
        if(isFormPost()) {
            requestBuilder.addQueryParam(name, value);
        }
    }

    @Override
    public void addOrReplaceCookie(org.asynchttpclient.cookie.Cookie cookie) {
        requestBuilder.addOrReplaceCookie(cookie);
    }

    @Override
    public void setRequestTimeout(int requestTimeout) {
        requestBuilder.setRequestTimeout(requestTimeout);
    }

    @Override
    public String getFinalUrl() {
        return modifyScheme + modifyHost + modifyPath;
    }

    @Override
    public Request build() {
        requestBuilder.setUrl(getFinalUrl());
        return requestBuilder.build();
    }
}
