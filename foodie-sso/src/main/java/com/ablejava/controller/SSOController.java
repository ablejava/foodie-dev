package com.ablejava.controller;

import com.ablejava.foodiedev.common.util.JSONResult;
import com.ablejava.foodiedev.common.util.JsonUtils;
import com.ablejava.foodiedev.common.util.MD5Utils;
import com.ablejava.foodiedev.common.util.RedisOperator;
import com.ablejava.foodiedev.pojo.Users;
import com.ablejava.foodiedev.pojo.vo.UsersVO;
import com.ablejava.foodiedev.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @Author: xiazhongwei
 * @Date: 2021/7/25 15:09
 */
@Controller
public class SSOController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;
    /**
     * redis_user_token:用户全局会话前缀
     */
    private static final String REDIS_USER_TOKEN = "redis_user_token";
    /**
     * redis_user_ticket:用户全局门票前缀，用于表示用户在CAS端的一个登录状态
     */
    private static final String REDIS_USER_TICKET = "redis_user_ticket";
    /**
     * redis_tmp_ticket:用户临时门票前缀，用于颁发给用户进行一次性的验证的票据，有时效性
     */
    private static final String REDIS_TMP_TICKET = "redis_tmp_ticket";
    /**
     * cookie_user_ticket:用户前端cookie前缀
     */
    private static final String COOKIE_USER_TICKET = "cookie_user_ticket";

    @GetMapping("/login")
    public String login(String returnUrl,
                        Model model,
                        HttpServletRequest request) {
        model.addAttribute("returnUrl", returnUrl);
        //从cookie中获取userTicket，如果cookie中能够获取到，证明用户登录过，此时签发一个一次性的临时票据并且回跳
        String userTicket = getCookie(request, COOKIE_USER_TICKET);

        boolean isVerified = verifyUserTicket(userTicket);
        if (isVerified) {
            String tmpTicket = createTmpTicket();
            return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
        }
        //CAS登录步骤4：验证未登录,重新跳转登录页面
        //CAS登录步骤5：显示CAS登录页面
        return "login";
    }
    /**
     * CAS的统一登录接口
     * 1.登录后创建用户的全局会话--->uniqueToken
     * 2.创建用户的全局门票,用于表示用户在cas端是否登录---->userToken
     * 3.创建用户的临时票据，用于回跳回传---->tmpTicket
     */
    @PostMapping("/doLogin")
    public String doLogin(String username,
                          String password,
                          String returnUrl,
                          Model model,
                          HttpServletResponse response) throws Exception {
        model.addAttribute("returnUrl", returnUrl);
        //判断用户名和密码必须不为空
        if (StringUtils.isBlank(username) ||
                StringUtils.isBlank(password)) {
            model.addAttribute("errmsg", "用户名或密码不能为空");
            return "login";
        }
        //CAS登录步骤6：用户名密码登录
        Users userResult = userService.queryUserForLogin(username,
                MD5Utils.getMD5Str(password));
        if (userResult == null) {
            model.addAttribute("errmsg", "用户名或密码不正确");
            return "login";
        }
        //CAS登录步骤7：登录成功
        //CAS登录步骤8：创建用户会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult, usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(),
                JsonUtils.objectToJson(usersVO));
        //CAS登录步骤9：创建用户全局门票
        String userTicket = UUID.randomUUID().toString().trim();
        //用户全局门票需要放入CAS端的cookie中
        setCookie(COOKIE_USER_TICKET, userTicket, response);
        //userTicket关联用户id，并且放入到redis中，代表这个用户拥有门票
        redisOperator.set(REDIS_USER_TICKET + ":" + userTicket, userResult.getId());
        //CAS登录步骤10：创建临时票据， 相当于微信获取code, 通过code获取session_token
        String tmpTicket = createTmpTicket();
        //CAS登录步骤11：回跳并携带临时票据
        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
    }


    @PostMapping("/verifyTmpTicket")
    @ResponseBody
    public JSONResult verifyTmpTicket(String tmpTicket, HttpServletRequest request) throws Exception {
        String tmpTicketValue = redisOperator.get(REDIS_TMP_TICKET + ":" + tmpTicket);
        if (StringUtils.isBlank(tmpTicketValue)) {
            return JSONResult.errorUserTicket("用户票据异常");
        }
        //CAS登录步骤13：校验并成功
        //如果临时票据OK，则需要销毁，并且拿到CAS端cookie中的全局userTicket，以此再获取用户会话
        if (!tmpTicketValue.equals(MD5Utils.getMD5Str(tmpTicket))) {
            return JSONResult.errorUserTicket("用户票据异常");
        } else {
            //销毁临时票据
            redisOperator.del(REDIS_TMP_TICKET + ":" + tmpTicket);
        }
        //获取全局票据，验证并且获取用户的userTicket
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorUserTicket("用户票据异常");
        }
        //验证门票对应的user会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return JSONResult.errorUserTicket("用户票据异常");
        }
        //CAS登录步骤14：用户会话回传
        return JSONResult.ok(JsonUtils.jsonToPojo(userRedis, UsersVO.class));
    }

    @PostMapping("/logout")
    @ResponseBody
    public JSONResult logout(String userId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {
        // 0. 获取CAS中的用户门票
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        // 1. 清除userTicket票据，redis/cookie
        deleteCookie(COOKIE_USER_TICKET, response);
        redisOperator.del(REDIS_USER_TICKET + ":" + userTicket);
        // 2. 清除用户全局会话（分布式会话）
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);
        return JSONResult.ok();
    }

    /**
     * 创建临时票据
     * @return
     */
    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();
        try {
            redisOperator.set(REDIS_TMP_TICKET + ":" + tmpTicket,MD5Utils.getMD5Str(tmpTicket), 600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpTicket;
    }

    private void setCookie(String key,String val,HttpServletResponse response) {
        Cookie cookie = new Cookie(key, val);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void deleteCookie(String key, HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

    private String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || StringUtils.isBlank(key)) {
            return null;
        }
        String cookieValue = null;
        for (int i = 0 ; i < cookieList.length; i ++) {
            if (cookieList[i].getName().equals(key)) {
                cookieValue = cookieList[i].getValue();
                break;
            }
        }
        return cookieValue;
    }

    /**
     * 校验CAS全局用户门票
     * @param userTicket
     * @return
     */
    private boolean verifyUserTicket(String userTicket) {
        // 0. 验证CAS门票不能为空
        if (StringUtils.isBlank(userTicket)) {
            return false;
        }
        // 1. 验证CAS门票是否有效
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        // 2. 验证门票对应的user会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return false;
        }
        return true;
    }
}
