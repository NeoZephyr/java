## 活跃性问题
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


## Lock api
```java
void lock();
void unlock();
```

支持中断的 api，如果被其他线程中断，抛出 `InterruptedException` 异常
```java
void lockInterruptibly() throws InterruptedException;
```

支持非阻塞获取锁的 api，获取成功返回 `true`，否则返回 `false`
```java
boolean tryLock();
```

支持超时的 api，若在等待时发生中断抛出 `InterruptedException` 异常，若在等待时获得了锁，返回 `true`
```java
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
```


## 公平锁与非公平锁
ReentrantLock 和 synchronized 都是默认使用非公平锁，但 synchronized 无法设置公平锁

锁都对应着一个等待队列，如果一个线程没有获得锁，就会进入等待队列，当有线程释放锁的时候，就需要从等待队列中唤醒一个等待的线程。如果是公平锁，唤醒的策略就是谁等待的时间长，就唤醒谁，很公平；如果是非公平锁，当锁被释放之后，正好来了另外一个线程获取锁，那么该线程就获取到锁而不用排队

公平锁是减少线程饥饿情况发生的一个办法，但保证公平会让活跃线程得不到锁，进入等待状态，引起上下文切换，降低了整体的效率

在恢复一个被挂起线程与该线程真正开始运行之间，存在着一个很严重的延迟，这是由于线程间上下文切换带来的。因为这个延迟，造成公平锁在使用中出现 CPU 空闲。而非公平锁正是将这个延迟带来的时间差利用起来，优先让正在运行的线程获得锁，避免线程的上下文切换


## Lock 示例
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

内部使用同一个整数变量表示锁的状态，16 位用于读锁，16 位用于写锁。使用一个变量便于进行 CAS 操作，锁的等待队列其实也只有一个。写锁的获取需要确保当前没有其他线程持有任何锁，否则就等待。写锁释放后，也就是将等待队列中的第一个线程唤醒，唤醒的可能是等待读锁的，也可能是等待写锁的。读锁的获取只需要写锁没有被持有就可以获取。在获取到读锁后会检查等待队列，逐个唤醒最前面的等待读锁的线程，直到第一个等待写锁的线程。若有其他线程持有写锁，获取读锁会等待

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


## `AbstractQueuedSynchronizer`
```java
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    protected AbstractQueuedSynchronizer() {}
    
    // 等待队列中的线程被包装成 Node 节点
    static final class Node {
    
        // 标识节点在共享模式下等待
        static final Node SHARED = new Node();
        
        // 标识节点在独占模式下等待
        static final Node EXCLUSIVE = null;
        
        // 线程取消等待锁
        static final int CANCELLED =  1;
        
        // 当前节点的后继节点对应的线程需要被唤醒
        static final int SIGNAL    = -1;
        static final int CONDITION = -2;
        static final int PROPAGATE = -3;
        
        volatile int waitStatus;
        
        // 前驱节点
        volatile Node prev;
        
        // 后继节点
        volatile Node next;
        
        // 节点对应线程
        volatile Thread thread;
        Node nextWaiter;
        
        final boolean isShared() {
            return nextWaiter == SHARED;
        }
        
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }
        
        Node() {}
        
        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }
        
        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
    
    private transient volatile Node head;
    
    private transient volatile Node tail;
    
    private volatile int state;
    
    
}
```

### AQS 状态
```java
protected final int getState() {
    return state;
}

protected final void setState(int newState) {
    state = newState;
}

protected final boolean compareAndSetState(int expect, int update) {
    // See below for intrinsics setup to support this
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```

