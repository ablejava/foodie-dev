package com.ablejava.config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Author: xiazhongwei
 * @Date: 2021/7/26 21:26
 */
@Configuration
public class EsConfig {
    /**
     * 解决netty 引起都issue
     */
    @PostConstruct
    void init() {
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }
}
