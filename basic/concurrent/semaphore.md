## 信号量方法
1. init 设置计数器的初始值
2. down 计数器的值减 1；如果此时计数器的值小于 0，则当前线程将被阻塞
3. up 计数器的值加 1；如果此时计数器的值小于或者等于 0，则唤醒等待队列中的一个线程（只能唤醒一个），并将其从等待队列中移除

这三个方法都是原子性的


## semaphore 示例
```java
static int count;
static final Semaphore semaphore = new Semaphore(1);

static void addOne() {
    // 阻塞获取许可
    semaphore.acquire();

    try {
        count += 1;
    } finally {
        semaphore.release();
    }
}
```

semaphore 可以允许多个线程访问一个临界区
```java
class ObjectPool<T, R> {
    final List<T> pool;
    final Semaphore semaphore;

    ObjectPool(int size, T t) {
        pool = new Vector<T>(){};

        for (int i = 0; i < size; ++i) {
            pool.add(t);
        }

        semaphore = new Semaphore(size);
    }

    R exec(Function<T, R> func) {
        T t = null;
        semaphore.acquire();
        try {
            t = pool.remove(0);
            return func.apply(t);
        } finally {
            pool.add(t);
            semaphore.release();
        }
    }
}

ObjectPool<Long, String> pool = new ObjectPool<Long, String>(10, 2);
pool.exec(t -> {
    return t.toString();
});
```


## Semaphore VS 锁
Semaphore 若将 permits 的值设为 1，类似于锁但又与一般的锁不同

1. 一般锁只能由持有锁的线程释放，而 Semaphore 只是表示一个许可数，任意线程都可以调用其 release 方法
2. Semaphore 不可重入，每次 acquire 调用都会消耗一个许可