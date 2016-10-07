package classic;

import java.util.Scanner;

/**
 * Created by 14160 on 2016/10/6.
 */
// 约瑟夫环（约瑟夫问题）是一个数学的应用问题：
// 已知n个人（以编号1，2，3...n分别表示）围坐在一张圆桌周围,
// 从编号为k(一般默认为1)的人开始报数，数到m的那个人出列；他的下一个人又从1开始报数，数到m的那个人又出列；
// 依此规律重复下去，直到圆桌周围的人全部出列，求最后出列那个人的编号
// 找出递推关系：Num(i) = (Num(i - 1) + m - 1) % i + 1
// Num(i):表示当桌子上的人数为i时，最后存活的那个人的编号。其中Num(1) = 1
public class Josephus {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()){
            int n = Integer.parseInt(sc.nextLine());
            int m = Integer.parseInt(sc.nextLine());

            int originNumber = getLive(n, m);
            System.out.println(originNumber);
        }
    }

    // 返回最后存活那个人最原始的编号
    public static int getLive(int i, int m){
        if(i == 1){
            return 1;
        }
        return (getLive(i - 1, m) + m - 1) % i + 1;
    }
}
