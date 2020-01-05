## JVM 调优工具

### jps
显示指定系统内所有的 HotSpot 虚拟机进程

#### 可选参数
-l：输出主类全名或 jar 路径
-q：只输出 LVMID
-m：输出 JVM 启动时传递给 main() 的参数
-v：输出 JVM 启动时显示指定的 JVM 参数

### jstat
监视虚拟机运行时状态信息，可以显示出虚拟机进程中的类装载、内存、垃圾收集、JIT 编译等运行数据

#### 可选参数
-class：监视类装载、卸载数量、总空间以及耗费的时间
-compiler：JIT 编译过的方法数量耗时等
-gc：垃圾回收堆的行为统计
S0C : survivor0区的总容量
S1C : survivor1区的总容量
S0U : survivor0区已使用的容量
S1U : survivor1区已使用的容量
EC : Eden区的总容量
EU : Eden区已使用的容量
OC : Old区的总容量
OU : Old区已使用的容量
PC 当前perm的容量 (KB)
PU perm的使用 (KB)
YGC : 新生代垃圾回收次数
YGCT : 新生代垃圾回收时间
FGC : 老年代垃圾回收次数
FGCT : 老年代垃圾回收时间
GCT : 垃圾回收总消耗时间

jstat -gc 1262 2000 20
每隔2000ms输出1262的gc情况，一共输出20次

gccapacity
同-gc，不过还会输出Java堆各区域使用到的最大、最小空间
NGCMN : 新生代占用的最小空间
NGCMX : 新生代占用的最大空间
OGCMN : 老年代占用的最小空间
OGCMX : 老年代占用的最大空间
OGC：当前年老代的容量 (KB)
OC：当前年老代的空间 (KB)
PGCMN : perm占用的最小空间
PGCMX : perm占用的最大空间

-gcutil
同-gc，不过输出的是已使用空间占总空间的百分比

-gccause
垃圾收集统计概述（同-gcutil），附加最近两次垃圾回收事件的原因
LGCC：最近垃圾回收的原因
GCC：当前垃圾回收的原因

-gcnew
统计新生代的行为
TT：Tenuring threshold(提升阈值)
MTT：最大的tenuring threshold
DSS：survivor区域大小 (KB)

-gcnewcapacity
新生代与其相应的内存空间的统计
NGC:当前年轻代的容量 (KB)
S0CMX:最大的S0空间 (KB)
S0C:当前S0空间 (KB)
ECMX:最大eden空间 (KB)
EC:当前eden空间 (KB)

-gcold
统计旧生代的行为

-gcoldcapacity
统计旧生代的大小和空间

-gcpermcapacity
永生代行为统计

-printcompilation
hotspot编译方法统计

Compiled：被执行的编译任务的数量
Size：方法字节码的字节数
Type：编译类型
Method：编译方法的类名和方法名。类名使用”/” 代替 “.” 作为空间分隔符. 方法名是给出类的方法名. 格式是一致于HotSpot – XX:+PrintComplation 选项

### jmap
用于生成 heap dump 文件，如果不使用这个命令，还阔以使用 -XX:+HeapDumpOnOutOfMemoryError 参数来让虚拟机出现 OOM 的时候自动生成dump文件。jmap 不仅能生成 dump 文件，还可以查询 finalize 执行队列、Java 堆和永久代的详细信息，如当前使用率、当前使用的是哪种收集器等

#### 可选参数
dump : 生成堆转储快照
dump::live,format=b,file=<filename> pid
dump堆到文件，format指定输出格式，live指明是活着的对象，file指定文件名
jmap -dump:live,format=b,file=dump.hprof 28920
dump.hprof这个后缀是为了后续可以直接用MAT(Memory Anlysis Tool)打开

finalizerinfo : 显示在F-Queue队列等待Finalizer线程执行finalizer方法的对象
打印等待回收对象的信息
jmap -finalizerinfo 28920

heap : 显示Java堆详细信息
打印heap的概要信息，GC使用的算法，heap的配置及wise heap的使用情况,可以用此来判断内存目前的使用情况以及垃圾回收情况
jmap -heap 28920

histo : 显示堆中对象的统计信息
打印堆的对象统计，包括对象数、内存大小等等 （因为在dump:live前会进行full gc，如果带上live则只统计活对象，因此不加live的堆大小要大于加live堆的大小）
jmap -histo:live 28920 | more

permstat : to print permanent generation statistics
打印Java堆内存的永久保存区域的类加载器的智能统计信息。对于每个类加载器而言，它的名称、活跃度、地址、父类加载器、它所加载的类的数量和大小都会被打印。此外，包含的字符串数量和大小也会被打印
jmap -permstat 28920

F : 当-dump没有响应时，强制生成dump快照
强制模式。如果指定的pid没有响应，请使用jmap -dump或jmap -histo选项。此模式下，不支持live子选项

