## synchronized
### 修饰实例方法
保护当前实例对象，即 `this` 对象，`synchronized` 实例方法执行过程如下：
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


## `synchronized` 原理
### 对象头
Hotspot 虚拟机的对象头主要包括两部分数据：Mark Word（标记字段）、Klass Pointer（类型指针）。对象头一般占有两个机器码，若对象是数组类型，则需要三个机器码。其中，Klass Point 指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象所属的类；Mark Word 用于存储对象自身的运行时数据，如哈希码、GC 分代年龄、锁状态标志、线程持有的锁、偏向线程 ID、偏向时间戳等

### monitor
任意对象都有一个锁（monitor）和锁等待队列，这是 `synchronized` 实现同步的基础。`synchronized` 就由一对 `monitorenter/monitorexit` 指令实现。每一个被锁住的对象都会和一个 monitor 关联（对象头的 MarkWord 中的 LockWord 指向 monitor 的起始地址），同时 monitor 的 Owner 字段存放拥有该锁的线程的唯一标识，表示该锁被这个线程占用


## `synchronized` 优化
Java6 之前，monitor 实现完全依靠操作系统内部的互斥锁，因为需要进行用户态到内核态的切换，所有同步操作是一个无差别的重量级操作。Java6 进行优化，增加了从偏向锁到轻量级锁再到重量级锁的过度。其中自旋锁、轻量级锁与偏向锁都属于乐观锁

### 偏向锁
偏向锁不适合所有应用场景，因为撤销操作是比较重的操作，只有当存在较多不会真正竞争的 `synchronized` 代码块时，才会体现明显改善。偏向锁会延缓 JIT 预热的进程，所以很多性能测试中会显式地关闭偏斜锁
```
-XX:-UseBiasedLocking
```

#### 获取偏向锁
JVM 利用 CAS 在对象头上的 Mark Word 部分设置线程 id, 以表示这个对象偏向于当前线程，并不涉及真正的互斥锁；若 CAS 操作失败，则表示有竞争，当到达全局安全点（safepoint）时获得偏向锁的线程被挂起，偏向锁升级为轻量级锁，然后被阻塞在安全点的线程继续往下执行同步代码

#### 释放偏向锁
线程不会主动去释放偏向锁，只有存在竞争时，持有偏向锁的线程才会释放。偏向锁的撤销，需要等待全局安全点，它会首先暂停拥有偏向锁的线程，判断锁对象是否处于被锁定状态，撤销偏向锁后恢复到未锁定或轻量级锁的状态

### 轻量级锁
如果有另外的线程试图锁定某个已经偏斜过的对象，JVM 就需要撤销偏斜锁，并切换到轻量级锁实现

### 重量级锁
轻量级锁依赖 CAS 操作 Mark Word 来试图获取锁，若获取成功，就使用普通轻量级锁；否则进一步升级为重量级锁

### 自旋锁
#### 为什么使用自旋锁
线程的阻塞和唤醒需要 CPU 从用户态转为核心态，频繁的阻塞和唤醒对 CPU 来说是一件负担很重的工作。而且在许多应用上面，对象锁的锁状态只会持续很短的时间，为了这一段很短的时间频繁地阻塞和唤醒线程非常不值得

#### 工作原理
线程不会被立即挂起，而是等待持有锁的线程是否会很快释放。由于自旋线程一直占用 CPU 做无用功，所以需要设定一个自旋等待的最大时间。如果争用锁的线程在最大等待时间内还是获取不到锁，就会停止自旋进入阻塞状态

#### 使用场景
如果锁的竞争不激烈，且占用锁时间非常短，自旋锁能够避免上下文切换带来的开销；如果锁的竞争激烈，或者持有锁的线程需要长时间占用锁执行同步块，这时线程自旋的消耗可能大于线程上下文切换的销毁。自旋锁 JDK1.4 默认关闭，可以使用 `-XX:+UseSpinning` 开启，在 JDK1.6 中默认开启。同时自旋的默认次数为 10 次，可以通过参数 `-XX:PreBlockSpin` 调整

#### 自适应自旋锁
Java6 引入了适应性自旋锁，自旋的次数不再是固定的，而是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。若线程自旋成功则下次自旋的次数会更加多，若对于某个锁很少有自旋成功的，则减少自旋次数甚至省略掉自旋过程，以免浪费处理器资源

### 其他优化
#### 锁消除
在运行如下代码时，JVM 检测到变量 `vector` 没有逃逸出方法 `test` 之外，可以将 `vector` 内部的加锁操作消除
```java
public void test(){
    Vector<String> vector = new Vector<String>();
    for (int i = 0 ; i < 10 ; i++) {
        vector.add(i + "");
    }
}
```

#### 锁粗化
一系列的连续加锁解锁操作会导致不必要的性能损耗，锁粗化将多个连续的加锁、解锁操作连接在一起，扩展成一个范围更大的锁