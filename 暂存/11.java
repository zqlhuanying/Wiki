import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by qianliao.zhuang on 2016/7/6.
 */
@Service
public class BusAccountPreAutoServiceImpl implements BusAccountPreAutoService {
    private static final int DEFAULT_THREADS = 15;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(DEFAULT_THREADS);

    @Override
    public Map<String, BaseResp> doBatch(List<String> ids){
        // 多线程版本
        String msg = logSource == 0 ? "job触发" : "手动触发";
        int countsForRequest = 10;
        // 初始化Map大小，防止Map不断扩容
        int initMapSize = ids.size() / countsForRequest + 1;

        Map<String, Future<BaseResp>> futureMap = new HashMap<String, Future<BaseResp>>(initMapSize);
        List<String> requestIds = new ArrayList<String>();

        for (int i = 1; i <= ids.size(); i++) {
            requestIds.add(ids.get(i - 1));
            if(i % countsForRequest == 0){
                String idsSplitByDot = StringUtils.join(requestIds, ",");
                Future<BaseResp> future = doExecuteWithThreadPool(idsSplitByDot);
                futureMap.put(idsSplitByDot, future);
                requestIds.clear();
            }
        }
        if (!requestIds.isEmpty()) {
            String idsSplitByDot = StringUtils.join(requestIds, ",");
            Future<BaseResp> future = doExecuteWithThreadPool(idsSplitByDot);
            futureMap.put(idsSplitByDot, future);
        }

        // 遍历结果
        Map<String, BaseResp> resultMap = new HashMap<String, BaseResp>(initMapSize);
        int i = 0;
        List<String> keyList = new ArrayList<>(futureMap.keySet());
        while (i < futureMap.size()){
            String key = keyList.get(i);
            try {
                Future<BaseResp> f = futureMap.get(key);
                if (f.isDone()){
                    resultMap.put(key, f.get());
                    logger.info("[{}]处理用户{}的结果为：{}", msg, key, JSONObject.toJSONString(f.get()));
                    i++;
                }
//                BaseResp baseResp = futureMap.get(key).get();
//                resultMap.put(key, baseResp);
//                logger.info("[{}]处理用户{}的结果为：{}", msg, key, JSONObject.toJSONString(baseResp));
            } catch (InterruptedException | ExecutionException e) {
                logger.error("[{}]处理用户{}异常", msg, key, e);
            }

        }
//        for(String key : futureMap.keySet()){
//            try {
//                BaseResp baseResp = futureMap.get(key).get();
//                resultMap.put(key, baseResp);
//                logger.info("[{}]处理用户{}的结果为：{}", msg, key, JSONObject.toJSONString(baseResp));
//            } catch (InterruptedException | ExecutionException e) {
//                logger.error("[{}]处理用户{}异常", msg, key, e);
//            }
//        }
        return resultMap;

    }


    private Future<BaseResp> doExecuteWithThreadPool(final String ids){
        return threadPool.submit(new Callable<BaseResp>() {
            @Override
            public BaseResp call(){
                return doExecute1(ids);
            }
        });
    }

    private BaseResp doExecute1(String ids){
      // .....
      return;
    }


    public void setThreads(int nThreads) {
        close();
        threadPool = Executors.newFixedThreadPool(nThreads);
    }

    public void close(){
        if(threadPool != null){
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(5, TimeUnit.SECONDS)){
                    logger.info("线程是否已关闭 {}",threadPool.isShutdown());
                    logger.info("线程是否已终止 {}",threadPool.isTerminated());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            threadPool.shutdownNow();
        }
    }
}
