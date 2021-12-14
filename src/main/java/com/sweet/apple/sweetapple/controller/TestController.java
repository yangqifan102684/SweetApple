package com.sweet.apple.sweetapple.controller;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 描述
 *
 * @author yangqifan004
 * @date 2021/10/21 17:19
 */
@RestController
public class TestController {

    @RequestMapping("/test")
    public String test(HttpServletRequest request){

        return "success";
    }

    @RequestMapping("/login")
    public String login(String username,String password, HttpServletRequest request){
        StpUtil.login(username);
        StpUtil.getSession().set("key","value");
        return "success";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request){
        StpUtil.logout();
        return "success";
    }

    @RequestMapping("/get")
    public String t(){
        Object value = StpUtil.getSession().get("key");
        System.out.println(value);
        return "success";
    }


}
