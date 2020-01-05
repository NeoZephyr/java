## `Error`
`Error` 一般不大可能出现，绝大部分的 `Error` 都会导致程序（比如 JVM 自身）处于非正常的、不可恢复状态。既然是非正常情况，所以不便于也不需要捕获。常见的比如 `OutOfMemoryError`、`StackOverflowError`、`NoClassDefFoundError` 之类，都是 `Error` 的子类

## 异常
### 异常种类
编译时异常：在编译前必须处理，或称为可检查异常，必须显式地进行捕获处理。常见的如 `IOException` 等
运行时异常：可以在运行时处理，或称为不可检查异常。常见的如 `NullPointerException`, `ArrayIndexOutOfBoundsException` 等

### 异常转换
```java
try {

} catch (SomeException e) {
  throw new RuntimeException(e);
  // throw new RuntimeException("some message", e);
}
```

### 异常声明
- 编译时异常抛出时必须要声明
- 编译时异常可以声明但不抛出
- 子类不能抛出父类方法中没有声明的编译时异常
```java
public void test() throws MyException {}
```

### 异常处理
1. 尽量不要捕获类似 `Exception` 这样的通用异常，而是应该捕获特定异常
2. 不要生吞异常，这样很可能会导致出现的问题难以诊断
3. 应该将异常输出到日志系统中，而不是输出到标准出错
4. 遵循 'Throw early, catch late' 原则，对于不清楚如何处理的异常，应该保留原有异常的 cause 信息，直接抛出或者构建新的异常。在更高层面有了清晰的业务逻辑，再做处理
5. `try/catch` 会产生额外的性能开销，应该只捕获有必要的代码段
6. 每实例化一个 `Exception`，都会对当时的栈进行快照，这是一个相对比较重的操作。如果发生的非常频繁，这个开销可就不能被忽略了
7. 使用 'Try-with-resources' 方式捕获异常
```
try (BufferedReader br = new BufferedReader(…);
     BufferedWriter writer = new BufferedWriter(…)) {
    // do sth.
} catch ( IOException | XEception e) { // Multiple catch
    // handle exception.
}
```

### 异常设计
1. 考虑是否需要定义成 Checked Exception，这种类型设计的初衷更是为了从异常情况恢复
2. 在保证诊断信息足够的同时，避免包含敏感信息，以免导致潜在的安全问题

```java
public enum PaymentCode implements ErrorCode {
    PAY_TIMEOUT(101),
    INSUFFICIENT_FUNDS(102);

    private final int code;

    private PaymentCode(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
```
Service 异常
```java
public class NotFindUserException extends RuntimeException {
    public NotFindUserException() {
        super("找不到此用户");
    }
    
    public NotFindUserException(String message) {
        super(message);
    }
}
```
API 异常
```java
public class ApiException extends RuntimeException {
    protected int Code;
    protected Map<String, Object> data = new HashMap<>();
}
```
```
@ControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandlerAdvice {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorDTO> exception(Exception exception, HttpServletResponse response) {
        ErrorDTO errorDTO = new ErrorDTO();
        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException)exception;
            errorDTO.setCode(apiException.getCode());
        } else {
            errorDTO.setCode(0);
        }
        
        // errorDTO.setTip(exception.getMessage());
        ResponseEntity<ErrorDTO> responseEntity = new ResponseEntity<>(errorDTO, HttpStatus.valueOf(response.getStatus()));
        return responseEntity;
    }
}
```

```java
public static String getUserText(ErrorCode code) {
    if (code == null) {
        return null;
    }

    String key = errorCode.getClass().getSimpleName() + "_" + errorCode;
    ResourceBundle bundle = ResourceBundle.getBundle("com.pain.exception");
    return bundle.getString(key);
}
```

### `finally`
1. 是保证重点代码一定要被执行的一种机制
2. 若在 `try/catch` 内有 `return` 语句，则 `return` 语句在 `finally` 语句执行结束后才执行，且 `finally` 不能改变返回值
3. 若在 `finally` 中也有 `return` 语句，则在 `try/catch` 中的 `return` 语句会丢失，实际会返回 `finally` 中的返回值；`finally` 中的 `return` 不仅会覆盖 `try/catch` 内的返回值，而且会掩盖 `try/catch` 内的异常
4. 若在 `finally` 中抛出异常会掩盖原异常
5. 应该避免在 `finally` 中使用 `return` 或者抛出异常，对可能抛出异常的代码进行捕获处理

### `finalize`
`finalize` 保证对象在被垃圾收集前完成特定资源的回收。`finalize` 的执行是和垃圾收集关联在一起的，一旦实现了非空的 `finalize` 方法，就会导致相应对象回收呈现数量级上的变慢；`finalize` 还会掩盖资源回收时的出错信息，一旦出现异常或者出错，得不到任何有效信息

### `final`
`final` 修饰的 `class` 代表不可以继承扩展，`final` 的变量是不可以修改的，而 `final` 的方法也是不可以重写的
