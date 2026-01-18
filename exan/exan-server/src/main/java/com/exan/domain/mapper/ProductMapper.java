package com.exan.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exan.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
