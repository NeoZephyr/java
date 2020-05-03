## 线程池优点
1. 重用存在的线程，减少对象创建、消亡的开销
2. 可有效控制最大并发线程数，提高系统资源利用率，同时可以避免过多资源竞争，避免阻塞
3. 提供定时执行、定期执行、单线程、并发数控制等功能


## ExecutorService
### shutdown 方法
不接受新任务，但已提交的任务会继续执行，即使任务还未开始执行

### shutdownNow 方法
不接受新任务，终止已提交但尚未执行的任务，对于正在执行的任务，一般会调用线程的 interrupt 方法尝试中断，不过线程可能不响应中断。shutdownNow 会返回已提交但尚未执行的任务列表

### isShutdown 方法
shutdown 和 shutdownNow 不会阻塞等待，它们返回后不代表所有任务都已结束，不过 isShutdown 方法会返回 true。可以通过 awaitTermination 等待所有任务结束，并限定等待的时间。若超时前所有任务都结束了，即 isTerminated 返回 true，则返回 true，否则返回 false

### invokeAll 方法
invokeAll 等待所有任务完成，返回的 Future 列表中，每个 Future 的 isDone 方法都返回 true，不过 isDone 为 true 不代表任务执行成功，可能是被取消；invokeAll 可以指定等待时间，若超时后有的任务没完成则取消

### invokeAny 方法
invokeAny 只需要有一个任务在限时内完成就会返回该任务的结果，其他任务会被取消，若没有任务在限时内完成，抛出 `TimeoutException` 异常，若限时内所有任务都发生了异常，抛出 `ExecutionException` 异常


## Executors
### newCachedThreadPool
快速创建一个拥有自动回收线程功能且没有限制的线程池

### newFixedThreadPool
创建一个固定线程大小的线程池

### newScheduledThreadPool
定时线程池，支持定时及周期性任务执行

### newSingleThreadExecutor
创建一个单线程的执行器，适用于需要确保所有任务被顺序执行的场合


## ThreadPoolExecutor
```java
ThreadPoolExecutor(
    int corePoolSize,
    int maximumPoolSize,
    long keepAliveTime,
    TimeUnit unit,
    BlockingQueue<Runnable> workQueue,
    ThreadFactory threadFactory,
    RejectedExecutionHandler handler);
```

### corePoolSize
表示线程池保有的最小线程数。当有新任务到来时，若当前线程个数小于 corePoolSize，则创建一个新线程来执行该任务，即使此时其他线程是空闲的；若当前线程个数大于等于 corePoolSize 则不会立即创建新线程，而是先尝试排队，若队列满或其他原因不能立即入队则不排队，进一步检查线程个数是否达到 maximumPoolSize，若没有则继续创建线程，直到线程数达到 maximumPoolSize

默认情况下，核心工作线程在初始的创建，新任务到来时才被启动。可以通过调用 prestartCoreThread 或 prestartAllCoreThreads 方法改变这种行为，通常会在应用启动时 WarmUp 核心线程，从而达到任务过来能够立马执行的结果

如果是 CPU 密集型任务，主要是消耗 CPU 资源，可以将线程数设置为 N（CPU 核心数）+ 1
如果是 I/O 密集型任务：系统会用大部分的时间来处理 I/O 交互，而不会占用 CPU 来处理，这时就可以将 CPU 交出给其它线程使用。因此在 I/O 密集型任务的应用中，可以将线程数设置为 2N

通用计算：
线程数 = N（CPU 核数）*（1 + WT（线程等待时间）/ ST（线程时间运行时间））
可以通过 JDK 自带的工具 VisualVM 来查看 WT/ST 比例

### maximumPoolSize
表示线程池创建的最大线程数

### keepAliveTime
如果一个线程空闲了 keepAliveTime 这么久，而且线程池的线程数大于 corePoolSize，那么这个空闲的线程就要被回收了。当该值为 0 时表示所有线程都不会超时终止

### workQueue
若使用无界队列，线程个数最多只能达到 corePoolSize，到达 corePoolSize 后，新的任务总会排队，参数 maximumPoolSize 失去意义。若使用 SynchronousQueue，由于没有实际存储元素的空间，当尝试排队时，只在正好有空闲线程在等待接受任务的情况下才会入队成功，否则会创建新线程，最终达到 maximumPoolSize

### threadFactory
可以自定义创建线程

