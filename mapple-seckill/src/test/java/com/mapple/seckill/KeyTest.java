package com.mapple.seckill;

import com.mapple.common.utils.redis.cons.Key;
import com.mapple.common.utils.redis.cons.RedisKeyUtils;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author zsc
 * @version 1.0
 * @date 2022/3/24 0:53
 */
@SpringBootTest
public class KeyTest {
    @Resource
    RedissonClient redissonClient;
    @Resource
    private HashOperations<String, String, String> hashOperations;
    @Test
    public void test(){
        System.out.println(Key.SKU_PREFIX.name());
        RMapCache<Object, Object> userMap = redissonClient.getMapCache(RedisKeyUtils.SECKILL_USER_PREFIX);
        userMap.put("userId" + "-" + "key", "1", 60, TimeUnit.SECONDS);
//        System.out.println(hashOperations.hasKey(RedisKeyUtils.SECKILL_USER_PREFIX, "userId" + "-" + "key"));
    }
}
