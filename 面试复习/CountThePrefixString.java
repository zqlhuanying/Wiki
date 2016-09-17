package classic;

import java.util.*;

/**
 * Created by 14160 on 2016/9/10.
 */
// It is well known that AekdyCoin is good at string problems as well as number theory problems.
// When given a string s, we can write down all the non-empty prefixes of this string. For example:
// s: "abab"
// The prefixes are: "a", "ab", "aba", "abab"
// For each prefix, we can count the times it matches in s.
// So we can see that prefix "a" matches twice, "ab" matches twice too, "aba" matches once, and "abab" matches once.
// Now you are asked to calculate the sum of the match times for all the prefixes.
// For "abab", it is 2 + 2 + 1 + 1 = 6.
// The answer may be very large, so output the answer mod 10007.
public class CountThePrefixString {
    // 存储前面已匹配的前缀出现的索引下标
    private Map<String, List<Integer>> storage = new HashMap<>();
    private final String theString;

    public CountThePrefixString(String theString) {
        this.theString = theString;
    }

    public int count(){
        int sum = 0;
        int i = 0;
        for (; i < theString.length(); i++) {
            putIndex(theString.substring(0, i + 1));
            List<Integer> indexes = getIndex(theString.substring(0, i + 1));
            if(indexes != null && indexes.size() > 1){
                sum += indexes.size();
            } else {
                break;
            }
        }
        sum += (theString.length() - i);
        return sum;
    }

    private List<Integer> getIndex(String str){
        return storage.get(str);
    }

    private void putIndex(String str){
        List<Integer> indexes = new ArrayList<>();

        String previous = str.substring(0, str.length() - 1);
        if("".equals(previous)){
            int i = theString.indexOf(str);
            while (i >= 0){
                indexes.add(i);
                i = theString.indexOf(str, i + 1);
            }
        } else {
            List<Integer> prevIndex = getIndex(previous);
            for (Integer index : prevIndex){
                if(match(theString, index, str)){
                    indexes.add(index);
                }
            }
        }
        storage.put(str, indexes);
    }

    public boolean match(String origin, int startIndex, String str){
        int endIndex = startIndex + str.length();
        endIndex = endIndex > origin.length() ? origin.length() : endIndex;
        return str.equals(origin.substring(startIndex, endIndex));
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()){
            int n = Integer.parseInt(sc.nextLine());
            for (int i = 0; i < n; i++) {
                int length = Integer.parseInt(sc.nextLine());
                String str = sc.nextLine();
                CountThePrefixString countThePrefixString = new CountThePrefixString(str);
                System.out.println(countThePrefixString.count() % 10007);
            }
        }
    }
}
