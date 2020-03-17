## File
`File` 既可以表示文件，也可以表示目录

### 构造器
`File` 中的路径可以是已经存在的，也可以是不存在的；通过 `new` 创建一个 `File` 对象不会实际创建一个文件，只是创建一个表示文件或目录的对象，`File` 对象创建之后路径不可变
```java
public class File implements Serializable, Comparable<File> {

  // 标准化路径名字符串
  private final String path;
  private final transient int prefixLength;

  private File(String pathname, int prefixLength) {
    this.path = pathname;
    this.prefixLength = prefixLength;
  }

  private File(String child, File parent) {
    assert parent.path != null;
    assert (!parent.path.equals(""));
    this.path = fs.resolve(parent.path, child);
    this.prefixLength = parent.prefixLength;
  }

  public File(String pathname) {
    if (pathname == null) {
      throw new NullPointerException();
    }
    this.path = fs.normalize(pathname);
    this.prefixLength = fs.prefixLength(this.path);
  }

  public File(String parent, String child) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent != null) {
      if (parent.equals("")) {
        this.path = fs.resolve(fs.getDefaultParent(),
                               fs.normalize(child));
      } else {
        this.path = fs.resolve(fs.normalize(parent),
                               fs.normalize(child));
      }
    } else {
      this.path = fs.normalize(child);
    }
    this.prefixLength = fs.prefixLength(this.path);
  }

  public File(File parent, String child) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent != null) {
      if (parent.path.equals("")) {
        this.path = fs.resolve(fs.getDefaultParent(),
                               fs.normalize(child));
      } else {
        this.path = fs.resolve(parent.path,
                               fs.normalize(child));
      }
    } else {
      this.path = fs.normalize(child);
    }
    this.prefixLength = fs.prefixLength(this.path);
  }

  public File(URI uri) {
    // Check our many preconditions
    if (!uri.isAbsolute())
      throw new IllegalArgumentException("URI is not absolute");
    if (uri.isOpaque())
      throw new IllegalArgumentException("URI is not hierarchical");

    String scheme = uri.getScheme();
    if ((scheme == null) || !scheme.equalsIgnoreCase("file"))
      throw new IllegalArgumentException("URI scheme is not \"file\"");
    if (uri.getAuthority() != null)
      throw new IllegalArgumentException("URI has an authority component");
    if (uri.getFragment() != null)
      throw new IllegalArgumentException("URI has a fragment component");
    if (uri.getQuery() != null)
      throw new IllegalArgumentException("URI has a query component");
    String p = uri.getPath();
    if (p.equals(""))
      throw new IllegalArgumentException("URI path component is empty");

    // Okay, now initialize
    p = fs.fromURIPath(p);
    if (File.separatorChar != '/')
      p = p.replace('/', File.separatorChar);
    this.path = fs.normalize(p);
    this.prefixLength = fs.prefixLength(this.path);
  }
}
```

### 其他方法
返回由此文件或目录的名称，只是路径名称序列中的最后一个名称
```java
public String getName() {
  int index = path.lastIndexOf(separatorChar);
  if (index < prefixLength) return path.substring(prefixLength);
  return path.substring(index + 1);
}
```
判断路径是否为绝对路径
```java
public boolean isAbsolute() {
  return fs.isAbsolute(this);
}
```
返回构造 `File` 对象时的完整路径名，包括路径和文件名称
```java
public String getPath() {
  return path;
}
```
返回完整的绝对路径名
```java
public String getAbsolutePath() {
  return fs.resolve(this);
}
```
返回标准的完整路径名，去除路径中的冗余名称（"."，".."），跟踪软连接等
```java
public String getCanonicalPath() throws IOException {
  if (isInvalid()) {
    throw new IOException("Invalid file path");
  }
  return fs.canonicalize(fs.resolve(this));
}
```
返回上一级的路径名
```java
public String getParent() {
  int index = path.lastIndexOf(separatorChar);
  if (index < prefixLength) {
    if ((prefixLength > 0) && (path.length() > prefixLength))
      return path.substring(0, prefixLength);
    return null;
  }
  return path.substring(0, index);
}
```
返回上一级
```java
public File getParentFile() {
  String p = this.getParent();
  if (p == null) return null;
  return new File(p, this.prefixLength);
}
```
返回此抽象路径名的绝对形式
```java
public File getAbsoluteFile() {
  String absPath = getAbsolutePath();
  return new File(absPath, fs.prefixLength(absPath));
}
```
返回此抽象路径名的规范形式
```java
public File getCanonicalFile() throws IOException {
  String canonPath = getCanonicalPath();
  return new File(canonPath, fs.prefixLength(canonPath));
}
```

```java
// 最后修改时间的毫秒数
public long lastModified() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkRead(path);
  }
  if (isInvalid()) {
    return 0L;
  }
  return fs.getLastModifiedTime(this);
}
```
```java
public boolean setLastModified(long time) {
  if (time < 0) throw new IllegalArgumentException("Negative time");
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkWrite(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.setLastModifiedTime(this, time);
}
```

