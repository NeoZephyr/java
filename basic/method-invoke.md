## 重载
在同一个类中名字相同，参数类型不同的方法之间的关系，称之为重载

重载的方法在编译过程中即可完成识别。具体到每一个方法调用，Java 编译器会根据所传入参数的声明类型来选取重载方法。选取的过程共分为三个阶段：

1. 在不考虑对基本类型自动装拆箱，以及可变长参数的情况下选取重载方法
2. 如果在第一阶段中没有找到适配的方法，那么在允许自动装拆箱，但不允许可变长参数的情况下选取重载方法
3. 如果在第二阶段中没有找到适配的方法，那么在允许自动装拆箱以及可变长参数的情况下选取重载方法

如果 Java 编译器在同一个阶段中找到了多个适配的方法，那么它会在其中选择一个最为贴切的，而决定贴切程度的一个关键就是形式参数类型的继承关系

```java
void invoke(Object obj, Object... args) {}
void invoke(String s, Object obj, Object... args) {}
```
```java
invoke(null, 1);
```
当传入 null 时，既可以匹配第一个方法中声明为 Object 的形式参数，也可以匹配第二个方法中声明为 String 的形式参数。由于 String 是 Object 的子类，因此 Java 编译器会认为第二个方法更为贴切

除了同一个类中的方法，重载也可以作用于这个类所继承而来的方法。也就是说，如果子类定义了与父类中非私有方法同名的方法，而且这两个方法的参数类型不同，那么在子类中，这两个方法同样构成了重载


## 重写
如果子类定义了与父类中非私有方法同名的方法，而且这两个方法的参数类型相同。如果这两个方法都是静态的，那么子类中的方法隐藏了父类中的方法；如果这两个方法都不是静态的，且都不是私有的，那么子类的方法重写了父类中的方法


## 方法识别
Java 虚拟机识别方法的关键在于类名、方法名以及方法描述符。方法描述符，它是由方法的参数类型以及返回类型所构成。在同一个类中，如果同时出现多个名字相同且描述符也相同的方法，那么 Java 虚拟机会在类的验证阶段报错

可以看到，Java 虚拟机与 Java 语言不同，它并不限制名字与参数类型相同，但返回类型不同的方法出现在同一个类中，对于调用这些方法的字节码来说，由于字节码所附带的方法描述符包含了返回类型，因此 Java 虚拟机能够准确地识别目标方法

如果子类定义了与父类中非私有、非静态方法同名的方法，那么只有当这两个方法的参数类型以及返回类型一致，Java 虚拟机才会判定为重写

对于 Java 语言中重写而 Java 虚拟机中非重写的情况，编译器会通过生成桥接方法来实现 Java 中的重写语义

Java 虚拟机中的静态绑定指的是在解析时便能够直接识别目标方法的情况，而动态绑定则指的是需要在运行过程中根据调用者的动态类型来识别目标方法的情况

具体来说，Java 字节码中与调用相关的指令共有五种：
1. invokestatic：用于调用静态方法
2. invokespecial：用于调用私有实例方法、构造器，以及使用 super 关键字调用父类的实例方法或构造器，和所实现接口的默认方法
3. invokevirtual：用于调用非私有实例方法
4. invokeinterface：用于调用接口方法
5. invokedynamic：用于调用动态方法

```java
interface Customer {
    boolean isVip();
}

class Shop {
    public double discount(double price, Customer customer) {
        return price * 0.8;
    }
}

class JdShop extends Shop {
    public double discount(double price, Customer customer) {
        if (customer.isVip()) { // invokeinterface
            return price * comupteDiscount(); // invokestatic
        } else {
            return super.discount(price, customer); // invokespecial
        }
    }

    private static double comupteDiscount() {
        return new Random() // invokespecial
            .nextDouble() // invokevirtual
            + 0.8;
    }
}
```

对于 invokestatic 以及 invokespecial 而言，Java 虚拟机能够直接识别具体的目标方法。而对于 invokevirtual 以及 invokeinterface 而言，在绝大部分情况下，虚拟机需要在执行过程中，根据调用者的动态类型，来确定具体的目标方法

