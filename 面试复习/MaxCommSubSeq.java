/**
 * Created by 14160 on 2016/9/4.
 */
// 求两个序列最大公共子序列
// AC
public class MaxCommSubSeq {
    public static void main(String[] args){
        String seq1 = "acfdg";
        String seq2 = "cdg";
        char[] res = maxSubSeq(seq1.toCharArray(), seq2.toCharArray());
        System.out.println(res.length);
    }

    public static char[] maxSubSeq(char[] first, char[] second){
        // init two-dimensions array
        // 多生成一行和一列，功能类似挡板
        int rows =  first.length + 1;
        int columns = second.length + 1;
        int[][] storage = new int[rows][columns];

        init(storage, rows, columns);
        compute(storage, first, second);
        return find(storage, first, second);
    }

    public static void init(int[][] storage, int rows, int columns){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                storage[i][j] = 0;
            }
        }
    }

    public static void compute(int[][] storage, char[] first, char[] second){
        for (int i = 1; i <= first.length; i++) {
            for (int j = 1; j <= second.length; j++){
                if(first[i - 1] == second[j - 1]){
                    storage[i][j] = storage[i - 1][j - 1] + 1;
                } else {
                    if(isDeterminedByI(storage, i, j)){
                        storage[i][j] = storage[i - 1][j];
                    } else {
                        storage[i][j] = storage[i][j - 1];
                    }
                }
            }
        }
    }

    public static char[] find(int[][] storage, char[] first, char[] second){
        StringBuilder sb = new StringBuilder();
        for(int i = first.length, j = second.length; i > 0 && j > 0;){
            if(first[i - 1] == second[j - 1]){
                sb.append(first[i - 1]);
                i--;
                j--;
            } else {
                if(isDeterminedByI(storage, i, j)){
                    i--;
                } else {
                    j--;
                }
            }
        }
        return sb.reverse().toString().toCharArray();
    }

    // 确定 storage[i][j] 的数据来源于 storage[i - 1][j] or storage[i][j - 1]
    public static boolean isDeterminedByI(int[][] storage, int i, int j){
        if(storage[i - 1][j] >= storage[i][j - 1]){
            return true;
        }
        return false;
    }
}
