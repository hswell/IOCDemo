package com.well.platform.myUtils;

import com.well.platform.myAnno.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author huangs-e
 * @Date 2020/11/24 14:41
 * @Version 1.0
 */
@Slf4j
public class ConfigurationUtils {
    public static Properties properties;


    public ConfigurationUtils(String propertiesPath) {
        properties = this.getBeanScanPath(propertiesPath);
    }

    /**
     * 读取文件信息
     *
     * @param propertiesPath
     * @return
     */
    public Properties getBeanScanPath(String propertiesPath) {
        if (StringUtils.isEmpty(propertiesPath)) {
            propertiesPath = "/application.properties";
        }
        Properties properties = new Properties();
        try (InputStream inputStream = ConfigurationUtils.class.getResourceAsStream(propertiesPath)) {
            System.out.println("正在加载配置文件application.properties");
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 根据配置文件的key获取value的值
     *
     * @param propertiesKey
     * @return
     */
    public static Object getPropertiesByKey(String propertiesKey) {
        if (properties.size() > 0) {
            return properties.get(propertiesKey);
        }
        return null;
    }

}
