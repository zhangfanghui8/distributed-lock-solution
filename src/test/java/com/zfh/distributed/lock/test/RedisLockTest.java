package com.zfh.distributed.lock.test;

import com.zfh.distributed.lock.Bootstrap;
import com.zfh.distributed.lock.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @auth zhangfanghui
 * @since 2019-09-20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
public class RedisLockTest {

    @Autowired
    CacheService cacheService;
    @Test
    public void test(){
        String lockkey = "zfhtestW";
        boolean falg = cacheService.tryLock(lockkey,"test",120);
          falg = cacheService.tryLock(lockkey,"test",120);
    }
}
