## 启动参数
以 - 开头为标准参数，所有的 JVM 都要实现这些参数，并且向后兼容

-D 设置系统属性

以 -X 开头为非标准参数，基本都是传给 JVM 的，默认 JVM 实现这些参数的功能，但是并不保证所有 JVM 实现都满足，且不保证向后兼容。可以使用 java -X 命令来查看当前 JVM 支持的非标准参数

以 –XX: 开头为非稳定参数，专门用于控制 JVM 的行为，跟具体的 JVM 实现有关，随时可能会在下个版本取消

-XX:+-Flags 形式，+- 是对布尔值进行开关
-XX:key=value 形式，指定某个选项的值

### 系统属性
```
-Dfile.encoding=UTF-8
-Duser.timezone=GMT+08
-Dmaven.test.skip=true
-Dio.netty.eventLoopThreads=8
```

### 运行模式
-server：设置 JVM 使用 server 模式，特点是启动速度比较慢，但运行时性能和内存管理效率很高，适用于生产环境。在具有 64 位能力的 JDK 环境下将默认启用该模式

-client：设置 JVM 使用 client 模式，特点是启动速度比较快，但运行时性能和内存管理效率不高，通常用于客户端应用程序或者 PC 应用开发和调试。JDK1.7 之前在 32 位的 x86 机器上的默认值是 -client 选项

-Xint：在解释模式下运行，-Xint 标记会强制 JVM 解释执行所有的字节码，这会降低运行速度，通常低 10 倍或更多

-Xcomp：-Xcomp 参数与 -Xint 正好相反，JVM 在第一次使用时会把所有的字节码编译成本地代码，从而带来最大程度的优化（注意预热）

-Xmixed：-Xmixed 是混合模式，将解释模式和编译模式进行混合使用，由 JVM 自己决定。这是 JVM 的默认模式，也是推荐模式

### 堆内存
-Xmx：最大堆内存

-Xms：堆内存空间的初始大小。指定的内存大小，并不是操作系统实际分配的初始值，而是 GC 先规划好，用到才分配。专用服务器上需要保持 –Xms 和 –Xmx 一致，否则应用刚启动可能就有好几个 FullGC。当两者配置不一致时，堆内存扩容可能会导致性能抖动

-Xmn：等价于 -XX:NewSize，使用 G1 垃圾收集器不应该设置该选项。官方建议设置为 -Xmx 的 1/2 ~ 1/4

-XX:MaxPermSize=size：这是 JDK1.7 之前使用的。Java8 默认允许的 Meta 空间无限大，此参数无效

-XX:MaxMetaspaceSize=size：Java8 默认不限制 Meta 空间，一般不允许设置该选项

-XX:MaxDirectMemorySize=size：系统可以使用的最大堆外内存，这个参数跟 -Dsun.nio.MaxDirectMemorySize 效果相同

-Xss：每个线程栈的字节数。与 -XX:ThreadStackSize=size 等价

### GC 相关
-XX:+UseG1GC：使用 G1 垃圾回收器
-XX:+UseConcMarkSweepGC：使用 CMS 垃圾回收器
-XX:+UseSerialGC：使用串行垃圾回收器
-XX:+UseParallelGC：使用并行垃圾回收器

Java 11+
-XX:+UnlockExperimentalVMOptions -XX:+UseZGC

Java 12+
-XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC

### 分析诊断
-XX:+-HeapDumpOnOutOfMemoryError 选项，当 OutOfMemoryError 产生，
自动 Dump 堆内存

-XX:HeapDumpPath 选项，指定内存溢出时 Dump 文件的目录。如果没有指定则默认为启动 Java 程序的工作目录

-XX:OnError 选项，发生致命错误时执行的脚本

-XX:OnOutOfMemoryError 选项，抛出 OutOfMemoryError 错误时执行的脚本

-XX:ErrorFile=filename 选项，致命错误的日志文件名，绝对路径或者相对路径

-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1506，远程调试

