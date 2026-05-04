package com.zesheng.common.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKnife4j
public class Knife4jConfig {
    //全局文档基础信息
    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                    .title("泽晟板砖助手-接口文档")
                    .version("1.0.0"));
    }
}
