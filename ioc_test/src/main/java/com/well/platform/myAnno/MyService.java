package com.well.platform.myAnno;

import java.lang.annotation.*;

/**
 * @Author huangs-e
 * @Date 2020/11/24 11:36
 * @Version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface MyService {
    String value() default "";
}
