package com.just.contentcenter.controller.content;

import com.just.contentcenter.service.content.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author yanghao
 * @date 2020/1/19 14:41
 */
@RestController
@RequestMapping("/shares")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("{id}")
//    @HystrixCommand(fallbackMethod = "findByIdFail")
    public Object findById(@PathVariable Integer id,HttpServletRequest request) {
        Map<Object, Object> map = new HashMap<>();
        map.put("code", "500");
        map.put("msg", shareService.findById(id));
        return map;
    }

    /*public Object findByIdFail(Integer id, HttpServletRequest request) {

        //监控报警
        String findByIdKey = "find-By-Id";
        String sendValue = redisTemplate.opsForValue().get(findByIdKey);
        final String ip = request.getRemoteAddr();
        new Thread(() -> {
            if (StringUtils.isBlank(sendValue)) {
                System.err.println("紧急短信，用户下单失败，请离开查找原因，地址是："+ip);
                redisTemplate.opsForValue().set(findByIdKey, "find-By-Id-fail", 20, TimeUnit.SECONDS);
            } else {
                System.err.println("已经发送，20秒内不重复发送");
            }
        }).start();


        Map<Object, Object> map = new HashMap<>();
        map.put("code", "500");
        map.put("msg", "您被挤出来了，稍后重试");
        return map;
    }*/
}
