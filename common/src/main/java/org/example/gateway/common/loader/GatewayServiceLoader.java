package org.example.gateway.common.loader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GatewayServiceLoader {

    private static final String servicePath;

    private static Map<String, String> mapping = new HashMap<>();

    static {
        String classpath = GatewayServiceLoader.class.getClassLoader().getResource("").getPath();
        servicePath = classpath + "META-INF" + "/" + "services";
        final File file = new File(servicePath);
        if(!file.exists()) {

        }
        if(!file.isDirectory()) {
            throw new RuntimeException("[" + servicePath + "] is not directory");
        }
        final File[] files = file.listFiles();
        if(files == null || files.length == 0) {
            throw new RuntimeException("[" + servicePath + "] does not contain any files");
        }
        BufferedReader bufferedReader = null;
        for(File fileTmp : files) {
            try {
                bufferedReader = new BufferedReader(new FileReader(fileTmp));
                final String line = bufferedReader.readLine();
                mapping.put(fileTmp.getName(), line);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                if(bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * 加载META-INF/service目录下的服务
     * @param service
     * @return
     */
    public static<T> T load(Class<T> service) {
        final String implClass = mapping.get(service.getName());
        Object instance = null;
        try {
            instance = Class.forName(implClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (T)instance;
    }
}
