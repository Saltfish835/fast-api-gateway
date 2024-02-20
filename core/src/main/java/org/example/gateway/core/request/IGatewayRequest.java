package org.example.gateway.core.request;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

public interface IGatewayRequest {


    /**
     * 修改目标服务主机
     * @param host
     */
    void setModifyHost(String host);

    /**
     * 获取目标服务主机
     */
    String getModifyHost();

    /**
     * 修改目标服务路径
     * @param path
     */
    void setModifyPath(String path);

    /**
     * 获取目标服务路径
     * @return
     */
    String getModifyPath();

    /**
     * 添加请求头
     * @param name
     * @param value
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求参数
     * @param name
     * @param value
     */
    void setHeader(CharSequence name, String value);

    /**
     * 添加请求参数
     * @param name
     * @param value
     */
    void addQueryParam(String name, String value);

    /**
     * 添加表单请求参数
     * @param name
     * @param value
     */
    void addFormParam(String name, String value);

    /**
     * 添加或替换Cookie
     * @param cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 设置超时时间
     * @param requestTimeout
     */
    void setRequestTimeout(int requestTimeout);

    /**
     * 获取最终的请求路径
     * eg: http://localhost:8081/api/admin?name=zhangsan
     * @return
     */
    String getFinalUrl();

    /**
     * 构建请求对象
     * @return
     */
    Request build();
}
