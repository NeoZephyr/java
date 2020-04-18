## 工作原理

ThreadLocal 仅仅是一个代理工具类，内部并不持有任何与线程相关的数据，所有和线程相关的数据都存储在 Thread 里面
```java
public class Thread implements Runnable {
    ThreadLocal.ThreadLocalMap threadLocals = null;
}

public class ThreadLocal<T> {
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    public T get() {
        // 获取线程持有的 ThreadLocalMap
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            // 在 ThreadLocalMap 中查找变量
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    static class ThreadLocalMap {
        private Entry[] table;

        private Entry getEntry(ThreadLocal<?> key) {
            //...
        }

        static class Entry extends WeakReference<ThreadLocal<?>> {
            //...
        }
    }
}
```

Thread 持有 ThreadLocalMap，而且 ThreadLocalMap 里对 ThreadLocal 的引用是弱引用。只要 Thread 对象可以被回收，那么 ThreadLocalMap 就能被回收


## 内存泄露
线程池中线程的存活时间太长，往往都是和程序同生共死的，这就意味着 Thread 持有的 ThreadLocalMap 一直都不会被回收，再加上 ThreadLocalMap 中的 Entry 对 ThreadLocal 是弱引用，所以只要 ThreadLocal 结束了自己的生命周期是可以被回收掉的。但是 Entry 中的 Value 却是被 Entry 强引用的，所以即便 Value 的生命周期结束了，Value 也是无法被回收的，从而导致内存泄露

```java
ExecutorService es;
ThreadLocal threadLocal;

es.execute(() -> {
    threadLocal.set(obj);

    try {

    } finally {
        threadLocal.remove();
    }
})
```


## InheritableThreadLocal
支持继承