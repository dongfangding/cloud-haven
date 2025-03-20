package com.snowball.cloud.haven.api.urlreplace;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * 功能描述:数据脱敏序列化
 *
 * @author snowball
 * @date 2020/9/25 0025 11:56
 **/
@Configuration
@Slf4j
public class UrlReplaceSerialize extends JsonSerializer<String>
        implements ContextualSerializer, BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * "@PostConstruct"注解标记的类中，由于ApplicationContext还未加载，导致空指针<br>
     * 因此实现BeanFactoryPostProcessor注入ConfigurableListableBeanFactory实现bean的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;
    /**
     * Spring应用上下文环境
     */
    private static ApplicationContext applicationContext;

    /**
     * 存储桶
     */
    private String bucket;

    public UrlReplaceSerialize() {
    }

    public UrlReplaceSerialize(final String bucket) {
        this.bucket = bucket;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializers)
            throws IOException {
        try {
            final StaticProperties bean = beanFactory.getBean(StaticProperties.class);
            String bucket = this.bucket;
            final Map<String, List<String>> resourceProxyHosts = bean.getResourceProxyHosts();
            if (StringUtils.isBlank(value) || CollUtil.isEmpty(resourceProxyHosts) || !resourceProxyHosts.containsKey(
                    bucket)) {
                jsonGenerator.writeString(value);
            } else {
                final List<String> hosts = resourceProxyHosts.get(bucket);
                if (CollUtil.isEmpty(hosts)) {
                    jsonGenerator.writeString(value);
                    return;
                }
                String resourceProxyHost = hosts.get(RandomUtil.randomInt(0, hosts.size()));

                // 处理一个字段中可能存在多个url的问题
                final String[] multipleValue = value.split("[,，]");
                StringBuilder allTextAfterReplace = new StringBuilder();
                for (int i = 0; i < multipleValue.length; i++) {
                    String singleValue = multipleValue[i];
                    String currentTextAfterReplace = "";
                    final URI uri = URLUtil.toURI(singleValue);
                    if (StringUtils.isBlank(uri.getHost())) {
                        currentTextAfterReplace = resourceProxyHost + (singleValue.startsWith("/") ? "" : "/")
                                + singleValue;
                    } else {
                        currentTextAfterReplace = singleValue.replace(
                                uri.getScheme() + "://" + uri.getHost(), resourceProxyHost);
                    }
                    allTextAfterReplace.append(currentTextAfterReplace);
                    if (i < multipleValue.length - 1) {
                        allTextAfterReplace.append(",");
                    }
                }
                jsonGenerator.writeString(allTextAfterReplace.toString());
            }
        } catch (Exception e) {
            log.error("连接全局替换出现异常", e);
            jsonGenerator.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
            throws JsonMappingException {
        // 为空直接跳过
        if (beanProperty != null) {
            // 非 String 类直接跳过
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                UrlReplace urlReplace = beanProperty.getAnnotation(UrlReplace.class);
                if (urlReplace == null) {
                    urlReplace = beanProperty.getContextAnnotation(UrlReplace.class);
                }
                if (urlReplace != null) {
                    return new UrlReplaceSerialize(urlReplace.bucket());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(beanProperty);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        UrlReplaceSerialize.beanFactory = factory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        UrlReplaceSerialize.applicationContext = applicationContext;
    }
}
