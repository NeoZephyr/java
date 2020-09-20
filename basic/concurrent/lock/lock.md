## ReentrantLock
## api
```java
void lock();
void unlock();
```

支持中断的 api，如果被其他线程中断，抛出 InterruptedException 异常
```java
void lockInterruptibly() throws InterruptedException;
```

支持非阻塞获取锁的 api，获取成功返回 true，否则返回 false
```java
boolean tryLock();
```

支持超时的 api，若在等待时发生中断抛出 InterruptedException 异常，若在等待时获得了锁，返回 true
```java
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
```

### 示例
```java
public class BlockedQueue<T> {
    final Lock lock = new ReentrantLock();

    final Condition notFull = lock.newCondition();
    final Condition notEmpty = lock.newCondition();

    void enqueue(T x) {
        lock.lock();

        try {
            while (full) {
                // 进入等待。被唤醒之后，并不立即执行，仅仅是从条件变量的等待队列进到入口等待队列里面
                // 因此，当再次执行的时候，条件可能已经不满足了，所以需要以循环方式检验条件变量
                notFull.await();
            }

            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    void dequeue() {
        lock.lock();

        try {
            while (empty) {
                notEmpty.await();
            }

            notFull.signal();
        } finally {
            lock.unlock();
        }
    }
}
```

signal 与 signalAll。如果要使用 signal，需要考虑以下三个条件：
1. 所有等待线程拥有相同的等待条件
2. 所有等待线程被唤醒后，执行相同的操作
3. 只需要唤醒一个线程


## ReadWriteLock
多个线程的读操作可以并行，在读多写少的场景中，让读操作并行可以明显提高性能

ReadWriteLock 基于 AQS 实现的，它的自定义同步器（继承 AQS）需要在同步状态 state 上维护多个读线程和一个写线程的状态。RRW 使用高低位实现一个整型控制两种状态的功能，读写锁将变量切分成了两个部分，高 16 位表示读，低 16 位表示写

### 获取写锁
一个线程尝试获取写锁时，会先判断同步状态 state 是否为 0。如果 state 等于 0，说明暂时没有其它线程获取锁；如果 state 不等于 0，则说明有其它线程获取了锁

此时再判断同步状态 state 的低 16 位（w）是否为 0，如果 w 为 0，则说明其它线程获取了读锁，此时进入 CLH 队列进行阻塞等待；如果 w 不为 0，则说明其它线程获取了写锁，此时要判断获取了写锁的是不是当前线程，若不是就进入 CLH 队列进行阻塞等待；若是，就应该判断当前线程获取写锁是否超过了最大次数，若超过，抛异常，反之更新同步状态

### 获取读锁
一个线程尝试获取读锁时，同样会先判断同步状态 state 是否为 0。如果 state 等于 0，说明暂时没有其它线程获取锁，此时判断是否需要阻塞，如果需要阻塞，则进入 CLH 队列进行阻塞等待；如果不需要阻塞，则 CAS 更新同步状态为读状态

如果 state 不等于 0，会判断同步状态低 16 位，如果存在写锁，则获取读锁失败，进入 CLH 阻塞队列；反之，判断当前线程是否应该被阻塞，如果不应该阻塞则尝试 CAS 同步状态，获取成功更新同步锁为读状态

```java
class Cache<K, V> {
    final Map<K, V> m = new HashMap<>();
    final ReadWriteLock lock = new ReentrantReadWriteLock();

    final Lock readLock = lock.readLock();
    final Lock writeLock = lock.writeLock();

    V get(K key) {
        V v = null;
        readLock.lock();

        try {
            V v = m.get(key);
        } finally {
            readLock.unlock();
        }

        if (v != null) {
            return v;
        }

        writeLock.lock();

        try {
            // 再次验证，避免高并发场景下重复查询数据的问题
            v = m.get(key);

            if (v == null) {
                // todo get v
                m.put(key, v);
            }
        } finally {
            writeLock.unlock();
        }

        return v;
    }
}
```

ReadWriteLock 并不支持锁的升级，但允许锁的降级
```java
volatile boolean cacheValid;

r.lock();

if (!cacheValid) {
    // 释放读锁，因为不允许读锁的升级
    r.unlock();
    w.lock();

    try {
        if (!cacheValid) {
            // get data
            cacheValid = true;
        }

        // 释放写锁前，降级为读锁
        r.lock();
    } finally {
        w.unlock();
    }

    try {
        use(data);
    } finally {
        r.unlock();
    }
}
```

只有写锁支持条件变量，读锁是不支持条件变量的，读锁调用 newCondition() 会抛出 UnsupportedOperationException 异常


## StampedLock
乐观读
```java
final StampedLock lock = new StampedLock();

// 获取悲观读锁
long stamp = lock.readLock();

try {
    // TODO
} finally {
    lock.unlockRead(stamp);
}
```

写锁
```java
final StampedLock lock = new StampedLock();

// 获取写锁
long stamp = sl.writeLock();

try {
    // TODO
} finally {
    lock.unlockWrite(stamp);
}
```

StampedLock 的性能之所以比 ReadWriteLock 好，其关键是 StampedLock 支持乐观读的方式。ReadWriteLock 支持多个线程同时读，但是当多个线程同时读的时候，所有的写操作会被阻塞；而 StampedLock 提供的乐观读，是允许一个线程获取写锁的，也就是说不是所有的写操作都被阻塞

