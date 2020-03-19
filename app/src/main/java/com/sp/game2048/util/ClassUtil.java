package com.sp.game2048.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @do 类管理工具
 * @author liuhua
 * @date 2020/3/12 7:20 PM
 */
public class ClassUtil {
    //存入对象
    private static Map<Class,Object> classObjectMap = new ConcurrentHashMap<>();
    /**
     * @do 放入对象
     * @author liuhua
     * @date 2020/3/12 7:10 PM
     */
    public static void push(Object ... objects){
        for (Object object : objects) {
            classObjectMap.put(object.getClass(),object);
        }
    }
    /**
     * @do 取出对象
     * @author liuhua
     * @date 2020/3/12 7:13 PM
     */
    public static <T> T get(Class<T> clazz){
        return (T) classObjectMap.get(clazz);
    }
    /**
     * @do 删除对象
     * @author liuhua
     * @date 2020/3/12 7:22 PM
     */
    public static void remove(Class clazz){
        classObjectMap.remove(clazz);
    }
}
