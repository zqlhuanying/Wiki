import java.util.ArrayList;
import java.util.List;

/**
 * Created by 14160 on 2016/9/9.
 */
// 请设计一个复杂度为O(n)的算法，计算一个未排序数组中排序后相邻元素的最大差值。
// 给定一个整数数组A和数组的大小n，请返回最大差值。保证数组元素个数大于等于2小于等于500。
// 测试样例：
// [9,3,1,10],4
// 返回：6
// AC
public class MaxNeighberDivision {
    public static void main(String[] args){
        int[] test = new int[]{208,254,473,153,389,579,398};
        Bucket bucket = new Bucket(test);
        System.out.println(bucket.findMaxDivision());
    }

    public static class Bucket {
        private int numBuckets;
        private double gap;
        private BucketData[] buckets;
        private ArrayInfo array;

        public Bucket(int[] array) {
            this.array = new ArrayInfo(array);
            initBucket();
        }

        public void initBucket(){
            initNumBuckets();
            initGap();
            initBuckets();
        }

        public void initNumBuckets(){
            this.numBuckets = this.array.getLength() - 1;
        }

        public void initGap(){
            this.gap = (this.array.getMax() - this.array.getMin()) * 1.0 / this.numBuckets;
        }

        public void initBuckets(){
            // 填充桶
            if(this.buckets == null){
                this.buckets = new BucketData[this.numBuckets];
            }

            int min = this.array.getMin();
            int length = this.array.getLength();
            int[] data = this.array.getData();

            for (int i = 0; i < length; i++) {
                int index = (int) ((data[i] - min) / this.gap);
                if(index == this.buckets.length){
                    index--;
                }
                BucketData bucketData = this.buckets[index];
                if(bucketData == null){
                    bucketData = new BucketData();
                }
                bucketData.appendData(data[i]);
                this.buckets[index] = bucketData;
            }
        }

        public int findMaxDivision(){
            if(this.numBuckets <= 0){
                return 0;
            }
            if(this.numBuckets <= 1){
                return this.array.getMax() - this.array.getMin();
            }

            int maxDivision = -1;
            for (int i = 0; i < this.buckets.length;) {
                if(buckets[i] == null){
                    continue;
                }
                int j = findNextBucket(i);
                if(j >= this.buckets.length){
                    break;
                }
                int maxDivision_0 = new ArrayInfo(buckets[j].getData()).getMin() -
                        new ArrayInfo(buckets[i].getData()).getMax();
                if(maxDivision_0 > maxDivision){
                    maxDivision = maxDivision_0;
                }
                i = j;
            }
            return maxDivision;
        }

        public int findNextBucket(int start){
            // 寻找下一个非空的桶
            int j = start + 1;
            while (j < this.buckets.length && this.buckets[j] == null){
                j++;
            }
            return j;
        }
    }

    public static class BucketData{
        private List<Integer> data;

        public List<Integer> getData() {
            return data;
        }

        public void setData(List<Integer> data) {
            this.data = data;
        }

        public void appendData(int x){
            if(data == null){
                data = new ArrayList<>();
            }
            data.add(x);
        }
    }

    public static class ArrayInfo{
        private int max;
        private int min;
        private int[] data;
        private int length;

        public ArrayInfo(List<Integer> array){
            int[] res = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                res[i] = array.get(i);
            }
            init(res);
        }

        public ArrayInfo(int[] array) {
            init(array);
        }

        public void init(int[] array){
            this.data = array;
            this.length = array.length;
            initMaxAndMin(array);
        }

        public void initMaxAndMin(int[] array){
            int max = array[0];
            int min = array[0];

            for (int i = 1; i < array.length; i++) {
                if(array[i] > max){
                    max = array[i];
                }
                if(array[i] < min){
                    min = array[i];
                }
            }
            this.max = max;
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public int getMin() {
            return min;
        }

        public int[] getData() {
            return data;
        }

        public int getLength() {
            return length;
        }
    }
}
