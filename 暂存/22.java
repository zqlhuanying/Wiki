import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by qianliao.zhuang on 2016/9/14.
 */
public class Test {

    public static void main(String[] args) {
        ExecutorService executors = Executors.newFixedThreadPool(5);
        List<Future<Integer>> future = new ArrayList<>();
        for (int j = 0; j < 100; j++) {
            for (int i = 0; i < 1000; i++) {
                Future<Integer> res = executors.submit(new Task());
                future.add(res);
            }

            if(j >= 1){
                executors.shutdown();
            }
            for (int i = 0; i < 1000; i++) {
                try {
                    Integer k = future.get(i).get();
                    System.out.println(k);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    static class Task implements Callable<Integer>{
        @Override
        public Integer call(){
            int i = 0;
            while (i < 100){
                i++;
                System.out.println(i);
            }
            return 0;
        }
    }
}
