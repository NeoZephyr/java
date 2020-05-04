top
```sh
# 查看各个进程 cpu 占用情况
top

# 查看该进程下各个线程的 cpu 占用情况
top -Hp <pid>
```

vmstat：观察进程的上下文切换
```sh
vmstat 1 3
```
r：等待运行的进程数
b：处于非中断睡眠状态的进程数
swpd：虚拟内存使用情况
free：空闲的内存
buff：用来作为缓冲的内存数
si：从磁盘交换到内存的交换页数量
so：从内存交换到磁盘的交换页数量
bi：发送到块设备的块数
bo：从块设备接收到的块数
in：每秒中断数
cs：每秒上下文切换次数
us：用户 CPU 使用时间
sy：内核 CPU 系统使用时间
id：空闲时间
wa：等待 I/O 时间
st：运行虚拟机窃取的时间

pidstat
```sh
yum install sysstat
```
-u：默认的参数，显示各个进程的 cpu 使用情况
-r：显示各个进程的内存使用情况
-d：显示各个进程的 I/O 使用情况
-w：显示每个进程的上下文切换情况
-p：指定进程号
-t：显示进程中线程的统计信息

```sh
pidstat -p <pid> -r 1 3
```

jcmd：查看垃圾收集器的具体设置参数
```sh
jcmd <pid> VM.flags
```

jstat
```sh
jstat -gc <pid> interval
```
S0C：年轻代中 To Survivor 的容量（单位 KB）
S1C：年轻代中 From Survivor 的容量（单位 KB）
S0U：年轻代中 To Survivor 目前已使用空间（单位 KB）
S1U：年轻代中 From Survivor 目前已使用空间（单位 KB）
EC：年轻代中 Eden 的容量（单位 KB）
EU：年轻代中 Eden 目前已使用空间（单位 KB）
OC：Old 代的容量（单位 KB）
OU：Old 代目前已使用空间（单位 KB）
MC：Metaspace 的容量（单位 KB）
MU：Metaspace 目前已使用空间（单位 KB）
YGC：从应用程序启动到采样时年轻代中 gc 次数
YGCT：从应用程序启动到采样时年轻代中 gc 所用时间 (s)
FGC：从应用程序启动到采样时 old 代（全 gc）gc 次数
FGCT：从应用程序启动到采样时 old 代（全 gc）gc 所用时间 (s)
GCT：从应用程序启动到采样时 gc 用的总时间 (s)


jstack
```sh
# 查看 java 进程的堆栈状态
# 包含 JVM 中所有存活线程
# 隔段时间执行一次比较差别
jstack <pid>
```

jstack 获取 thread dump，根据 thread dump 中各个线程的状态进行分析

RUNNABLE 表示线程处于执行中，BLOCKED 表示线程被阻塞，WAITING 表示线程正在等待

locked <id> 说明线程对地址为 id 的对象进行加锁，waiting to lock <id> 说明线程在等待为 id 的对象上的锁，waiting for monitor entry [id] 说明线程通过 `synchronized` 关键字进入监视器的临界区，并处于 "Entry Set" 队列，等待 monitor


jmap
```sh
jmap -heap <pid>
```
查看堆内存中的对象数目、大小统计直方图，带上 live 则只统计活对象
```sh
jmap -histo[:live] <pid>
```
dump 到文件中
```sh
jmap -dump:format=b,file=/tmp/heap.hprof <pid>
```

