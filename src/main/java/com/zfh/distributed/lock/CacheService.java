package com.zfh.distributed.lock;

/**
 * @auth zhangfanghui
 * @since 2019-09-19
 */
public interface CacheService {
    /**
     * 尝试获取锁
     * @param lockKey
     * @param clientId
     * @param seconds
     * @return
     */
    Boolean tryLock(String lockKey, String clientId, long seconds);

    /**
     * 释放锁
     * @param lockKey
     * @param clientId
     * @return
     */
    Boolean releaseLock(String lockKey, String clientId);

}