### handler
任务拒绝策略。若队列有界，且 maximumPoolSize 有限，当队列排满且线程个数也达到 maximumPoolSize 时，会触发线程池的任务拒绝策略。默认情况下，提交任务的方法会抛出 `RejectedExecutionException` 异常

ThreadPoolExecutor 已经提供了以下 4 种策略：
CallerRunsPolicy：提交任务的线程自己去执行该任务
AbortPolicy：默认的拒绝策略，会抛出 RejectedExecutionException 异常
DiscardPolicy：直接丢弃任务，没有任何异常抛出
DiscardOldestPolicy：丢弃最老的任务，其实就是把最早进入工作队列的任务丢弃，然后把新任务加入到工作队列

注意事项：
1. 强烈建议使用有界队列
2. 默认拒绝策略要慎重使用
3. 捕获所有异常并按需处理
4. 为不同的任务创建不同的线程池，相同线程池中的任务相互独立

```java
BlockingQueue<Task> queue = LinkedBlockingQueue<>(2000);

void start() {
    ExecutorService executor = executors.newFixedThreadPool(5);

    for (int i = 0; i < 5; ++i) {
        executor.execute(() -> {
            try {
                while (true) {
                    List<Task> tasks = pollTasks();
                    execTasks(tasks);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

List<Task> pollTasks() throws InterruptedException {
    List<Task> tasks = new LinkedList<>();

    // 阻塞获取
    Task task = queue.take();

    while (task != null) {
        tasks.add(task);

        // 非阻塞获取
        task = queue.poll();
    }

    return tasks;
}

void execTasks(List<Task> tasks) {}
```


## Runnable vs Callable
1. Runnable 没有返回结果，不能抛出异常
2. Callable 有返回结果，能抛出异常


## Future
Future 接口有一下 5 个方法：
1. 取消任务的方法 cancel()
2. 判断任务是否已取消的方法 isCancelled()
3. 判断任务是否已结束的方法 isDone()，包括正常结束、抛出异常、任务取消等情况
4. 获得任务执行结果的 get()
5. 获得任务执行结果的 get(timeout, unit)

取消任务：
若任务已完成、或已经取消、或由于某种原因不能取消，则返回 false，否则返回 true；若任务未开始，则不再运行；若任务已运行，则不一定能取消。当任务正在执行时，若参数 `mayInterruptIfRunning` 为 true，调用 `interrupt` 方法会尝试中断线程，反之不会

只要 cancel 方法返回了 true，isCancelled 方法都会返回 true，即使执行任务的线程还未真正结束

获取任务最终结果时：
1. 任务未执行完成则会阻塞等待，若指定了等待时间且超时任务未完成则会抛出 `TimeoutException` 异常
2. 任务正常完成，返回执行结果
3. 任务执行抛出异常，get 方法将异常包装为 `ExecutionException` 异常重新抛出，通过异常的 `getCause` 方法可以获取原异常
4. 任务取消，get 方法抛出 `CancellationException` 异常
5. get 方法的线程被中断，抛出 `InterruptedException` 异常


## 任务提交
1. 提交 Runnable 任务 submit(Runnable task)：Runnable 接口的 run() 方法是没有返回值的，所以 submit(Runnable task) 这个方法返回的 Future 仅可以用来断言任务已经结束了，类似于 Thread.join()
2. 提交 Callable 任务 submit(Callable<T> task)：这个方法的参数是一个 Callable 接口，它只有一个 call() 方法，并且这个方法是有返回值的，所以这个方法返回的 Future 对象可以通过调用其 get() 方法来获取任务的执行结果
3. 提交 Runnable 任务及结果引用 submit(Runnable task, T result)：方法返回的 Future 对象调用 get() 的返回值就是传给 submit() 方法的参数 result。result 相当于主线程和子线程之间的桥梁，通过它主子线程可以共享数据

```java
ExecutorService executor = Executors.newFixedThreadPool(1);

Result result = new Result();
result.setData(data);

Future<Result> future = executor.submit(new Task(result), result);
Result fResult = future.get();

class Task implements Runnable {
    Result result;

    Task(Result result) {
        this.result = result;
    }

    void run() {
        Object data = result.getData();
        result.getNewData();
    }
}
```


## FutureTask
实现了 Runnable 接口，可以将 FutureTask 对象作为任务提交给 ThreadPoolExecutor 去执行，也可以直接被 Thread 执行；实现了 Future 接口，可以用来获得任务的执行结果
