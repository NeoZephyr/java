滑动窗口
```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        if (s.length() == 1) {
            return 1;
        }

        int i = 0, j = -1;

        Map<Character, Integer> map = new HashMap<>();

        int len = -1;

        while (j + 1 < s.length()) {
            while (j + 1 < s.length() && map.get(s.charAt(j + 1)) == null) {
                map.put(s.charAt(j + 1), 1);
                ++j;
            }

            len = Math.max(len, j - i + 1);

            if (j + 1 == s.length()) {
                break;
            }

            while (i <= j) {
                map.remove(s.charAt(i));

                if (s.charAt(i++) == s.charAt(j + 1)) {
                    break;
                }
            }
        }

        return len;
    }
}
```

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        if (s.length() == 1) {
            return 1;
        }

        int i = 0, j = -1;
        int[] freq = new int[256];
        
        int len = -1;

        while (j + 1 < s.length()) {
            if (j + 1 < s.length() && freq[s.charAt(j + 1)] == 0) {
                freq[s.charAt(++j)]++;
            } else {
                freq[s.charAt(i++)]--;
            }

            len = Math.max(len, j - i + 1);
        }

        return len;
    }
}
```