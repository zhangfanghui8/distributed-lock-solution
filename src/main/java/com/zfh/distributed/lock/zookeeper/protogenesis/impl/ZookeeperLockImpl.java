package com.zfh.distributed.lock.zookeeper.protogenesis.impl;

import com.zfh.distributed.lock.zookeeper.ZkLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @auth zhangfanghui
 * @since 2019-10-30
 */
public class ZookeeperLockImpl implements ZkLock, Watcher {
    private ZooKeeper zk = null;
    // 根节点
    private String ROOT_LOCK = "/root";
    // 等待的前一个锁
    private String WAIT_LOCK;
    // 当前锁
    private String CURRENT_LOCK;
    // 计数器
    private CountDownLatch countDownLatch;
    private int sessionTimeout = 30000;

    /**
     * 配置分布式锁
     * @param config 连接的url
     */
    public ZookeeperLockImpl(String config) {
        try {
            // 连接zookeeper
            zk = new ZooKeeper(config, sessionTimeout, this);
            Stat stat = zk.exists(ROOT_LOCK, false);
            if (stat == null) {
                // 如果根节点不存在，则创建根节点
                zk.create(ROOT_LOCK, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
    @Override
    public Boolean tryLock(String lockKey, Long waiteTime, TimeUnit timeUnit) {
        try {
            // 创建临时有序节点 (临时有序节点，zK自动会在节点名称后面加序号)
            CURRENT_LOCK = zk.create(assembleCompleteLockKey(lockKey), new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName()+"："+CURRENT_LOCK + " 已经创建");
            // 取所有子节点
            List<String> subNodes = zk.getChildren(ROOT_LOCK, null);
            //查看该lockkey的自节点
            subNodes =
                    subNodes.stream().filter(f -> f.indexOf(lockKey)!=-1).collect(Collectors.toList());
            Collections.sort(subNodes);
            if(CURRENT_LOCK.equals(assembleCompleteLockKey(subNodes.get(0)))){
                return true;
            }else{
                // 取出所有lockName的锁
                for (int i = 0 ; i < subNodes.size() ; i++) {
                    if(CURRENT_LOCK.equals(assembleCompleteLockKey(subNodes.get(i)))){
                        WAIT_LOCK = assembleCompleteLockKey(subNodes.get(i-1));
                    }
                }
                if(Objects.nonNull(WAIT_LOCK)){
                    zk.exists(WAIT_LOCK,this);
                }
                this.countDownLatch = new CountDownLatch(1);
                // 计数等待，若等到前一个节点消失，则precess中进行countDown，停止等待，获取锁
                boolean flg = this.countDownLatch.await(waiteTime, timeUnit);
                return flg;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void closeClient(){
        if(Objects.nonNull(zk)){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public Boolean lock(String lockKey) {
        // 创建临时有序节点 (临时有序节点，zK自动会在节点名称后面加序号)
        try {
            CURRENT_LOCK = zk.create(assembleCompleteLockKey(lockKey), new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            return true;
        } catch (KeeperException e) {
            if(e instanceof KeeperException.NodeExistsException){
                System.out.println("锁已经被占用，请稍后重试");
            }else{
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean unlock(String lockKey) {
        try {
            System.out.println("释放锁 " + CURRENT_LOCK);
            zk.delete(CURRENT_LOCK, -1);

            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void process(WatchedEvent event) {
        if (Event.EventType.NodeDeleted.name().equals(event.getType().name()) && this.countDownLatch != null) {
            this.countDownLatch.countDown();
        }
    }
    public class LockException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public LockException(String e){
            super(e);
        }
        public LockException(Exception e){
            super(e);
        }
    }
    private String assembleCompleteLockKey(String lockKey){
        if(!StringUtils.isEmpty(lockKey)){
            return ROOT_LOCK+"/"+lockKey;
        }else{
            return null;
        }
    }
}
