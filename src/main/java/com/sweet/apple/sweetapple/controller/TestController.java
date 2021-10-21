package com.sweet.apple.sweetapple.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @author yangqifan004
 * @date 2021/10/21 17:19
 */
@RestController
public class TestController {

    @Value("${test.config.hei}")
    String value;

    @RequestMapping("/test")
    public String test(String input){

        return "config:" + value + "result:" + input;
    }
}
