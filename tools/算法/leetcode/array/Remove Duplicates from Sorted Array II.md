允许出现两次
```java
class Solution {
    public int removeDuplicates(int[] nums) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        if (nums.length <= 2) {
            return nums.length;
        }

        int pos = 0;
        for (int i = 0; i < nums.length; ++i) {
            if (pos < 2) {
                nums[pos++] = nums[i];
                continue;
            }

            if (nums[i] == nums[pos - 1] && nums[i] == nums[pos - 2]) {
                continue;
            } else {
                nums[pos++] = nums[i];
            }
        }

        return pos;
    }
}
```

推荐方案
```java
class Solution {
    public int removeDuplicates(int[] nums) {
        if (nums == null || nums.length == 0)
            return 0;

        int index = 1, count = 1;
        for (int i = 1; i < nums.length; i++) {
            if (nums[i] == nums[i - 1]) {
                if (count < 2) {
                    nums[index] = nums[i];
                    index++;
                    count++;
                }
            } else {
                count = 1;
                nums[index] = nums[i];
                index++;
            }
        }
        return index;
    }
}
```