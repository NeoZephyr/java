## 方法句柄
方法句柄是一个强类型的，能够被直接执行的引用

方法句柄的类型是由所指向方法的参数类型以及返回类型组成的。它是用来确认方法句柄是否适配的唯一关键。当使用方法句柄时，我们并不关心方法句柄所指向方法的类名或者方法名

对于用 invokestatic 调用的静态方法，使用 Lookup.findStatic 方法；对于用 invokevirutal 调用的实例方法，以及用 invokeinterface 调用的接口方法，使用 findVirtual 方法；对于用 invokespecial 调用的实例方法，使用 findSpecial 方法

```java
class Foo {
    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    private static void bar(Object o) {
        System.out.println("bar");
    }

    private void foo(Object o) {
        System.out.println("foo");
    }
}
```
```java
private static void test1() throws Throwable {
    MethodHandles.Lookup lookup = Foo.lookup();
    Method method1 = Foo.class.getDeclaredMethod("bar", Object.class);
    MethodHandle methodHandle = lookup.unreflect(method1);
    methodHandle.invoke(null);

    Method method2 = Foo.class.getDeclaredMethod("foo", Object.class);
    methodHandle = lookup.unreflect(method2);
    methodHandle.invoke(new Foo(), null);
}

private static void test2() throws Throwable {
    MethodHandles.Lookup lookup = Foo.lookup();
    MethodType methodType = MethodType.methodType(void.class, Object.class);
    MethodHandle methodHandle = lookup.findStatic(Foo.class, "bar", methodType);
    methodHandle.invoke(null);

    methodHandle = lookup.findVirtual(Foo.class, "foo", methodType);
    methodHandle.invoke(new Foo(), null);
}
```

调用方法句柄，和原本对应的调用指令是一致的。也就是说，对于原本用 invokevirtual 调用的方法句柄，它也会采用动态绑定；而对于原本用 invkespecial 调用的方法句柄，它会采用静态绑定

方法句柄同样也有权限问题。但它与反射 API 不同，其权限检查是在句柄的创建阶段完成的。在实际调用过程中，Java 虚拟机并不会检查方法句柄的权限。如果该句柄被多次调用的话，与反射调用相比，它将省下重复权限检查的开销

需要注意的是，方法句柄的访问权限不取决于方法句柄的创建位置，而是取决于 Lookup 对象的创建位置。例如，对于一个私有字段，如果 Lookup 对象是在私有字段所在类中获取的，那么这个 Lookup 对象便拥有对该私有字段的访问权限，即使是在所在类的外边，也能够通过该 Lookup 对象创建该私有字段的 getter 或者 setter

由于方法句柄没有运行时权限检查，因此，应用程序需要负责方法句柄的管理。一旦它发布了某些指向私有方法的方法句柄，那么这些私有方法便被暴露出去了


## 方法句柄的操作
### invokeExact
假设一个方法句柄将接收一个 Object 类型的参数，如果你直接传入 String 作为实际参数，那么方法句柄的调用会在运行时抛出方法类型不匹配的异常

```java
public final native @PolymorphicSignature Object invokeExact(Object... args) throws Throwable;
```
方法句柄 API 有一个特殊的注解类 @PolymorphicSignature。在碰到被它注解的方法调用时，Java 编译器会根据所传入参数的声明类型来生成方法描述符，而不是采用目标方法所声明的描述符。当传入的参数是 String 时，对应的方法描述符包含 String 类；而当我们转化为 Object 时，对应的方法描述符则包含 Object 类

invokeExact 会确认该 invokevirtual 指令对应的方法描述符，和该方法句柄的类型是否严格匹配。在不匹配的情况下，便会在运行时抛出异常

### invoke
invoke 会调用 MethodHandle.asType 方法，生成一个适配器方法句柄，对传入的参数进行适配，再调用原方法句柄。调用原方法句柄的返回值同样也会先进行适配，然后再返回给调用者

