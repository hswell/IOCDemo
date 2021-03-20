package com.well.platform.testPakage.controller;

import com.well.platform.myAnno.MyAutowired;
import com.well.platform.myAnno.MyController;
import com.well.platform.myAnno.Value;
import com.well.platform.testPakage.service.LoginService;

/**
 * @Author huangs-e
 * @Date 2020/11/26 18:17
 * @Version 1.0
 */
@MyController
public class LoginController {

    @MyAutowired
    LoginService loginService;
    public String login() {
        return loginService.login();
    }
}
