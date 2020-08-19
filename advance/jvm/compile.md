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




## 初始化时机
只有当对类主动使用时才会导致类的初始化，类的主动使用包括以下六种：
– 创建类的实例，也就是 `new` 关键字实例化对象
– 访问类或接口的静态变量，或者对该静态变量赋值
– 调用类的静态方法
– 反射（如 `Class.forName("com.pain.Test")`）调用
– 初始化某个类的子类，则其父类也会被初始化
– Java 虚拟机启动时被标明为启动类的类

以下几种情况，不会触发类初始化 
1、通过子类引用父类的静态字段，只会触发父类的初始化，而不会触发子类的初始化
2、定义对象数组，不会触发该类的初始化
3、常量在编译期间会存入调用类的常量池中，本质上并没有直接引用定义常量的类，不会触发定义常量所在的类的初始化
4、通过类名获取Class对象，不会触发类的初始化(Car.class)
5、通过Class.forName加载指定类时，如果指定参数initialize为false时，也不会触发类初始化，这个参数告诉虚拟机是否要对类进行初始化
6、通过ClassLoader默认的loadClass方法，也不会触发初始化动作
```
new ClassLoader(){}.loadClass("Car");
```


### 结束
Java 虚拟机将结束生命周期的情况有如下几种：
– 执行 `System.exit()` 方法
– 程序正常执行结束
– 程序在执行过程中遇到了异常或错误而异常终止
– 由于操作系统出现错误而导致 Java 虚拟机进程终止

## `class` 文件来源
– 从本地系统中加载
– 从网络下载 class 文件
– 从 zip，jar 等归档文件中加载 class 文件
– 从专有数据库中提取 class 文件
– 将 Java 源文件动态编译为 class 文件

## 类加载方式
- 命令行启动应用时候由 JVM 初始化加载
- 通过 `Class.forName()` 方法动态加载，将类的文件加载到 JVM 中并对类进行解释，执行类中的静态块
- 通过 `ClassLoader.loadClass()` 方法动态加载，只将类文件加载到 JVM 中

## 类加载器
对于任意一个类，都需要由加载它的类加载器和这个类本身一同确立其在Java虚拟机中的唯一性。如果两个类来源于同一个Class文件，只要加载它们的类加载器不同，那么这两个类就必定不相等。父类加载器采用组合实现而不是继承关系

### `Bootstrap ClassLoader`
启动类加载器，负责加载存放在 JDK\jre\lib 下，或由 `-Xbootclasspath` 参数指定的路径中的能被虚拟机识别的类库

### `Extension ClassLoader`
扩展类加载器，负责加载 JDK\jre\lib\ext 下，或由 `java.ext.dirs` 系统变量指定的路径中的所有类库，开发者可以直接使用扩展类加载器

### `Application ClassLoader`
应用程序类加载器，负责加载用户类路径所指定的类，开发者可以直接使用该类加载器。若应用程序中没有自定义过自己的类加载器，则将该类加载器作为程序中默认的类加载器

## 类加载机制
### 全盘负责
当一个类加载器加载某个类时，该类所依赖和引用的其他类也将由该类加载器负责载入，除非显示使用另外一个类加载器来加载

### 父类委托
先让父类加载器试图加载该类，只有在父类加载器无法加载该类时才尝试从自己的类路径中加载该类

### 缓存机制
所有加载过的类都会被缓存，当程序中需要使用某个类时，类加载器先从缓存区寻找该类，只有缓存区不存在，系统才会读取该类对应的二进制数据进行加载

### 双亲委派模型
#### 双亲委派模型流程
- 判断类是否已被加载
- 若没有被加载，就委托给父类加载或者委派给启动类加载器加载
- 若不存在父加载器，检查是否是由启动类加载器加载的类
- 若父类加载器和启动类加载器都不能完成加载则调用自身的加载功能
```java
public Class<?> loadClass(String name) throws ClassNotFoundException {
  return loadClass(name, false);
}

protected Class<?> loadClass(String name, boolean resolve)
  throws ClassNotFoundException
{
  synchronized (getClassLoadingLock(name)) {
    // First, check if the class has already been loaded
    Class<?> c = findLoadedClass(name);
    if (c == null) {
      long t0 = System.nanoTime();
      try {
        if (parent != null) {
          c = parent.loadClass(name, false);
        } else {
          c = findBootstrapClassOrNull(name);
        }
      } catch (ClassNotFoundException e) {
        // ClassNotFoundException thrown if class not found
        // from the non-null parent class loader
      }

      if (c == null) {
        // If still not found, then invoke findClass in order
        // to find the class.
        long t1 = System.nanoTime();
        c = findClass(name);

        // this is the defining class loader; record the stats
        sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
        sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
        sun.misc.PerfCounter.getFindClasses().increment();
      }
    }

    if (resolve) {
      resolveClass(c);
    }
    return c;
  }
}
```

#### 双亲委派模型意义
- 防止内存中出现多份同样的字节码
- 保证 Java 程序安全稳定运行

### 自定义类加载器
自定义类加载器一般都是继承自 `ClassLoader` 类，只需要重写 `findClass` 方法即可
``` java
public class MyClassLoader extends ClassLoader {
 
  private String root;

  protected Class<?> findClass(String name) throws ClassNotFoundException {
    byte[] classData = loadClassData(name);
    if (classData == null) {
      throw new ClassNotFoundException();
    } else {
      // 将字节码转换为 Class 对象 
      return defineClass(name, classData, 0, classData.length);
    }
  }

  private byte[] loadClassData(String className) {
    String fileName = root + File.separatorChar
            + className.replace('.', File.separatorChar) + ".class";
    try {
      InputStream ins = new FileInputStream(fileName);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int bufferSize = 1024;
      byte[] buffer = new byte[bufferSize];
      int length = 0;
      while ((length = ins.read(buffer)) != -1) {
        baos.write(buffer, 0, length);
      }
      return baos.toByteArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getRoot() {
    return root;
  }

  public void setRoot(String root) {
    this.root = root;
  }
}
```

### ClassLoader vs Class.forName
Class.forName()：将类的.class文件加载到jvm中之外，还会对类进行解释，执行类中的static块
```java
// 使用系统类加载器加载
public static Class<?> forName(String className)
            throws ClassNotFoundException {}

// 指定 ClassLoader 加载
public static Class<?> forName(String name, boolean initialize,
                               ClassLoader loader)
    throws ClassNotFoundException {}
```

ClassLoader.loadClass()：只干一件事情，就是将.class文件加载到jvm中，不会执行static中的内容