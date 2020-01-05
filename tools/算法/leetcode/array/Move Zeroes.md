```java
class Solution {
    public void moveZeroes(int[] nums) {
        assert nums != null && nums.length > 0;

        int pos = 0;
        for (int i = 0; i < nums.length; ++i) {
            if (nums[i] != 0) {
                nums[pos++] = nums[i];
            }
        }

        for (int i = pos; i < nums.length; ++i) {
            nums[i] = 0;
        }
    }
}
```