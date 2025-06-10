package com.ll.framework.ioc;

import com.ll.framework.ioc.annotations.Component;
import lombok.extern.apachecommons.CommonsLog;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.sql.Ref;
import java.util.*;

public class ApplicationContext {
    private final String basePackage;
    private final Map<String, Object> beans = new HashMap<>();
    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    public void init() { // 프리로딩? 지연로딩?
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);

        for(Class<?> clazz : classes){
            if(clazz.isAnnotation()) continue; //annotation인 경우 스킵
            if(!beans.containsKey(extractClassName(clazz))) beans.put(extractClassName(clazz), genBean(clazz));
        }
    }


    public <T> T genBean(String beanName){ // Map의 빈을 불러오는 단순 기능으로 변경
        return (T) beans.get(makeClassName(beanName));
    }

    public Object genBean(Class<?> clazz){ // 빈을 생성하는 새로운 메서드
        String className = extractClassName(clazz);
        if(beans.containsKey(className)){ // 이미 생성된 빈인 경우 가져오기 => 싱글톤 패턴 유지
            return beans.get(className);
        }

        Constructor<?> constructor = clazz.getDeclaredConstructors()[0]; // 생성자 가져오기
        //getDeclaredConstructor의 경우, 기본 생성자(파라미터가 없는)만 가져옴.
        //getDeclaredConstrcutors의 경우 모든 생성자, 생성자가 하나 뿐이므로 해당 메소드로 가져옴

        List<Object> input_params = new ArrayList<>();
        for(Class<?> param : constructor.getParameterTypes()){ // 생성자 파라미터들 가져오기
            input_params.add(genBean(param));
        }

        try {
            Object instance = constructor.newInstance(input_params.toArray());
            beans.put(className, instance);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("빈 생성 중 문제가 발생했습니다.");
        }
        return null;
    }

    public String makeClassName(String beanName){
        // 앞글자 대문자로 변경
        return beanName.substring(0,1).toUpperCase() + beanName.substring(1);
    }

    public String extractClassName(Class<?> clazz){
        // clazz.getName() = 패키지까지 같이 추출
        return clazz.getName().replace(clazz.getPackageName() + ".", "");
    }
}
