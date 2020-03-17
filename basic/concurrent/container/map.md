## ConcurrentHashMap
1. ConcurrentHashMap 的 key 是无序的
2. key 和 value 都不能为空，否则会抛出空指针异常

### 迭代器
ConcurrentHashMap 所提供的迭代器不会抛出 `ConcurrentModificationException` 异常，不需要为其加锁。并发容器的迭代器具有弱一致性，容忍并发的修改，可以（但是不保证）将迭代器上的修改操作反映给容器

### 对比同步容器
1. 对于一些复合操作，使用同步容器时，调用方必须加锁，而 ConcurrentMap 能保证原子操作
2. ConcurrentHashMap 在迭代时不需要加锁，若另一个线程对容器进行了修改，迭代会继续，不会抛出 `ConcurrentModificationException` 异常


## ConcurrentSkipListMap
1. ConcurrentSkipListMap 基于 SkipList 实现，SkipList 称为跳跃表或跳表，采用跳表而不是树是因为跳表更易于实现高效并发算法
2. 实现 ConcurrentMap 接口，直接支持一些原子复合操作
3. 实现 SortedMap 和 NavigableMap 接口，可排序。因此，key 是有序的
4. key 和 value 都不能为空，否则会抛出空指针异常

### 并发访问
ConcurrentSkipListMap 没有使用锁，所有操作都是无阻塞的，所有操作都可以并行，包括写，多个线程可以同时写

### 弱一致性
迭代可能反映最新修改也可能不反映，一些方法如 `putAll`、`clear` 不是原子操作


## SkipList
跳表是基于链表的，在链表的基础上加了多层索引结构。ConcurrentSkipListMap 会构造跳表结构，最下面一层是最基本的单向链表，这个链表是有序的；为了快速查找，跳表有多层索引结构，高层的索引节点一定同时是低层的索引节点

### 查找
从最高层开始，将待查值与下一个索引节点的值进行比较，若大于索引节点向右移动，继续比较；若小于则向下移动到下一层进行比较，复杂度是 O(log(N))

### 索引更新
随机计算一个数代表该元素最高索引层，一层的概率为 1/2，二层为 1/4，三层为 1/8，依次类推。然后从高到低，在每一层为该元素建立索引节点

### 删除
为避免并发冲突，先进行标记，然后在内部遍历元素的过程中真正删除