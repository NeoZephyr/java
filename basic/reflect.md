## 反射实现
```java
public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("com.pain.flame.lab.ReflectTest");
        Method method = klass.getMethod("trace", int.class);
        method.invoke(null, 0);
    }

    public static void trace(int i) {
        new Exception("# " + i).printStackTrace();
    }
}
```
反射调用先是调用了 Method.invoke，然后进入委派实现，再然后进入本地实现，最后到达目标方法。Java 的反射调用机制还设立了另一种动态生成字节码的实现，即动态实现，直接使用 invoke 指令来调用目标方法。之所以采用委派实现，便是为了能够在本地实现以及动态实现中切换

动态实现和本地实现相比，其运行效率要快上 20 倍。这是因为动态实现无需经过 Java 到 C++ 再到 Java 的切换，但由于生成字节码十分耗时，仅调用一次的话，反而是本地实现要快上 3 到 4 倍

考虑到许多反射调用仅会执行一次，Java 虚拟机设置了一个阈值 15（可以通过 -Dsun.reflect.inflationThreshold 来调整），当某个反射调用的调用次数在 15 之下时，采用本地实现；当达到 15 时，便开始动态生成字节码，并将委派实现的委派对象切换至动态实现，这个过程我们称之为 Inflation

```java
public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("com.pain.flame.lab.ReflectTest");
        Method method = klass.getMethod("trace", int.class);

        for (int i = 0; i < 20; ++i) {
            method.invoke(null, i);
        }
    }

    public static void trace(int i) {
        new Exception("# " + i).printStackTrace();
    }
}
```

```
java -verbose:class ReflectTest
```

使用 -verbose:class 打印加载的类，可以看到，在第 15 次反射调用时，触发了动态实现的生成。这时候，Java 虚拟机加载了 GeneratedMethodAccessor1 类。并且，从第 16 次反射调用开始，便切换至这个刚刚生成的动态实现

反射调用的 Inflation 机制是可以通过参数（-Dsun.reflect.noInflation=true）来关闭。这样，在反射调用一开始便会直接生成动态实现，而不会使用委派实现或者本地实现


## 反射开销
在刚才的反射调用中，先后进行了 Class.forName，Class.getMethod 以及 Method.invoke 三个操作。其中，Class.forName 会调用本地方法，Class.getMethod 则会遍历该类的公有方法。如果没有匹配到，它还将遍历父类的公有方法。这两个操作都非常费时

值得注意的是，以 getMethod 为代表的查找方法操作，会返回查找得到结果的一份拷贝。因此，应当避免在热点代码中使用返回 Method 数组的 getMethods 或者 getDeclaredMethods 方法，以减少不必要的堆空间消耗。在实践中，可以在应用程序中缓存 Class.forName 和 Class.getMethod 的结果

```java
public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("com.pain.flame.lab.ReflectTest");
        Method method = klass.getMethod("trace", int.class);
        long current = System.currentTimeMillis();

        for (int i = 1; i < 2000000000; ++i) {
            if (i % 100000000 == 0) {
                long temp = System.currentTimeMillis();
                System.out.println(temp - current);
                current = temp;
            }

            method.invoke(null, 128);
        }
    }

    public static void trace(int i) {
    }
}
```

第一，由于 Method.invoke 是一个变长参数方法，在字节码层面它的最后一个参数会是 Object 数组。Java 编译器会在方法调用处生成一个长度为传入参数数量的 Object 数组，并将传入参数一一存储进该数组中

第二，由于 Object 数组不能存储基本类型，Java 编译器会对传入的基本类型参数进行自动装箱

这两个操作除了带来性能开销外，还可能占用堆内存

Java 缓存了 [-128, 127] 中所有整数所对应的 Integer 对象。当需要自动装箱的整数在这个范围之内时，便返回缓存的 Integer，否则需要新建一个 Integer 对象。可以将这个缓存的范围扩大至覆盖 128（对应参数 -Djava.lang.Integer.IntegerCache.high=128），便可以避免需要新建 Integer 对象的场景。或者在循环外缓存 128 自动装箱得到的 Integer 对象，并且直接传入反射调用中
```java
for (int i = 1; i < 2000000000; ++i) {
    if (i % 100000000 == 0) {
        long temp = System.currentTimeMillis();
        System.out.println(temp - current);
        current = temp;
    }

    method.invoke(null, arg);
}
```

