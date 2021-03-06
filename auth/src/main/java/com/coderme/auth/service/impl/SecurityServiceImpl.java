package com.coderme.auth.service.impl;

import com.coderme.auth.constans.Constants.*;
import com.coderme.auth.controller.param.LoginParam;
import com.coderme.auth.controller.param.RegisterParam;
import com.coderme.auth.dao.IUserDao;
import com.coderme.auth.dao.IUserIdxDao;
import com.coderme.auth.data.base.AccessToken;
import com.coderme.auth.data.po.User;
import com.coderme.auth.exception.ServiceException;
import com.coderme.auth.service.ICacheService;
import com.coderme.auth.service.ISecurityService;
import com.coderme.commons.base.utils.IDGenerator;
import com.coderme.commons.base.utils.Md5Util;
import com.coderme.commons.base.utils.StringUtils;
import com.coderme.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.coderme.auth.enums.UserEnums.*;

import java.util.List;
import java.util.Objects;

/**
 * @author qiudm
 * @date 2018/5/30 16:00
 * @desc
 */
@Service
public class SecurityServiceImpl implements ISecurityService {

    @Autowired
    private IUserDao userDao;
    @Autowired
    private IUserIdxDao userIdxDao;
    @Autowired
    private ICacheService cacheService;


    @Override
    public void register(RegisterParam param) {
        validateRegisterParam(param);
        User user = BeanUtil.copy(param, User.class);
        user.setUserType(UserType.COMMON_USER.getType());
        user.setId(String.valueOf(IDGenerator.idWorker.nextId()));
        user.setStatus(UserStatus.WAIT_FOR_ACTIVE.getStatus());
        user.setRegistTime(System.currentTimeMillis());
        user.setUserId(userIdxDao.nextUserId(user.getUserType()));
        user.setOpenId(StringUtils.randomUUID());
        user.setPassword(Md5Util.md5Hex(user.getPassword()));
        userDao.insert(user);
    }

    private void validateRegisterParam(RegisterParam param) {
        User dbUser = userDao.getByNickName(param.getNickName());
        if (Objects.nonNull(dbUser)) {
            throw new ServiceException("该昵称已被注册");
        }
        if (Objects.nonNull(param.getTel())) {
            dbUser = userDao.getByTel(param.getTel());
            if (Objects.nonNull(dbUser)) {
                throw new ServiceException("该昵称已被注册");
            }
        }
    }


    @Override
    public AccessToken login(LoginParam param) {
        List<String> allAccountTypes = AccountType.getAllType();
        if (!allAccountTypes.contains(param.getAccountType())) {
            throw new ServiceException("登录账户类型异常");
        }

        AccessToken accessToken = new AccessToken();
        String token = StringUtils.randomUUID();
        cacheService.cacheLoginUserToken(token, Expire.MONTH, accessToken);
        return accessToken;
    }


}
