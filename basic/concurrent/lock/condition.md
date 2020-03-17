synchronized 只有一个条件变量，而 Lock 支持多个条件变量


## Condition
```java
public interface Condition {

    // 响应中断的，若发生中断会抛出 InterruptedException 异常，中断标志位被清空
    void await() throws InterruptedException;
    void awaitUninterruptibly();
    long awaitNanos(long nanosTimeout) throws InterruptedException;
    boolean await(long time, TimeUnit unit) throws InterruptedException;
    boolean awaitUntil(Date deadline) throws InterruptedException;

    void signal();
    void signalAll();
}
```

### `await`
与 `Object` 的 `wait` 方法一样，调用 `await` 方法需要先获取锁，若没有获取锁则抛出 `IllegalMonitorStateException` 异常。`await` 在进入等待队列后，会释放锁；当被其他线程唤醒、等待超时、发生中断异常后，需要重新获取锁，获取锁后才会从 `await` 方法中返回。`await` 返回后，其等待的条件不一定满足，还需要继续判断

### `signal`
`signal/signalAll` 与 `notify/notifyAll` 一样，调用前需要先获取锁，若没有获取锁则会抛出 `IllegalMonitorStateException` 异常。`signal` 挑选一个线程进行唤醒，`signalAll` 唤醒所有等待线程，这些被唤醒的线程需要重新竞争锁，获取锁后才会从 `await` 调用中返回


## Condition 示例
```java
private final Lock lock = new ReentrantLock();
private final Condition done = lock.newCondition();

Object get(int timeout) {
    long start = System.nanoTime();
    lock.lock();

    try {
        while (!isDone()) {
            done.await(timeout);

            long current = System.nanoTime();

            if (isDone() || current - start > timeout) {
                break;
            }
        }
    } finally {
        lock.unlock();
    }

    if (!isDone()) {
        throw new TimeoutException();
    }

    return returnFromResponse();
}

boolean isDone() {
    return response != null;
}

void doReceived(Response response) {
    lock.lock();
    try {
        this.response = response;
        done.signal();
    } finally {
        lock.unlock();
    }
}
```


## `AbstractQueuedSynchronizer`
```java
public abstract class AbstractQueuedSynchronizer
    extends AbstractOwnableSynchronizer
    implements java.io.Serializable {

    // 完全释放，将 state 置为 0
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }
    
    final boolean isOnSyncQueue(Node node) {
    
        // 节点从条件队列移动到阻塞队列后，waitStatus 置为 0
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;
            
        // 节点有后继节点，说明在阻塞队列中
        if (node.next != null) // If has successor, it must be on queue
            return true;
        
        // 从阻塞队列尾部开始向前遍历，查找节点
        return findNodeFromTail(node);
    }
    
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (;;) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }
    
    // 将节点从条件队列转移到阻塞队列
    final boolean transferForSignal(Node node) {
        
        // 节点已经不为 Node.CONDITION 状态，说明节点已经取消
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        // 自旋进入阻塞队列队尾，并返回 node 的前驱节点
        Node p = enq(node);
        int ws = p.waitStatus;
        
        // 1. 若 ws > 0，说明 node 前驱节点取消了锁等待，这时直接唤醒 node 节点代表的线程
        // 2. 若 ws <= 0，node 节点入队，将前驱节点状态设为 Node.SIGNAL
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            LockSupport.unpark(node.thread);
        return true;
    }
    
    
    final boolean transferAfterCancelledWait(Node node) {
    
        // 如果 CAS 成功，说明在 signal 之前发生的中断，因为 signal 会将 waitStatus 置为 0
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }
        
        // signal 已将 waitStatus 置为 0，但是节点还未进入阻塞队列
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    // ConditionObject 内部维持一个条件等待队列
    // 由于 ConditionObject 是 AQS 的成员内部类，可直接访问 AQS 中的数据，比如 AQS 中定义的锁等待队列
    public class ConditionObject implements Condition, java.io.Serializable {
        private transient Node firstWaiter;
        private transient Node lastWaiter;
        
        public ConditionObject() {}
        
        // 可中断等待
        public final void await() throws InterruptedException {
            if (Thread.interrupted())
                throw new InterruptedException();
                
            // 当当前线程的节点添加到条件队列中
            Node node = addConditionWaiter();
            
            // 释放锁，并返回释放锁之前的 state 值
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            
            // 判断 node 节点是否已经转移到阻塞队列中
            while (!isOnSyncQueue(node)) {
            
                // 线程挂起之后的可能情况：
                // 1. signal -> 转移节点到阻塞队列 -> 获取锁（unpark）
                // 2. 线程中断，另外的线程对此线程进行了中断
                // 3. 转移时前驱节点取消或者对前驱节点 CAS 操作失败
                // 4. 假唤醒
                LockSupport.park(this);
                
                // 检查是否发生中断
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            
            // 被唤醒后进入阻塞队列，等待获取锁
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }
        
        // 将当前线程加入到条件队列队尾
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            
            // 如果条件队列最后节点取消了，将其清除
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            
            // 条件队列为空
            if (t == null)
                firstWaiter = node;
            else
                t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }
        
        // 从 firstWaiter 节点遍历，去除取消的节点
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                }
                else
                    trail = t;
                t = next;
            }
        }
        
        // 1. signal 之前中断，返回 THROW_IE
        // 2. signal 之后中断，返回 REINTERRUPT
        // 3. 没有中断发生，返回 0
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                0;
        }
        
        // 唤醒线程
        public final void signal() {
            // 调用 signal 方法的线程必须持有当前的独占锁
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }
        
        // 从条件队列头部向后遍历，找出第一个需要转移的 node
        private void doSignal(Node first) {
            do {
                // 如果队头移除后，后面没有节点在等待，需要将 lastWaiter 置为 null
                if ( (firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                     (first = firstWaiter) != null); // 如果 first 节点转移到阻塞队列失败，则选择下一个节点进行转移
        }
        
        
    }
}
```

## 获取 `Condition`
```java
public class ReentrantLock implements Lock, java.io.Serializable {
    private final Sync sync;
    
    public Condition newCondition() {
        return sync.newCondition();
    }
    
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

        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }


        final ConditionObject newCondition() {
            return new ConditionObject();
        }
    }
}
```
