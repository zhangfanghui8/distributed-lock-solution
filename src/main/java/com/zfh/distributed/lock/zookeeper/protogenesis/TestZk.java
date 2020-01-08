package com.zfh.distributed.lock.zookeeper.protogenesis;

import com.zfh.distributed.lock.zookeeper.protogenesis.impl.ZookeeperLockImpl;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangfanghui
 * @since 2019-11-07
 */
public class TestZk {
    public static void main(String[] args) {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                ZookeeperLockImpl lock = null;
//                try {
//                    lock = new ZookeeperLockImpl("dev1.zk.scsite.net:2181");
//                    if(lock.lock("zfh")){
//                        // 模拟业务代码执的时间
//                        try {
//                            System.out.println(Thread.currentThread().getName() + "正在运行中");
//                            Thread.sleep(1000);
//                            System.out.println(Thread.currentThread().getName() + "运行结束");
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } finally {
//                    lock.closeClient();
//                }
//            }
//        };
//        for (int i = 0; i < 3; i++) {
//            Thread t = new Thread(runnable);
//            t.start();
//        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ZookeeperLockImpl lock = null;
                boolean flg = false;
                try {
                    lock = new ZookeeperLockImpl("dev1.zk.scsite.net:2181");
                    if(lock.tryLock("zfh",20L, TimeUnit.SECONDS)){
                        // 模拟业务代码执的时间
                        flg = true;
                        try {
                            System.out.println(Thread.currentThread().getName() + "正在运行中");
                            Thread.sleep(1000);
                            System.out.println(Thread.currentThread().getName() + "运行结束");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    lock.closeClient();
                }
            }
        };
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
