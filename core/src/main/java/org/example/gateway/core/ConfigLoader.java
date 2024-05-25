package org.example.gateway.core;

import org.example.gateway.common.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String CONFIG_FILE = "gateway.properties";
    private static final String ENV_PREFIX = "GATEWAY_";
    private static final String JVM_PREFIX = "gateway.";
    private Config config;

    /**
     * 单例
     */
    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private ConfigLoader() {
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    public static Config getConfig() {
        return INSTANCE.config;
    }

    /**
     * 加载配置
     * 优先级高的会覆盖优先级低的
     * 运行参数 -> JVM参数 -> 环境变量 -> 配置文件 -> 默认值
     * @param args
     * @return
     */
    public Config load(String args[]) {
        // 使用默认配置
        config = new Config();
        // 使用配置文件中的配置
        loadFromConfigFile();
        // 使用环境变量中的配置
        loadFromEnv();
        // 使用JVM参数
        loadFromJVM();
        // 使用运行参数
        loadFromArgs(args);
        return config;
    }

    /**
     * 加载配置文件中的配置
     */
    private void loadFromConfigFile() {
        final InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if(inputStream != null) {
            final Properties properties = new Properties();
            try {
                properties.load(inputStream);
                // 使用配置文件中的配置覆盖默认配置
                PropertiesUtils.properties2Object(properties, config);
                logger.info("load config from config file: {}", properties.toString());
            } catch (IOException e) {
                logger.warn("load config file {} error", CONFIG_FILE, e);
            }finally {
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 加载环境变量中的配置
     */
    private void loadFromEnv() {
        final Map<String, String> env = System.getenv();
        final Properties properties = new Properties();
        properties.putAll(env);
        PropertiesUtils.properties2Object(properties, config, ENV_PREFIX);
        logger.info("load config from environment variables: {}", properties.toString());
    }

    /**
     * 加载JVM参数
     */
    private void loadFromJVM() {
        final Properties properties = System.getProperties();
        PropertiesUtils.properties2Object(properties, config, JVM_PREFIX);
        logger.info("load config from jvm options: {}", properties.toString());
    }

    /**
     * 加载运行参数
     * eg: --port=1234
     * @param args
     */
    private void loadFromArgs(String[] args) {
        if(args != null && args.length > 0) {
            final Properties properties = new Properties();
            for(String arg : args) {
                if(arg.startsWith("--") && arg.contains("=")) {
                    properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=")+1));
                }
            }
            PropertiesUtils.properties2Object(properties, config);
            logger.info("load config from program arguments: {}", properties.toString());
        }
    }

}
