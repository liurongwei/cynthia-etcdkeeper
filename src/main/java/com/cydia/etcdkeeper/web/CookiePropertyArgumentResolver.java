package com.cydia.etcdkeeper.web;

import com.cydia.etcdkeeper.annotations.CookieProperty;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
//import sun.plugin.com.TypeConverter;˚
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CookiePropertyArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return methodParameter.getParameterAnnotation(CookieProperty.class)!=null;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory)
            throws Exception {

        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);

        Field[] fields = methodParameter.getParameterType().getDeclaredFields();

        Map<String,String> cookies = new HashMap<>(request.getCookies().length);

        for (Cookie cookie: request.getCookies()
             ) {
            cookies.put(cookie.getName(),cookie.getValue());
        }


        Object parameter  = methodParameter.getParameterType().newInstance();
        for (Field field: fields
             ) {
            CookieProperty cookieProperty = field.getAnnotation(CookieProperty.class);

            if(field.getType().isInterface() || field.getType().isAnnotation() || cookieProperty==null){
                continue;
            }

            String cookeName = cookieProperty.value();
            if(StringUtils.isBlank(cookeName)){
                cookeName = field.getName();
            }

            if(!cookies.containsKey(cookeName)){
                continue;
            }

            Object value= null;
            if(field.getType() == String.class){
                value = cookies.get(cookeName);
            }
            else if(field.getType().isPrimitive()){
                value= ConvertUtils.convert(cookies.get(cookeName), field.getType());
            }
            else{
                try {
                    Gson gson = new Gson();
                    value = gson.fromJson(cookies.get(cookeName), field.getType());
                }
                catch (Exception e){
                    log.warn("cann't convert field %s from cookie %s 's value : %s", field.getName(),
                            cookeName, cookies.get(cookeName));
                }
            }

            if(value!=null){
                field.setAccessible(true);
                field.set( parameter, value);
            }
        }

        return parameter;
    }
}
