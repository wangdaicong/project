package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
import com.yibiai.thesis.dto.LoginRequest;
import com.yibiai.thesis.dto.RegisterRequest;
import com.yibiai.thesis.entity.User;
import com.yibiai.thesis.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            user.setPassword(null);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            user.setPassword(null);
            return ApiResponse.success("登录成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ApiResponse.success(user);
                })
                .orElse(ApiResponse.error("用户不存在"));
    }
}