唯一的例外在于，如果虚拟机能够确定目标方法有且仅有一个，那么它可以不通过动态类型，直接确定目标方法


## 调用指令的符号引用
在编译过程中，我们并不知道目标方法的具体内存地址。因此，Java 编译器会暂时用符号引用来表示该目标方法。这一符号引用包括目标方法所在的类或接口的名字，以及目标方法的方法名和方法描述符

符号引用存储在 class 文件的常量池之中。根据目标方法是否为接口方法，这些引用可分为接口符号引用和非接口符号引用。可以利用 `javap -v` 打印某个类的常量池

对于非接口符号引用，假定该符号引用所指向的类为 C，则 Java 虚拟机会按照如下步骤进行查找：
1. 在 C 中查找符合名字及描述符的方法
2. 如果没有找到，在 C 的父类中继续搜索，直至 Object 类
3. 如果没有找到，在 C 所直接实现或间接实现的接口中搜索，这一步搜索得到的目标方法必须是非私有、非静态的。并且，如果目标方法在间接实现的接口中，则需满足 C 与该接口之间没有其他符合条件的目标方法。如果有多个符合条件的目标方法，则任意返回其中一个

从这个解析算法可以看出，静态方法也可以通过子类来调用。此外，子类的静态方法会隐藏父类中的同名、同描述符的静态方法

对于接口符号引用，假定该符号引用所指向的接口为 I，则 Java 虚拟机会按照如下步骤进行查找：
1. 在 I 中查找符合名字及描述符的方法
2. 如果没有找到，在 Object 类中的公有实例方法中搜索
3. 如果没有找到，则在 I 的超接口中搜索。这一步的搜索结果的要求与非接口符号引用步骤 3 的要求一致

经过上述的解析步骤之后，符号引用会被解析成实际引用。对于可以静态绑定的方法调用而言，实际引用是一个指向方法的指针。对于需要动态绑定的方法调用而言，实际引用则是一个方法表的索引


```java
interface Customer {
    boolean isVIP();
}

class Merchant {
    public Number actionPrice(double price, Customer customer) {
        return 0;
    }
}

class NaiveMerchant extends Merchant {
    @Override
    public Double actionPrice(double price, Customer customer) {
        return 0.0;
    }
}
```
父类 Merchant 的 actionPrice 的返回值是 Number 类型，子类 NaiveMerchant 重写 actionPrice 返回的值是 Double 类型，对于 Java 语言是重写的，但对于 Java 虚拟机来说，只有当两个方法的参数类型以及返回类型一致时，才会被判定为重写。为了保持重写的语义，Java 编译器会在 NaiveMerchant 的字节码文件中自动生成一个桥接方法来保证重写语义。类似这样：
```java
public Number actionPrice(double price, Customer customer) {
     return this.actionPrice(price, customer);
}
```

```java
interface Customer {
    boolean isVIP();
}

class VIP implements Customer {

    @Override
    public boolean isVIP() {
        return true;
    }
}

class Merchant<T extends Customer> {
    public double actionPrice(double price, T customer) {
        return 0.0;
    }
}

class VIPOnlyMerchant extends Merchant<VIP> {
    @Override
    public double actionPrice(double price, VIP customer) {
        return 0.0;
    }
}
```
父类 MerchantOther 的参数实际上是 Customer 类型，为了保证重写的语义，生成桥接方法。类似这样：
```java
public double actionPrice(double price, Customer customer) {
    return this.actionPrice(price, (VIP) customer);
}
```


## 虚方法调用
Java 里所有非私有实例方法调用都会被编译成 invokevirtual 指令，而接口方法调用都会被编译成 invokeinterface 指令。这两种指令，均属于 Java 虚拟机中的虚方法调用

在绝大多数情况下，Java 虚拟机需要根据调用者的动态类型，来确定虚方法调用的目标方法。这个过程就是动态绑定。相对于静态绑定的非虚方法调用来说，虚方法调用更加耗时

