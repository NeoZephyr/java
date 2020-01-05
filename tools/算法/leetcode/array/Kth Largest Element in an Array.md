利用快速排序 partition
```java
class Solution {
    public int findKthLargest(int[] nums, int k) {
        assert nums != null && nums.length > 0;

        return partition(nums, 0, nums.length - 1, k - 1);
    }
    
    private int partition(int[] nums, int l, int r, int idx) {
        int k = doPartition(nums, l, r);
        if (k == idx) {
            return nums[idx];
        } else if (k < idx) {
            return partition(nums, k + 1, r, idx);
        } else {
            return partition(nums, l, k - 1, idx);
        }
    }

    private int doPartition(int[] nums, int l, int r) {
        int pivot = nums[l];
        // [lt, r]
        int i = l + 1;
        int lt = r + 1;
        while (i < lt) {
            if (nums[i] >= pivot) {
                ++i;
            } else {
                swap(nums, i, --lt);
            }
        }

        swap(nums, l, i - 1);

        return i - 1;
    }
    
    private void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
```