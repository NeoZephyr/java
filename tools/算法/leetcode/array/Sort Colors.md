大量重复元素的排序，使用计数排序
```java
class Solution {
    public void sortColors(int[] nums) {
        if (nums == null || nums.length < 2) {
            return;
        }

        int[] count = {0, 0, 0};
        for (int i = 0; i < nums.length; ++i) {
            assert nums[i] >= 0 && nums[i] <= 2;
            ++count[nums[i]];
        }

        int k = 0;
        for (int i = 0; i < count[0]; ++i) {
            nums[k++] = 0;
        }

        for (int i = 0; i < count[1]; ++i) {
            nums[k++] = 1;
        }

        for (int i = 0; i < count[2]; ++i) {
            nums[k++] = 2;
        }
    }
}
```

3 路快排的思路，注意边界条件
```java
// 0 [0, lt - 1]
// 1 [lt, i - 1]
// 2 [gt, len - 1]
class Solution {
    public void sortColors(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return;
        }
        
        int lt = 0, gt = nums.length;
        int i = 0;

        while (i < gt){
            if (nums[i] == 0) {
                swap(nums, i++, lt++);
            } else if (nums[i] == 2) {
                swap(nums, i, --gt);
            } else {
                ++i;
            }
        }
    }

    private void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```