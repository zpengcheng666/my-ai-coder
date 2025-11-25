package org.example.ai;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

//public class CASTest {
//    private static final int THREAD_NUM = 1000;
//    private static final int TASK_NUM = 1000;
//
//    public static void main(String[] args) throws InterruptedException {
//        UnsafeCounter unsafeCounter = new UnsafeCounter();
//        CountDownLatch latch = new CountDownLatch(THREAD_NUM);
//
//        // 启动1000个线程，每个线程计数1000次
//        for (int i = 0; i < THREAD_NUM; i++) {
//            new Thread(() -> {
//                for (int j = 0; j < TASK_NUM; j++) {
//                    unsafeCounter.increment();
//                }
//                latch.countDown();
//            }).start();
//        }
//
//        latch.await(); // 等待所有线程完成
//        System.out.println("非CAS计数器结果（预期1000000）：" + unsafeCounter.getCount());
//    }
//}

// CAS原子计数器
class CASCounter {
    private AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        // 方式1：直接调用原子方法（底层CAS）
        count.incrementAndGet();

        // 方式2：手动CAS循环（模拟incrementAndGet底层逻辑）
        /*
        int oldValue;
        int newValue;
        do {
            oldValue = count.get(); // 获取当前值（预期值）
            newValue = oldValue + 1; // 计算新值
            // CAS操作：如果当前值等于oldValue，则更新为newValue，返回true；否则返回false重试
        } while (!count.compareAndSet(oldValue, newValue));
        */
    }

    public int getCount() {
        return count.get();
    }
}

public class CASTest {
    private static final int THREAD_NUM = 1000;
    private static final int TASK_NUM = 1000;

    public static void main(String[] args) throws InterruptedException {
        CASCounter casCounter = new CASCounter();
        CountDownLatch latch = new CountDownLatch(THREAD_NUM);

        for (int i = 0; i < THREAD_NUM; i++) {
            new Thread(() -> {
                for (int j = 0; j < TASK_NUM; j++) {
                    casCounter.increment();
                }
                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println("CAS计数器结果（预期1000000）：" + casCounter.getCount());
    }
}
