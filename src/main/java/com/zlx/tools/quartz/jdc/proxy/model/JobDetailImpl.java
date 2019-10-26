package com.zlx.tools.quartz.jdc.proxy.model;


import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobDetailImpl implements Job {
    private Business business;

    public JobDetailImpl() {

    }

//    public JobDetailImpl(Business business) {
//        this.business = business;
//    }


    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        business.execute();
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        Business job = (Business) jobDataMap.get("job");
        job.execute();
    }
}
