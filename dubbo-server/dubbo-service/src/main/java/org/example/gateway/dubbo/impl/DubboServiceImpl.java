package org.example.gateway.dubbo.impl;

import org.example.gateway.client.core.ApiInvoker;
import org.example.gateway.client.core.ApiProtocol;
import org.example.gateway.client.core.ApiService;
import org.example.gateway.dubbo.rpc.DubboService;


@ApiService(serviceId = "backend-dubbo-server", protocol = ApiProtocol.DUBBO, patternPath = "/dubbo-server/**")
@org.apache.dubbo.config.annotation.DubboService
public class DubboServiceImpl implements DubboService {

    /**
     * 扫描的时候得到映射关系 /dubbo-server/testDubbo --> org.example.gateway.dubbo.impl.DubboServiceImpl::testDubbo
     * @param msg
     * @return
     */
    @Override
    @ApiInvoker(path = "/dubbo-server/testDubbo")
    public String testDubbo(String msg) {
        return "pong dubbo" + msg;
    }


    /**
     * 扫描的时候得到映射关系 /dubbo-server/testDubbo2 --> org.example.gateway.dubbo.impl.DubboServiceImpl::testDubbo2
     * 将其注册到注册中心
     * 网关发送请求时，通过uniqueId找到服务实例，再通过url找到具体的方法进行调用，完成请求转发
     * @param msg
     * @return
     */
    @ApiInvoker(path = "/dubbo-server/testDubbo2")
    @Override
    public String testDubbo2(String msg, String msg2) {
        return "pong dubbo " + msg + " and " + msg2;
    }
}
