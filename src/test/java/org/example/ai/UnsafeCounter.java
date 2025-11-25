package org.example.ai;

// 非线程安全的计数器
class UnsafeCounter {
    private int count = 0;

    public void increment() {
        count++; // 非原子操作（读取-修改-写入）
    }

    public int getCount() {
        return count;
    }
}