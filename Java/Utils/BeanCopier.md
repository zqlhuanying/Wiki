```
package com.weimob.itgirl.groupon.server.util;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class BeanUtil {

    private final static Cache<String, BeanCopier> CACHE = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(10, TimeUnit.HOURS).build();

    private BeanUtil() {
    }

    /**
     * 单个对象复制功能
     * @param s  源class
     * @param t  目标class
     * @param source  源对象
     * @param <S>
     * @param <T>
     * @return 复制后的对象
     */
    public static <S, T> T copyProperties(final Class<S> s, final Class<T> t, S source) {
        Assert.notNull(s, "Source Class must not be null");
        Assert.notNull(t, "Target Class must not be null");
        Assert.notNull(source, "Source Object must not be null");
        try {
            String key = s.getName() + t.getName();
            BeanCopier beanCopier = CACHE.get(key, new Callable<BeanCopier>() {
                @Override
                public BeanCopier call() throws Exception {
                    return BeanCopier.create(s, t, false);
                }
            });
            return copyProperties(beanCopier, s, t, source);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("数据转换异常", throwable.getCause());
        }
    }


    private static <S, T> T copyProperties(final BeanCopier beanCopier, final Class<S> s, final Class<T> t, S source) {
        Assert.notNull(beanCopier, "BeanCopier must not be null");
        Assert.notNull(s, "Source Class must not be null");
        Assert.notNull(t, "Target Class must not be null");
        Assert.notNull(source, "Source Object must not be null");
        try {
            T result = t.newInstance();
            beanCopier.copy(source, result, null);
            return (T) result;
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("数据转换异常", throwable.getCause());
        }
    }
```
