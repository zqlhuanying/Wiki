1. Cglib BeanCopier(性能最好，但是 copy 间的对象要求比较严格)(测试代码可见[BeanCopier](../../Utils/BeanCopier.md))
  * 属性名相同，并且属性类型相同(OK)
  * 基本类型与包装类型之间的copy(不会copy，虽然程序员儿认为Integer与int是一样，而且赋值时能自动拆箱、装箱，但不幸的是，BeanCopier还是认为他们类型不同！)
  * target 的 setter不规范(抛异常)  
  在 PropertyDescriptor.getWriteMethod 方法中会对 Setter 返回的类型做判断，如果不是返回 void，就认为是不规范的 Setter。

2. apache BeanUtils
  * 属性名相同，并且属性类型相同(OK)
  * 基本类型与包装类型之间的copy(OK)
  * target 的 setter不规范(不会copy，但是不会抛异常)

3. spring BeanUtils
  * 属性名相同，并且属性类型相同(OK)
  * 基本类型与包装类型之间的copy(OK)
  * target 的 setter不规范(OK)  
  因为 Spring 使用了  GenericTypeAwarePropertyDescriptor 类，继承于 PropertyDescriptor，并重写了  getWriteMethod，并不做 Setter 返回类型的检查。
