## 不可变性的类

### 不可变性
对象的所有属性都是 final 的，并不能保证不可变性。需要清楚是否要求属性对象也具备不可变性

```java
public class SafeRange {
    class Range {
        final int upper;
        final int lower;

        Range(int upper, int lower) {
            this.upper = upper;
            this.lower = lower;
        }
    }

    final AtomicReference<Range> rangeRefer = new AtomicReference<>(
        new Range(0, 0));

    void setUpper(int upper) {
        while (true) {
            Range range = rangeRefer.get();

            if (upper < range.lower) {
                throw new IllegalArgumentException();
            }

            Range newRange = new Range(upper, range.lower);

            if (rangeRefer.compareAndSet(range, newRange)) {
                return;
            }
        }
    }
}
```

如果具备不可变性的类，需要提供类似修改的功能，比较简单的做法是创建一个新的不可变对象。但是，所有的修改操作都创建一个新的不可变对象，可能导致创建的对象太多。对此，可以利用享元模式减少创建对象的数量，从而减少内存占用。Java 语言里面 Long、Integer 等包装类都用到了享元模式

### 享元模式
利用享元模式创建对象的逻辑也很简单：创建之前，首先去对象池里看看是不是存在；如果已经存在，就利用对象池里的对象；如果不存在，就会新创建一个对象，并且把这个新创建出来的对象放进对象池里。

Long 内部维护了一个静态的对象池，仅缓存了 [-128,127] 之间的数字，这个对象池在 JVM 启动的时候就创建好了，而且这个对象池一直都不会变化

```java
public static Long valueOf(long l) {
    final int offset = 128;
    if (l >= -128 && l <= 127) { // will cache
        return LongCache.cache[(int)l + offset];
    }
    return new Long(l);
}

private static class LongCache {
    private LongCache(){}

    static final Long cache[] = new Long[-(-128) + 127 + 1];

    static {
        for(int i = 0; i < cache.length; i++)
            cache[i] = new Long(i - 128);
    }
}
```
