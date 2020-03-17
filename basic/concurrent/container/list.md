## CopyOnWriteArrayList
CopyOnWriteArrayList 内部维护了一个数组，成员变量 array 就指向这个内部数组，所有的读操作都是基于 array 进行的

### 迭代
基于 synchronized 的同步容器在迭代时需要对整个列表对象加锁，否则会抛出 `ConcurrentModificationException` 异常，而 CopyOnWriteArrayList 迭代时不需要加锁

CopyOnWriteArrayList 迭代器是只读的，不支持增删改。因为迭代器遍历的仅仅是一个快 照，而对快照进行增删改是没有意义的。例如 Collections 的 sort 方法，但不会抛出 `ConcurrentModificationException` 异常

### 写时复制
如果在遍历 array 的同时，还有一个写操作，CopyOnWriteArrayList 会将 array 复制一份，然后在新复制处理的数组上进行操作，操作完之后再将 array 指向这个新的数组。这样，遍历操作一直都是基于原 array 执行，而写操作则是基于新 array 进行，然后以原子方式设置内部的数组引用

由于每次修改 CopyOnWriteArrayList 都会有容器元素复制的开销，所以更适合迭代操作远多于修改操作的使用场景，不适用于大数组且修改频繁的场景，而且能够容忍读写的短暂不一致

### 对比同步容器
以原子方式支持一些复合操作，基于 synchronized 的同步容器在进行这些复合操作时需要调用方加锁