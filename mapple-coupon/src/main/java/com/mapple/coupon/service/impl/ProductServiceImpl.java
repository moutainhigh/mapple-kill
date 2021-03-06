package com.mapple.coupon.service.impl;

import java.math.BigDecimal;
import java.util.*;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mapple.common.exception.RRException;
import com.mapple.common.utils.redis.cons.RedisKeyUtils;
import com.mapple.coupon.dao.ProductSessionDao;
import com.mapple.coupon.entity.ProductSessionEntity;
import com.mapple.coupon.entity.vo.productSessionVo_Skus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mapple.common.utils.PageUtils;
import com.mapple.common.utils.Query;

import com.mapple.coupon.dao.ProductDao;
import com.mapple.coupon.entity.ProductEntity;
import com.mapple.coupon.service.ProductService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("productService")
public class ProductServiceImpl extends ServiceImpl<ProductDao, ProductEntity> implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductSessionDao productSessionDao;

    @Autowired
    public RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductEntity> page = this.page(
                new Query<ProductEntity>().getPage(params),
                new QueryWrapper<ProductEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public void saveProduct(ProductEntity product) {
        checkRiskLevel(product);
        if (checkDepositTime(product)) {
            save(product);
        }
    }

    @Override
    @Transactional
    public void updateProductById(productSessionVo_Skus productSessionVo_Skus) {
        //???????????????????????????
        BoundHashOperations<String, String, String> operationsForSku = redisTemplate.boundHashOps(RedisKeyUtils.SKU_PREFIX);
        //???????????????????????????id???????????????list??????
        ArrayList<String> sessionId_list = new ArrayList<>();
        Set<String> sku_keys = operationsForSku.keys();
        for (String sku_key : sku_keys) {
            if (sku_key.contains(productSessionVo_Skus.getProductId())){
                sessionId_list.add(sku_key.split("-")[0]);
            }
        }
        //????????????session????????????????????????
        BoundHashOperations<String, String, String> operations_forSessions = redisTemplate.boundHashOps(RedisKeyUtils.SESSIONS_PREFIX);
        Date nowTime = new Date();
        for (String sessionId : sessionId_list) {
            String sessionValue = operations_forSessions.get(sessionId);
            String startTime = sessionValue.split("-")[0];
            if (Long.parseLong(startTime)<nowTime.getTime()){
                throw new RRException("????????????????????????????????????????????????");
            }
        }


        //???????????????????????????????????????redis??????
        ProductEntity product = new ProductEntity();
        //??????product??????
        BeanUtils.copyProperties(productSessionVo_Skus, product);
        //??????product????????????????????????
        if (!StringUtils.isEmpty(product.getRiskLevel())) {
            checkRiskLevel(product);
        }
        if (!StringUtils.isEmpty(product.getDepositTime())) {
            checkDepositTime(product);
        }
        //????????????
        productDao.updateById(product);

        //??????????????????????????????????????????
        if ((!StringUtils.isEmpty(productSessionVo_Skus.getSeckillPrice()) || (!StringUtils.isEmpty(productSessionVo_Skus.getTotalCount())))) {
            ProductSessionEntity productSessionEntity = new ProductSessionEntity();
            BeanUtils.copyProperties(productSessionVo_Skus, productSessionEntity);
            //?????????????????????
            productSessionDao.update(productSessionEntity
                    ,new QueryWrapper<ProductSessionEntity>()
                            .eq("session_id",productSessionEntity.getSessionId())
                            .eq("product_id",productSessionEntity.getProductId()));
        }

        //??????redis?????????
        //1. redis hash????????????sku?????????

        //??????rediskey
//        String sessionId = productSessionVo_Skus.getSessionId();
        String randomCode=null;
        for (String sessionId : sessionId_list) {
            String redisKey =  sessionId + "-" + productSessionVo_Skus.getProductId();
            //???redis???????????????vo??????
            if (operationsForSku.hasKey(redisKey)) {
                productSessionVo_Skus productSessionVo_skus_fromRedis = JSON.parseObject(operationsForSku.get(redisKey), productSessionVo_Skus.class);
                //?????????????????????????????????vo??? ,??????????????????????????????
                assert productSessionVo_skus_fromRedis != null;
                BeanUtil.copyProperties(productSessionVo_Skus, productSessionVo_skus_fromRedis, new CopyOptions().setIgnoreNullValue(true));
                operationsForSku.put(redisKey, //???fastJson?????????json??????
                        JSON.toJSONString(productSessionVo_skus_fromRedis));
            }

            //2. skus??????
            BoundHashOperations<String, String, String> operationsForSkus = redisTemplate.boundHashOps(RedisKeyUtils.SKUS_PREFIX);
            //???????????????????????????????????????
            if (operationsForSkus.hasKey(sessionId)) {
                String skus_fromRedis = operationsForSkus.get(sessionId);
                List<productSessionVo_Skus> skusList = JSONObject.parseArray(skus_fromRedis, productSessionVo_Skus.class);
                for (int i = 0; i < skusList.size(); i++) {
                    if (skusList.get(i).getProductId().equals(productSessionVo_Skus.getProductId())){
                        productSessionVo_Skus vo_skus = skusList.get(i);
                        BeanUtil.copyProperties(productSessionVo_Skus,vo_skus, new CopyOptions().setIgnoreNullValue(true));
                        skusList.set(i,vo_skus);
                        //???????????????randomcode
                        randomCode = vo_skus.getRandomCode();
                    }
                }
                operationsForSkus.delete(sessionId);
                operationsForSkus.put(sessionId,JSON.toJSONString(skusList));
            }
        }

        //3.stock????????????
        //??????????????????
        if (productSessionVo_Skus.getTotalCount()>0){
            Integer stockCount = productSessionVo_Skus.getTotalCount();
            if (!StringUtils.isEmpty(stockCount)){
                ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
                opsForValue.set(RedisKeyUtils.STOCK_PREFIX+randomCode,
                        stockCount.toString());
            }
        }

    }


    /**
     * ??????????????????
     *
     * @param product
     */
    private Boolean checkDepositTime(ProductEntity product) {
        String depositTime = product.getDepositTime();
        String[] strs = depositTime.split("-");
        int year = Integer.parseInt(strs[0]);
        int month = Integer.parseInt(strs[1]);
        if ((year == 0 && month > 0) || (year > 0 && year < 100 && month == 0)) {
            return true;
        } else {
            throw new RRException("??????????????????");
        }
    }

    /**
     * ???????????????????????????
     *
     * @param product
     */
    private void checkRiskLevel(ProductEntity product) {
        Integer riskLevel = product.getRiskLevel();
        if (riskLevel != 1 && riskLevel != 2 && riskLevel != 3) {
            throw new RRException("????????????????????????");
        }
    }


    //2. ??????????????????

}
