package com.sweet.apple.sweetapple.controller;

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


}
