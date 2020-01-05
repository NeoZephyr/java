```java
class Solution {
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> res = new ArrayList<>();

        if (s == null || p == null) {
            return res;
        }

        if (s.length() < p.length()) {
            return res;
        }

        int[] freq = new int[256];

        for (int i = 0; i < p.length(); ++i) {
            freq[p.charAt(i)]++;
        }

        int i = 0, j = -1;

        while (j + 1 < s.length()) {
            while (j + 1 < s.length() && freq[s.charAt(j + 1)] > 0) {
                freq[s.charAt(++j)]--;
            }

            if (j - i + 1 == p.length()) {
                res.add(i);
            }

            if (j + 1 == s.length()) {
                break;
            }
            
            freq[s.charAt(i++)]++;
        }

        return res;
    }
}
```

```java

```