package org.example.gateway.core;

/**
 * api网关启动类
 */
public class Bootstrap {


    public static void main(String[] args) {

        // 加载网关静态配置
        final Config config = ConfigLoader.getInstance().load(args);
        System.out.println(config.getPort());

        // 插件初始化

        // 连接配置中心，监听配置的新增、修改、删除

        // 启动容器

        // 连接注册中心，将注册中心的实例加载到本地

        // 服务优雅关闭


    }

}
