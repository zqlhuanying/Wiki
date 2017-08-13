1. 在 xml 文件中首先会配置如下一段配置
```java
<bean id="propertyConfigurer"class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
  <property name="location">
    <value>conf/jdbc.properties</value>
  </property>
  <property name="fileEncoding">
    <value>UTF-8</value>
  </property>
</bean>
```
该配置表明，会从 /conf/jdbc.properties 文件中读取配置信息。
  * 将文件地址（/conf/jdbc.properties）转换成 Resource 对象
  * 由 PropertiesLoaderSupport 类来读取配置信息，所有的配置信息会放入	Properties[] localProperties 结构体中，用以以后的解析。

2. 解析
  * 解析时会借助于 PropertyPlaceholderHelper 和 PlaceholderResolver 两个类
    * PropertyPlaceholderHelper: 用于占位符的解析 如Spring默认会解析 ${...}这种类型的配置，从 ${key} 解析出 key，再由 PlaceholderResolver 来完成对 key 的解析。目前Spring支持 ${key}, ${key + valueSeperator + value}, $(key), $[key] 的解析。不过默认的是${key}, ${key:value}, 其他形式的解析都需要设置相应的参数才能生效。
    * PlaceholderResolver: 对真正的 key 进行解析。从配置文件、JVM参数、环境变量中依次对 key 完成解析。其优先级逐级递减。

3. BeanDefinition
  * BeanDefinition 可以算是 Bean 元数据的载体
  * 首先会根据 xml 中<bean></bean>的定义语句，来构建一个BeanDefinition对象
  * 其次通过 BeanDefinitionVisitor 来访问该Bean，并通过传入的 Resolver 来完成Bean中占位符的解析
  * 最后应该会根据 BeanDefinition 在需要的时候来构建 Bean 实例。

4. BeanDefinition
  * 由 XmlBeanDefinitionReader 从 xml 资源文件中读取 Bean 的配置
    * 由 DefaultDocumentLoader 从 xml 资源文件中读取 xml 的内容
    * XmlBeanDefinitionReader 会将 BeanDefinition 生成交由  DefaultBeanDefinitionDocumentReader 来代理完成
    * 通过  DefaultBeanDefinitionDocumentReader.registerBeanDefinitions 以 (beanName, beanDefinition)的形式注册到 DefaultListableBeanFactory 的 beanDefinitionMap 中，到此 BeanDefinition 便生成了。
  * BeanDefinition 中的 MutablePropertyValues 承载着 xml 配置文件中 name/value 值，此处的value 有可能是以占位符的形式出现，因为value被定义为Object对象
  * 由此便展开了后续占位符解析的全过程
