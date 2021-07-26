package com.ablejava.foodiedev.service.center;

import com.ablejava.foodiedev.pojo.Users;
import com.ablejava.foodiedev.pojo.bo.center.CenterUserBO;

/**
 * @author mrcode
 * @date 2021/2/18 20:59
 */
public interface CenterUserService {
    /**
     * 根据用户 id 查询用户信息
     *
     * @param userId
     * @return
     */
    Users queryUserInfo(String userId);

    /**
     * 修改用户信息
     *
     * @param userId
     * @param centerUserBO
     */
    Users updateUserInfo(String userId, CenterUserBO centerUserBO);

    /**
     * 用户头像更新
     *
     * @param userId
     * @param faceUrl
     * @return
     */
    Users updateUserFace(String userId, String faceUrl);

}