### 权限
是否为隐藏文件
```java
public boolean isHidden() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkRead(path);
  }
  if (isInvalid()) {
    return false;
  }
  return ((fs.getBooleanAttributes(this) & FileSystem.BA_HIDDEN) != 0);
}
```
```java
public boolean canExecute() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkExec(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.checkAccess(this, FileSystem.ACCESS_EXECUTE);
}
```

```java
public boolean canRead() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkRead(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.checkAccess(this, FileSystem.ACCESS_READ);
}
```
```java
public boolean canWrite() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkWrite(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.checkAccess(this, FileSystem.ACCESS_WRITE);
}
```

```java
public boolean setReadOnly() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkWrite(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.setReadOnly(this);
}

public boolean setReadable(boolean readable);
public boolean setReadable(boolean readable, boolean ownerOnly);
```

```java
public boolean setWritable(boolean writable) {
  return setWritable(writable, true);
}

public boolean setWritable(boolean writable, boolean ownerOnly);
```

```java
public boolean setExecutable(boolean executable) {
  return setExecutable(executable, true);
}

public boolean setExecutable(boolean executable, boolean ownerOnly);
```

### 创建文件
当且仅当具有该名称的文件尚不存在时，以原子方式创建
```java
public boolean createNewFile() throws IOException {
  SecurityManager security = System.getSecurityManager();
  if (security != null) security.checkWrite(path);
  if (isInvalid()) {
    throw new IOException("Invalid file path");
  }
  return fs.createFileExclusively(path);
}
```
临时文件的完整路径名是系统指定的、唯一的，但可以通过参数指定前缀、后缀和目录
```java
public static File createTempFile(String prefix, String suffix)
  throws IOException
{
  return createTempFile(prefix, suffix, null);
}

public static File createTempFile(String prefix, String suffix,
                                  File directory);
```

### 删除文件
删除文件或目录，如果此文件为目录，则目录必须是空才能被删除
```java
public boolean delete() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkDelete(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.delete(this);
}
```
将 `File` 对象加入到待删列表，在 Java 虚拟机正常退出的时候进行实际删除
```java
public void deleteOnExit() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkDelete(path);
  }
  if (isInvalid()) {
    return;
  }
  DeleteOnExitHook.add(path);
}
```

```java
public boolean renameTo(File dest) {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkWrite(path);
    security.checkWrite(dest.path);
  }
  if (dest == null) {
    throw new NullPointerException();
  }
  if (this.isInvalid() || dest.isInvalid()) {
    return false;
  }
  return fs.rename(this, dest);
}
```

### 创建目录
如果某一个中间父目录不存在，则创建目录失败
```java
public boolean mkdir() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkWrite(path);
  }
  if (isInvalid()) {
    return false;
  }
  return fs.createDirectory(this);
}
```
如果某一个中间父目录不存在则创建对应中间父目录
```java
public boolean mkdirs() {
  if (exists()) {
    return false;
  }
  if (mkdir()) {
    return true;
  }
  File canonFile = null;
  try {
    canonFile = getCanonicalFile();
  } catch (IOException e) {
    return false;
  }

  File parent = canonFile.getParentFile();
  return (parent != null && (parent.mkdirs() || parent.exists()) &&
          canonFile.mkdir());
}
```

### 文件列表
```java
public String[] list() {
  SecurityManager security = System.getSecurityManager();
  if (security != null) {
    security.checkRead(path);
  }
  if (isInvalid()) {
    return null;
  }
  return fs.list(this);
}
```
```java
public File[] listFiles() {
  String[] ss = list();
  if (ss == null) return null;
  int n = ss.length;
  File[] fs = new File[n];
  for (int i = 0; i < n; i++) {
    fs[i] = new File(ss[i], this);
  }
  return fs;
}
```

```java
public String[] list(FilenameFilter filter) {
  String names[] = list();
  if ((names == null) || (filter == null)) {
    return names;
  }
  List<String> v = new ArrayList<>();
  for (int i = 0 ; i < names.length ; i++) {
    if (filter.accept(this, names[i])) {
      v.add(names[i]);
    }
  }
  return v.toArray(new String[v.size()]);
}
```
```java
public File[] listFiles(FileFilter filter) {
  String ss[] = list();
  if (ss == null) return null;
  ArrayList<File> files = new ArrayList<>();
  for (String s : ss) {
    File f = new File(s, this);
    if ((filter == null) || filter.accept(f))
      files.add(f);
  }
  return files.toArray(new File[files.size()]);
}
```
```java
public File[] listFiles(FilenameFilter filter) {
  String ss[] = list();
  if (ss == null) return null;
  ArrayList<File> files = new ArrayList<>();
  for (String s : ss)
    if ((filter == null) || filter.accept(this, s))
      files.add(new File(s, this));
  return files.toArray(new File[files.size()]);
}
```