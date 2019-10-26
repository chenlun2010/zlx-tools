package com.zlx.tools.quartz.jdc.proxy.model;

import com.zlx.tools.quartz.helper.ClassLoadHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.quartz.Job;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class JobProxy implements InvocationHandler {

    private JobDetailImpl target = null;
    private Business business;

    public JobProxy(Business b) {
        this.business = b;
        target = new JobDetailImpl();
        target.setBusiness(b);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        method.invoke(target, args);
        business.execute();
        return null;
    }

    public Job getProxy() {
        return (Job) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }

    public static void main(String[] args) {
        try {
            List<Class> list = ClassLoadHelper.loadClassByLoader(JobProxy.class.getClassLoader());
            System.out.println("sdf");
        } catch (Exception e) {
            System.out.println("occur the test");
        }
    }

    public static Job cglib() throws JobExecutionException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(JobDetailImpl.class);
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy)
                    throws Throwable {
                System.out.println("before");
                Object res = methodProxy.invokeSuper(obj, args);
                System.out.println("after");
                return res;
            }
        });
        Job job = (Job) enhancer.create();

        return job;
    }
}
