package com.well.platform.testPakage.dao.imp;

import com.well.platform.myAnno.MyMapping;
import com.well.platform.testPakage.dao.LoginMapping;

/**
 * @Author huangs-e
 * @Date 2020/11/26 18:13
 * @Version 1.0
 */
@MyMapping
public class LoginMappingImpl implements LoginMapping {

    @Override
    public String login() {
        return "项目启动成功";
    }
}
