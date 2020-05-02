package com.gajewski.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String homePage(){
        return "index";
    }

    @GetMapping("/outofsync")
    public String outOfSyncPage(){
        return "outofsync";
    }
}
