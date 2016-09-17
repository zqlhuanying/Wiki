package classic;

import java.util.Scanner;

/**
 * Created by 14160 on 2016/9/9.
 */
// 最大连续子序列和问题
// AC
public class MaxSubSeqSum {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()){
            int n = Integer.parseInt(sc.nextLine());
            if(n == 0){
                break;
            }
            int[] array = convertStringToIntArray(sc.nextLine());

            boolean isAllNegative = true;
            for (int i = 0; i < array.length; i++) {
                if(array[i] >= 0){
                    isAllNegative = false;
                    break;
                }
            }
            if(isAllNegative){
                System.out.println("0 " + array[0] + " " + array[array.length - 1]);
                continue;
            }
            SubSequence subSequence = maxSubSeqSum(array);
            int start = subSequence.getStart();
            int end = subSequence.getEnd();
            int[] data = subSequence.getData();
            System.out.println(subSequence.getSum() + " " + data[start] + " " + data[end]);
        }
    }

    public static int[] convertStringToIntArray(String str){
        String[] strArray = str.split(" ");
        int[] origin = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            origin[i] = Integer.parseInt(strArray[i]);
        }
        return origin;
    }

    public static SubSequence maxSubSeqSum(int[] array){
        int sum = 0;
        int max = array[0];
        int startForMax = 0;
        int endForMax = 0;
        int startForSum = 0;
        int endForSum = 0;

        for (int i = 0; i < array.length; i++) {
            if(sum <= 0){
                sum = array[i];
                startForSum = i;
                endForSum = i;
            } else {
                sum += array[i];
                endForSum++;
            }
            if(sum > max){
                max = sum;
                startForMax = startForSum;
                endForMax = endForSum;
            }
        }
        return new SubSequence(startForMax, endForMax, array);
    }

    public static class SubSequence{
        private int start;
        private int end;
        private int sum;
        private int[] data;

        public SubSequence(int start, int end, int[] data) {
            this.start = start;
            this.end = end;
            this.data = data;
            this.sum = computeSum();
        }

        public int computeSum(){
            int sum = 0;
            for (int i = this.start; i <= this.end; i++) {
                sum += this.data[i];
            }
            return sum;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getSum() {
            return sum;
        }

        public int[] getData() {
            return data;
        }
    }
}
