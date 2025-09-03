package com.chinmay.bfh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@SpringBootApplication
public class BajajFinservHealthOaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BajajFinservHealthOaApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        var f = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        f.setConnectTimeout(10_000);
        f.setReadTimeout(15_000);
        return new RestTemplate(f);
    }

}
