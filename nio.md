Buffer
mark：初始值为-1，用于备份当前的position
position：初始值为0，position表示当前可以写入或读取数据的位置，当写入或读取一个数据后，position向前移动到下一个位置
limit：写模式下，limit表示最多能往Buffer里写多少数据，等于capacity值；读模式下，limit表示最多可以读取多少数据
capacity：缓存数组大小

```
public final Buffer mark() {
    mark = position;
    return this;
}

public final Buffer reset() {
    int m = mark;
    if (m < 0) {
        throw new InvalidMarkException();
    }

    position = m;
    return this;
}
```
clear()：一旦读完Buffer中的数据，需要让Buffer准备好再次被写入，clear会恢复状态值，但不会擦除数据
```
public final Buffer clear() {
    position = 0;
    limit = capacity;
    mark = -1;
    return this;
}
```
flip()：Buffer有两种模式，写模式和读模式，flip后Buffer从写模式变成读模式
```
public final Buffer flip() {
    limit = position;
    position = 0;
    mark = -1;
    return this;
}
```
rewind()：重置position为0，从头读写数据
```
public final Buffer rewind() {
    position = 0;
    mark = -1;
    return this;
}
```

Buffer的实现类
ByteBuffer的实现类包括"HeapByteBuffer"和"DirectByteBuffer"两种
HeapByteBuffer通过初始化字节数组hd，在虚拟机堆上申请内存空间
DirectByteBuffer通过unsafe.allocateMemory在物理内存中申请地址空间（非jvm堆内存），并在ByteBuffer的address变量中维护指向该内存的地址。 unsafe.setMemory(base, size, (byte) 0)方法把新申请的内存数据清零

Channel
NIO把它支持的I/O对象抽象为Channel，Channel又称“通道”，类似于原I/O中的流（Stream）
1、流是单向的，通道是双向的，可读可写
2、流读写是阻塞的，通道可以异步读写
3、流中的数据可以选择性的先读到缓存中，通道的数据总是要先读到一个缓存中，或从缓存中写入

Channel 的实现类
FileChannel
读步骤
1. 申请一块和缓存同大小的DirectByteBuffer bb
2. 读取数据到缓存bb，底层由NativeDispatcher的read实现
3. 把bb的数据读取到dst（用户定义的缓存，在jvm中分配内存）

写步骤
1. 申请一块DirectByteBuffer，bb大小为byteBuffer中的limit - position
2. 复制byteBuffer中的数据到bb中
3. 把数据从bb中写入到文件，底层由NativeDispatcher的write实现


```
File file = new RandomAccessFile("data.txt", "rw");
FileChannel channel = file.getChannel();
ByteBuffer buffer = ByteBuffer.allocate(48);

int bytesRead = channel.read(buffer);
while (bytesRead != -1) {
    buffer.flip();
    while (buffer.hasRemaining()) {
        System.out.print((char)buffer.get());
    }

    buffer.clear();
    bytesRead = channel.read(buffer);
}

file.close();
```

