package com.zlx.tools.quartz.helper;

import com.zlx.tools.quartz.annotation.ScheduleTask;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class ClassLoadHelper {


    //通过loader加载所有类
    public static List<Class> loadClassByLoader(ClassLoader load) throws Exception {
        Enumeration<URL> urls = load.getResources("");
        //放所有类型
        List<Class> classes = new ArrayList<Class>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            //文件类型（其实是文件夹）
            if (url.getProtocol().equals("file")) {
                loadClassByPath(null, url.getPath(), classes, load);
            }
        }
        return classes;
    }

    //通过文件路径加载所有类 root 主要用来替换path中前缀（除包路径以外的路径）
    private static void loadClassByPath(String root, String path, List<Class> list, ClassLoader load) {
        File f = new File(path);
        if (root == null) root = f.getPath();
        //判断是否是class文件
        if (f.isFile() && f.getName().matches("^.*\\.class$")) {
            try {
                String classPath = f.getPath();
                //截取出className 将路径分割符替换为.（windows是\ linux、mac是/）
                String className = classPath.substring(root.length() + 1, classPath.length() - 6).replace('/', '.').replace('\\', '.');
                list.add(load.loadClass(className));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            File[] fs = f.listFiles();
            if (fs == null) return;
            for (File file : fs) {
                loadClassByPath(root, file.getPath(), list, load);
            }
        }
    }

    public static List<Class> getScheduleTaskClass() {
        try {
            List<Class> list = loadClassByLoader(ClassLoadHelper.class.getClassLoader());
            return list.stream().filter(t -> t.isAnnotationPresent(ScheduleTask.class)).collect(Collectors.toList());
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }


    public static void main(String[] args) {
        List<Class> list = getScheduleTaskClass();
        System.out.println("sdf");
    }
}