### 获取锁
```java
public final void acquire(int arg) {
    if (!tryAcquire(arg) &&
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
        // 设置中断标志位
        selfInterrupt();
}
```
```java
// 尝试获取锁
protected boolean tryAcquire(int arg) {
    throw new UnsupportedOperationException();
}
```
```java
// 将 node 节点加入阻塞队列
private Node addWaiter(Node mode) {
    Node node = new Node(Thread.currentThread(), mode);
    // Try the fast path of enq; backup to full enq on failure
    Node pred = tail;
    
    // 队列不为空
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
    
    // 队列为空
    enq(node);
    return node;
}

private Node enq(final Node node) {
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
}

final boolean acquireQueued(final Node node, int arg) {
    boolean failed = true;
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            
            // 当前 head 可能是刚刚初始化的 node, 不属于任一线程，可以先尝试获取锁
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                failed = false;
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}

// 一般第一次进入该方法，返回 false
private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
    int ws = pred.waitStatus;
    if (ws == Node.SIGNAL)
        return true;

    // 前驱节点中的线程已经取消
    if (ws > 0) {
        do {
            node.prev = pred = pred.prev;
        } while (pred.waitStatus > 0);
        pred.next = node;
    } else {
        // CAS 设置前驱节点 waitStatus 为 Node.SIGNAL
        compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
    }
    return false;
}

// 挂起线程，等待唤醒
private final boolean parkAndCheckInterrupt() {
    LockSupport.park(this);
    return Thread.interrupted();
}
```

### 释放锁
```java
public final boolean release(int arg) {
    if (tryRelease(arg)) {
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
```
```java
// 尝试释放锁
protected boolean tryRelease(int arg) {
    throw new UnsupportedOperationException();
}
```
```java
private void unparkSuccessor(Node node) {

    int ws = node.waitStatus;
    if (ws < 0)
        compareAndSetWaitStatus(node, ws, 0);

    // 寻找排在最前且未取消的节点
    Node s = node.next;
    if (s == null || s.waitStatus > 0) {
        s = null;
        for (Node t = tail; t != null && t != node; t = t.prev)
            if (t.waitStatus <= 0)
                s = t;
    }
    if (s != null)
        // 唤醒线程
        LockSupport.unpark(s.thread);
}
```

### `AbstractOwnableSynchronizer`
属于 `AbstractQueuedSynchronizer` 的父类，用于保存锁的当前持有线程，提供了方法进行查询和设置
```java
public abstract class AbstractOwnableSynchronizer
    implements java.io.Serializable {

    protected AbstractOwnableSynchronizer() {}
    
    private transient Thread exclusiveOwnerThread;
    
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }
    
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
```


## `ReentrantLock`
```java
public class ReentrantLock implements Lock, java.io.Serializable {
    
    abstract static class Sync extends AbstractQueuedSynchronizer {
        abstract void lock();
        
        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
        
        // 尝试释放锁，公平锁与非公平锁共用
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            
            // 判断是否完全释放
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }
        
        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }
        
        final ConditionObject newCondition() {
            return new ConditionObject();
        }
        
        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }
        
        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }
        
        final boolean isLocked() {
            return getState() != 0;
        }
    }
    
    // 用于实现非公平锁
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        final void lock() {
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
                acquire(1);
        }

        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }
    
    // 用于实现公平锁
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        final void lock() {
            acquire(1);
        }

        // 尝试获取锁
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            
            // state 为 0 表示没有线程持有锁
            if (c == 0) {
                // 由于是公平锁，在没有锁的情况下还要看是否有线程在等待
                // 若没有线程在等待则通过 CAS 尝试获取
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    
                    // 设置锁 owner
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // state 不为 0 但是该线程已经拥有了该锁
            else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
    
    private final Sync sync;
    
    public ReentrantLock() {
        sync = new NonfairSync();
    }
    
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
}
```

### `lock`
```java
public void lock() {
    sync.lock();
}

public void lockInterruptibly() throws InterruptedException {
    sync.acquireInterruptibly(1);
}

// 使用 tryLock 可以避免死锁
// 可以睡眠随机时间，避免活锁
public boolean tryLock() {
    return sync.nonfairTryAcquire(1);
}
```

### `unlock`
```java
public void unlock() {
    sync.release(1);
}
```

### 其他方法
```java
public Condition newCondition() {
    return sync.newCondition();
}

// 锁被当前线程持有的数量
public int getHoldCount() { ... }

// 锁是否被当前线程持有
public boolean isHeldByCurrentThread() { ... }

// 锁是否被持有
public boolean isLocked() { ... }

// 锁等待策略是否公平
public final boolean isFair() { ... }

// 获取锁的 owner
protected Thread getOwner() { ... }

// 是否有线程在等待该锁
public final boolean hasQueuedThreads() { ... }

// 指定的线程是否在等待锁
public final boolean hasQueuedThread(Thread thread) { ... }

// 等待锁的线程数
public final int getQueueLength() { ... }
```
