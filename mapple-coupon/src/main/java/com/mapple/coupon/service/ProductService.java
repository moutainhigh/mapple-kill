package com.mapple.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mapple.common.utils.PageUtils;
import com.mapple.coupon.entity.ProductEntity;

import java.util.Map;

/**
 * 产品信息表
 *
 * @author sicheng
 * @email sicheng_zhou@qq.com
 * @date 2022-03-13 15:23:08
 */
public interface ProductService extends IService<ProductEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

