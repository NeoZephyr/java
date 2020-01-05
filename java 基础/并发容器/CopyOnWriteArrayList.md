## `CopyOnWriteArrayList`
```java
public class CopyOnWriteArrayList<E>
    implements List<E>, RandomAccess, Cloneable, java.io.Serializable {}
```

### 写时复制
`CopyOnWriteArrayList` 内部是一个数组，对这个数组的更新是以原子方式进行的。每次修改操作，都会新建一个数组，复制原数组的内容到新数组，在新数组上进行需要的修改，然后以原子方式设置内部的数组引用。由于每次修改 `CopyOnWriteArrayList` 都会有容器元素复制的开销，所以更适合迭代操作远多于修改操作的使用场景，不适用于大数组且修改频繁的场景

### 迭代
基于 `synchronized` 的同步容器在迭代时需要对整个列表对象加锁，否则会抛出 `ConcurrentModificationException` 异常，而 `CopyOnWriteArrayList` 迭代时不需要加锁。`CopyOnWriteArrayList` 的迭代器不支持修改操作以及一些依赖迭代器修改方法的操作，例如 `Collections` 的 `sort` 方法，但不会抛出 `ConcurrentModificationException` 异常

#### 并发访问
所有读操作，都是先拿到当前引用的数组，然后直接访问该数组，在读取过程中，若内部数组引用已经修改也不会影响读操作，依旧访问原数组内容。读操作不需要锁同步，可以并行，且读和写也可以并行；但多个线程不能同时写，每个写操作都需要先获取锁

#### 对比同步容器
并发容器以原子方式支持一些复合操作，基于 `synchronized` 的同步容器在进行这些复合操作时需要调用方加锁