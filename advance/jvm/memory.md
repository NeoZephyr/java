## JVM 内存模型
JVM 内存模型主要分为堆、程序计数器、方法区、虚拟机栈和本地方法栈


## 堆
堆内存是 JVM 中最大的一块，被所有线程共享。几乎所有的对象实例都在这里分配内存

堆被划分为新生代和老年代，新生代又被进一步划分为 Eden 和 Survivor 区，最后 Survivor 由 From Survivor 和 To Survivor 组成

在 Java6 版本中，永久代在非堆内存区；到了 Java7 版本，永久代的静态变量和运行时常量池被合并到了堆中；而到了 Java8，永久代被元空间取代了

### 控制参数
1. -Xms 设置堆的最小空间，-Xmx 设置堆的最大空间
2. -XX:NewSize 设置新生代最小空间，-XX:MaxNewSize 设置新生代最大空间
3. -XX:PermSize 设置永久代最小空间，-XX:MaxPermSize 设置永久代最大空间
4. -Xss 设置每个线程的堆栈
5. 通过设置堆空间大小和新生代空间大小两个参数来间接控制老年代的参数


## 程序计数器
程序计数器是一块很小的内存空间，主要用来记录各个线程执行的字节码的地址，例如，分支、循环、跳转、异常、线程恢复等都依赖于计数器

如果一个线程的时间片用完了，或者是其它原因导致这个线程的 CPU 资源被提前抢夺，那么这个退出的线程就需要单独的一个程序计数器，来记录下一条运行的指令


## 方法区
HotSpot 虚拟机使用永久代来实现方法区，在其它虚拟机中，例如，Oracle 的 JRockit、IBM 的 J9 不存在永久代一说。可以说，在 HotSpot 虚拟机中，设计人员使用了永久代来实现了 JVM 规范的方法区

方法区主要是用来存放已被虚拟机加载的类相关信息，包括类信息、运行时常量池、字符串常量池。类信息又包括了类的版本、字段、方法、接口和父类等信息


JVM 在执行某个类的时候，必须经过加载、连接、初始化，而连接又包括验证、准备、解析三个阶段。在加载类的时候，JVM 会先加载 class 文件，而在 class 文件中除了有类的版本、字段、方法和接口等描述信息外，还有一项信息是常量池，用于存放编译期间生成的各种字面量和符号引用

字面量包括字符串（String a="b"）、基本类型的常量（final 修饰的变量），符号引用则包括类和方法的全限定名（例如 String 这个类，它的全限定名就是 Java/lang/String）、字段的名称和描述符以及方法的名称和描述符

当类加载到内存中后，JVM 就会将 class 文件常量池中的内容存放到运行时的常量池中；在解析阶段，JVM 会把符号引用替换为直接引用（对象的索引值）


方法区与堆空间类似，也是一个共享内存区，所以方法区是线程共享的。假如两个线程都试图访问方法区中的同一个类信息，而这个类还没有装入 JVM，那么此时就只允许一个线程去加载它，另一个线程必须等待


在 HotSpot 虚拟机、Java7 版本中已经将永久代的静态变量和运行时常量池转移到了堆中，其余部分则存储在 JVM 的非堆内存中

Java8 版本将方法区中实现的永久代去掉，并用元空间代替了之前的永久代，并且元空间的存储位置是本地内存。之前永久代的类的元数据存储在了元空间，永久代的静态变量以及运行时常量池则跟 Java7 一样，转移到了堆中

移除永久代是为了融合 HotSpot JVM 与 JRockit VM 而做出的努力，因为 JRockit 没有永久代，所以不需要配置永久代

永久代内存经常不够用或发生内存溢出，这是因为在 JDK1.7 版本中，指定的 PermGen 区大小为 8M，由于 PermGen 中类的元数据信息在每次 FullGC 的时候都可能被收集，回收率都偏低。而且，为 PermGen 分配多大的空间很难确定，PermSize 的大小依赖于很多因素，比如，JVM 加载的 class 总数、常量池的大小和方法的大小等


## 虚拟机栈
Java 虚拟机栈是线程私有的内存空间，它和 Java 线程一起创建

当创建一个线程时，会在虚拟机栈中申请一个线程栈，用来保存方法的局部变量、操作数栈、动态链接方法和返回地址等信息，并参与方法的调用和返回

每一个方法的调用都伴随着栈帧的入栈操作，方法的返回则是栈帧的出栈操作


## 本地方法栈
本地方法栈与虚拟机栈非常相似，区别仅仅是虚拟机栈为虚拟机执行 Java 方法，而本地方法栈则是 Native 方法服务


## jvm 运行原理
```java
public class JVMCase {
    // 常量
    public final static String MAN_SEX_TYPE = "man";
 
    // 静态变量
    public static String WOMAN_SEX_TYPE = "woman";
 
    public static void main(String[] args) {
        Student stu = new Student();
        stu.setName("nick");
        stu.setSexType(MAN_SEX_TYPE);
        stu.setAge(20);

        JVMCase jvmcase = new JVMCase();
        
        // 调用静态方法
        print(stu);
        // 调用非静态方法
        jvmcase.sayHello(stu);
    }

    // 常规静态方法
    public static void print(Student stu) {
        System.out.println("name: " + stu.getName() + "; sex:" + stu.getSexType() + "; age:" + stu.getAge()); 
    }

    // 非静态方法
    public void sayHello(Student stu) {
        System.out.println(stu.getName() + "say: hello"); 
    }
}
```

1. JVM 通过配置参数或者默认配置参数向操作系统申请内存空间。操作系统根据内存大小找到具体的内存分配表，然后把内存段的起始地址和终止地址分配给 JVM

2. JVM 获得内存空间后，会根据配置参数分配堆、栈以及方法区的内存大小

3. class 文件加载、验证、准备以及解析，其中准备阶段会为类的静态变量分配内存，初始化为系统的初始值

4. JVM 执行构造器 <clinit> 方法。编译器会在 .java 文件被编译成 .class 文件时，收集所有类的初始化代码，包括静态变量赋值语句、静态代码块、静态方法，收集在一起成为 <clinit>() 方法

5. 启动 main 线程，执行 main 方法，开始执行第一行代码。此时堆内存中会创建一个 student 对象，对象引用 student 就存放在栈中

6. 创建 JVMCase 对象，调用 sayHello 非静态方法，sayHello 方法入栈，并通过栈中的 student 引用调用堆中的 Student 对象；之后，调用静态方法 print，print 静态方法属于 JVMCase 类，是从静态方法中获取，之后放入到栈中，也是通过 student 引用调用堆中的 student 对象

