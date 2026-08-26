package com.essay.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.essay.agent.mapper")
public class EssayAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EssayAgentApplication.class, args);
    }

}