package com.zfh.distributed.lock.zookeeper.curator;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangfanghui
 * @since 2019-12-17
 */
public class TestCurator {

    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                CuratorLockImpl lock = null;
                boolean isLocked = false;
                try {
                    lock = new CuratorLockImpl();
                    lock.newCuratorFramework();
                    if (lock.tryLock("testzf", 20L, TimeUnit.SECONDS)) {
                        isLocked = true;
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
//                    if (isLocked) {
//                        lock.unlock("testzf");
//                    }
                    //关闭Curator客户端
                    lock.closeClient();
                    System.out.println(Thread.currentThread().getName()+"关闭客户端");
                }
            }
        };
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
//
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                CuratorLockImpl lock = null;
//                boolean isLocked = false;
//                try {
//                    lock = new CuratorLockImpl();
//                    lock.newCuratorFramework();
//                    if (lock.lock("testzfh")) {
//                        isLocked = true;
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
//                    if (isLocked) {
//                        lock.unlock("test");
//                    }
//                    //关闭Curator客户端
//                    lock.closeClient();
//                }
//            }
//        };
//        for (int i = 0; i < 10; i++) {
//            Thread t = new Thread(runnable);
//            t.start();
//        }
    }
}