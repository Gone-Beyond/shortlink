package com.beyond.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.beyond.shortlink.common.convention.exception.ClientException;
import com.beyond.shortlink.common.enums.UserErrorCodeEnum;
import com.beyond.shortlink.dao.entity.UserDO;
import com.beyond.shortlink.dao.mapper.UserMapper;
import com.beyond.shortlink.dto.req.UserLoginReqDTO;
import com.beyond.shortlink.dto.req.UserRegisterReqDTO;
import com.beyond.shortlink.dto.req.UserUpdateReqDTO;
import com.beyond.shortlink.dto.resp.UserDTO;
import com.beyond.shortlink.dto.resp.UserLoginRespDTO;
import com.beyond.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.beyond.shortlink.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.beyond.shortlink.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.beyond.shortlink.common.enums.UserErrorCodeEnum.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    public UserDTO getUserByUsername(String username) {

        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);

        //  extends ServiceImpl<UserMapper, UserDO> 里面的字段
        UserDO userDO = this.baseMapper.selectOne(queryWrapper);

        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        return BeanUtil.copyProperties(userDO, UserDTO.class);

    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (userRegisterCachePenetrationBloomFilter.contains(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }

        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());

        if (!lock.tryLock()) {
            throw new ClientException(USER_EXIST);
        }

        try {

            int insert = this.baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));

            if (insert < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }

            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());

        }  catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST.message(), ex, USER_EXIST);
        } finally {
            lock.unlock();
        }


    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        //TODO 验证当前用户是登录者吗（后面会改 先留着）

        LambdaUpdateWrapper<UserDO> wrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());

        int updated = baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), wrapper);

        if (updated < 1) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }

    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {

        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);

        UserDO userDO = baseMapper.selectOne(wrapper);

        if (userDO == null) {
            throw new ClientException("用户名或密码错误");
        }

        String loginKey = USER_LOGIN_KEY + requestParam.getUsername();

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(loginKey))) {
            throw new ClientException("用户已登录");
        }

        String token = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(loginKey, token, JSONUtil.toJsonStr(userDO));
        stringRedisTemplate.expire(loginKey, 30L, TimeUnit.MINUTES);


        return new UserLoginRespDTO(token);
    }


}
