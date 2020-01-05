## Class
### Class 信息
.class 不会引发初始化，Class.forName() 会立即引发初始化
#### 使用类前的准备
1. 类加载器执行类加载，查找字节码文件并创建一个 `Class` 对象
2. 链接阶段验证字节码并为静态域分配空间
3. 初始化包括静态初始化器和静态初始化块

##### 类初始化时机
1. 创建类的实例
2. 访问类的静态变量
3. 调用类的静态方法
4. 初始化某个类的子类
5. 使用反射强制创建对象
```java
class Phone {
  Phone() {}
  Phone(double price) {}
}

interface Call {}
interface Message {}

class IPhone extends Phone implements Call, Message {
  IPhone(double price) {
    super(price);
  }

  public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    // Class clazz = Class.forName("com.pain.type.IPhone");
    // 相比 forName 获取 Class 对象引用更安全
    Class clazz = IPhone.class;
    System.out.println("class name: " + clazz.getName());
    System.out.println("simple class name: " + clazz.getSimpleName());
    System.out.println("canonical name: " + clazz.getCanonicalName());
    System.out.println("is interface: " + clazz.isInterface());

    for (Class face : clazz.getInterfaces()) {
      System.out.println(face.getSimpleName() + " is interface: " + face.isInterface());
    }

    Class base = clazz.getSuperclass();
    Object obj = base.newInstance();
    System.out.println(obj.getClass().getSimpleName() + " is interface: " + obj.getClass().isInterface());
  }
}
```

### Class 泛型
```java
class ClazzTest {
  public static void main(String[] args) {
    Class clazz1 = int.class;
    Class clazz2 = Integer.class;
    Class<Integer> clazz = int.class;
    clazz = clazz2;

    clazz1 = double.class;
    // error
    //clazz = double.class;

    Class<?> clazz3 = int.class;
    clazz3 = double.class;

    Class<? extends Number> clazz4 = int.class;
    clazz4 = double.class;
    // error
    //clazz4 = Exception.class;
  }
}
```

### 反射
#### 构造方法
```java
Class class = Class.forName("");
Constructor[] cons = class.getConstructors();
for (Constructor con : cons) {}
cons = class.getDeclaredConstructors();
```
```java
Class class = Class.forName("");
Constructor con = class.getConstructor();
Object obj = con.newInstance();
con = class.getConstructor(String.class, int.class);
obj = con.newInstance();
```