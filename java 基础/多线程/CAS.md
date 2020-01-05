## `AtomicInteger`
```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    
    static {
        try {
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }
    
    private volatile int value;
    
    public AtomicInteger(int initialValue) {
        value = initialValue;
    }
    
    public AtomicInteger() {
    }
    
    public final int get() {
        return value;
    }
    
    public final void set(int newValue) {
        value = newValue;
    }
}
```

### `getAndSet`
```java
public final int getAndSet(int newValue) {
    return unsafe.getAndSetInt(this, valueOffset, newValue);
}
```
```java
// Unsafe.getAndSetInt
public final int getAndSetInt(Object var1, long var2, int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1, var2);
        
    // object, offset, oldValue, newValue
    } while(!this.compareAndSwapInt(var1, var2, var5, var4));

    return var5;
}
```

### `compareAndSet`
```java
public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
}
```

### `getAndIncrement`
```java
public final int getAndIncrement() {
    return unsafe.getAndAddInt(this, valueOffset, 1);
}

// Unsafe.getAndAddInt
public final int getAndAddInt(Object var1, long var2, int var4) {
    int var5;
    do {
        var5 = this.getIntVolatile(var1, var2);
    } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

    return var5;
}
```

## `AtomicReference`
原子引用类型
```java
private static AtomicReference<Integer> count = new AtomicReference<>(0);
count.compareAndSet(0, 2);
System.out.println("count: " + count.get());
```

## `AtomicIntegerArray`
原子 `Integer` 数组类型，以原子的方式更新数组中的每个元素

## `AtomicIntegerFieldUpdater`
以原子方式更新对象中的 `Integer` 字段

## `AtomicLongFieldUpdater`
以原子方式更新对象中的 `Long` 字段

## `AtomicReferenceFieldUpdater`
以原子方式更新对象中的 `Reference` 字段

```java
// 更新 User 类的实例其中的一个 total 字段
// 更新字段必须要用 volatile 修饰，且为非 static
private volatile String total;

private static AtomicIntegerFieldUpdater<User> updater = new AtomicIntegerFieldUpdater.newUpdater(User.class, "total");

User user = new User();
if (updater.compare(user, 100, 10)) {
    System.out.println("update success, count: " + user.getCount());
}
```

## `LongAdder`
统计时有并发更新会导致统计不准确
当多线程更新用于收集统计数据等常用总和时，此类通常比 AtomicLong 更受欢迎，而不是用于细粒度同步控制。在低更新争用下，这两个类别具有相似的特征。但是，在高度争论中，该类别的预期吞吐量显着较高，但以较高的空间消耗为代价

## `AtomicStampedReference`
在修改值的同时附加一个时间戳，只有值和时间戳都相同才进行修改，用于解决 ABA 问题

内部 `AtomicStampedReference` 将引用与时间戳组合为一个 `Pair` 对象
```java
private static class Pair<T> {
  final T reference;
  final int stamp;

  private Pair(T reference, int stamp) {
    this.reference = reference;
    this.stamp = stamp;
  }

  static <T> Pair<T> of(T reference, int stamp) {
    return new Pair<T>(reference, stamp);
  }
}

private volatile Pair<V> pair;
```
```java
public boolean compareAndSet(V   expectedReference,
                             V   newReference,
                             int expectedStamp,
                             int newStamp) {
    Pair<V> current = pair;
    return
        expectedReference == current.reference &&
        expectedStamp == current.stamp &&
        ((newReference == current.reference &&
          newStamp == current.stamp) ||
         casPair(current, Pair.of(newReference, newStamp)));
}
```

## CAS 缺点
1. 在并发量比较高的情况下，如果许多线程反复尝试更新某一个变量，却又一直更新不成功，循环往复，CPU 开销较大
2. CAS 机制所保证的只是一个变量的原子性操作，而不能保证整个代码块的原子性