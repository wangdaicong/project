package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.exam.entity.User;
import com.volunteer.exam.entity.UserFavorite;
import com.volunteer.exam.entity.UserHistory;
import com.volunteer.exam.mapper.UserFavoriteMapper;
import com.volunteer.exam.mapper.UserHistoryMapper;
import com.volunteer.exam.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private UserFavoriteMapper userFavoriteMapper;
    
    @Resource
    private UserHistoryMapper userHistoryMapper;
    
    @Resource
    private RestTemplate restTemplate;
    
    // 微信登录功能已屏蔽
    // @Value("${wechat.appid}")
    /**
     * 微信登录（已屏蔽）
     * @param code 微信登录凭证
     * @return 用户信息
     */
    public Map<String, Object> wxLogin(String code) {
        log.info("微信登录功能已屏蔽，code: {}", code);
        
        // 微信登录功能已屏蔽，返回空结果
        Map<String, Object> result = new HashMap<>();
        result.put("message", "登录功能已屏蔽");
        return result;
    }
    
    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, Map<String, Object> userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (userInfo.containsKey("nickname")) {
            user.setNickname((String) userInfo.get("nickname"));
        }
        if (userInfo.containsKey("avatar")) {
            user.setAvatar((String) userInfo.get("avatar"));
        }
        if (userInfo.containsKey("gender")) {
            user.setGender((Integer) userInfo.get("gender"));
        }
        if (userInfo.containsKey("phone")) {
            user.setPhone((String) userInfo.get("phone"));
        }
        if (userInfo.containsKey("province")) {
            user.setProvince((String) userInfo.get("province"));
        }
        if (userInfo.containsKey("city")) {
            user.setCity((String) userInfo.get("city"));
        }
        if (userInfo.containsKey("score")) {
            user.setScore((Integer) userInfo.get("score"));
        }
        if (userInfo.containsKey("year")) {
            user.setYear((Integer) userInfo.get("year"));
        }
        if (userInfo.containsKey("subjectType")) {
            user.setSubjectType((String) userInfo.get("subjectType"));
        }
        
        userMapper.updateById(user);
        log.info("更新用户信息，userId: {}", userId);
    }
    
    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }
    
    /**
     * 添加收藏
     */
    public void addFavorite(Long userId, String type, Long targetId) {
        // 检查是否已收藏
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getType, type)
               .eq(UserFavorite::getTargetId, targetId);
        
        UserFavorite existing = userFavoriteMapper.selectOne(wrapper);
        if (existing != null) {
            return; // 已收藏，不重复添加
        }
        
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setType(type);
        favorite.setTargetId(targetId);
        favorite.setCreateTime(LocalDateTime.now());
        userFavoriteMapper.insert(favorite);
        
        log.info("添加收藏，userId: {}, type: {}, targetId: {}", userId, type, targetId);
    }
    
    /**
     * 取消收藏
     */
    public void removeFavorite(Long userId, String type, Long targetId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getType, type)
               .eq(UserFavorite::getTargetId, targetId);
        userFavoriteMapper.delete(wrapper);
        
        log.info("取消收藏，userId: {}, type: {}, targetId: {}", userId, type, targetId);
    }
    
    /**
     * 获取收藏列表
     */
    public List<UserFavorite> getFavorites(Long userId, String type) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(UserFavorite::getType, type);
        }
        wrapper.orderByDesc(UserFavorite::getCreateTime);
        return userFavoriteMapper.selectList(wrapper);
    }
    
    /**
     * 添加浏览历史
     */
    public void addHistory(Long userId, String type, Long targetId) {
        UserHistory history = new UserHistory();
        history.setUserId(userId);
        history.setType(type);
        history.setTargetId(targetId);
        history.setViewTime(LocalDateTime.now());
        userHistoryMapper.insert(history);
        
        log.info("添加浏览历史，userId: {}, type: {}, targetId: {}", userId, type, targetId);
    }
    
    /**
     * 获取浏览历史
     */
    public List<UserHistory> getHistory(Long userId, String type, Integer limit) {
        LambdaQueryWrapper<UserHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserHistory::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(UserHistory::getType, type);
        }
        wrapper.orderByDesc(UserHistory::getViewTime);
        if (limit != null && limit > 0) {
            wrapper.last("limit " + limit);
        }
        return userHistoryMapper.selectList(wrapper);
    }
    
    /**
     * 清空浏览历史
     */
    public void clearHistory(Long userId) {
        LambdaQueryWrapper<UserHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserHistory::getUserId, userId);
        userHistoryMapper.delete(wrapper);
        
        log.info("清空浏览历史，userId: {}", userId);
    }
}
