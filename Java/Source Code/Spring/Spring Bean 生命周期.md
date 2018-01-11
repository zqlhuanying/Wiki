1. 读取配置文件或扫描注解，生成BeanDefinition，放入BeanFactory（DefaultListableBeanFactory ）中。BeanFactory便是IOC容器。
2. Spring中有很多BeanFactoryPostProcessor和BeanPostProcessor用于对Bean进行后置处理
  * BeanFactoryPostProcessor是工厂级别的，作用于Bean实例化之前，因此此处只能拿到BeanDefinition对象，在真正实例化之前，可以对Bean元信息进行更改。如PropertyPlaceholderConfigurer，用于Bean占位符的解析。
  * BeanPostProcessor是容器级别的，通过容器初始化时，从BeanFactory中读取所有的BeanPostProcessors存入到某个变量中。因为是容器级别的，当前容器中所有的Bean都会被回调处理，在使用时一定要做好处理，只处理感兴趣的Bean，其他Bean则不做任何处理。与此同时，如果在Before Initation之前返回Null，则会阻止该Bean的初始化，不会调用后续的回调逻辑，也不会调用初始化方法，同时在BeanFactory中该Bean也会被置为Null。因为是容器级别的，所以只对当前容器内所有的Bean有效，对父容器或子容器内的Bean无效。
    * BeanPostProcessor可以被设置多个，如果需要改变被调用的顺序，可以实现Ordered接口，BeanPostProcessor被循环调用之前，会按照顺序Sort，进而根据顺序分优先级调用，具体可以参见 https://zhuanlan.zhihu.com/p/30112785
  * BeanFactoryPostProcessor和BeanPostProcessor的区别具体可以参见 http://blog.csdn.net/caihaijiang/article/details/35552859
3. 整个Bean的创建包括BeanPostProcessor被回调的逻辑都是在AbstractAutowireCapableBeanFactory.doCreateBean中完成
  * 实例化Bean
  * 属性注入，填充（populate）
  * Bean Before 初始化
  * 初始化（包括默认的afterPropertiesSet和自定义的init-method(可以在xml文件中配置，也可以在BeanFactoryPostProcessor中修改Bean的元信息，设置初始化函数名)）
  * Bean After 初始化

备注：
  Spring Aop讲解：
  * 读取Aop定义的Bean，生成BeanDefinition放入到BeanFactory中。
  * 通过BeanPostProcessor对Aop类进行代理。代理可以使用CGLIB或JDK原生代理，可以通过参数设置。
  * 具体可以参见 http://www.importnew.com/24430.html 和 http://www.importnew.com/24459.html