### JavaAgent
Agent 可以通过无侵入方式来做很多事情，比如注入 AOP 代码，执行统计等等，权限非常大。设置 agent 的语法如下：
-agentlib:libname[=options]：启用 native 方式的 agent，参考 LD_LIBRARY_PATH 路径
-agentpath:pathname[=options]：启用 native 方式的 agent
-javaagent:jarpath[=options]：启用外部的 agent 库，比如 pinpoint.jar 等等
-Xnoagent：禁用所有 agent

开启 CPU 使用时间抽样分析：
JAVA_OPTS="-agentlib:hprof=cpu=samples,file=cpu.samples.log"


## 堆对象的生存周期
新建一个对象时，对象会被优先分配到新生代的 Eden 区中，这时虚拟机会给对象定义一个对象年龄计数器（通过参数 -XX:MaxTenuringThreshold 设置）

当 Eden 空间不足时，虚拟机将会执行 Minor GC。这时 JVM 会把存活的对象转移到 Survivor 中，并给对象的年龄 +1。对象在 Survivor 中同样也会经历 MinorGC，每经过一次 MinorGC，对象的年龄将会 +1

可以通过参数 -XX:PetenureSizeThreshold 设置直接被分配到老年代的最大对象，这时如果分配的对象超过了设置的阀值，对象就会直接被分配到老年代，这样可以减少新生代的垃圾回收


## 堆内存分配
查看堆内存配置的默认值
```sh
java -XX:+PrintFlagsFinal -version | grep HeapSize 
jmap -heap 17284
```

JDK1.7
年轻代和老年代的比例是 1:2，可以通过 –XX:NewRatio 重置该配置项
年轻代中的 Eden 和 To Survivor、From Survivor 的比例是 8:1:1，可以通过 -XX:SurvivorRatio 重置该配置项

如果开启了 -XX:+UseAdaptiveSizePolicy 配置项，JVM 将会动态调整 Java 堆中各个区域的大小以及进入老年代的年龄，–XX:NewRatio 和 -XX:SurvivorRatio 将会失效

JDK1.8
默认开启 -XX:+UseAdaptiveSizePolicy 配置项，不要随便关闭 UseAdaptiveSizePolicy 配置项，除非你已经对初始化堆内存 / 最大堆内存、年轻代 / 老年代以及 Eden 区 / Survivor 区有非常明确的规划了。否则 JVM 将会分配最小堆内存，年轻代和老年代按照默认比例 1:2 进行分配，年轻代中的 Eden 和 Survivor 则按照默认比例 8:2 进行分配。这个内存分配未必是应用服务的最佳配置，因此可能会给应用服务带来严重的性能问题


## 内存调优
### 调优参考指标
GC 频率：高频的 FullGC 会给系统带来非常大的性能消耗，虽然 MinorGC 相对 FullGC 来说好了许多，但过多的 MinorGC 仍会给系统带来压力

堆内存：分析堆内存大小是否合适，年轻代和老年代的比例是否合适。如果内存不足或分配不均匀，会增加 FullGC，严重的将导致 CPU 持续爆满，影响系统性能

吞吐量：频繁的 FullGC 将会引起线程的上下文切换，增加系统的性能开销，从而影响每次处理的线程请求，最终导致系统的吞吐量下降

延时：JVM 的 GC 持续时间也会影响到每次请求的响应时间

### 调优方法
调整堆内存空间减少 FullGC：堆内存基本被用完，而且存在大量 FullGC，这意味着堆内存严重不足，需要调大堆内存空间

```
java -jar -Xms4g -Xmx4g heapTest.jar
```

-Xms：堆初始大小
-Xmx：堆最大值

调整年轻代减少 MinorGC：可以将年轻代设置得大一些，从而减少一些 MinorGC

```
java -jar -Xms4g -Xmx4g -Xmn3g heapTest.jar
```

设置 Eden、Survivor 区比例：如果开启 AdaptiveSizePolicy，则每次 GC 后都会重新计算 Eden、From Survivor 和 To Survivor 区的大小，计算依据是 GC 过程中统计的 GC 时间、吞吐量、内存占用量。这个时候 SurvivorRatio 默认设置的比例会失效

