package com.yaoyaoing.autoscript;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@tk.mybatis.spring.annotation.MapperScan(basePackages = "com.yaoyaoing.autoscript.dao")
public class AutoScriptApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoScriptApplication.class, args);
    }

}
