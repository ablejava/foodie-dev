package com.ablejava.foodiedev.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @author mrcode
 * @date 2021/6/30 22:39
 */
@ApiIgnore
@RestController
@RequestMapping("redis")
public class RedisController {
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/set")
    public Object set(String key, String value) {
        // opsForxxx 里面很好多是我们之前讲解过的 redis 数据类型对应的
        redisTemplate.opsForValue().set(key, value);
        return "OK";
    }

    @GetMapping("/get")
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @GetMapping("/delete")
    public Object delete(String key) {
        // 删除 key 是通用操作
        redisTemplate.delete(key);
        return "OK";
    }
}
