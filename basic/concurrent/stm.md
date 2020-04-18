## 软件事务内存
```java
// 带版本号的对象引用
public final class VersionedRef<T> {
    final T value;
    final long version;

    public VersionedRef(T value, long version) {
        this.value = value;
        this.version = version;
    }
}

public class TxnRef<T> {
    volatile VersionedRef curRef;

    public TxnRef(T value) {
        this.curRef = new VersionedRef(value, 0L);
    }

    // 获取当前事务中的数据
    public T getValue(Txn txn) {
        return txn.get(this);
    }

    // 在当前事务中设置数据
    public void setValue(T value, Txn txn) {
        txn.set(this, value);
    }
}
```
```java
public interface Txn {
    <T> T get(TxnRef<T> ref);
    <T> void set(TxnRef<T> ref, T value);
}

public final class STMTxn implements Txn {
    private static AtomicLong txnSeq = new AtomicLong(0);

    // 用于保存当前事务中所有读写的数据的快照
    private Map<TxnRef, VersionedRef> inTxnMap = new HashMap<>();

    // 用于保存当前事务需要写入的数据
    private Map<TxnRef, Object> writeMap = new HashMap<>();

    // 当前事务 ID
    private long txnId;

    STMTxn() {
        txnId = txnSeq.incrementAndGet();
    }

    // 获取当前事务中的数据
    public <T> T get(TxnRef<T> ref) {
        if (!inTxnMap.containsKey(ref)) {
            inTxnMap.put(ref, ref.curRef);
        }

        return (T) inTxnMap.get(ref).value;
    }

    // 在当前事务中修改数据
    public <T> void set(TxnRef<T> ref, T value) {
        // 将需要修改的数据，加入 inTxnMap
        if (!inTxnMap.containsKey(ref)) {
            inTxnMap.put(ref, ref.curRef);
        }

        writeMap.put(ref, value);
    }

    boolean commit() {
        synchronized (STM.commitLock) {
            boolean isValid = true;

            // 校验所有读过的数据是否发生过变化
            for(Map.Entry<TxnRef, VersionedRef> entry : inTxnMap.entrySet()) {
                VersionedRef curRef = entry.getKey().curRef;
                VersionedRef readRef = entry.getValue();

                if (curRef.version != readRef.version) {
                    isValid = false;
                    break;
                }
            }

            if (isValid) {
                writeMap.forEach((k, v) -> {
                    k.curRef = new VersionedRef(v, txnId);
                });
            }

            return isValid;
        }
    }
}
```

```java
@FunctionalInterface
public interface TxnRunnable {
    void run(Txn txn);
}

public final class STM {
    private STM() {
    }

    static final Object commitLock = new Object();

    public static void atomic(TxnRunnable action) {
        boolean committed = false;

        while (!committed) {
            STMTxn txn = new STMTxn();
            action.run(txn);
            committed = txn.commit();
        }
    }
}
```

```java
class Account {
    private TxnRef<Integer> balance;

    public Account(int balance) {
        this.balance = new TxnRef<Integer>(balance);
    }

    public void transfer(Account target, int amt) {
        STM.atomic((txn) -> {
            Integer from = balance.getValue(txn);
            balance.setValue(from - amt, txn);
            Integer to = target.balance.getValue(txn);
            target.balance.setValue(to + amt, txn);
        });
    }
}
```