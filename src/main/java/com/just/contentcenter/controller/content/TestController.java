package com.just.contentcenter.controller.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yanghao
 * @date 2020/1/19 16:39
 */
@RestController
@RequestMapping("test")
public class TestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/test2")
    public List<ServiceInstance> setDiscoveryClient(){
        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
        return instances;
    }

    @Autowired
    private Source source;

    @GetMapping("test-stream")
    public String testStream() {
        source.output()
                .send(
                        MessageBuilder
                                .withPayload("消息体")
                                .build()
                );
        return "success";
    }
}
