package com.zfh.distributed.lock.zookeeper;

import java.util.concurrent.TimeUnit;

/**
 * @auth zhangfanghui
 * @since 2019-10-30
 */
public interface ZkLock {
    /**
     * 尝试获取锁 (zk 临时顺序节点)
     * @param lockKey
     * @param waiteTime
     * @param timeUnit
     * @return zk节点全称
     */
    Boolean tryLock(String lockKey,Long waiteTime, TimeUnit timeUnit);

    /**
     * 获取锁，立即返回结果 (zk 临时节点)
     * @param lockKey
     * @return
     */
    Boolean lock(String lockKey);

    /**
     *针对除了临时节点的释放锁
     * @param lockKey
     * @return
     */
    Boolean unlock(String lockKey);

    /**
     * 关闭客户端
     */
    void closeClient();


}
