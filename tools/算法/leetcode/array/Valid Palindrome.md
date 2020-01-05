```java
class Solution {
    public boolean isPalindrome(String s) {
        if (s == null || s.length() < 1) {
            return true;
        }

        s = s.toLowerCase();
        int i = 0, j = s.length() - 1;

        while (i < j) {
            char a = s.charAt(i);
            char b = s.charAt(j);

            if (!Character.isLetterOrDigit(a)) {
                ++i;
                continue;
            }

            if (!Character.isLetterOrDigit(b)) {
                --j;
                continue;
            }

            if (a != b) {
                return false;
            }

            ++i;
            --j;
        }

        return true;
    }
}
```