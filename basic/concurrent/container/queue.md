并发队列队列迭代都不会抛出 `ConcurrentModificationException` 异常，都是弱一致的

## 单端阻塞队列
### ArrayBlockingQueue
基于数组的普通阻塞队列，实现 Queue 接口，表示先进先出的队列。基于循环数组实现，有界，创建时需要指定大小，在运行过程中不会改变。ArrayDeque 也是基于循环数组实现的，但无界，会自动扩展

### LinkedBlockingQueue
基于链表的普通阻塞队列，实现 Queue 接口，表示先进先出的队列。基于单向链表实现的，在创建时可以指定最大长度，也可以不指定，默认是无限的，节点都是动态创建的

### SynchronousQueue
没有存储元素的空间，入队操作要等待另一个线程的出队操作，反之亦然。适用于两个线程之间直接传递信息、事件或任务

### LinkedTransferQueue
融合 LinkedBlockingQueue 和 SynchronousQueue 的功能，性能比 LinkedBlockingQueue 更好。实现 TransferQueue 接口，TransferQueue 是 BlockingQueue 的子接口

### PriorityBlockingQueue
优先级阻塞队列，按优先级出队的，实现 BlockingQueue 接口。基于堆实现，与 PriorityQueue 一样，没有大小限制，无界，内部的数组大小会动态扩展

### DelayQueue
延时阻塞队列，基于 PriorityQueue 实现，无界。DelayQueue 需要每个元素都实现 Delayed 接口，Delayed 扩展 Comparable 接口，DelayQueue 的每个元素都是可比较的，方法 getDelay 返回延迟时间，若小于等于 0 表示不再延迟

DelayQueue 按元素的延时时间出队，只有当元素的延时过期之后才能被从队列中拿走，take 方法总是返回第一个过期的元素，若没有则阻塞等待


## 双端阻塞队列
### LinkedBlockingDeque
基于链表的普通阻塞队列，实现 Deque 接口，是一个双端队列。基于双向链表实现，最大长度在创建时可选，默认无限


## 单端非阻塞队列
### ConcurrentLinkedQueue
无锁非阻塞并发队列，适用于多个线程并发使用一个队列的场合，基于链表实现，没有限制大小，是无界的。实现 Queue 接口，表示一个先进先出的队列，内部是一个单向链表


## 双端非阻塞队列
### ConcurrentLinkedDeque
无锁非阻塞并发队列，适用于多个线程并发使用一个队列的场合，基于链表实现，没有限制大小，是无界的。实现 Deque 接口，表示一个双端队列，在两端都可以入队和出队，内部是一个双向链表
