import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianliao.zhuang on 2017/9/28.
 * Guava Iterables, Lists 有些相关操作都是返回相应的视图，
 * 并且只有在真正调用的地方才会去执行 apply 函数。
 * 所以如果在多个地方使用到返回的视图，那么每次都会生成新的对象，
 * 亦或是对全局变量（只要将变量定义在 apply 函数之前，则这个变量就是全局变量）进行多次更改。
 */
public class TransformTest {
    public static void main(String[] args) {
        List<Person> persons = new ArrayList<>();
        Person person = new Person("张三", 14);
        persons.add(person);
        Person person1 = new Person("李四", 15);
        persons.add(person1);

        List<Person> transform = Lists.transform(persons, new Function<Person, Person>() {
            int position = 0;
            @Override
            public Person apply(Person input) {
                System.out.println(position);
                Person newPerson = new Person();
                newPerson.setName(input.getName());
                newPerson.setAge(15);
                position++;
                return newPerson;
            }
        });

        print(transform);
    }


    static void print(List<Person> transform){
        for(Person p : transform){
            System.out.println(p);
        }

        for(Person p : transform){
            System.out.println(p);
        }
    }

    static class Person{
        private String name;
        private int age;

        public Person(){

        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
