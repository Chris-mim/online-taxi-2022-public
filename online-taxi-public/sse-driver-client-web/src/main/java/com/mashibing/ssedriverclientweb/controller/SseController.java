package com.mashibing.ssedriverclientweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {
    @GetMapping("/connect/{driverId}")
    public SseEmitter connect(@PathVariable String driverId){
        System.out.println("司机ID: "+driverId);
        // 永不过期
        SseEmitter sseEmitter = new SseEmitter(0L);
        // SseEmitter 服务器推送技术
        return sseEmitter;
    }

}
