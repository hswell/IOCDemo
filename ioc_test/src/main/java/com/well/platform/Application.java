package com.well.platform;

import com.well.platform.context.MyApplicationContext;
import com.well.platform.myAnno.Value;
import com.well.platform.testPakage.controller.LoginController;

/**
 * @Author huangs-e
 * @Date 2020/11/26 18:21
 * @Version 1.0
 */
public class Application {
    @Value(value = "ioc.scan.path")
    private String path;
    public static void main(String[] args) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        MyApplicationContext applicationContext = new MyApplicationContext();
        LoginController loginController = (LoginController) applicationContext.getBean("LoginController");
        String login = loginController.login();
        System.out.println(login);
    }
}
