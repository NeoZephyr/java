## 类编译
编译后的字节码文件主要包括常量池和方法表集合这两部分

常量池主要记录的是类文件中出现的字面量以及符号引用
方法表集合中主要包含一些方法的字节码、方法访问权限、方法名索引（与常量池中的方法引用对应）、描述符索引、JVM 执行指令以及属性集合等


## 即时编译
在 HotSpot 虚拟机中，内置了两个 JIT，分别为 C1 编译器和 C2 编译器，这两个编译器的编译过程是不一样的

C1 编译器是一个简单快速的编译器，主要的关注点在于局部性的优化，适用于执行时间较短或对启动性能有要求的程序

C2 编译器是为长期运行的服务器端应用程序做性能调优的编译器，适用于执行时间较长或对峰值性能有要求的程序。根据各自的适配性，这两种即时编译也被称为 Client Compiler 和 Server Compiler

Java7 引入了分层编译，这种方式综合了 C1 的启动性能优势和 C2 的峰值性能优势。分层编译将 JVM 的执行状态分为了 5 个层次：

第 0 层：程序解释执行，默认开启性能监控功能（Profiling），如果不开启，可触发第二层编译
第 1 层：可称为 C1 编译，将字节码编译为本地代码，进行简单、可靠的优化，不开启 Profiling
第 2 层：也称为 C1 编译，开启 Profiling，仅执行带方法调用次数和循环回边执行次数 profiling 的 C1 编译
第 3 层：也称为 C1 编译，执行所有带 Profiling 的 C1 编译
第 4 层：可称为 C2 编译，也是将字节码编译为本地代码，但是会启用一些编译耗时较长的优化，甚至会根据性能监控信息进行一些不可靠的激进优化

在 Java8 中，默认开启分层编译，-client 和 -server 的设置是无效的。如果只想开启 C2，可以关闭分层编译（-XX:-TieredCompilation），如果只想用 C1，可以在打开分层编译的同时，使用参数：-XX:TieredStopAtLevel=1

除了这种默认的混合编译模式，可以使用 -Xint 参数强制虚拟机运行于只有解释器的编译模式下，这时 JIT 完全不介入工作；也可以使用参数 -Xcomp 强制虚拟机运行于只有 JIT 的编译模式下


## 热点探测
热点探测是基于计数器的热点探测，采用这种方法的虚拟机会为每个方法建立计数器，统计方法的执行次数，如果执行次数超过一定的阈值就认为它是热点方法

虚拟机为每个方法准备了两类计数器：方法调用计数器（Invocation Counter）和回边计数器（Back Edge Counter）。在确定虚拟机运行参数的前提下，这两个计数器都有一个确定的阈值，当计数器超过阈值溢出了，就会触发 JIT 编译

方法调用计数器：用于统计方法被调用的次数，方法调用计数器的默认阈值在 C1 模式下是 1500 次，在 C2 模式在是 10000 次，可通过 -XX:CompileThreshold 来设定；而在分层编译的情况下，-XX:CompileThreshold 指定的阈值将失效，此时将会根据当前待编译的方法数以及编译线程数来动态调整。当方法计数器和回边计数器之和超过方法计数器阈值时，就会触发 JIT 编译器

回边计数器：用于统计一个方法中循环体代码执行的次数，在字节码中遇到控制流向后跳转的指令称为回边（Back Edge），该值用于计算是否触发 C1 编译的阈值，在不开启分层编译的情况下，C1 默认为 13995，C2 默认为 10700，可通过 -XX:OnStackReplacePercentage=N 来设置；而在分层编译的情况下，-XX:OnStackReplacePercentage 指定的阈值同样会失效，此时将根据当前待编译的方法数以及编译线程数来动态调整

建立回边计数器的主要目的是为了触发 OSR（On StackReplacement）编译，即栈上编译。在一些循环周期比较长的代码段中，当循环达到回边计数器阈值时，JVM 会认为这段是热点代码，JIT 编译器就会将这段代码编译成机器语言并缓存，在该循环时间段内，会直接将执行代码替换，执行缓存的机器语言


