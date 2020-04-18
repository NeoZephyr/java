## 令牌桶算法
如果线程请求令牌的时间在下一令牌产生时间之后，那么该线程立刻就能够获取令牌；如果请求时间在下一令牌产生时间之前，那么该线程是在下一令牌产生的时间获取令牌。由于此时下一令牌已经被该线程预占，所以下一令牌产生的时间需要加上 1 秒
```java
class SimpleLimiter {
    // 下一令牌产生时间
    long next = System.nanoTime();
    long interval = 1000000000;

    // 预占令牌，返回能够获取令牌的时间
    synchronized long reserve(long now) {

        // 请求时间在下一令牌产生时间之后
        if (now > next) {
            next = now;
        }

        // 能够获取令牌的时间
        long at = next;

        // 设置下一令牌产生时间
        next += interval;

        return Math.max(at, 0L);
    }

    // 申请令牌
    void acquire() {
        long now = System.nanoTime();
        long at = reserve(now);
        long waitTime = max(at - now, 0);

        if (waitTime > 0) {
            try {
                TimeUnit.NANOSECONDS.sleep(waitTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
```

令牌桶的容量大于 1
```java
class SimpleLimiter {
    // 当前令牌桶中的令牌数量
    long storedPermits = 0;

    // 令牌桶的容量
    long maxPermits = 3;
    long next = System.nanoTime();
    long interval = 1000000000;

    void acquire() {
        long now = System.nanoTime();
        long at = reserve(now);
        long waitTime = max(at - now, 0);

        if (waitTime > 0) {
            try {
                TimeUnit.NANOSECONDS.sleep(waitTime);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    synchronized long reserve(long now) {
        resync(now);

        // 能够获取令牌的时间
        long at = next;

        long fb = min(1, storedPermits);
        long nr = 1 - fb;
        next = next + nr * interval;
        storedPermits -= fb;

        return at;
    }

    void resync(long now) {
        if (now > next) {
            long newPermits = (now - next) / interval;
            storedPermits = min(maxPermits, storedPermits + newPermits);
            next = now;
        }
    }
}
```