在 JDK1.8 中，默认是开启 AdaptiveSizePolicy 的，可以通过 -XX:-UseAdaptiveSizePolicy 关闭该项配置，或显示运行 -XX:SurvivorRatio=8 将 Eden、Survivor 的比例设置为 8:2。如果大部分新对象都是在 Eden 区创建的，可以固定 Eden 区的占用比例，来调优 JVM 的内存分配性能

## 垃圾回收机制
### 回收对象
JVM 的内存区域中，程序计数器、虚拟机栈和本地方法栈这 3 个区域是线程私有的，随着线程的创建而创建，销毁而销毁；栈中的栈帧随着方法的进入和退出进行入栈和出栈操作，每个栈帧中分配多少内存基本是在类结构确定下来的时候就已知的，因此这三个区域的内存分配和回收都具有确定性

因此，垃圾回收的重点就是堆和方法区中的内存。堆中的回收主要是对象的回收，方法区的回收主要是废弃常量和无用的类的回收

### 回收标准
一般一个对象不再被引用，就代表该对象可以被回收。目前有以下两种算法可以判断该对象是否可以被回收

引用计数算法：通过一个对象的引用计数器来判断该对象是否被引用了。每当对象被引用，引用计数器就会加 1；每当引用失效，计数器就会减 1。当对象的引用计数器的值为 0 时，就说明该对象不再被引用，可以被回收了。虽然引用计数算法的实现简单，判断效率也很高，但它存在着对象之间相互循环引用的问题

可达性分析算法：GC Roots 是该算法的基础，GC Roots 是所有对象的根对象，在 JVM 加载时，会创建一些普通对象引用正常对象。这些对象作为正常对象的起始点，在垃圾回收时，会从这些 GC Roots 开始向下搜索，当一个对象到 GC Roots 没有任何引用链相连时，就证明此对象是不可用的。目前 HotSpot 虚拟机采用的就是这种算法

GC Roots 主要包含以下几类：
1. 虚拟机栈中引用的对象
2. 方法区中类静态属性实体引用的对象
3. 方法区中常量引用的对象
4. 本地方法栈中 JNI 引用的对象

以上两种算法都是通过引用来判断对象是否可以被回收。在 JDK 1.2 之后，Java 对引用的概念进行了扩充，将引用分为了以下四种：
强引用：被强引用关联的对象永远不会被回收
软引用：软引用关联的对象，只有当系统将发生内存溢出时，才回去回收软引用的引用对象
弱引用：只被弱引用关联的对象，只要发生垃圾回收事件，就会被回收
虚引用：被虚引用关联的对象的唯一作用是能在这个对象被回收时收到一个系统通知

### 回收策略
JVM 垃圾回收遵循以下两个特性：
自动性：Java 提供了一个系统级的线程来跟踪每一块分配出去的内存空间，当 JVM 处于空闲循环时，垃圾收集器线程会自动检查每一块分配出去的内存空间，然后自动回收每一块空闲的内存块

不可预期性：一旦一个对象没有被引用了，该对象是否立刻被回收是不可预期的。很难确定一个没有被引用的对象是不是会被立刻回收掉，因为有可能当程序结束后，这个对象仍在内存中

### 回收时机
Full GC：
1. 年轻代晋升到老年代的对象大小，并比目前老年代剩余的空间大小还要大时
2. 当老年代的空间使用率超过某阈值时
3. 当元空间不足时（JDK1.7 永久代不足）
4. System.gc()


## gc 算法
### 标记-清除算法（Mark-Sweep）
算法分为标记、清除两个阶段：首先标记出所有需要回收的对象，在标记完成后统一回收掉所有被标记的对象。该算法是最基础的收集算法，后续的收集算法都是基于这种思路并对其缺点进行改进而得到的

优点：
不需要移动对象，简单高效

