package com.zfh.distributed.lock.zookeeper.protogenesis;

import com.zfh.distributed.lock.zookeeper.protogenesis.impl.ZookeeperLockImpl;

/**
 * @author zhangfanghui
 * @since 2019-11-07
 */
public class TestZk {
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ZookeeperLockImpl lock = null;
                try {
                    lock = new ZookeeperLockImpl("dev1.zk.scsite.net:2181", "test1");
                    if(lock.lock()){
                        // 模拟业务代码执的时间
                        try {
                            System.out.println(Thread.currentThread().getName() + "正在运行中");
                            Thread.sleep(1000);
                            System.out.println(Thread.currentThread().getName() + "运行结束");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        };
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