## 编译优化技术
### 方法内联
调用一个方法通常要经历压栈和出栈。调用方法是将程序执行顺序转移到存储该方法的内存地址，将方法的内容执行完后，再返回到执行该方法前的位置

这种执行操作要求在执行前保护现场并记忆执行的地址，执行后要恢复现场，并按原来保存的地址继续执行。 因此，方法调用会产生一定的时间和空间方面的开销

那么对于那些方法体代码不是很大，又频繁调用的方法来说，这个时间和空间的消耗会很大。方法内联的优化行为就是把目标方法的代码复制到发起调用的方法之中，避免发生真实的方法调用

JVM 会自动识别热点方法，并对它们使用方法内联进行优化。我们可以通过 -XX:CompileThreshold 来设置热点方法的阈值。但要强调一点，热点方法不一定会被 JVM 做内联优化，如果这个方法体太大了，JVM 将不执行内联操作。而方法体的大小阈值，也可以通过参数设置来优化：

经常执行的方法，默认情况下，方法体大小小于 325 字节的都会进行内联，可以通过 -XX:MaxFreqInlineSize=N 来设置

不是经常执行的方法，默认情况下，方法大小小于 35 字节才会进行内联，可以通过 -XX:MaxInlineSize=N 来设置 

通过配置 JVM 参数来查看到方法被内联的情况：
在控制台打印编译过程信息
-XX:+PrintCompilation

解锁对 JVM 进行诊断的选项参数。默认是关闭的，开启后支持一些特定参数对 JVM 进行诊断
-XX:+UnlockDiagnosticVMOptions

将内联方法打印出来
-XX:+PrintInlining


一般可以通过以下几种方式来提高方法内联：
1. 通过设置 JVM 参数来减小热点阈值或增加方法体阈值，以便更多的方法可以进行内联，但这种方法意味着需要占用更多地内存
2. 在编程中，避免在一个方法中写大量代码，习惯使用小方法体
3. 尽量使用 final、private、static 关键字修饰方法，编码方法因为继承，会需要额外的类型检查

### 逃逸分析
逃逸分析（Escape Analysis）是判断一个对象是否被外部方法引用或外部线程访问的分析技术，编译器会根据逃逸分析的结果对代码进行优化

栈上分配，逃逸分析如果发现一个对象只在方法中使用，就会将对象分配在栈上

分别设置 VM 参数：
Xmx1000m -Xms1000m -XX:-DoEscapeAnalysis -XX:+PrintGC
-Xmx1000m -Xms1000m -XX:+DoEscapeAnalysis -XX:+PrintGC

通过 VisualVM 工具，查看堆中创建的对象数量

锁消除
在非线程安全的情况下，尽量不要使用线程安全容器。如果使用线程安全容器，在局部方法中创建的对象只能被当前线程访问，无法被其它线程访问，这个变量的读写肯定不会有竞争，这个时候 JIT 编译会对这个对象的方法锁进行锁消除

标量替换
逃逸分析证明一个对象不会被外部访问，如果这个对象可以被拆分的话，当程序真正执行的时候可能不创建这个对象，而直接创建它的成员变量来代替。将对象拆分后，可以分配对象的成员变量在栈或寄存器上，原本的对象就无需分配内存空间了。这种编译优化就叫做标量替换


通过设置 JVM 参数来开关逃逸分析，还可以单独开关同步消除和标量替换，在 JDK1.8 中 JVM 是默认开启这些操作的

开启逃逸分析（jdk1.8 默认开启，其它版本未测试）
-XX:+DoEscapeAnalysis

关闭逃逸分析
-XX:-DoEscapeAnalysis

开启锁消除（jdk1.8 默认开启，其它版本未测试）
-XX:+EliminateLocks

关闭锁消除
-XX:-EliminateLocks

开启标量替换（jdk1.8 默认开启，其它版本未测试）
-XX:+EliminateAllocations

关闭
-XX:-EliminateAllocations