缺点：
标记和清除过程的效率都不高
标记清除之后产生大量不连续的内存碎片，碎片太多可能会导致无法分配空间给较大对象而提前触发另一次垃圾收集动作

### 复制算法（Copying）
该算法将可用内存按容量划分为大小相等的两块，每次只使用其中的一块。当这一块的内存用完了，就将还存活着的对象复制到另外一块上面，然后再把已使用过的内存空间一次清理掉

优点：
简单高效，不产生内存碎片

缺点：
内存使用率低，且有可能产生频繁复制问题，特别是在对象存活率较高的情况

### 标记-整理算法（Mark-Compact）
标记过程与标记-清除算法一样，但后续步骤不是直接对可回收对象进行清理，而是让所有存活的对象都向一端移动，然后直接清理掉端边界以外的内存

### 分代收集算法（Generational Collection）
把堆分为新生代和老年代，从而根据各个年代的特点采用最适当的收集算法

在新生代中，每次垃圾收集时都有大批对象死去，只有少量存活，则选用复制算法，这样只需要付出少量存活对象的复制成本就可以完成收集

在老年代中，由于对象存活率高、没有额外空间对它进行分配担保，则使用标记-清除或标记-整理算法来进行回收


## 垃圾收集器
### Serial New/Old
回收算法：复制算法/标记-清除算法

单线程复制回收，简单高效，但会暂停程序导致停顿

-XX:+UseSerialGC：年轻代、老年代回收器为：Serial New、Serial Old

### ParNew New/Old
回收算法：复制算法/标记-整理算法

多线程复制回收，降低了停顿时间，但容易增加上下文切换

-XX:+UseParNewGC：年轻代、老年代回收器为：ParNew New、Serial Old，jdk1.8 中无效
-XX:+UseParallelOldGC：年轻代、老年代回收器为：Parallel Scavenge、Parallel Old

### Parallel Scavenge
回收算法：复制算法

并行回收器，追求高吞吐量，高效利用 CPU

-XX:+UseParallelGC：年轻代、老年代回收器为：Parallel Scavenge、Serial Old
-XX:ParallelGCThreads=4 设置并发线程

### CMS（Concurrent Mark Sweep）
回收算法：标记-清除算法

整个过程分为如下步骤：
1. Initial Mark，初始化标记：发生 STW，暂停所有应用线程，并行标记可直达的存活对象
2. Concurrent Mark，并发标记：GC 线程和应用线程并发执行。继续递归遍历老年代，并标记可直接或间接到达的所有老年代存活对象。由于是并发执行，对象可能会发生变化，如果发生变化，变化的对象所在的 Card 标识为 Dirty
3. Concurrent Preclean，并发预清理：重新扫描前一个阶段标记的 Dirty 对象，并标记被 Dirty 对象直接或间接引用的对象，然后清除 Card 标识
4. Concurrent Abortable Preclean，可中止的并发预清理：标记可达的老年代对象，扫描处理 Dirty Card 中的对象
5. Final Remark，重新标记：发生 STW，暂停所有应用线程，重新扫描之前并发处理阶段的所有残留更新对象
6. Concurrent Sweep，并发清理：清理所有未被标记的死亡对象，回收被占用的空间
7. Concurrent Reset，并发重置：清理并恢复在 CMS GC 过程中的各种状态，重新初始化 CMS 相关数据结构

老年代回收器，高并发、低停顿，追求最短 GC 回收停顿时间，CPU 占用比较高，响应时间快、停顿时间短

-XX:+UseConcMarkSweepGC：年轻代、老年代回收器为：ParNew New、CMS（Serial Old 作为备用）

### G1
回收算法：标记-整理+复制算法

G1 使用一种 Region 方式对堆内存进行了划分，同样也分年轻代、老年代，但每一代使用的是 N 个不连续的 Region 内存块，每个 Region 占用一块连续的虚拟内存地址

G1 还使用一种 Humongous 区域，用于存储特别大的对象。一旦发现没有引用指向巨型对象，则可直接在年轻代的 YoungGC 中被回收掉

