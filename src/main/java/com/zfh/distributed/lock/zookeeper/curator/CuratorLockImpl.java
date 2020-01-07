package com.zfh.distributed.lock.zookeeper.curator;

import com.zfh.distributed.lock.zookeeper.ZkLock;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * https://www.cnblogs.com/qingyunzong/p/8666288.html
 * @author zhangfanghui
 * @since 2019-12-15
 */

/**
 * 疑问；
 * 1，怎么保证操作原子性
 * 2
 */
public class CuratorLockImpl implements ZkLock {
    ReentrantLock lock = new ReentrantLock();
    // 会话超时时间（毫秒）
    private static int sessionTimeoutMs = 60*1000;
    //
    private static int connectionTimeoutMs = 15*1000;

    private static String zkAddress = "dev1.zk.scsite.net:2181";

    String rootNode =  "/root" ;

    private CuratorFramework curatorFramework = null;

    private String currentLock = null;

    // 等待的前一个锁
    private String WAIT_LOCK = null;

    CountDownLatch latch = null;

    ThreadLocal<Map> mapThreadLocal = new ThreadLocal<Map>();

    public void newCuratorFramework(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorFramework =  CuratorFrameworkFactory.newClient(
                zkAddress,
                sessionTimeoutMs,
                connectionTimeoutMs,
                retryPolicy);
        curatorFramework.start();
    }

    public void closeClient(){
        if(Objects.nonNull(curatorFramework)){
            curatorFramework.close();
        }
    }


    private String assembleCompleteLockKey(String lockKey){
        if(!StringUtils.isEmpty(lockKey)){
           return rootNode+"/"+lockKey;
        }else{
            return null;
        }
    }

    @Override
    public Boolean tryLock(String lockKey, Long waiteTime, TimeUnit timeUnit) {
        try {
            // 创建临时节点（后面的数字为10位）
            currentLock = curatorFramework
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(assembleCompleteLockKey(lockKey));
            List<String> childrenNodes = curatorFramework.getChildren().forPath(rootNode);
            if(!CollectionUtils.isEmpty(childrenNodes)){
                //查看该lockkey的自节点
                childrenNodes =
                        childrenNodes.stream().filter(f -> f.indexOf(lockKey)!=-1).collect(Collectors.toList());
                Collections.sort(childrenNodes);
                if(currentLock.equals(assembleCompleteLockKey(childrenNodes.get(0)))){
                    return true;
                }else{
                    for(int i = 0 ; i < childrenNodes.size() ; i++){
                        if(currentLock.equals(assembleCompleteLockKey(childrenNodes.get(i)))){
                            WAIT_LOCK  = childrenNodes.get(i-1);
                        }
                    }
                    if(Objects.nonNull(WAIT_LOCK)){
                        try {
                            latch = new CountDownLatch(1);
                            // 监听方式一：跟原生api差不多，只监听一次，若要反复监听需多次注册
                            // 监听方式二：cache机智监听
                            curatorFramework.getData().usingWatcher(new Watcher() {
                                @Override
                                public void process(WatchedEvent event) {
                                    //删除节点事件
                                    System.out.println(WAIT_LOCK+"删除事件 " );
                                    latch.countDown();
                                }
                            }).forPath(assembleCompleteLockKey(WAIT_LOCK));

                             Boolean flg = latch.await(waiteTime,timeUnit);
                              return  flg;
                        }catch (Exception e){
                            e.printStackTrace();
                            System.out.println(e.getLocalizedMessage());
                        }
                    }
                    return false;
                }
            }else{
                System.out.println("跟节点没有子节点数据");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean lock(String lockKey) {
        try {
            // 创建临时节点
            currentLock = curatorFramework
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(assembleCompleteLockKey(lockKey));
            return true;
        } catch (Exception e) {
            if(e instanceof KeeperException.NodeExistsException){
                System.out.println("节点已经被占用，请稍后重试");
            }
            return false;
        }
    }

    @Override
    public Boolean unlock(String lockKey) {
        try {
            if (curatorFramework.checkExists().forPath(assembleCompleteLockKey(lockKey)) != null) {
                curatorFramework.delete().forPath(assembleCompleteLockKey(lockKey));
                System.out.println(lockKey+"锁删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework curatorFramework =  CuratorFrameworkFactory.newClient(
                zkAddress,
                sessionTimeoutMs,
                connectionTimeoutMs,
                retryPolicy);
        curatorFramework.start();
        try {
            List<String> childrenNodes = curatorFramework.getChildren().forPath("/root");
            if (curatorFramework.checkExists().forPath("/root/"+childrenNodes.get(0)) != null) {
                curatorFramework.delete().forPath("/root/"+childrenNodes.get(0));
                System.out.println(childrenNodes.get(0)+"锁删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
