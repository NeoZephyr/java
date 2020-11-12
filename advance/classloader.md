## 类文件来源
1. 从本地系统中加载
2. 从网络下载 class 文件
3. 从 zip，jar 等归档文件中加载 class 文件
4. 从专有数据库中提取 class 文件
5. 将 Java 源文件动态编译为 class 文件

归档
```
jar cvf hello.jar Hello.class
```


## 类加载
一个类在 JVM 里的生命周期有 7 个阶段：分别是加载、验证、准备、解析、初始化、使用、卸载。其中前五个部分统称为类加载

### 加载
这个阶段主要的操作是：根据知道的 class 完全限定名，获取二进制的字节流。如果找不到二进制表示形式，则会抛出 NoClassDefFound 错误。装载阶段并不会检查 class 文件的语法和格式

### 校验
链接过程的第一个阶段是校验，确保 class 文件里的字节流信息符合当前虚拟机的要求，不会危害虚拟机的安全。校验过程检查 class 文件的语义，判断常量池中的符号，并执行类型检查，主要目的是判断字节码的合法性。这些检查过程中可能会抛出 VerifyError、ClassFormatError 或 UnsupportedClassVersionError

因为 class 文件的验证属是链接阶段的一部分，所以这个过程中可能需要加载其他类，在某个类的加载过程中，JVM 必须加载其所有的超类和接口

如果类层次结构有问题，例如该类是自己的超类或接口，则 JVM 将抛出 ClassCircularityError；如果实现的接口并不是一个 interface，或者声明的超类是一个 interface，也会抛出 IncompatibleClassChangeError

### 准备
创建静态字段，并将其初始化为标准默认值，并分配方法表，即在方法区中分配这些变量所使用的内存空间。请注意，准备阶段并未执行任何 Java 代码。例如：
```
public static int i = 1;
```
在准备阶段 i 的值会被初始化为 0，后面在类初始化阶段才会执行赋值为 1

但是下面如果使用 final 作为静态常量，在准备阶段就会被赋值 1
```
public static final int i = 1;
```

除了分配内存外，部分 Java 虚拟机还会在此阶段构造其他跟类层次相关的数据结构，比如说用来实现虚方法的动态绑定的方法表

### 解析
解析常量池，主要有以下四种：类或接口的解析、字段解析、类方法解析、接口方法解析

当一个变量引用某个对象的时候，这个引用在 class 文件中是以符号引用来存储的，在解析阶段就需要将其解析并链接为直接引用（相当于指向实际对象）。如果有了直接引用，那引用的目标必定在堆中存在

如果符号引用指向一个未被加载的类，或者未被加载类的字段或方法，那么解析将触发这个类的加载（但未必触发这个类的链接以及初始化）

加载一个 class 时, 需要加载所有的 super 类和 super 接口

Java 虚拟机规范并没有要求在链接过程中完成解析。它仅规定了：如果某些字节码使用了符号引用，那么在执行这些字节码之前，需要完成对这些符号引用的解析

### 初始化
1. 类构造器方法
2. static 静态变量赋值语句
3. 静态代码块

如果是一个子类进行初始化会先对其父类进行初始化，保证其父类在子类之前进行初始化

为了提高性能，HotSpot JVM 通常要等到类初始化时才去装载和链接类。因此，如果 A 类引用了 B 类，那么加载 A 类并不一定会去加载 B 类。主动对 B 类执行第一条指令时才会导致 B 类的初始化，这就需要先完成对 B 类的装载和链接

在 Java 代码中，如果要初始化一个静态字段，可以在声明时直接赋值，也可以在静态代码块中对其赋值。如果直接赋值的静态字段被 final 所修饰，并且它的类型是基本类型或字符串时，那么该字段便会被编译器标记成常量值（ConstantValue），其初始化直接由 Java 虚拟机完成。除此之外的直接赋值操作，以及所有静态代码块中的代码，则会被 Java 编译器置于同一方法中，并把它命名为 <clinit>。JVM 通过加锁来确保类的 <clinit> 方法仅被执行一次

