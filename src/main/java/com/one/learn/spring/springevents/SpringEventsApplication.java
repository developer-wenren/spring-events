package com.one.learn.spring.springevents;

import com.one.learn.spring.springevents.generic.GenericEventSupport;
import com.one.learn.spring.springevents.generic.Order;
import com.one.learn.spring.springevents.publish.CustomeEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * 引导类
 *
 * @author One
 */
@SpringBootApplication
@RestController
@EnableAsync
public class SpringEventsApplication {
    @Autowired
    CustomeEventPublisher publisher;

    @Autowired
    GenericEventSupport eventSupport;

    public static void main(String[] args) {
        SpringApplication.run(SpringEventsApplication.class, args);

        /**
         * 方式一
         */
//        SpringApplication springApplication = new SpringApplication(SpringEventsApplication.class);
//        springApplication.addListeners(new NormalCustomEventListener());
//        springApplication.run(args);

        /**
         * 方式二
         */
//        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(SpringEventsApplication.class);
//        springApplicationBuilder.listeners(new NormalCutomEventListener()).run(args);
    }

    @GetMapping("/hello")
    public String hello(String param) {
        System.out.println(Thread.currentThread() + "接受请求");
        try {
            publisher.publishEvent(param);
        } catch (Exception e) {
            e.printStackTrace();
        }

        eventSupport.createOrder(new Order(new Random(32).nextInt(100), param), true);

        return "hello," + param;
    }

}