G1 分为 Young GC、Mix GC 以及 Full GC

G1 Young GC 主要是在 Eden 区进行，当 Eden 区空间不足时，则会触发一次 Young GC。将 Eden 区数据移到 Survivor 空间时，如果 Survivor 空间不足，则会直接晋升到老年代。此时 Survivor 的数据也会晋升到老年代。Young GC 的执行是并行的，期间会发生 STW

1. 根扫描，并行标记可直达的存活对象
2. 更新 RSet，将引用更新到 RSet 容器中
3. 处理 RSet，检测从新生代指向老年代的对象
4. 对象拷贝，拷贝存活的对象到 survivor/old 区域
5. 处理引用队列，软引用，弱引用，虚引用处理

当堆空间的占用率达到一定阈值后会触发 G1 Mix GC（阈值由命令参数 -XX:InitiatingHeapOccupancyPercent 设定，默认值 45），Mix GC 主要包括了四个阶段，其中只有并发标记阶段不会发生 STW，其它阶段均会发生 STW

1. Initail mark，初始标记，发生 STW，暂停所有应用线程，并行标记可直达的存活对象
2. Concurrent marking，并发标记，GC 线程和应用线程将并发执行，整个堆中查找存活的对象
3. Remark，最终标记，发生 STW，暂停所有应用线程，清空 SATB 缓冲区，跟踪未被访问的存活对象，并执行引用处理
4. Cleanup，清除垃圾，发生 STW，暂停所有应用线程，软引用，弱引用，虚引用处理


G1 和 CMS 主要的区别在于：
1. CMS 主要集中在老年代的回收，而 G1 集中在分代回收，包括了年轻代的 Young GC 以及老年代的 Mix GC
2. G1 使用了 Region 方式对堆内存进行了划分，且基于标记整理算法实现，整体减少了垃圾碎片的产生
3. 在初始化标记阶段，搜索可达对象使用到的 Card Table，其实现方式不一样

垃圾回收都是从 Root 开始搜索，这会先经过年轻代再到老年代，也有可能老年代引用到年轻代对象，如果发生 Young GC，除了从年轻代扫描根对象之外，还需要再从老年代扫描根对象，确认引用年轻代对象的情况

这种属于跨代处理，非常消耗性能。为了避免在回收年轻代时跨代扫描整个老年代，CMS 和 G1 都用到了 Card Table 来记录这些引用关系。只是 G1 在 Card Table 的基础上引入了 RSet，每个 Region 初始化时，都会初始化一个 RSet，RSet 记录了其它 Region 中的对象引用本 Region 对象的关系

除此之外，CMS 和 G1 在解决并发标记时漏标的方式也不一样，CMS 使用的是 Incremental Update 算法，而 G1 使用的是 SATB 算法

在并发标记中，G1 和 CMS 都是基于三色标记算法来实现的：
黑色：根对象，或者对象和对象中的子对象都被扫描
灰色：对象本身被扫描，但还没扫描对象中的子对象
白色：不可达对象
基于这种标记有一个漏标的问题，也就是说，当一个白色标记对象，在垃圾回收被清理掉时，正好有一个对象引用了该白色标记对象，此时由于被回收掉了，就会出现对象丢失的问题

为了避免上述问题，CMS 采用了 Incremental Update 算法，只要在写屏障里发现一个白对象的引用被赋值到一个黑对象的字段里，那就把这个白对象变成灰色的。而在 G1 中，采用的是 SATB 算法，该算法认为开始时所有能遍历到的对象都是需要标记的，即认为都是活的

G1 具备 Pause Prediction Model ，即停顿预测模型。用户可以设定整个 GC 过程中期望的停顿时间，用参数 -XX:MaxGCPauseMillis 可以指定一个 G1 收集过程的目标停顿时间，默认值 200ms

G1 会根据这个模型统计出来的历史数据，来预测一次垃圾回收所需要的 Region 数量，通过控制 Region 数来控制目标停顿时间的实现

