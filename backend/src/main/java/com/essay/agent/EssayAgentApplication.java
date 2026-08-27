package com.essay.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class, MybatisPlusAutoConfiguration.class})
@MapperScan("com.essay.agent.mapper")
public class EssayAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EssayAgentApplication.class, args);
    }

}