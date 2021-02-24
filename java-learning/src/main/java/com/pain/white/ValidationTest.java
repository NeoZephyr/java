package com.pain.white;

import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.Length;

import javax.validation.*;
import javax.validation.constraints.*;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationTest {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        validateConstruct();
    }

    private static void validateObject() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId("1");
        userInfo.setUserName("jack");
        userInfo.setPassword("123456");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.NOVEMBER, 1);
        userInfo.setBirthday(calendar.getTime());
        userInfo.setFriends(Lists.newArrayList(new UserInfo()));

        Set<ConstraintViolation<UserInfo>> violations = validator.validate(userInfo,
                UserInfo.LoginGroup.class, UserInfo.RegisterGroup.class);

        violations.forEach(violation -> {
            System.out.println(violation.getMessage());
        });
    }

    private static void validateMethod() throws NoSuchMethodException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ExecutableValidator executableValidator = validator.forExecutables();

        UserService userService = new UserService();
        Method method = userService.getClass().getMethod("setUserInfo", UserInfo.class);
        Object[] objects = new Object[]{new UserInfo()};

        Set<ConstraintViolation<UserService>> violations = executableValidator.validateParameters(userService, method, objects);
        violations.forEach(violation -> {
            System.out.println(violation.getMessage());
        });
    }

    private static void validateReturn() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ExecutableValidator executableValidator = validator.forExecutables();

        UserService userService = new UserService();
        Method method = userService.getClass().getMethod("getUserInfo");
        Object result = method.invoke(userService);
        Set<ConstraintViolation<UserService>> violations = executableValidator.validateReturnValue(userService, method, result);
        violations.forEach(violation -> {
            System.out.println(violation.getMessage());
        });
    }

    private static void validateConstruct() throws NoSuchMethodException {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ExecutableValidator executableValidator = validator.forExecutables();
        Constructor<UserService> constructor = UserService.class.getConstructor(UserInfo.class);
        Object[] objects = new Object[]{new UserInfo()};
        Set<ConstraintViolation<UserService>> violations = executableValidator.validateConstructorParameters(constructor, objects);
        violations.forEach(violation -> {
            System.out.println(violation.getMessage());
        });
    }
}

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
@interface Phone {
    String message() default "invalid phone";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class PhoneValidator implements ConstraintValidator<Phone, String> {
    @Override
    public void initialize(Phone constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String validPhone = "139\\d{8}";
        Pattern pattern = Pattern.compile(validPhone);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }
}

class UserService {
    public UserService() {}
    public UserService(@Valid UserInfo userInfo) {}

    public void setUserInfo(@Valid UserInfo userInfo) {}

    public @Valid UserInfo getUserInfo() {
        return new UserInfo();
    }
}

class UserInfo {

    interface LoginGroup {}

    interface RegisterGroup {}

    @GroupSequence({
        LoginGroup.class,
        RegisterGroup.class,
        Default.class
    })
    interface Group {}

    @NotNull(message = "userId can not be null")
    private String userId;

    @NotEmpty(message = "userName can not be empty")
    private String userName;

    @NotBlank(message = "password can not be blank", groups = LoginGroup.class)
    @Length(min = 8, max = 20, message = "password length must be greater than 8 and less than 20")
    private String password;

    @NotBlank(message = "email can not be null", groups = RegisterGroup.class)
    @Email(message = "email format error")
    private String email;

    @Min(value = 18, message = "age can not less than 18")
    @Max(value = 45, message = "age can not more than 45")
    private int age;

    @Phone(message = "phone must begin with 139")
    private String phone;

    @Past(message = "birthday can not be future")
    private Date birthday;

    @Size(min = 1, message = "friends can not less than 1")
    private List<@Valid UserInfo> friends;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public List<UserInfo> getFriends() {
        return friends;
    }

    public void setFriends(List<UserInfo> friends) {
        this.friends = friends;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
