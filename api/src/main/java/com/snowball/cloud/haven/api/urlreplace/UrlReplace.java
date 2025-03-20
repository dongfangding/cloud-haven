package com.snowball.cloud.haven.api.urlreplace;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * 标识要替换的文案， 仅支持jackson
 *
 * @author snowball
 * @date 2020/9/25 0025 11:54
 **/
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = UrlReplaceSerialize.class)
public @interface UrlReplace {

    /**
     * 存储桶名称
     *
     * @return
     */
    String bucket() default "default";
}
