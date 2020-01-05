```java
class Solution {
    public String reverseVowels(String s) {
        char[] arr = s.toCharArray();

        int i = 0, j = arr.length - 1;
        while (i < j) {
            if (!isVowels(arr[i])) {
                ++i;
                continue;
            }

            if (!isVowels(arr[j])) {
                --j;
                continue;
            }

            swap(arr, i++, j--);
        }

        return String.valueOf(arr);
    }
    
    private boolean isVowels(char c) {
        c = Character.toLowerCase(c);
        if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
            return true;
        }
        return false;
    }
    
    private void swap(char[] nums, int i, int j) {
        char tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```