package com.mashibing.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableFeignClients
public class ServicePayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicePayApplication.class, args);
    }

    @PostMapping("/test")
    public String test(){
        System.out.println("支付宝回调了");
        return "外网穿透测试";
    }
}