高并发、低停顿，可预测停顿时间

-XX:+UseG1GC：年轻代、老年代回收器为：G1、G1
-XX:MaxGCPauseMillis=200：设置最大暂停时间


## GC 性能衡量指标
吞吐量：应用程序所花费的时间和系统总运行时间的比值。GC 的吞吐量一般不能低于 95%

停顿时间：对于串行回收器而言，停顿时间可能会比较长；而使用并发回收器，由于垃圾收集器和应用程序交替运行，程序的停顿时间就会变短，但其效率很可能不如独占垃圾收集器，系统的吞吐量也很可能会降低

垃圾回收频率：通常垃圾回收的频率越低越好，增大堆内存空间可以有效降低垃圾回收发生的频率，但同时也意味着堆积的回收对象越多，最终也会增加回收时的停顿时间。所以只要适当地增大堆内存空间，保证正常的垃圾回收频率即可


## GC 日志
-XX:+PrintGC：输出 GC 日志
-XX:+PrintGCDetails：输出 GC 的详细日志
-XX:+PrintGCTimeStamps：输出 GC 的时间戳（以基准时间的形式）
-XX:+PrintGCDateStamps：输出 GC 的时间戳（以日期的形式，如 2013-05-04T21:53:59.234+0800）

-XX:+PrintHeapAtGC：在进行 GC 的前后打印出堆的信息
-Xloggc:../logs/gc.log：日志文件的输出路径

通过 GCView 工具打开日志文件
GCeasy 是一款非常直观的 GC 日志分析工具，可以将日志文件压缩之后，上传到 GCeasy 官网即可看到非常清楚的 GC 日志分析结果


## GC 调优策略
### 降低 Minor GC 频率
通常情况下，由于新生代空间较小，Eden 区很快被填满，就会导致频繁 Minor GC，可以通过增大新生代空间来降低 Minor GC 的频率

扩容 Eden 区虽然可以减少 Minor GC 的次数，但会增加单次 Minor GC 的时间

单次 Minor GC 时间是由两部分组成：T1（扫描新生代）和 T2（复制存活对象）。假设一个对象在 Eden 区的存活时间为 500ms，Minor GC 的时间间隔是 300ms，那么正常情况下，Minor GC 的时间为 ：T1 + T2

如果增大新生代空间，Minor GC 的时间间隔可能会扩大到 600ms，此时一个存活 500ms 的对象就会在 Eden 区中被回收掉，此时就不存在复制存活对象了，所以再发生 Minor GC 的时间为：两次扫描新生代，即 2T1

可见，扩容后，Minor GC 时增加了 T1，但省去了 T2 的时间。通常在虚拟机中，复制对象的成本要远高于扫描成本

如果在堆内存中存在较多的长期存活的对象，此时增加年轻代空间，反而会增加 Minor GC 的时间。如果堆中的短期对象很多，那么扩容新生代，单次 Minor GC 时间不会显著增加。因此，单次 Minor GC 时间更多取决于 GC 后存活对象的数量，而非 Eden 区的大小

### 降低 Full GC 的频率
通常情况下，由于堆内存空间不足或老年代对象太多，会触发 Full GC，频繁的 Full GC 会带来上下文切换，增加系统的性能开销

减少创建大对象：如果大对象超过年轻代最大对象阈值，会被直接创建在老年代；即使被创建在了年轻代，由于年轻代的内存空间有限，通过 Minor GC 之后也会进入到老年代。这种大对象很容易产生较多的 Full GC

增大堆内存空间：在堆内存不足的情况下，增大堆内存空间，且设置初始化堆内存为最大堆内存，也可以降低 Full GC 的频率

### 选择合适的 GC 回收器
如果要求每次操作的响应时间必须在 500ms 以内，选择响应速度较快的 GC 回收器，如 CMS（Concurrent Mark Sweep）回收器和 G1 回收器

如果对系统吞吐量有要求时，可以选择 Parallel Scavenge 回收器来提高系统的吞吐量

