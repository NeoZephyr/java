Collections 这个类中还提供了一套完备的包装类，分别把 ArrayList、HashSet 和 HashMap 包装成了线程安全的 List、Set 和 Map

## 遍历
存在并发问题
```java
List list = Collections.synchronizedList(new ArrayList());
Iterator iter = list.iterator();

while (iter.hasNext()) {
    foo(iter.next());
}
```

锁住 list 之后再执行遍历操作
```java
List list = Collections.synchronizedList(new ArrayList());

synchronized (list) {
    Iterator iter = list.iterator();

    while (iter.hasNext()) {
        foo(iter.next());
    }
}
```