在 Java 虚拟机中，静态绑定包括用于调用静态方法的 invokestatic 指令，和用于调用构造器、私有实例方法以及超类非私有实例方法的 invokespecial 指令。如果虚方法调用指向一个标记为 final 的方法，那么 Java 虚拟机也可以静态绑定该虚方法调用的目标方法


## 方法表
Java 虚拟机中为每个类生成一张方法表，用以快速定位目标方法。类加载机制的链接部分中的准备阶段，除了为静态字段分配内存之外，还会构造与该类相关联的方法表

方法表本质上是一个数组，每个数组元素指向一个当前类及其祖先类中非私有的实例方法。这些方法可能是具体的、可执行的方法，也可能是没有相应字节码的抽象方法

方法表满足两个特质：
1. 子类方法表中包含父类方法表中的所有方法
2. 子类方法在方法表中的索引值，与它所重写的父类方法的索引值相同

方法调用指令中的符号引用会在执行之前解析成实际引用。对于静态绑定的方法调用而言，实际引用将指向具体的目标方法。对于动态绑定的方法调用而言，实际引用则是方法表的索引值（实际上并不仅是索引值）

在执行过程中，Java 虚拟机将获取调用者的实际类型，并在该实际类型的虚方法表中，根据索引值获得目标方法。这个过程便是动态绑定


## 内联缓存
内联缓存是一种加快动态绑定的优化技术。它能够缓存虚方法调用中调用者的动态类型，以及该类型所对应的目标方法。在之后的执行过程中，如果碰到已缓存的类型，内联缓存便会直接调用该类型所对应的目标方法。如果没有碰到已缓存的类型，内联缓存则会退化至使用基于方法表的动态绑定

单态（monomorphic）指的是仅有一种状态的情况
多态（polymorphic）指的是有限数量种状态的情况。二态（bimorphic）是多态的其中一种
超多态（megamorphic）指的是更多种状态的情况。通常我们用一个具体数值来区分多态和超多态。在这个数值之下，我们称之为多态。否则，我们称之为超多态

对于内联缓存来说，有对应的单态内联缓存、多态内联缓存和超多态内联缓存。单态内联缓存，便是只缓存了一种动态类型以及它所对应的目标方法。它的实现非常简单：比较所缓存的动态类型，如果命中，则直接调用对应的目标方法。多态内联缓存则缓存了多个动态类型及其目标方法。它需要逐个将所缓存的动态类型与当前动态类型进行比较，如果命中，则调用对应的目标方法。在实践中，大部分的虚方法调用均是单态的，也就是只有一种动态类型。为了节省内存空间，Java 虚拟机只采用单态内联缓存

当内联缓存没有命中的情况下，Java 虚拟机需要重新使用方法表进行动态绑定。对于内联缓存中的内容：
1. 替换单态内联缓存中的纪录。在最坏情况下，我们用两种不同类型的调用者，轮流执行该方法调用，那么每次进行方法调用都将替换内联缓存。也就是说，只有写缓存的额外开销，而没有用缓存的性能提升
2. 劣化为超多态状态。这也是 Java 虚拟机的具体实现方式。处于这种状态下的内联缓存，实际上放弃了优化的机会。它将直接访问方法表，来动态绑定目标方法。与替换内联缓存纪录的做法相比，它牺牲了优化的机会，但是节省了写缓存的额外开销


java -XX:CompileCommand='dontinline,*.passThroughImmigration' Passenger
```java
public class InlineTest {
    public static void main(String[] args) {
        Passenger a = new ChinesePassenger();
        Passenger b = new ForeignerPassenger();
        long current = System.currentTimeMillis();

        for (int i = 1; i <= 2000000000; i++) {
            if (i % 100000000 == 0) {
                long temp = System.currentTimeMillis();
                System.out.println(temp - current);
                current = temp;
            }
            Passenger c = (i < 1000000000) ? a : b;
            c.passThroughImmigration();
        }
    }
}

abstract class Passenger {
    abstract void passThroughImmigration();
}

class ChinesePassenger extends Passenger {
    @Override
    void passThroughImmigration() {}
}

class ForeignerPassenger extends Passenger {
    @Override
    void passThroughImmigration() {}
}
```
