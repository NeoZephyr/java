## cas 指令
CAS 指令包含 3 个参数：共享变量的内存地址 A、用于比较的值 B 和共享变量的新值 C；并且只有当内存中地址 A 处的值等于 B 时，才能将内存中地址 A 处的值更新为新值 C。作为一条 CPU 指令，CAS 指令本身是能够保证原子性的

## 原子化的基本数据类型
```
getAndIncrement
getAndDecrement
incrementAndGet
decrementAndGet

getAndAdd
addAndGet
compareAndSet
```

新值可以通过传入 func 函数来计算
```
getAndUpdate
updateAndGet
getAndAccumulate
accumulateAndGet
```


## 原子化的对象引用类型
AtomicReference

AtomicStampedReference：在修改值的同时附加一个时间戳，只有值和时间戳都相同才进行修改，用于解决 ABA 问题
```java
public boolean compareAndSet(V   expectedReference,
                             V   newReference,
                             int expectedStamp,
                             int newStamp);
```

AtomicMarkableReference：将版本号简化成了一个 Boolean 值
```java
public boolean compareAndSet(V   expectedReference,
                             V   newReference,
                             boolean expectedMark,
                             boolean newMark);
```


## 原子化数组
原子化地更新数组里面的每一个元素

AtomicIntegerArray
AtomicLongArray
AtomicReferenceArray


## 原子化对象属性更新器
原子化地更新对象的属性。需要注意的是，对象属性必须是 volatile 类型的，只有这样才能保证可见性；如果对象属性不是 volatile 类型的，newUpdater() 方法会抛出 IllegalArgumentException 这个运行时异常

AtomicIntegerFieldUpdater
AtomicLongFieldUpdater
AtomicReferenceFieldUpdater


## 原子化的累加器
仅用来执行累加操作，相比原子化的基本数据类型，速度更快，但是不支持 compareAndSet() 方法

DoubleAccumulator
DoubleAdder
LongAccumulator
LongAdder


## CAS 缺点
1. 在并发量比较高的情况下，如果许多线程反复尝试更新某一个变量，却又一直更新不成功，循环往复，CPU 开销较大
2. CAS 机制所保证的只是一个变量的原子性操作，而不能保证整个代码块的原子性

