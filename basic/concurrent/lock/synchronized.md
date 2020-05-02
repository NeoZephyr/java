## synchronized
### 修饰实例方法
保护当前实例对象，即 this 对象，synchronized 实例方法执行过程如下：
- 尝试获得锁，如果不能够获得锁则加入等待队列，阻塞并等待唤醒 
- 执行实例方法
- 释放锁，若等待队列上有等待的线程，唤醒其中一个，若有多个等待的线程，不保证公平性

### 修饰代码块

### 修饰静态方法
保护类对象


## 锁多个资源
this 只能保护自己的余额 this.balance，无法保护别人的余额 target.balance
```java
class Account {
    private int balance;

    synchronized void transfer(Account target, int amt) {
        if (this.balance > amt) {
            this.balance -= amt;
            target.balance += amt;
        }
    }
}
```

使用 Account.class 作为共享的锁
```java
class Account {
    private int balance;

    void transfer(Account target, int amt) {
        synchronized(Account.class) {
            if (this.balance > amt) {
                this.balance -= amt;
                target.balance += amt;
            }
        }
    }
}
```

细粒度锁提高并行度
```java
class Account {
    private int balance;

    void transfer(Account target, int amt) {
        synchronized (this) {
            synchronized (target) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    }
}
```
但是有可能会发生死锁。死锁在发生以下情况时才会出现：
1. 互斥，共享资源 X 和 Y 只能被一个线程占用
2. 占有且等待，线程 T1 已经取得共享资源 X，在等待共享资源 Y 的时候，不释放共享资源 X
3. 不可抢占，其他线程不能强行抢占线程 T1 占有的资源
4. 循环等待，线程 T1 等待线程 T2 占有的资源，线程 T2 等待线程 T1 占有的资源


破坏占用且等待条件
```java
class Allocator {
    private List<Object> res = new ArrayList<>();

    // 一次申请所有资源
    synchronized boolean apply(Object from, Object to) {
        if (res.contains(from) || res.contains(to)) {
            return false;
        } else {
            res.add(from);
            res.add(to);
        }

        return true;
    }

    // 归还资源
    synchronized void free(Object from, Object to) {
        res.remove(from);
        res.remove(to);
    }
}
```
```java
class Account {
    private Allocator allocator;
    private int balance;

    void transfer(Account target, int amt) {
        while (!allocator.apply(this, target));

        try {
            synchronized (this) {
                synchronized (target) {
                    if (this.balance > amt) {
                        this.balance -= amt;
                        target.balance += amt;
                    }
                }
            }
        } finally {
            allocator.free(this, target);
        }
    }
}
```
如果 apply 操作耗时长，或者并发冲突量大的时候，可能需要上万次才能获取到锁，会特别消耗 CPU

使用 notify/wait 机制改进
```java
class Allocator {
    private List<Object> res = new ArrayList<>();

    // 一次申请所有资源
    synchronized boolean apply(Object from, Object to) {
        while (res.contains(from) || res.contains(to)) {
            try {
                // 当前线程阻塞，并进入到等待队列中，同时释放持有的互斥锁
                this.wait();
            } catch (Exception e) {}
        }

        res.add(from);
        res.add(to);
        return true;
    }

    // 归还资源
    synchronized void free(Object from, Object to) {
        res.remove(from);
        res.remove(to);

        // 通知等待队列中的线程，告诉它条件曾经满足过
        notifyAll();
    }
}
```
wait()、notify()、notifyAll() 这三个方法能够被调用的前提是已经获取了相应的互斥锁

notify() 是会随机地通知等待队列中的一个线程，而 notifyAll() 会通知等待队列中的所有线程。使用 notify() 可能导致某些线程永远不会被通知到。一般情况下，尽量使用 notifyAll()

破坏不可抢占条件，主要在于能够主动释放它占有的资源。这一点 synchronized 是做不到的。原因是 synchronized 申请资源的时候，如果申请不到，线程直接进入阻塞状态了，而线程进入阻塞状态，无法释放已经占有的资源

破坏循环等待条件
```java
class Account {
    private int id;
    private int balance;

    void transfer(Account target, int amt) {
        Account left = this;
        Account right = target;

        if (this.id > target.id) {
            left = target;
            right = this
        }

        synchronized (left) {
            synchronized (right) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    }
}
```


## wait
每个对象有一个条件等待队列，用于线程间的协作。wait 调用会把当前线程放到条件队列上并阻塞，等待期间可以被中断，若被中断会抛出 `InterruptedException` 异常；wait 只能在 synchronized 代码块内被调用，若调用 wait 方法时，当前线程没有持有对象锁，会抛出异常 `IllegalMonitorStateException` 异常

wait 执行过程如下：
1. 将当前线程放入条件等待队列，释放对象锁，阻塞等待（WAITING 或 TIMED_WAITING）
2. 等待时间到或被其他线程唤醒
3. 重新竞争对象锁，若能获得锁，线程状态变为 RUNNABLE，并从 wait 调用中返回，否则该线程加入对象锁等待队列，线程状态变为 BLOCKED，只有在获得锁后才从 wait 调用中返回
4. 从 wait 调用中返回后其等待的条件不一定成立，需要重新检查

sleep 方法和 wait 方法都可以用来放弃 CPU 一定的时间，不同点在于如果线程持有某个对象的监视器，sleep 方法不会放弃这个对象的监视器，wait 方法会放弃这个对象的监视器

wait 方法立即释放对象监视器，notify/notifyAll 方法则会等待线程剩余代码执行完毕才会放弃对象监视器


## notify
notify 从条件队列中选一个线程，将其移除并唤醒。notify 只能在 synchronized 代码块内被调用，若调用 notify 方法时，当前线程没有持有对象锁，会抛出异常 IllegalMonitorStateException 异常


## notifyAll
notifyAll 移除条件队列中所有的线程并全部唤醒

由于多线程可以基于不同的条件谓词在同一个条件队列上等待，因此如果使用 notify 而不是 notifyAll 是一种危险的操作，因为单一的通知容易导致类似于信号丢失的问题；而 notifyAll 会唤醒所有线程导致他们发生锁的竞争
