### beanutils
```java
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

public static <T> void setDefaultProp(T target, Class<T> clazz) {
  PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(clazz);

  for (PropertyDescriptor propertyDescriptor : descriptors) {
    String fieldName = propertyDescriptor.getName();
    Object value = PropertyUtils.getProperty(target,fieldName );
    if (String.class.isAssignableFrom(propertyDescriptor.getPropertyType()) && value == null) {
      PropertyUtils.setProperty(target, fieldName, "");
    } else if (Number.class.isAssignableFrom(propertyDescriptor.getPropertyType()) && value == null) {
      BeanUtils.setProperty(target, fieldName, "0");
    }
  }
}
```