```java
final StampedLock lock = new StampedLock();

// 乐观读
long stamp = lock.tryOptimisticRead();
int curX = x;
int curY = y;

// 判断执行读操作期间，是否存在写操作，如果存在则返回 false
if (!lock.validate(stamp)) {

    // 升级为悲观读锁
    stamp = lock.readLock();

    try {
        curX = x;
        curY = y;
    } finally {
        lock.unlockRead(stamp);
    }
}

return Math.sqrt(curX * curX + curY * curY);
```

有几点需要注意：
1. StampedLock 不支持重入
2. StampedLock 的悲观读锁、写锁都不支持条件变量
3. 使用 StampedLock 一定不要调用中断操作，如果需要支持中断功能，一定使用可中断的悲观读锁 readLockInterruptibly() 和写锁 writeLockInterruptibly()。否则可能导致 cpu 飙升


## AQS
AQS 类结构中包含一个基于链表实现的等待队列（CLH 队列），用于存储所有阻塞的线程，AQS 中还有一个 state 变量，该变量对 ReentrantLock 来说表示加锁状态

该队列的操作均通过 CAS 操作实现

获取锁的流程如下：
state 为 0 -> cas 获取锁 -> 获取成功 -> 更新 state 状态 -> 获取到锁
state 为 0 -> cas 获取锁 -> 获取失败 -> 将线程放入 CLH 列表中
state 不为 0 -> 锁为当前线程持有 -> 当前线程获取写锁超过最大次数 -> 异常
state 不为 0 -> 锁为当前线程持有 -> 当前线程获取写锁没有超过最大次数 -> 更新 state 状态 -> 获取到锁
state 不为 0 -> 锁不为当前线程持有 -> 将线程放入 CLH 列表中

线程进入 CLH 列表 -> 为首节点且 cas 获取锁 -> 更新 state 状态 -> 获取到锁
线程进入 CLH 列表 -> 非首节点或 cas 获取锁失败 -> 休眠等待其他线程释放锁


## ReentrantLock 与 synchronized
### 相同点
1. 都是独占锁，也是悲观锁
2. 都具有可重入性（通过记录锁的持有线程和持有数量实现）
3. 都具有内存可见性：释放锁时，把共享变量的最新值刷新到主内存；获得锁后，将清空工作内存中共享变量的值，从而使用共享变量时需要从主内存中重新读取最新的值

### 不同点
1. ReentrantLock 是类，synchronized 属于关键字
2. ReentrantLock 由 jdk 实现，synchronized 依赖 jvm 实现
3. ReentrantLock 提供能够中断等待锁的线程的机制，可以破坏死锁条件，synchronized 不支持
4. ReentrantLock 支持以非阻塞方式获取锁、限时等，可以破坏死锁条件，相对于 synchronized 更加灵活
5. ReentrantLock 可以指定为公平锁或非公平锁，synchronized 不能
6. ReentrantLock 提供 Condition 类，可以分组唤醒需要唤醒的线程


## 公平锁与非公平锁
ReentrantLock 和 synchronized 都是默认使用非公平锁，但 synchronized 无法设置公平锁

锁都对应着一个等待队列，如果一个线程没有获得锁，就会进入等待队列，当有线程释放锁的时候，就需要从等待队列中唤醒一个等待的线程。如果是公平锁，唤醒的策略就是谁等待的时间长，就唤醒谁，很公平；如果是非公平锁，当锁被释放之后，正好来了另外一个线程获取锁，那么该线程就获取到锁而不用排队

公平锁是减少线程饥饿情况发生的一个办法，但保证公平会让活跃线程得不到锁，进入等待状态，引起上下文切换，降低了整体的效率

在恢复一个被挂起线程与该线程真正开始运行之间，存在着一个很严重的延迟，这是由于线程间上下文切换带来的。因为这个延迟，造成公平锁在使用中出现 CPU 空闲。而非公平锁正是将这个延迟带来的时间差利用起来，优先让正在运行的线程获得锁，避免线程的上下文切换

## 活跃性问题
### 死锁
死锁条件
1. 互斥条件
2. 请求和保持条件
3. 不剥夺条件
4. 环路等待条件

避免死锁
1. 加锁顺序
2. 加锁时限
3. 死锁检测

减少锁竞争
1. 缩小锁范围，减小锁粒度，锁分段
2. 缓存热点数据
3. 读写锁、原子变量代替独占锁

### 活锁
两个线程互相谦让，即使没有发生阻塞，也会导致程序执行不下去。解决方法比较简单，就是尝试等待一个随机的时间

```java
class Account {
    private int balance;
    private final Lock lock = new ReentrantLock();

    void transfer(Account account, int amount) {
        while (true) {
            if (this.lock.tryLock()) {
                try {
                    if (tar.lock.tryLock()) {
                        try {
                            this.balance -= amount;
                            tar.balance += amount;
                            break;
                        } finally {
                            tar.lock.unlock();
                        }
                    }
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }
}
```

### 饥饿
线程因无法访问所需资源而无法执行下去。解决饥饿问题有以下方法：
1. 保证资源充足
2. 公平地分配资源（使用公平锁，排在等待队列前面的线程会优先获得资源）
3. 避免持有锁的线程长时间执行


## 性能问题
1. 使用无锁的算法和数据结构。例如：线程本地存储、写入时复制、乐观锁等
2. 减少锁持有的时间。例如：使用细粒度的锁、使用读写锁


## 注意事项
1. Integer 和 String 类型的对象在 JVM 里面是可能被重用的，不适合做锁。锁应该是私有的、不可变的、不可重用的
2. 多个线程安全操作组合，可能导致线程不安全