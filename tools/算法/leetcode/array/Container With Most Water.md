```java
class Solution {
    public int maxArea(int[] height) {
        if (height == null || height.length < 2) {
            return 0;
        }

        int l = 0, r = height.length - 1;

        int max = -1;
        while (l < r) {

            int cand = (r - l) * Math.min(height[r], height[l]);
            if (cand > max) {
                max = cand;
            }

            // 右墙左移，舍弃候选区间：[(l, l + 1, ...), r]，而这些区间的面积一定小于 [l, r] 区间
            if (height[r] < height[l]) {
                --r;
            } else {
                ++l;
            }
        }

        return max;
    }
}
```