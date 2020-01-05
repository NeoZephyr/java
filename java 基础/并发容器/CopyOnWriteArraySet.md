## `CopyOnWriteArraySet`
```java
public class CopyOnWriteArraySet<E> extends AbstractSet<E>
        implements java.io.Serializable {}
```

内部通过 `CopyOnWriteArrayList` 实现，与 `HashSet/TreeSet` 相比，性能比较低，不适用于元素个数特别多的集合，若元素个数比较多，可以考虑 `ConcurrentHashMap` 或 `ConcurrentSkipListSet`。Java 并发包中没有与 `HashSet` 对应的并发容器，但可以基于 `ConcurrentHashMap` 构建：利用 `Collections.newSetFromMap` 方法即可