```java
public class StaticVarTest {

    private static int var1 = 100;

    static {
        var1 = 200;
        var2 = 2000;
    }

    private static int var2 = 1000;

    public static void main(String[] args) {
        // 200
        System.out.println(StaticVarTest.var1);
        
        // 0 -> 2000 -> 1000
        // 1000
        System.out.println(StaticVarTest.var2);
    }
}
```


只有当初始化完成之后，类才正式成为可执行的状态

类的初始化触发情况：
1. 当虚拟机启动时，初始化用户指定的主类，就是启动执行的 main 方法所在的类
2. 当遇到用以新建目标类实例的 new 指令时，初始化 new 指令的目标类
3. 当遇到调用静态方法的指令时，初始化该静态方法所在的类
4. 当遇到访问静态字段的指令时，初始化该静态字段所在的类
5. 子类的初始化会触发父类的初始化
6. 如果一个接口定义了 default 方法，那么直接实现或者间接实现该接口的类的初始化，会触发该接口的初始化
7. 使用反射 API 对某个类进行反射调用时，初始化这个类
8. 当初次调用 MethodHandle 实例时，初始化该 MethodHandle 指向的方法所在的类

以下几种情况，不会触发类初始化 
1. 通过子类引用父类的静态字段，只会触发父类的初始化，而不会触发子类的初始化
2. 定义对象数组，不会触发该类的初始化
3. 常量在编译期间会存入调用类的常量池中，本质上并没有直接引用定义常量的类，不会触发定义常量所在的类的初始化
4. 通过类名获取 Class 对象，不会触发类的初始化（Car.class）
5. 通过 Class.forName 加载指定类时，如果指定参数 initialize 为 false 时，也不会触发类初始化，这个参数告诉虚拟机是否要对类进行初始化
6. 通过 ClassLoader 默认的 loadClass 方法，也不会触发初始化动作
```java
classLoader.loadClass("Car");
```

```java
public class Singleton {
    private Singleton() {}

    private static class LazyHolder {
        static final Singleton INSTANCE = new Singleton();
        static {
            System.out.println("LazyHolder.<clinit>");
        }
    }

    public static Object getInstance(boolean flag) {
        if (flag) return new LazyHolder[2];
        return LazyHolder.INSTANCE;
    }

    public static void main(String[] args) {
        getInstance(true);
        System.out.println("----");
        getInstance(false);
    }
}
```

打印类加载的先后顺序
```sh
java -verbose:class Singleton
```

新建数组会导致 LazyHolder 的加载，但不会导致它的初始化


## 类加载机制
类加载过程由类加载器来完成，系统自带的类加载器分为三种：
BootstrapClassLoader：启动类加载器，是用原生 C++ 代码来实现的，并不继承自 java.lang.ClassLoader，在 Java 的 API 里无法拿到。负责加载 Java 的核心类

ExtClassLoader：扩展类加载器，继承自 URLClassLoader 类。负责加载 jre\lib\ext 下，或由 java.ext.dirs 系统变量指定的路径中的所有类库，开发者可以直接使用扩展类加载器

AppClassLoader：应用类加载器，继承自 URLClassLoader 类。在应用程序代码里可以通过 ClassLoader 的静态方法 getSystemClassLoader 来获取应用类加载器。如果没有使用自定义类加载器，则用户自定义的类都由此加载器加载

自定义类加载器：自定义类加载器都以应用类加载器作为父加载器。应用类加载器的父类加载器为扩展类加载器


类加载机制特点：
1. 双亲委托：当一个自定义类加载器需要加载一个类，它不会直接试图加载它，而是先委托自己的父加载器去加载，父加载器如果发现自己还有父加载器，会一直往前找，只要上级加载器已经加载了这个类，所有的子加载器都不需要自己加载了。如果几个类加载器都没有加载到指定名称的类，那么会抛出 ClassNotFountException 异常

2. 负责依赖：如果一个加载器在加载某个类的时候，发现这个类依赖于另外几个类或接口，也会去尝试加载这些依赖项

3. 缓存加载：为了提升加载效率，消除重复加载，一旦某个类被一个类加载器加载，那么它会缓存这个加载结果，不会重复加载

双亲委派模型意义：
1. 防止内存中出现多份同样的字节码
2. 保证 Java 程序安全稳定运行

