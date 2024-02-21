package org.example.gateway.core;

/**
 * api网关启动类
 */
public class Bootstrap {


    public static void main(String[] args) {

        // 加载网关配置
        final Config config = ConfigLoader.getInstance().load(args);

        // 插件初始化

        // 连接配置中心，监听配置的新增、修改、删除

        // 启动容器
        final Container container = new Container(config);
        container.start();

        // 连接注册中心，将注册中心的实例加载到本地

        // 服务优雅关闭


    }

}
