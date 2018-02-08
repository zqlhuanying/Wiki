<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

		- [调研目标](#调研目标)
		- [解决的问题](#解决的问题)
		- [可选的方案](#可选的方案)
		- [不同方案之间的对比](#不同方案之间的对比)
		- [引入Hystrix的风险点](#引入hystrix的风险点)
		- [结论](#结论)
		- [附录](#附录)

<!-- /TOC -->
### 调研目标
高并发下的服务降级，系统容灾能力
### 解决的问题
为了解决远程调用中，由于服务者提供的服务不可靠时，导致整个接口不可用，进而导致服务雪崩效应。
### 可选的方案
1. Hystrix框架
2. Dubbo框架
### 不同方案之间的对比
|          | Hystrix   | Dubbo    |
| :------- | :-------- | :------- |
| 是否提供熔断机制       | 有       | 目前没有，不过看Git，好像有引入的意愿 |
| 服务降级  | 触发降级逻辑：<br>1.run()方法抛出非HystrixBadRequestException异常<br> 2.run()方法调用超时<br> 3.熔断器开启拦截调用<br> 4.线程池/队列/信号量满   | 只有服务抛出RPC异常(不包括业务异常)，才会触发降级逻辑。(**备注**：目前公司内好多Dubbo调用是以BaseResponse形式返回的，并不会以异常形式返回，所以有可能根本就不会触发降级逻辑)   |
| 性能耗时 | 附网上的一段性能测试结论：<br> 1.单个HystrixCommand的额外耗时基本稳定处于0.3ms左右，和线程池大小无关，和client数量无关<br> 2.Hystrix的额外耗时和执行的HystrixCommand数量有关系，随着command数量增多，耗时增加，但是增量较小，没有比例关系<br> 3.App刚启动时，第一个请求耗时300+ms，随后请求的耗时降低至1ms以下；刚启动的一小段时间内耗时略大于Hot状态时耗时，总体不超过1ms | 暂时没找到 |
| 界面展示 | [Hystrix Dashboard/Turbine](http://www.ityouknow.com/springcloud/2017/05/18/hystrix-dashboard-turbine.html) | soa-admin
### 引入Hystrix的风险点
目前也仅仅只是处于调研的阶段，只有在真正引入的时候，才会发现是不是有很多坑的存在。Hystrix框架是隶属于Spring Cloud，与Spring Cloud能很好的进行集成，属于微服务范畴。上手的难度会比Dubbo大。
### 结论
可以尝试引入Hystrix框架
### 附录
1. 服务雪崩：是一种因服务提供者的不可用导致服务调用者的不可用,并将不可用逐渐放大的过程.具体可以参见：[防雪崩利器：熔断器Hystrix的原理与使用](https://segmentfault.com/a/1190000005988895)
2. 如何应对服务雪崩：
  * 限流: 流量控制，常见的算法包括：计数器、滑动窗口、漏桶算法以及令牌桶算法。[接口限流算法总结](https://www.cnblogs.com/clds/p/5850070.html)。如Guava提供的RateLimiter类就提供了令牌桶算法。
  * 缓存: 在应用中使用缓存，但是要防止缓存被击穿。
  * 降级：牺牲非核心的业务功能，保证核心功能的稳定运行
    * 容错降级：由于调用远程服务失败，而进行业务流程放通
      * 调用远程服务发生RPC异常
      * 调用远程服务发生业务异常
    * 屏蔽降级：通过动态配置，在高峰期直接将服务暂时停掉，将该资源用在核心服务上。主要是人工降级。
  * 熔断：服务的过载保护，不会发起远程调用，直接返回特定的数据。
  * 降级和熔断区别
    * 不管是什么降级，都会发生RPC调用，只不过在调用失败后，会触发降级逻辑，以一种友好的方式将数据返回给用户，而不是直接显示服务异常，当然除了人工手动暂停服务外。因此可以说服务降级并不会降低服务提供者的压力，只是当服务出现异常时，仍能以友好的方式展示。
    * 在服务熔断期间，不会发起远程调用，直接返回Mock数据。有点类似手动暂停服务，只不过有自己的熔断逻辑，是一种自动的过程。
    * 其他区别：[谈谈我对服务熔断、服务降级的理解](http://blog.csdn.net/guwei9111986/article/details/51649240)

3. Hystrix
  * Hystrix是什么？  
  Netflix公司开源的Hystrix框架，对延迟和故障可以提供强大的容错能力，在分布式系统中对请求远程系统、服务或者第三方库产生的错误，通过熔断、线程池隔离等手段，可以及时停止系统中的级联错误从而起到自适应调节的作用。
  * Hystrix为了解决什么问题？  
    * 对第三方接口潜在的依赖调用的失败提供保护和控制机制。
    * 在分布式系统中隔离资源，降低耦合，防止导致级连失败，相互影响。
    * 快速失败以及迅速恢复。
    * 在合适的时机进行优雅的降级。
    * 提供近实时的监控，报警。
  * 原理
    * 流程框架图  
    ![Hystrix 框架流程图](/Image/Research/hystrix.png)
    * 说明
    ```
    1.Construct a HystrixCommand or HystrixObservableCommand Object
    2.Execute the Command
    3.Is the Response Cached?
    4.Is the Circuit Open?
    5.Is the Thread Pool/Queue/Semaphore Full?
    6.HystrixObservableCommand.construct() or HystrixCommand.run()
    7.Calculate Circuit Health
    8.Get the Fallback
    9.Return the Successful Response
    ```
    * 如何实现服务降级？
      * 由流程图可知，在真正调用远程服务之前，会首先经过熔断处理，如果熔断器处于打开状态，会直接走降级逻辑，而不会再经过一层远程调用，可以有效降低服务提供者的压力。
      * DEMO

      ```java
      public class HelloWorldCommand extends HystrixCommand<String> {  
        private final String name;  
        public HelloWorldCommand(String name) {  
          super(
          //最少配置:指定命令组名(CommandGroup)  
          Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
          // 熔断器设置
          .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
          .withExecutionTimeoutInMilliseconds(50)//超时时间
          .withCircuitBreakerRequestVolumeThreshold(5)
          .withCircuitBreakerSleepWindowInMilliseconds(1000)
          .withCircuitBreakerErrorThresholdPercentage(50))  
          // 线程池设置
          .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("ExampleGroup-pool"))
          .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(4))
          );  
          this.name = name;  
        }  
        // 具体调用远程服务
        @Override  
        protected String run() {  
          // 依赖逻辑封装在run()方法中  
          return "Hello " + name +" thread:" + Thread.currentThread().getName();  
        }  
        // 服务降级逻辑
        @Override  
        protected String getFallback() {  
          return "exeucute Falled";  
        }
        //调用实例  
        public static void main(String[] args) throws Exception{  
          //每个Command对象只能调用一次,不可以重复调用,  
          //重复调用对应异常信息:This instance can only be executed once. Please instantiate a new instance.  
          HelloWorldCommand helloWorldCommand = new HelloWorldCommand("Synchronous-hystrix");  
          //使用execute()同步调用代码,效果等同于:helloWorldCommand.queue().get();   
          String result = helloWorldCommand.execute();  
          System.out.println("result=" + result);  

          helloWorldCommand = new HelloWorldCommand("Asynchronous-hystrix");  
          //异步调用,可自由控制获取结果时机,  
          Future<String> future = helloWorldCommand.queue();  
          //get操作不能超过command定义的超时时间,默认:1秒  
          result = future.get(100, TimeUnit.MILLISECONDS);  
          System.out.println("result=" + result);  
          System.out.println("mainThread=" + Thread.currentThread().getName());  
        }  
      }  
      ```
  * 熔断器：Circuit Breaker
    * 每个熔断器默认维护10个bucket,每秒创建一个bucket,每个bucket记录成功,失败,超时,拒绝的状态，默认错误超过50%且10秒内超过20个请求进行中断拦截.
    * 状态转换  
    ![状态转换](/Image/Research/Breaker.png)
      * 说明
      ```
      1.当熔断器开关关闭时, 请求被允许通过熔断器. 如果当前健康状况高于设定阈值, 开关继续保持关闭. 如果当前健康状况低于设定阈值, 开关则切换为打开状态.
      2.当熔断器开关打开时, 请求被禁止通过.
      3.当熔断器开关处于打开状态, 经过一段时间后, 熔断器会自动进入半开状态, 这时熔断器只允许一个请求通过. 当该请求调用成功时, 熔断器恢复到关闭状态. 若该请求失败, 熔断器继续保持打开状态, 接下来的请求被禁止通过.
      ```
    * 服务的健康状况 = 请求失败数 / 请求总数.
    * 熔断器开关由关闭到打开的状态转换是通过当前服务健康状况和设定阈值比较决定的.
    * 熔断器的开关能保证服务调用者在调用异常服务时, 快速返回结果, 避免大量的同步等待. 并且熔断器能在一段时间后继续侦测请求执行结果, 提供恢复服务调用的可能.
    * 怎么计算失败率？
      * 涉及到Hystrix Metrics的原理。
      * Hystrix的Metrics中保存了当前服务的健康状况, 包括服务调用总次数和服务调用失败次数等. 根据Metrics的计数, 熔断器从而能计算出当前服务的调用失败率, 用来和设定的阈值比较从而决定熔断器的状态切换逻辑. 因此Metrics的实现非常重要.
  * 隔离机制
    * 为每个依赖提供一个小的线程池（或信号），如果线程池已满调用将被立即拒绝，默认不采用排队.加速失败判定时间
    * 线程隔离：把执行依赖代码的线程与请求线程分离，请求线程可以自由控制离开的时间(异步过程)。通过线程池大小可以控制并发量，当线程池饱和时可以提前拒绝服务,防止依赖问题扩散。**请求线程和处理线程分开，适用于IO密集型**
    * 信号量隔离：信号隔离也可以用于限制并发访问，防止阻塞扩散, 与线程隔离最大不同在于执行依赖代码的线程依然是请求线程（该线程需要通过信号申请）。**执行线程依然是请求线程，没有线程上下午切换，适用于CPU密集型**
  * 其他参考资料
    * [Hystrix 那些事](https://juejin.im/entry/58d24b32570c350058bc248a)
    * [防雪崩利器：熔断器 Hystrix 的原理与使用](https://segmentfault.com/a/1190000005988895)
    * [Hystrix使用入门手册](https://www.jianshu.com/p/b9af028efebb)
    * [Hystrix入门指南](https://www.cnblogs.com/gaoyanqing/p/7470085.html)
    * [Github](https://github.com/Netflix/Hystrix/wiki)
    * [Netflix的Hystrix使用教程](https://my.oschina.net/yu120/blog/656199)
    * [使用 Hystrix 实现自动降级与依赖隔离](http://www.importnew.com/25704.html)

4. Dubbo服务降级
  * 目前Dubbo社区没有引进熔断机制，只有服务降级.
  * mock支持的配置大体分为2类，一类用于屏蔽，一类用于容错。
    * 屏蔽降级：mock=force:return+null，不发起远程调用，直接返回null。这个不能在配置文件使用，通过控制台直接往注册中心写入该mock规则。
    * 容错降级
      * Dubbo的容错降级，只支持抛出非业务异常，才会触发。
      * mock="true"。如果配置为true，则缺省使用mock类名，即类名+Mock后缀，但该类要和暴露的服务路径相同，没有这个类，项目启动时会报Class Not Found 错误。
      ```
      <dubbo:reference id = "test" interface="XXXService" timeout="3000" version="1.0.0" mock="true"/>
      ```
      因为配置了mock="true"，则必须定义一个XXXServiceMock类，实现于XXXService接口，专门为降级服务。
      * mock="return null".
      ```
      <dubbo:reference id = "test" interface="XXXService" timeout="3000" version="1.0.0" mock="return null"/>
      ```
      这时候如果远程调用失败，就会直接返回null,不抛出异常。
      * 具体参见：[微服务架构之Dubbo服务降级](http://luckylau.tech/2018/01/22/%E5%BE%AE%E6%9C%8D%E5%8A%A1%E6%9E%B6%E6%9E%84%E4%B9%8BDubbo-6/)
