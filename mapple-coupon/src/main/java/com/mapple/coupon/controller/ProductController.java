package com.mapple.coupon.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.mapple.common.utils.PageUtils;
import com.mapple.common.utils.result.CommonResult;
import com.mapple.coupon.entity.ProductEntity;
import com.mapple.coupon.entity.vo.productSessionVo_Skus;
import com.mapple.coupon.service.ProductService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Map;


/**
 * 产品信息表
 *
 * @author sicheng
 * @email sicheng_zhou@qq.com
 * @date 2022-03-13 15:23:08
 */
@RestController
@RequestMapping("coupon/product")
public class ProductController {
    @Resource
    private ProductService productService;

    /**
     * 列表
     */
    @GetMapping("/list")
//    @SentinelResource(value = "couponlist"
//            ,blockHandlerClass = sentinelHandler.class
//            ,blockHandler = "handlerExceptionSentinel")
    public CommonResult list(@RequestParam Map<String, Object> params){
        PageUtils page = productService.queryPage(params);
        return CommonResult.ok().put("page", page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    //@RequiresPermissions("coupon:product:info")
    public CommonResult info(@PathVariable("id") String id){
		ProductEntity product = productService.getById(id);

        return CommonResult.ok().put("product", product);
    }

    /**
     * 保存产品信息
     */
    @ApiOperation(value = "添加产品信息")
    @PostMapping("/save")
    public CommonResult save(@Valid @ApiParam(name = "productSessionVo_new",
            value = "请传入productEntity中的信息，注意产品存期的格式",
            required = true)@RequestBody ProductEntity product){
        productService.saveProduct(product);
        return CommonResult.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("曹尼玛")
    //@RequiresPermissions("coupon:product:update")
    public CommonResult update(@Valid @ApiParam(name = "productSessionVo_Skus",
            value = "请传入sessionId和productId,然后加上要修改的字段，注意一次修改一个产品信息，修改产品字段或者修改场次和产品关联的两个字段：秒杀价和库存，都可以在这个接口传入",
            required = true)@RequestBody productSessionVo_Skus productSessionVo_Skus){
        productService.updateProductById(productSessionVo_Skus);
        return CommonResult.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    //@RequiresPermissions("coupon:product:delete")
    public CommonResult delete(@RequestBody String[] ids){
		productService.removeByIds(Arrays.asList(ids));

        return CommonResult.ok();
    }

}
