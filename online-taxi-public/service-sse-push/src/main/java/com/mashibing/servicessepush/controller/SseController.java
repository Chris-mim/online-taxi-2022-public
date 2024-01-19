package com.mashibing.servicessepush.controller;

import com.mashibing.internalcommon.util.SsePrefixUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class SseController {

    public static Map<String, SseEmitter> sseEmitterMap = new HashMap<>();

    @GetMapping("/connect")
    public SseEmitter connect(@RequestParam Long userId, @RequestParam String identity) {
        String sseMapKey = SsePrefixUtils.generatorSseKey(userId, identity);
        log.info("用户ID: " + userId + "身份："+identity);
        // 永不过期
        SseEmitter sseEmitter = new SseEmitter(0L);
        // SseEmitter 服务器推送技术
        sseEmitterMap.put(sseMapKey, sseEmitter);
        return sseEmitter;
    }

    /**
     * 发送消息
     * @param userId 用户id
     * @param identity 身份
     * @param content 内容
     * @return
     */
    @GetMapping("/push")
    public String push(@RequestParam Long userId, @RequestParam String identity, @RequestParam String content) {
        try {
            String sseMapKey = SsePrefixUtils.generatorSseKey(userId, identity);
            log.info("用户ID: " + userId + "身份："+identity +", 内容："+content);
            if (sseEmitterMap.containsKey(sseMapKey)) {
                sseEmitterMap.get(sseMapKey).send(content);
            } else {
                return "推送失败";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "给用户：" + userId + ",发送了消息：" + content;

    }

    /**
     * 关闭连接
     *
     * @param driverId
     * @return
     */
    @GetMapping("/close")
    public String close(@RequestParam Long userId, @RequestParam String identity) {
        String sseMapKey = SsePrefixUtils.generatorSseKey(userId, identity);
        System.out.println("关闭连接：" + sseMapKey);
        sseEmitterMap.remove(sseMapKey);
        return "close 成功";
    }
}
