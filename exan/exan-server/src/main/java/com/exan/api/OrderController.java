package com.exan.api;

import com.exan.app.dto.order.CreateOrderRequest;
import com.exan.app.dto.order.OrderVO;
import com.exan.app.service.OrderService;
import com.exan.domain.entity.Entitlement;
import com.exan.infra.web.ApiResponse;
import com.exan.infra.web.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderVO> create(@RequestBody @Valid CreateOrderRequest req, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        return ApiResponse.ok(orderService.createOrder(userId, req.productId()));
    }

    @PostMapping("/{orderNo}/mock-pay")
    public ApiResponse<Void> mockPay(@PathVariable String orderNo, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        orderService.mockPay(userId, orderNo);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<OrderVO>> list(@RequestParam(defaultValue = "20") int limit, HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        return ApiResponse.ok(orderService.listOrders(userId, limit));
    }

    @GetMapping("/entitlements")
    public ApiResponse<List<Entitlement>> entitlements(HttpServletRequest request) {
        long userId = UserContext.currentUserId(request);
        return ApiResponse.ok(orderService.listEntitlements(userId));
    }
}
