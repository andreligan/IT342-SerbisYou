package edu.cit.serbisyo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RunningController {
        @GetMapping("/running")
    public String printName(){
        return "Hello John Clyde H. aa";
    }
}