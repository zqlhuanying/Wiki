<!-- TOC depthFrom:1 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Spring 中 @Transaction 的注意点](#spring-中-transaction-的注意点)
- [Spring 事务管理器](#spring-事务管理器)
	- [事务传播行为](#事务传播行为)
	- [事务隔离级别](#事务隔离级别)

<!-- /TOC -->

# Spring 中 @Transaction 的注意点
* @Transaction 底层采用 AOP 的思想来实现的，因此只有被切面拦截才会感知到事务的存在，否则此注解无效（不会报错，但是没有事务特性）  
* 注解只能修饰 public 方法，对 private 修饰符无效。AOP实际使用cglib来做的，cglib用的是java继承和多态性，所以要从外部调用public或者protected接口才会触发多态性才能进入到invocationhandler里面给你的代码套上transactional的外衣  
* 默认只对 RuntimeException 异常才会触发事务，当然可以通过配置 @rollbackFor 来配置其他异常生效。但是异常必须在抛出之时被事务所感知，否则被 catch 的异常也无法触发事务

  ** 注解无效的情况：**
  1. 一个类的内部方法之间相互调用，就算被调用的对象加了注解，因为不会被切面拦截，因此注解无效  
  2. 在到达事务边界时，异常被 catch

  ** 注解生效的情况：**
  1. 不同类之间方法的调用  

  ** For Example: **  

```
@Service("someService")
public class SomeService implements ISomeService {

    @Autowired
    private ISomeDao someDao;

    @Transactional(rollbackFor = Throwable.class)
    public void a() {
        try {
            b(1);
        } catch (Exception e) {
            // No roll back ...
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void aLoop() {
        for (int i=0; i<5 i++) {
            try {
                b(i);
            } catch (Exception e) {
                // No roll back ... but I want to roll back n(i) operation ...
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void b(int i) {
        someDao.save(i);
        // This method throw a exception
    }
}

@Controller("someController")
public class B {

    @Autowired
    private ISomeService someService;

    @RequestMapping("/")
    public void someMethod() {
        try {
            someService.b(1);
        } catch (Exception e) {
            // Here will roll back ... why?
        }
    }
}
```
解释：  
Spring事务处理逻辑是以切面的方式织入到业务（代理）对象中的。
如果一个业务对象中两个方法（a，b）都配置了事务逻辑，如果在业务对象中方法a中直接调用了方法b，则这只是简单的方法级别调用，方法b上面的事务根本就在本次调用中感知不到。  
所以场景1，在调用了业务（代理）对象的a方法，a方法在调用b方法时抛出了异常，而且你在a方法体内catch了这个异常，对于调用者来说，a方法调用是正常的。  
场景2，你调用了业务（代理）对象的b方法，但是在调用处catch了异常，代理对象b方法的异常已经抛给了事务切面，事务已经回滚啦。  
如果你想在a方法中保证b方法的事务性，则在方法a中必须要获得业务（代理）对象。  

# Spring 事务管理器
Spring并不直接管理事务，而是提供了多种事务管理器，他们将事务管理的职责委托给Hibernate或者JTA等持久化机制所提供的相关平台框架的事务来实现。
Spring事务管理器的接口是org.springframework.transaction.PlatformTransactionManager，通过这个接口，Spring为各个平台如JDBC、Hibernate等都提供了对应的事务管理器，但是具体的实现就是各个平台自己的事情了。此接口的内容如下：
```
Public interface PlatformTransactionManager()...{  
    // 由TransactionDefinition得到TransactionStatus对象
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;
    // 提交
    Void commit(TransactionStatus status) throws TransactionException;  
    // 回滚
    Void rollback(TransactionStatus status) throws TransactionException;  
  }
```
从这里可知具体的具体的事务管理机制对Spring来说是透明的，它并不关心那些，那些是对应各个平台需要关心的，所以Spring事务管理的一个优点就是为不同的事务API提供一致的编程模型，如JTA、JDBC、Hibernate、JPA。

## 事务传播行为
事务的第一个方面是传播行为（propagation behavior）。当事务方法被另一个事务方法调用时，必须指定事务应该如何传播。例如：方法可能继续在现有事务中运行，也可能开启一个新事务，并在自己的事务中运行。Spring定义了七种传播行为：  

| 传播行为  | 含义  |
| ---------|-------|
| PROPAGATION_REQUIRED |  表示当前方法必须运行在事务中。如果当前事务存在，方法将会在该事务中运行。否则，会启动一个新的事务  |
| PROPAGATION_SUPPORTS  | 表示当前方法不需要事务上下文，但是如果存在当前事务的话，那么该方法会在这个事务中运行  |
| PROPAGATION_MANDATORY | 表示该方法必须在事务中运行，如果当前事务不存在，则会抛出一个异常  |
| PROPAGATION_REQUIRED_NEW  | 表示当前方法必须运行在它自己的事务中。一个新的事务将被启动。如果存在当前事务，在该方法执行期间，当前事务会被挂起。如果使用JTATransactionManager的话，则需要访问TransactionManager  |
| PROPAGATION_NOT_SUPPORTED | 表示该方法不应该运行在事务中。如果存在当前事务，在该方法运行期间，当前事务将被挂起。如果使用JTATransactionManager的话，则需要访问TransactionManager |
| PROPAGATION_NEVER | 表示当前方法不应该运行在事务上下文中。如果当前正有一个事务在运行，则会抛出异常 |
| PROPAGATION_NESTED  | 表示如果当前已经存在一个事务，那么该方法将会在嵌套事务中运行。嵌套的事务可以独立于当前事务进行单独地提交或回滚。如果当前事务不存在，那么其行为与PROPAGATION_REQUIRED一样。注意各厂商对这种传播行为的支持是有所差异的。可以参考资源管理器的文档来确认它们是否支持嵌套事务  |

## 事务隔离级别
* 并发事务引起的问题  
在典型的应用程序中，多个事务并发运行，经常会操作相同的数据来完成各自的任务。并发虽然是必须的，但可能会导致以下的问题。  

| 事务问题 | 含义  |
| ------- |  ---- |
| 第一类丢失更新（回滚丢失） | 当2个事务更新相同的数据源，如果第一个事务被提交，而另外一个事务却被撤销，那么会连同第一个事务所做的跟新也被撤销。也就是说第一个事务做的跟新丢失了。  |
| 脏读  | 脏读发生在一个事务读取了另一个事务改写但尚未提交的数据时。如果改写在稍后被回滚了，那么第一个事务获取的数据就是无效的。 |
| 不可重复读 | 不可重复读发生在一个事务执行相同的查询两次或两次以上，但是每次都得到不同的数据时。这通常是因为另一个并发事务在两次查询期间进行了更新。**不可重复读的重点在于修改同一条记录的数据**  |
| 第二类丢失更新（覆盖丢失） | 第二类更新丢失实在实际应用中经常遇到的并发问题，他和不可重复读本质上是同一类并发问题，通常他被看做不可重复读的特例：当2个或这个多个事务查询同样的记录然后各自基于最初的查询结果更新该行时，会造成第二类丢失更新。 |
| 幻读  | 幻读与不可重复读类似。它发生在一个事务（T1）读取了几行数据，接着另一个并发事务（T2）插入了一些数据时。在随后的查询中，第一个事务（T1）就会发现多了一些原本不存在的记录。**幻读的重点在于新增或删除** |

* 隔离级别

| 隔离级别  | 含义 |  解决哪类问题  | 还存在哪类问题 |
| -------- | ---- |  ------------ | ------------  |
| ISOLATION_DEFAULT | 使用后端数据库默认的隔离级别  | | |
| ISOLATION_READ_UNCOMMITTED  | 读未提交，允许读取尚未提交的数据变更  | 第一类丢失更新 | 脏读、不可重复读、第二类丢失更新、幻读 |
| ISOLATION_READ_COMMITTED  | 读已提交，允许读取并发事务已经提交的数据 |第一类丢失更新、脏读  | 不可重复读、第二类丢失更新、幻读  |
| ISOLATION_REPEATABLE_READ | 可重复读，对同一字段的多次读取结果都是一致的，除非数据是被本身事务自己所修改  | 第一类丢失更新、脏读、不可重复读、第二类丢失更新  | 幻读  |
| ISOLATION_SERIALIZABLE |  可序列化，最高的隔离级别，完全服从ACID的隔离级别，确保阻止脏读、不可重复读以及幻读，也是最慢的事务隔离级别，因为它通常是通过完全锁定事务相关的数据库表来实现的 | 第一类丢失更新、脏读、不可重复读、第二类丢失更新、幻读 | 无 |