在循环外新建一个 Object 数组，设置好参数，并直接交给反射调用
```java
Object[] argArr = new Object[1];
argArr[0] = 128;

for (int i = 1; i < 2000000000; ++i) {
    if (i % 100000000 == 0) {
        long temp = System.currentTimeMillis();
        System.out.println(temp - current);
        current = temp;
    }

    method.invoke(null, argArr);
}
```
加上这个更改之后，性能反而变差了。这是因为原本的反射调用被内联了，从而使得即时编译器中的逃逸分析将原本新建的 Object 数组判定为不逃逸的对象。如果一个对象不逃逸，那么即时编译器可以选择栈分配甚至是虚拟分配，也就是不占用堆空间。如果在循环外新建数组，即时编译器无法确定这个数组会不会中途被更改，因此无法优化掉访问数组的操作

关闭反射调用的 Inflation 机制，从而取消委派实现，并且直接使用动态实现。此外，每次反射调用都会检查目标方法的权限，而这个检查同样可以在 Java 代码里关闭
```java
// 添加如下虚拟机参数：
// -Dsun.reflect.noInflation=true
public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("com.pain.flame.lab.ReflectTest");
        Method method = klass.getMethod("trace", int.class);
        method.setAccessible(true);
        long current = System.currentTimeMillis();

        Integer arg = 128;
        Object[] argArr = new Object[1];
        argArr[0] = 128;

        for (int i = 1; i < 2000000000; ++i) {
            if (i % 100000000 == 0) {
                long temp = System.currentTimeMillis();
                System.out.println(temp - current);
                current = temp;
            }

            method.invoke(null, arg);
        }
    }

    public static void trace(int i) {
    }
}
```


由于 Java 虚拟机的关于调用点的类型 profile（注：对于 invokevirtual 或者 invokeinterface，Java 虚拟机会记录下调用者的具体类型，称之为类型 profile）无法同时记录这么多个类，因此可能出现所反射调用没有被内联的情况

在循环之前调用了 polluteProfile 的方法。该方法将反射调用另外两个方法，并且循环上 2000 遍，而循环则保持不变，结果性能变得很差。也就是说，只要误扰了 Method.invoke 方法的类型 profile，性能开销便会上升

之所以这么慢，除了没有内联之外，另外一个原因是逃逸分析不再起效。这时候，可以在循环外构造参数数组，并直接传递给反射调用

除此之外，还可以提高 Java 虚拟机关于每个调用能够记录的类型数目（对应虚拟机参数 -XX:TypeProfileWidth，默认值为 2，这里设置为 3）
```java
public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("com.pain.flame.lab.ReflectTest");
        Method method = klass.getMethod("trace", int.class);
        method.setAccessible(true);
        polluteProfile();
        long current = System.currentTimeMillis();

        Integer arg = 128;
        Object[] argArr = new Object[1];
        argArr[0] = 128;

        for (int i = 1; i < 2000000000; ++i) {
            if (i % 100000000 == 0) {
                long temp = System.currentTimeMillis();
                System.out.println(temp - current);
                current = temp;
            }

            method.invoke(null, 128);
        }
    }

    public static void polluteProfile() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method1 = ReflectTest.class.getMethod("trace1", int.class);
        Method method2 = ReflectTest.class.getMethod("trace2", int.class);

        for (int i = 0; i < 2000; ++i) {
            method1.invoke(null, 0);
            method2.invoke(null, 0);
        }
    }

    public static void trace(int i) {
    }

    public static void trace1(int i) {}

    public static void trace2(int i) {}
}
```

```java
public class ReflectTest {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> klass = Class.forName("com.pain.flame.lab.ReflectTest");
        Method method = klass.getMethod("trace", int.class);
        method.setAccessible(true);
        polluteProfile();
        long current = System.currentTimeMillis();

        Integer arg = 128;
        Object[] argArr = new Object[1];
        argArr[0] = 128;

        for (int i = 1; i < 2000000000; ++i) {
            if (i % 100000000 == 0) {
                long temp = System.currentTimeMillis();
                System.out.println(temp - current);
                current = temp;
            }

            method.invoke(null, argArr);
        }
    }

    public static void polluteProfile() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method1 = ReflectTest.class.getMethod("trace", int.class);
        Method method2 = ReflectTest.class.getMethod("trace", int.class);

        System.out.println(method1.equals(method2));

        for (int i = 0; i < 2000; ++i) {
            method1.invoke(null, 0);
            method2.invoke(null, 0);
        }
    }

    public static void trace(int i) {
    }

    public static void trace1(int i) {}

    public static void trace2(int i) {}
}
```