方法句柄还支持增删改参数的操作，这些操作都是通过生成另一个方法句柄来实现的
1. 改操作通过 MethodHandle.asType 方法实现
2. 删操作将传入的部分参数就地抛弃，再调用另一个方法句柄。它对应的 API 是 MethodHandles.dropArguments 方法
3. 增操作会往传入的参数中插入额外的参数，再调用另一个方法句柄，它对应的 API 是 MethodHandle.bindTo 方法

增操作还可以用来实现方法的柯里化。举个例子，有一个指向 f(x, y) 的方法句柄，可以通过将 x 绑定为 4，生成另一个方法句柄 g(y) = f(4, y)。在执行过程中，每当调用 g(y) 的方法句柄，它会在参数列表最前面插入一个 4，再调用指向 f(x, y) 的方法句柄


## 方法句柄的实现
调用方法句柄所使用的 invokeExact 或者 invoke 方法具备签名多态性的特性。它们会根据具体的传入参数来生成方法描述符


```java
public class MethodHandlerTest {
    public static void main(String[] args) throws Throwable {
        test();
    }

    private static void test() throws Throwable {
        MethodHandles.Lookup lookup = Foo.lookup();
        MethodType methodType = MethodType.methodType(void.class, Object.class);
        MethodHandle methodHandle = lookup.findStatic(Foo.class, "baz", methodType);
        methodHandle.invokeExact(new Object());
    }
}

class Foo {
    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    private static void baz(Object o) {
        new Exception().printStackTrace();
    }
}
```

从打印的堆栈信息中可以看出，也就是说，invokeExact 的目标方法就是方法句柄指向的方法。但是，invokeExact 会对参数的类型进行校验，并在不匹配的情况下抛出异常。如果它直接调用了方法句柄所指向的方法，那么这部分参数类型校验的逻辑将无处安放。因此，唯一的可能便是 Java 虚拟机隐藏了部分栈信息

启用 -XX:+UnlockDiagnosticVMOptions -XX:+ShowHiddenFrames 这个参数来打印被 Java 虚拟机隐藏了的栈信息，发现 main 方法和目标方法中间隔着两个貌似是生成的方法

实际上，Java 虚拟机会对 invokeExact 调用做特殊处理，调用至一个共享的、与方法句柄类型相关的特殊适配器中。这个适配器是一个 LambdaForm，我们可以通过添加虚拟机参数将之导出成 class 文件（-Djava.lang.invoke.MethodHandle.DUMP_CLASS_FILES=true）

在这个适配器中，它会调用 Invokers.checkExactType 方法来检查参数类型，然后调用 Invokers.checkCustomized 方法。后者会在方法句柄的执行次数超过一个阈值时进行优化（对应参数 -Djava.lang.invoke.MethodHandle.CUSTOMIZE_THRESHOLD，默认值为 127）。最后，它会调用方法句柄的 invokeBasic 方法

Java 虚拟机同样会对 invokeBasic 调用做特殊处理，这会调用至方法句柄本身所持有的适配器中。这个适配器同样是一个 LambdaForm

这个适配器将获取方法句柄中的 MemberName 类型的字段，并且以它为参数调用 linkToStatic 方法。Java 虚拟机也会对 linkToStatic 调用做特殊处理，它将根据传入的 MemberName 参数所存储的方法地址或者方法表索引，直接跳转至目标方法

方法句柄一开始持有的适配器是共享的。当它被多次调用之后，Invokers.checkCustomized 方法会为该方法句柄生成一个特有的适配器。这个特有的适配器会将方法句柄作为常量，直接获取其 MemberName 类型的字段，并继续后面的 linkToStatic 调用

可以看到，方法句柄的调用和反射调用一样，都是间接调用。因此，它也会面临无法内联的问题。不过，与反射调用不同的是，方法句柄的内联瓶颈在于即时编译器能否将该方法句柄识别为常量



