package com.ablejava.foodiedev.api.controller.interceptor;

import com.ablejava.foodiedev.common.util.JSONResult;
import com.ablejava.foodiedev.common.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: xiazhongwei
 * @Date: 2021/7/25 13:52
 */
public class UserTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userId = request.getHeader("userId");
        String userToken = request.getHeader("userToken");
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(userToken)) {
            returnErrorResponse(response, JSONResult.errorMsg("请登录"));
            return false;
        }
        String userTokenRedis = stringRedisTemplate.opsForValue().get(userId);
        if (StringUtils.isBlank(userTokenRedis) || !userTokenRedis.equals(userToken)) {
            return false;
        }
        return true;
    }

    public void returnErrorResponse(HttpServletResponse response, JSONResult result) {

        ServletOutputStream outputStream = null;
        try {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/json");
        outputStream = response.getOutputStream();
        outputStream.write(JsonUtils.objectToJson(result).getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        } catch (IOException e) {

        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                }catch (IOException e) {

                }
            }
        }

    }
}
