package com.zfh.distributed.lock.zookeeper.protogenesis;

import org.apache.zookeeper.KeeperException;

/**
 * @auth zhangfanghui
 * @since 2019-10-30
 */
public interface ZookeeperLock {
    /**
     *
     * @return
     */
    boolean tryLock();
    boolean waitForLock(String prev, long waitTime)throws KeeperException, InterruptedException ;
    boolean lock();

}
