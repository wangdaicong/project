package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.User;
import com.volunteer.exam.entity.UserFavorite;
import com.volunteer.exam.entity.UserHistory;
import com.volunteer.exam.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    
    @Resource
    private UserService userService;
    
    /**
     * 微信登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("登录凭证不能为空");
        }
        
        try {
            Map<String, Object> result = userService.wxLogin(code);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/info/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        User user = userService.getUserInfo(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info/{userId}")
    public Result updateUserInfo(@PathVariable Long userId, @RequestBody Map<String, Object> userInfo) {
        try {
            userService.updateUserInfo(userId, userInfo);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加收藏
     */
    @PostMapping("/favorite")
    public Result addFavorite(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String type = (String) params.get("type");
        Long targetId = Long.valueOf(params.get("targetId").toString());
        
        try {
            userService.addFavorite(userId, type, targetId);
            return Result.success("收藏成功");
        } catch (Exception e) {
            return Result.error("收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/favorite")
    public Result removeFavorite(@RequestParam Long userId, 
                                 @RequestParam String type, 
                                 @RequestParam Long targetId) {
        try {
            userService.removeFavorite(userId, type, targetId);
            return Result.success("取消收藏成功");
        } catch (Exception e) {
            return Result.error("取消收藏失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取收藏列表
     */
    @GetMapping("/favorite/{userId}")
    public Result<List<UserFavorite>> getFavorites(@PathVariable Long userId, 
                                                    @RequestParam(required = false) String type) {
        List<UserFavorite> favorites = userService.getFavorites(userId, type);
        return Result.success(favorites);
    }
    
    /**
     * 添加浏览历史
     */
    @PostMapping("/history")
    public Result addHistory(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String type = (String) params.get("type");
        Long targetId = Long.valueOf(params.get("targetId").toString());
        
        try {
            userService.addHistory(userId, type, targetId);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取浏览历史
     */
    @GetMapping("/history/{userId}")
    public Result<List<UserHistory>> getHistory(@PathVariable Long userId, 
                                                 @RequestParam(required = false) String type,
                                                 @RequestParam(required = false) Integer limit) {
        List<UserHistory> history = userService.getHistory(userId, type, limit);
        return Result.success(history);
    }
    
    /**
     * 清空浏览历史
     */
    @DeleteMapping("/history/{userId}")
    public Result clearHistory(@PathVariable Long userId) {
        try {
            userService.clearHistory(userId);
            return Result.success("清空成功");
        } catch (Exception e) {
            return Result.error("清空失败: " + e.getMessage());
        }
    }
}
