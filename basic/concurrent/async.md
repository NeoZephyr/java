## CompletableFuture
```java
// 使用默认线程池
static CompletableFuture<Void> runAsync(Runnable runnable);
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);

// 可以指定线程池
static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor);
static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor);
```

```java
CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> {
    // TODO step1
});

CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
    // TODO step2
    return "f2"
});

CompletableFuture<String> f3 = f1.thenCombine(f2, (__, tf) -> {
    return "f3" + tf
});

System.out.println(f3.join());
```


## CompletionStage
### 串行关系
```java
CompletionStage<R> thenApply(fn);
CompletionStage<R> thenApplyAsync(fn);
CompletionStage<Void> thenAccept(consumer);
CompletionStage<Void> thenAcceptAsync(consumer);
CompletionStage<Void> thenRun(action);
CompletionStage<Void> thenRunAsync(action);
CompletionStage<R> thenCompose(fn);
CompletionStage<R> thenComposeAsync(fn);
```

```java
CompletableFuture<String> f0 = CompletableFuture
    .supplyAsync(() -> "Hello World") // 启动一个异步流程
    .thenApply(s -> s + " QQ")
    .thenApply(String::toUpperCase);

System.out.println(f0.join());
```

### AND 汇聚
```java
CompletionStage<R> thenCombine(other, fn);
CompletionStage<R> thenCombineAsync(other, fn);
CompletionStage<Void> thenAcceptBoth(other, consumer);
CompletionStage<Void> thenAcceptBothAsync(other, consumer);
CompletionStage<Void> runAfterBoth(other, action);
CompletionStage<Void> runAfterBothAsync(other, action);
```

### OR 汇聚
```java
CompletionStage applyToEither(other, fn);
CompletionStage applyToEitherAsync(other, fn);
CompletionStage acceptEither(other, consumer);
CompletionStage acceptEitherAsync(other, consumer);
CompletionStage runAfterEither(other, action);
CompletionStage runAfterEitherAsync(other, action);
```

```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
    int t = getRandom(5, 10);
    sleep(t, TimeUnit.SECONDS);
    return String.valueOf(t);
});

CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
    int t = getRandom(5, 10);
    sleep(t, TimeUnit.SECONDS);
    return String.valueOf(t);
});

CompletableFuture<String> f3 = f1.applyToEither(f2, s -> s);

System.out.println(f3.join());
```

### 异常处理
```java
// 类似于 catch{}
CompletionStage exceptionally(fn);

// 类似于 finally{}
CompletionStage<R> whenComplete(consumer);
CompletionStage<R> whenCompleteAsync(consumer);

// 类似于 finally{}
CompletionStage<R> handle(fn);
CompletionStage<R> handleAsync(fn);
```

```java
CompletableFuture<Integer> f0 = CompletableFuture
    .supplyAsync(() -> 7 / 0))
    .thenApply(r -> r * 10)
    .exceptionally(e -> 0);

System.out.println(f0.join());
```


## CompletionService
```java
ExecutorService executor = Executors.newFixedThreadPool(3);
CompletionService<Integer> cs = new ExecutorCompletionService<>(executor);

cs.submit(() -> executeStep1());
cs.submit(() -> executeStep2());
cs.submit(() -> executeStep3());

for (int i = 0; i < 3; ++i) {
    Integer r = cs.take().get();
    executor.execute(() -> save(r));
}
```


## Fork/Join
```java
// 创建分治任务线程池
ForkJoinPool pool = new ForkJoinPool(4);

// 创建分治任务
FibonacciTask task = new FibonacciTask(30);

// 启动分治任务
Integer result = pool.invoke(task);

System.out.println(result);
```
```java
class FibonacciTask extends RecursiveTask<Integer> {
    final int n;

    FibonacciTask(int n) {
        this.n = n;
    }

    protected Integer compute() {
        if (n < 1) {
            return n;
        }

        FibonacciTask task1 = new FibonacciTask(n - 1);

        // 创建子任务
        task1.fork();

        FibonacciTask task2 = new FibonacciTask(n - 2);

        // 等待子任务结果，并合并结果
        return task2.compute() + task1.join();
    }
}
```