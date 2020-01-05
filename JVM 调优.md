## JVM 调优

### GC 参数
-XX:+PrintGC 输出GC日志
-XX:+PrintGCDetails 输出GC的详细日志
-XX:+PrintGCTimeStamps 输出GC的时间戳（以基准时间的形式）
-XX:+PrintGCDateStamps 输出GC的时间戳（以日期的形式，如 2017-09-04T21:53:59.234+0800）
-XX:+PrintClassHistogram 遇到Ctrl-Break后打印类实例的柱状信息
-XX:+HeapDumpOnOutOfMemoryError 系统会生成堆内存转储文件
-XX:+PrintHeapAtGC 在进行GC的前后打印出堆的信息
-Xloggc:../logs/gc.log 日志文件的输出路径

-XX:PretenureSizeThreshold=3145728 表示对象大于3145728（3M）时直接进入老年代分配，以字节为单位
-XX:MaxTenuringThreshold=1 表示对象年龄大于1，自动进入老年代
-XX:CompileThreshold=1000 表示一个方法被调用1000次之后，会被认为是热点代码，并触发即时编译

-XX:+TraceClassLoading 查看类的加载信息
-XX:+TraceClassUnLoading 查看类的卸载信息

-Xmx 最大堆体积，默认是物理内存的 1/4
-Xms 初始最小堆体积，默认是物理内存的 1/64
-XX:NewRatio 老年代与新生代的比例，默认为 2
-XX:NewSize 新生代大小
-XX:SurvivorRatio Eden 与 Survivor 的大小比例，默认为 8
-XX:PermSize 初始分配的永久代体积
-XX:MaxPermSize 最大永久代体积，默认是物理内存的 1/64
-XX:MaxMetaspaceSize 最大元数据体积，默认是物理内存的 1/4


-Xss虚拟机栈的大小
-Xoss本地方法栈的大小

TOMCAT 设置
JAVA_OPTS="-server -Xms2000m -Xmx2000m -Xmn800m -XX:PermSize=64m -XX:MaxPermSize=256m -XX:SurvivorRatio=4
-verbose:gc -Xloggc:$CATALINA_HOME/logs/gc.log 
-Djava.awt.headless=true 
-XX:+PrintGCTimeStamps -XX:+PrintGCDetails 
-Dsun.rmi.dgc.server.gcInterval=600000 -Dsun.rmi.dgc.client.gcInterval=600000
-XX:+UseConcMarkSweepGC -XX:MaxTenuringThreshold=15"

gc 日志分析
http://gceasy.io/

根据 gc 日志分析 jvm 内存分配、回收的情况来调整各区域内存比例或者 gc 回收的策略，根据 dump 出来的内存结构和线程栈分析代码中不合理的地方给予改进


输出 GC 详细日志
-XX:+PrintGCDetails
输出GC的时间戳（以日期的形式）
-XX:+PrintGCDateStamps
输出 GC 的详细日志
-Xloggc:gc.log

第一步优化：
为避免内存频繁的动态扩展，把 -Xms 和 -Xmx 配置一致
第二步优化：
根据机器内存大小分配合适内存大小
第三步优化：
关闭字节码验证，加快类装入的速度
-Xverify:none


如果GC执行时间满足下列所有条件，就没有必要进行GC优化了：
Minor GC执行非常迅速（50ms以内）
Minor GC没有频繁执行（大约10s执行一次）
Full GC执行非常迅速（1s以内）
Full GC没有频繁执行（大约10min执行一次）

大内存空间
减少了GC的次数
提高了GC的运行时间

小内存空间
增多了GC的次数
降低了GC的运行时间

当 NewRatio设为2或3时，整个GC的状态表现得更好

分析GC优化的结果，要考虑的因素：
单次Full GC运行时间
单次Minor GC运行时间
Full GC运行间隔
Minor GC运行间隔
整个Full GC的时间
整个Minor GC的运行时间
整个GC的运行时间
Full GC的执行次数
Minor GC的执行次数

查看启动线程数量
grep "Thread " js.log | wc -l



Native Memory Tracking
开启 NMT 并选择 summary 模式
-XX:NativeMemoryTracking=summary
-XX:+UnlockDiagnosticVMOptions -XX:+PrintNMTStatistics

