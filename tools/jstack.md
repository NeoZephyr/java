```sh
# 查看各个进程 cpu 占用情况
top

# 查看该进程下各个线程的 cpu 占用情况
top -Hp <pid>
```

```sh
# 查看 java 进程的堆栈状态
# 包含 JVM 中所有存活线程
# 隔段时间执行一次比较差别
jstack <pid>
```

jstack 获取 thread dump，根据 thread dump 中各个线程的状态进行分析

RUNNABLE 表示线程处于执行中，BLOCKED 表示线程被阻塞，WAITING 表示线程正在等待

locked <id> 说明线程对地址为 id 的对象进行加锁，waiting to lock <id> 说明线程在等待为 id 的对象上的锁，waiting for monitor entry [id] 说明线程通过 `synchronized` 关键字进入监视器的临界区，并处于 "Entry Set" 队列，等待 monitor