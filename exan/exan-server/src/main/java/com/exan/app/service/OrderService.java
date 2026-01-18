package com.exan.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exan.app.dto.order.OrderVO;
import com.exan.domain.entity.Entitlement;
import com.exan.domain.entity.Orders;
import com.exan.domain.entity.Product;
import com.exan.domain.mapper.EntitlementMapper;
import com.exan.domain.mapper.OrdersMapper;
import com.exan.domain.mapper.ProductMapper;
import com.exan.infra.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductMapper productMapper;
    private final OrdersMapper ordersMapper;
    private final EntitlementMapper entitlementMapper;

    @Transactional
    public OrderVO createOrder(long userId, long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null || !"ONLINE".equals(p.getStatus())) {
            throw new BizException(404, "商品不存在");
        }

        Orders o = new Orders();
        o.setUserId(userId);
        o.setProductId(productId);
        o.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        o.setAmountCent(p.getPriceCent());
        o.setStatus("CREATED");
        o.setPayChannel("MOCK");
        o.setCreatedAt(LocalDateTime.now());
        ordersMapper.insert(o);

        return new OrderVO(o.getOrderNo(), o.getAmountCent(), o.getStatus(), o.getCreatedAt(), o.getPaidAt());
    }

    @Transactional
    public void mockPay(long userId, String orderNo) {
        Orders o = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
            .eq(Orders::getOrderNo, orderNo));
        if (o == null || o.getUserId() == null || o.getUserId() != userId) {
            throw new BizException(404, "订单不存在");
        }
        if ("PAID".equals(o.getStatus())) {
            return;
        }
        if (!"CREATED".equals(o.getStatus())) {
            throw new BizException(400, "订单状态不允许支付");
        }

        Product p = productMapper.selectById(o.getProductId());
        if (p == null) {
            throw new BizException(400, "商品不存在");
        }

        o.setStatus("PAID");
        o.setPaidAt(LocalDateTime.now());
        ordersMapper.updateById(o);

        Entitlement ent = entitlementMapper.selectOne(new LambdaQueryWrapper<Entitlement>()
            .eq(Entitlement::getUserId, userId)
            .eq(Entitlement::getEntType, p.getProductType())
            .eq(Entitlement::getRefId, p.getRefId()));
        if (ent == null) {
            ent = new Entitlement();
            ent.setUserId(userId);
            ent.setEntType(p.getProductType());
            ent.setRefId(p.getRefId());
            ent.setOrderId(o.getId());
            ent.setStatus("ACTIVE");
            ent.setCreatedAt(LocalDateTime.now());
            entitlementMapper.insert(ent);
        }
    }

    public List<OrderVO> listOrders(long userId, int limit) {
        int l = Math.min(Math.max(limit, 1), 50);
        List<Orders> list = ordersMapper.selectList(new LambdaQueryWrapper<Orders>()
            .eq(Orders::getUserId, userId)
            .orderByDesc(Orders::getId)
            .last("limit " + l));
        return list.stream()
            .map(o -> new OrderVO(o.getOrderNo(), o.getAmountCent(), o.getStatus(), o.getCreatedAt(), o.getPaidAt()))
            .toList();
    }

    public List<Entitlement> listEntitlements(long userId) {
        return entitlementMapper.selectList(new LambdaQueryWrapper<Entitlement>()
            .eq(Entitlement::getUserId, userId)
            .eq(Entitlement::getStatus, "ACTIVE")
            .orderByDesc(Entitlement::getId));
    }
}
