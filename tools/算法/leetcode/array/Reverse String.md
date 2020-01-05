```java
class Solution {
    public String reverseString(String s) {
        return new StringBuffer(s).reverse().toString();   
    }
}
```

```java
class Solution {
    public String reverseString(String s) {
        char[] arr = s.toCharArray();
        int i = 0, j = arr.length - 1;
        while (i < j) {
            char temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
            i++;
            j--;
        }
        return String.valueOf(arr);
    }
}
```