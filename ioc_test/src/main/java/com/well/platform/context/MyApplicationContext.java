package com.well.platform.context;

import com.well.platform.myAnno.*;
import com.well.platform.myUtils.ConfigurationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.well.platform.myUtils.ConfigurationUtils.getPropertiesByKey;

/**
 * @Author huangs-e
 * @Date 2020/11/26 16:58
 * @Version 1.0
 */
@Slf4j
public class MyApplicationContext {
    /**
     * 类的存储集合
     */
    private final Set<String> classSet = new HashSet();
    /**
     * ioc容器
     */
    private final Map<String, Object> iocBeanMap = new ConcurrentHashMap(1024);

    public MyApplicationContext() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 初始化数据
        this.classLoader();
    }

    public Object getBean(String beanName) {
        if (!iocBeanMap.isEmpty()) {
            return iocBeanMap.get(toLowercaseIndex(beanName));
        } else {
            return null;
        }
    }
    private void classLoader() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        new ConfigurationUtils(null);
        // 获取扫描包路径
        String classScanPath = (String) ConfigurationUtils.properties.get("ioc.scan.path");
        if (StringUtils.isNotEmpty(classScanPath)) {
            classScanPath = classScanPath.replace(".", "/");
        } else {
            throw new RuntimeException("请配置项目包扫描路径 ioc.scan.path");
        }
        // 扫描项目根目录中所有的class文件
        getPackageClassFile(classScanPath);
        for (String className : classSet) {
            //将获取到的类名进行注入
            addServiceToIoc(Class.forName(className));
        }
        // 获取带有MyService注解类的所有的带MyAutowired注解的属性并对其进行实例化
        Set<String> beanKeySet = iocBeanMap.keySet();
        for (String beanName : beanKeySet) {
            addAutowiredToField(iocBeanMap.get(beanName));
        }
    }

    /**
     * 递归包名获取包下面的所有java类
     *
     * @param packageName 包名
     */
    public void getPackageClassFile(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName);
        File file = new File(Objects.requireNonNull(url).getFile());
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (File classFile : Objects.requireNonNull(files)) {
                if (classFile.isDirectory()) {
                    getPackageClassFile(packageName + "/" + classFile.getName());
                } else {
                    if (classFile.getName().endsWith(".class")) {
                        log.info("正在加载: " + packageName.replace("/", ".") + "." + classFile.getName());
                        classSet.add(packageName.replace("/", ".") + "." + classFile.getName().replace(".class", ""));
                    }
                }
            }
        } else {
            throw new RuntimeException("该目录下不存在");
        }
    }

    /**
     * 注入类控制反转
     * 将当前类交由IOC管理
     *
     * @param classZ 类
     */
    private void addServiceToIoc(Class classZ) throws IllegalAccessException, InstantiationException {
        if (Objects.nonNull(classZ.getAnnotation(MyController.class))) {
            iocBeanMap.put(toLowercaseIndex(classZ.getSimpleName()), classZ.newInstance());
            log.info("控制反转访问控制层:" + toLowercaseIndex(classZ.getSimpleName()));
        }
        if (Objects.nonNull(classZ.getAnnotation(MyService.class))) {
            MyService myService = (MyService) classZ.getAnnotation(MyService.class);
            iocBeanMap.put(StringUtils.isEmpty(myService.value()) ? toLowercaseIndex(classZ.getSimpleName()) : toLowercaseIndex(myService.value()), classZ.newInstance());
            log.info("控制反转服务层:" + toLowercaseIndex(classZ.getSimpleName()));
        }
        if (Objects.nonNull(classZ.getAnnotation(MyMapping.class))) {
            MyMapping myMapping = (MyMapping) classZ.getAnnotation(MyMapping.class);
            iocBeanMap.put(StringUtils.isEmpty(myMapping.value()) ? toLowercaseIndex(classZ.getSimpleName()) : toLowercaseIndex(myMapping.value()), classZ.newInstance());
            log.info("控制反转持久层:" + toLowercaseIndex(classZ.getSimpleName()));
        }
    }

    /**
     * 类名首字母小写
     *
     * @param className 类名
     * @return 类名首字母小写
     */
    public String toLowercaseIndex(String className) {
        if (!StringUtils.isEmpty(className)) {
            return className.substring(0, 1).toLowerCase() + className.substring(1);
        }
        return className;
    }

    /**
     * 依赖注入
     * 遍历这个IOC容器，获取到每一个类的实例，判断里面是否有依赖其他的类的实例
     *
     * @param bean bean
     * @throws IllegalAccessException 异常
     * @throws InstantiationException 异常
     */
    public void addAutowiredToField(Object bean) throws IllegalAccessException, InstantiationException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(MyAutowired.class) != null) {
                field.setAccessible(true);
                MyAutowired myAutowired = field.getAnnotation(MyAutowired.class);
                Class<?> fieldClass = field.getType();
                // 接口不能被实例化，需要对接口进行特殊处理获取其子类，获取所有实现类
                if (fieldClass.isInterface()) {
                    // 如果有指定获取子类名
                    if (StringUtils.isNotEmpty(myAutowired.value())) {
                        field.set(bean, iocBeanMap.get(myAutowired.value()));
                    } else {
                        List<Object> list = findSuperInterfaceByIoc(field.getType());
                        if (!list.isEmpty()) {
                            if (list.size() > 1) {
                                throw new RuntimeException(bean.getClass() + "  注入接口 " + field.getType() + "   失败，请在注解中指定需要注入的具体实现类");
                            } else {
                                field.set(bean, list.get(0));
                                // 递归依赖注入
                                addAutowiredToField(field.getType());
                            }
                        } else {
                            throw new RuntimeException("当前类" + bean.getClass() + "  不能注入接口 " + Class.class + "  ， 接口没有实现类不能被实例化");
                        }
                    }
                } else {
                    String beanName = StringUtils.isEmpty(myAutowired.value()) ? toLowercaseIndex(field.getName()) : toLowercaseIndex(myAutowired.value());
                    Object beanObj = iocBeanMap.get(beanName);
                    field.set(bean, beanObj == null ? field.getType().newInstance() : beanObj);
                    log.info("依赖注入" + field.getName());
//                递归依赖注入
                }
                addAutowiredToField(field.getType());
            }
            if (field.getAnnotation(Value.class) != null) {
                field.setAccessible(true);
                Value value = field.getAnnotation(Value.class);
                field.set(bean, StringUtils.isNotEmpty(value.value()) ? getPropertiesByKey(value.value()) : null);
                log.info("注入配置文件  " + bean.getClass() + " 加载配置属性" + value.value());
            }
        }

    }

    /**
     * 判断需要注入的接口所有的实现类
     *
     * @param classZ 接口名
     * @return 接口的实现类
     */
    private List<Object> findSuperInterfaceByIoc(Class classZ) {
        Set<String> beanNameList = iocBeanMap.keySet();
        ArrayList<Object> objectArrayList = new ArrayList<>();
        for (String beanName : beanNameList) {
            Object obj = iocBeanMap.get(beanName);
            Class<?>[] interfaces = obj.getClass().getInterfaces();
            if (ArrayUtils.contains(interfaces, classZ)) {
                objectArrayList.add(obj);
            }
        }
        return objectArrayList;
    }
}
