package com.ablejava.foodiedev.service;

import com.ablejava.foodiedev.pojo.Users;
import com.ablejava.foodiedev.pojo.bo.UserBO;

public interface UserService {
    /**
     * 查找用户名是否存在
     *
     * @param username
     * @return
     */
    boolean queryUsernameIsExist(String username);

    /**
     * 创建用户
     *
     * @param userBO 接受前端传递过来的 业务对象
     * @return
     */
    Users createUser(UserBO userBO);

    Users queryUserForLogin(String username, String passwod);
}
