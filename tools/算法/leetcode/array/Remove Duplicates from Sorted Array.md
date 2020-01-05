注意是有序数组
```java
class Solution {
    public int removeDuplicates(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        if (nums.length == 1) {
            return 1;
        }

        int pos = 1;
        for (int i = 1; i < nums.length; ++i) {
            if (nums[i] != nums[pos - 1]) {
                nums[pos++] = nums[i];
            }
        }
        return pos;
    }
}
```