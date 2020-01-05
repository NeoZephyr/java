```java
class Solution {
    public String minWindow(String s, String t) {
        if (s == null || t == null) {
            return "";
        }

        if (s.length() < t.length()) {
            return "";
        }

        int[] sFreq = new int[256];
        int[] tFreq = new int[256];

        for (int i = 0; i < t.length(); ++i) {
            tFreq[t.charAt(i)]++;
        }

        int count = 0;
        int l = 0, r = -1;

        String res = "";

        while (r + 1 < s.length()) {
            if (r + 1 < s.length() && count < t.length()) {
                sFreq[s.charAt(r + 1)]++;
                if (sFreq[s.charAt(r + 1)] <= tFreq[s.charAt(r + 1)]) {
                    ++count;
                }
                ++r;
            }

            if (count == t.length()) {
                while (sFreq[s.charAt(l)] > tFreq[s.charAt(l)]) {
                    sFreq[s.charAt(l++)]--;
                }

                if (res.isEmpty() || res.length() > (r - l + 1)) {
                    res = s.substring(l, r + 1);
                }

                sFreq[s.charAt(l++)]--;
                --count;
            }

            if (r + 1 == s.length()) {
                break;
            }
        }

        return res;
    }
}
```