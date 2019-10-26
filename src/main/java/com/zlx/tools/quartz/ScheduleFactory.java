package com.zlx.tools.quartz;

import com.zlx.tools.quartz.annotation.Cron;
import com.zlx.tools.quartz.helper.ClassLoadHelper;
import com.zlx.tools.quartz.jdc.proxy.model.Business;
import com.zlx.tools.quartz.jdc.proxy.model.JobDetailImpl;
import com.zlx.tools.quartz.jdc.proxy.model.JobProxy;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

public class ScheduleFactory {
    public static final String TRIGGER_SUFFIX = "trigger";
    public static final String JOB_SUFFIX = "job";
    private static final SchedulerFactory scheduleFact = new StdSchedulerFactory();
    private static final String GROUP = "default";
    private static Scheduler schdeduler = null;

//    static {
//        start();
//    }

    /**
     * @param cron
     * @param id
     */
    public static synchronized boolean startSchedule(String cron, String id, Class<? extends Job> jobClass, JobDataMap jobDataMap) {
        try {
            if (isClosed())
                start();
            JobDetail jobDetail = new JobDetail(id + JOB_SUFFIX, GROUP, jobClass);
            jobDetail.setJobDataMap(jobDataMap);
            Trigger trigger = new CronTrigger(id + TRIGGER_SUFFIX, GROUP, id + JOB_SUFFIX,
                    GROUP, cron);
            schdeduler.scheduleJob(jobDetail, trigger);
        } catch (ParseException e) {
            return false;
        } catch (SchedulerException e) {
            return false;
        }
        return true;
    }


    /**
     * 关闭定时器工厂
     *
     * @return
     */
    public synchronized static boolean close() {
        try {
            if (schdeduler != null && !schdeduler.isShutdown()) {
                schdeduler.shutdown(false);
            }
        } catch (SchedulerException e) {
            return false;
        }
        return true;
    }

    public synchronized static boolean isClosed() {
        try {
            return schdeduler == null || schdeduler.isShutdown();
        } catch (SchedulerException e) {
        }
        return true;
    }

    /**
     * @param id
     * @return
     */
    public synchronized static boolean deleteSchedule(String id) {
        try {
            schdeduler.deleteJob(id + JOB_SUFFIX, GROUP);
        } catch (SchedulerException e) {
            return false;
        }
        return true;
    }

    /**
     * 启动定时调度
     *
     * @return
     */
    public synchronized static boolean start() {
        try {
            //scheduler 关闭后，不能再重新启动，因此直接新建
            if (schdeduler == null || schdeduler.isShutdown()) {
                schdeduler = scheduleFact.getScheduler();
                schdeduler.start();
                addSchedule();
                return true;
            }
        } catch (SchedulerException e) {
            return false;
        }
        return true;
    }

    private static void addSchedule() {
        List<Class> list = ClassLoadHelper.getScheduleTaskClass();
        for (Class c : list) {
            for (Method method : c.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Cron.class)) {
                    Cron cronInfo = method.getAnnotation(Cron.class);
                    String cron = cronInfo.value();
                    try {
                        Object target = c.newInstance();
                        JobDataMap jobDataMap=new JobDataMap();
                        jobDataMap.put("job", new Business() {
                            public void execute() {
                                try {
                                    method.invoke(target);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        startSchedule(cron, UUID.randomUUID().toString(), JobDetailImpl.class, jobDataMap);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public static void main(String[] args0) throws InterruptedException, IllegalAccessException, InstantiationException, JobExecutionException {
        String cron = "0 30 0 * * ?";
        String cron1 = "*/2 * * * * ?";
        String cron2 = "*/5 * * * * ?";
        start();
        JobDataMap jobDataMap = new JobDataMap();

        JobProxy jobProxy = new JobProxy(() -> System.out.println("hello world"));

        jobDataMap.put("job", new Business() {
            public void execute() {
                System.out.println("hello world");
            }
        });
        startSchedule(cron1, "df", JobDetailImpl.class, jobDataMap);


        JobDataMap jobDataMap1=new JobDataMap();
        jobDataMap1.put("job", new Business() {
            @Override
            public void execute() {
                System.out.println("hello world111");
            }
        });

        startSchedule(cron2, "sdf", JobDetailImpl.class, jobDataMap1);
    }
}
