```java
class GuardedObject<T> {
    volatile T obj;
    final Lock lock = new ReentrantLock();
    final Condition done = lock.newCondition();
    final int timeout = 2;

    final static Map<Object, GuardedObject> goMap = new ConcurrentHashMap<>();

    static <K> GuardedObject create(K key) {
        GuardedObject go = new GuardedObject();
        goMap.put(key, go);
        return go
    }

    static <K, T> void fireEvent(K key, T obj) {
        GuardedObject go = goMap.remove(key);

        if (go != null) {
            go.onChanged(obj);
        }
    }

    T get(Predicate<T> p) {
        lock.lock();

        try {
            while (!p.test(obj)) {
                done.await(timeout, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        return obj;
    }

    void onChanged(T obj) {
        lock.lock();

        try {
            this.obj = obj;
            done.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
```

```java
Response handleRequest() {
    int id = idGenerator.get();
    Message msg = new Message(id, "");
    GuardedObject<Message> go = GuardedObject.create(id);
    send(msg);

    Message result = go.get(t -> t != null);
}

void onMessage(Message message) {
    GuardedObject.fireEvent(message.id, message);
}
```