自定义类加载器：一般都是继承自 ClassLoader 类，只需要重写 findClass 方法即可
```java
public class Hello {
    static {
        System.out.println("Hello Class Initialized!");
    }
}
```

需要说明的是两个没有关系的自定义类加载器之间加载的类是不共享的，这样就可以实现不同的类型沙箱的隔离性，可以通过多个类加载器，各自加载同一个类的不同版本，可以相互之间不影响彼此，从而在这个基础上可以实现类的动态加载卸载，热插拔的插件机制等

```java
public class HelloClassLoader extends ClassLoader {
    public static void main(String[] args) {
        try {
            new HelloClassLoader().findClass("jvm.Hello").newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String helloBase64 = "";
        byte[] bytes = decode(helloBase64);
        return defineClass(name, bytes, 0, bytes.length);
    }

    public byte[] decode(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
```


## 应用
### 找不到 Jar 包的问题
直接看到各个类加载器加载了哪些 jar，以及把哪些路径加到了 classpath 里
```java

public class JvmClassLoaderPrintPath {
    public static void main(String[] args) {
        URL[] urls = sun.misc.Launcher.getBootstrapClassPath().getURLs();
        System.out.println("启动类加载器");

        for (URL url : urls) {
            System.out.println("==> " +url.toExternalForm());
        }

        printClassLoader("扩展类加载器", JvmClassLoaderPrintPath.class.getClassLoader());
        printClassLoader("应用类加载器", JvmClassLoaderPrintPath.class.getClassLoader());
    }

    public static void printClassLoader(String name, ClassLoader cl) {
        if (cl != null) {
            System.out.println(name + " ClassLoader ‐> " + cl.toString());
            printURLForClassLoader(cl);
        } else {
            System.out.println(name + " ClassLoader ‐> null");
        }
    }

    public static void printURLForClassLoader(ClassLoader cl) {
        Object ucp = insightField(cl, "ucp");
        Object path = insightField(ucp, "path");
        ArrayList ps = (ArrayList) path;

        for (Object p : ps) {
            System.out.println(" ==> " + p.toString());
        }
    }

    private static Object insightField(Object obj, String fName) {
        try {
            Field f = null;
            if (obj instanceof URLClassLoader) {
                f = URLClassLoader.class.getDeclaredField(fName);
            } else {
                f = obj.getClass().getDeclaredField(fName);
            }

            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

### 类方法不一致的问题
出现 java.lang.NoSuchMethodError，很可能是加载了错误的或者重复加载了不同版本的 jar 包。这时候，可以用前面的方法就可以先排查一下

### 加载的类以及加载顺序
假如有两个地方有 Hello.class，一个是新版本，一个是旧的，可以直接打印加载的类清单和加载顺序。只需要在类的启动命令行参数加上 ‐XX:+TraceClassLoading 或者 ‐verbose 即可
```
java ‐XX:+TraceClassLoading HelloClassLoader
```

### 调整或修改 ext 和本地加载路径
自定义加载的 jar 包，只加载 rt.jar
```
java ‐Dsun.boot.class.path="jre\lib\rt.jar"
```

参数 ‐Djava.ext.dirs 表示扩展类加载器要加载什么，一般情况下不需要的话可以直接配置为空即可

### 运行期加载额外的 jar 包或者 class
1. 自定义 ClassLoader 的方式
2. 直接在当前的应用类加载器里，使用 URLClassLoader 类的方法 addURL，不过这个方法是 protected 的，需要反射处理一下，然后又因为程序在启动时并没有显示加载 Hello 类，所以在添加完了 classpath 以后，没法直接显式初始化，需要使用 Class.forName 的方式来拿到已经加载的 Hello 类

```java
public class JvmAppClassLoaderAddURL {
    public static void main(String[] args) {
        String appPath = "file:/d:/app/";
        URLClassLoader urlClassLoader = (URLClassLoader) JvmAppClassLoaderAddURL.class;

        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL");
            addURL.setAccessible(true);
            URL url = new URL(appPath);
            addURL.invoke(urlClassLoader, url);
            Class.forName("jvm.Hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

