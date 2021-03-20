package com.well.platform.testPakage.service.impl;

import com.well.platform.myAnno.MyAutowired;
import com.well.platform.myAnno.MyService;
import com.well.platform.testPakage.dao.LoginMapping;
import com.well.platform.testPakage.service.LoginService;

/**
 * @Author huangs-e
 * @Date 2020/11/26 18:07
 * @Version 1.0
 */
@MyService
public class LoginServiceImpl implements LoginService {
    @MyAutowired
    LoginMapping loginMapping;

    @Override
    public String login() {
        return loginMapping.login();
    }
}
