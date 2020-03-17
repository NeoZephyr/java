## CountDownLatch
CountDownLatch 是一次性的，打开后就不能再关上

```java
Executor executor = Executors.newFixedThreadPool(2);

while (condition) {
  // 计数器初始化为 2
  CountDownLatch latch = new CountDownLatch(2);

  executor.execute(() -> {
    pos = getPOrders();
    latch.countDown();
  });

  executor.execute(() -> {
    dos = getDOrders();
    latch.countDown();
  });

  // 等待两个查询操作结束
  latch.await();

  diff = check(pos, dos);
  save(diff);
}
```

await 检查计数是否为 0，若大于 0 则等待；await 可设置等待时间，表示任务在指定时间内完成
countDown 检查计数，若已经为 0 则直接返回，否则减少计数；若计数变为 0 则唤醒所有等待的线程
countDown 的调用应该放到 finally 语句中，确保在工作线程发生异常的情况下也会被调用


## CyclicBarrier
CyclicBarrier 适合用于并行迭代计算，每个线程负责一部分计算，然后在栅栏处等待其他线程完成，所有线程到齐后，再进行下一次迭代

```java
Vector<P> pos;
Vector<D> dos;

Executor executor = Executors.newFixedThreadPool(1);
final CyclicBarrier barrier = new CyclicBarrier(2, () -> {
  executor.execute(() -> check());
});

void check() {
  P p = pos.remove(0);
  D d = dos.remove(0);

  diff = check(p, d);
  save(diff);
}

void checkAll() {
  Thread t1 = new Thread(() -> {
    while (condition) {
      pos.add(getPOrders());
      barrier.await();
    }
  });
  t1.start();

  Thread t2 = new Thread(() -> {
    while (condition) {
        dos.add(getDOrders());
        barrier.await();
    }
  });
  t2.await();
}
```

调用 await 表示该线程已经到达，若是最后到达的线程需要执行 check 函数，执行后唤醒所有等待的线程，然后重置内部的同步计数，以循环使用

await 可以被中断，可以限定最长等待时间，中断或超时后会抛出异常。只要其中一个线程在调用 await 时被中断或者超时，栅栏就会被破坏。此外，若栅栏动作抛出了异常，栅栏也会被破坏。栅栏被破坏后，所有调用 await 的线程就会退出，抛出 `BrokenBarrierException` 异常


## CountDownLatch VS CyclicBarrier
1. CountDownLatch 主要用来解决一个线程等待多个线程的场景；CyclicBarrier 是一组线程之间互相等待
2. CountDownLatch 的计数器是不能循环利用的，一旦计数器减到 0，再有线程调用 await，该线程会直接通过；CyclicBarrier 的计数器可以循环利用，而且具备自动重置的功能，一旦计数器减到 0 会自动重置到初始值


## 生产者/消费者
在生产者/消费者模式中有两个条件，一个与队列满有关，一个与队列空有关。生产者为队列添加的条件是队列不满，消费者从队列中取出的条件是队列不空，两者等待条件不一样

### `wait/notify`
`wait/notify` 只能有一个条件等待队列，这样会等待线程都会加入相同的条件等待队列。由于条件不同而使用相同的等待队列，唤醒等待时需要调用 `notifyAll` 而不是 `notify`，因为调用 `notify` 只能唤醒一个线程，若唤醒的是同类线程起不到协调作用。只有一个条件等待队列，这是 Java `wait/notify` 机制的局限性，可以使用显式的锁和条件来解决该问题

### `await/signal`
使用显式锁可以创建多个条件队列：等待条件 `notFull`、等待条件 `notEmpty`，代码更为清晰，同时避免了不必要的唤醒和检查，提高了效率

```java
class MyBlockingQueue<E> {
  private Queue<E> queue = null;
  private int limit;
  private Lock lock = new ReentrantLock();
  private Condition notFull  = lock.newCondition(); 
  private Condition notEmpty = lock.newCondition(); 

  public MyBlockingQueue(int limit) {
    this.limit = limit;
    queue = new ArrayDeque<>(limit);
  }

  public void put(E e) throws InterruptedException {
    lock.lockInterruptibly();
    try {
      while (queue.size() == limit) {
        notFull.await();
      }
      queue.add(e);
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public E take() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      while (queue.isEmpty()) {
        notEmpty.await();
      }
      E e = queue.poll();
      notFull.signal();
      return e;
    } finally {
      lock.unlock();
    }
  }
}
```

## 同时开始
子线程调用 `preStart` 等待开始，主线程调用 `start` 发送开始信号
```java
static class StartFlag {
  private volatile boolean flag = false;

  public synchronized void preStart() throws InterruptedException {
    while (!flag) {
      wait();
    }
  }

  public synchronized void start() {
    this.flag = true;
    notifyAll();
  }
}
```

## 等待结束
#### `join` 方式
`join` 方法让主线程等待子线程结束，`join` 判断只有子线程是活着的就调用 `wait` 等待。子线程运行结束时，Java 系统调用 `notifyAll` 通知

#### 协作对象方式
主线程与各个子线程共享一个变量，表示未完成的线程个数，初始值为子线程个数。每个子线程结束后都将该值减一，当减为 0 时调用 `notifyAll` 唤醒主线程
```java
public class Latch {
  private int count;

  public Latch(int count) {
    this.count = count;
  }

  public synchronized void await() throws InterruptedException {
    while (count > 0) {
      wait();
    }
  }

  public synchronized void countDown() {
    count--;
    if (count <= 0) {
      notifyAll();
    }
  }
}
```

## 集合点
```java
public class AssemblePoint {
  private int n;

  public AssemblePoint(int n) {
    this.n = n;
  }

  public synchronized void await() throws InterruptedException {
    if (n > 0) {
      n--;
      if (n == 0) {
        notifyAll();
      } else {
        while (n != 0) {
          wait();
          
          // 继续
        }
      }
    }
  }
}
```