### jhat
与 jmap 搭配使用，用来分析 jmap 生成的 dump，jhat 内置一个微型的 HTTP/HTML 服务器，生成 dump 的分析结果后，可以在浏览器中查看。一般不会直接在服务器上进行分析，因为 jhat 是一个耗时并且耗费硬件资源的过程，一般把服务器生成的 dump 文件复制到本地或其他机器上进行分析
jhat [dumpfile]
jhat -J-Xmx512m dump.hprof
中间的-J-Xmx512m是在dump快照很大的情况下分配512M内存去启动HTTP服务器，运行完之后就可在浏览器打开Http://localhost:7000进行快照分析 堆快照分析主要在最后面的Heap Histogram里，里面根据class列出了dump的时候所有存活对象

分析同样一个dump快照，MAT需要的额外内存比jhat要小的多的多，所以建议使用MAT来进行分析

-stack false|true 关闭对象分配调用栈跟踪(tracking object allocation call stack)。 如果分配位置信息在堆转储中不可用. 则必须将此标志设置为 false. 默认值为 true.

-refs false|true 关闭对象引用跟踪(tracking of references to objects)。 默认值为 true. 默认情况下, 返回的指针是指向其他特定对象的对象,如反向链接或输入引用(referrers or incoming references), 会统计/计算堆中的所有对象。

-port port-number 设置 jhat HTTP server 的端口号. 默认值 7000

-exclude exclude-file 指定对象查询时需要排除的数据成员列表文件(a file that lists data members that should be excluded from the reachable objects query)。 例如, 如果文件列列出了 java.lang.String.value , 那么当从某个特定对象 Object o 计算可达的对象列表时, 引用路径涉及 java.lang.String.value 的都会被排除

-baseline exclude-file 指定一个基准堆转储(baseline heap dump)。 在两个 heap dumps 中有相同 object ID 的对象会被标记为不是新的(marked as not being new). 其他对象被标记为新的(new). 在比较两个不同的堆转储时很有用

-debug int 设置 debug 级别. 0 表示不输出调试信息。 值越大则表示输出更详细的 debug 信息

-version 启动后只显示版本信息就退出

-J< flag > 因为 jhat 命令实际上会启动一个JVM来执行, 通过 -J 可以在启动JVM时传入一些启动参数. 例如, -J-Xmx512m 则指定运行 jhat 的Java虚拟机使用的最大堆内存为 512 MB. 如果需要使用多个JVM启动参数,则传入多个 -Jxxxxxx

### jstack
用于生成java虚拟机当前时刻的线程快照。线程快照是当前java虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等。线程出现停顿的时候通过jstack来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么资源。如果java程序崩溃生成core文件，jstack工具可以用来获得core文件的java stack和native stack的信息，从而可以轻松地知道java程序是如何崩溃和在程序何处发生问题。另外，jstack工具还可以附属到正在运行的java程序中，看到当时运行的java程序的java stack和native stack的信息, 如果现在运行的java程序呈现hung的状态，jstack是非常有用的

#### 可选参数
-F : 当正常输出请求不被响应时，强制输出线程堆栈
-l : 除堆栈外，显示关于锁的附加信息
-m : 如果调用到本地方法的话，可以显示C/C++的堆栈

jstack -l 11494|more

### jinfo
实时查看和调整虚拟机运行参数。之前的jps -v口令只能查看到显示指定的参数，如果想要查看未被显示指定的参数的值就要使用jinfo口令

#### 可选参数
-flag : 输出指定args参数的值
-flags : 不需要args参数，输出所有JVM参数的值
-sysprops : 输出系统属性，等同于System.getProperties()

jinfo -flag 11494

### jvisualvm
VisualVM 是 jdk 自带的一款监控工具，提供了一个可视界面，用于查看虚拟机上运行的基于 Java 技术的程序的详细信息

使用 jvisualvm
1. 配置 JMX 管理 tomcat
set JAVA_OPTS=-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9008
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
2. 重启 tomcat
3. 双击 jvisualvm.exe 添加服务器 IP 地址，添加需要监控 jmx 端口

### jconsole
用于对 JVM 中内存，线程和类的监控，是一个基于 JMX 的 GUI 性能监测工具。jconsole使用 jvm 的扩展机制获取并展示虚拟机中运行的应用程序的性能和资源消耗等信息


如果监控远程服务需要在启动脚本中添加如下代码：
-Dcom.sun.management.jmxremote.port=6969
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.authenticate=false

### MAT
一个基于 Eclipse 的内存分析工具，是一个快速、功能丰富的 Java heap 分析工具

MAT 以 eclipse 插件的形式来安装，利用 jmap 命令生产堆文件导入 mat 中生成分析报告

### GC Easy
http://gceasy.io
http://heaphero.io/index.jsp