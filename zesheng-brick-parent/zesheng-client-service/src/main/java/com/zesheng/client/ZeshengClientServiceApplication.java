package com.zesheng.client;

import com.zesheng.common.config.SystemConfig;
import com.zesheng.common.kuaidi100.Kuaidi100Properties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.zesheng.client.mapper")
@ComponentScan(basePackages = {"com.zesheng.client", "com.zesheng.common"})
@EnableConfigurationProperties({SystemConfig.class, Kuaidi100Properties.class})
public class ZeshengClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeshengClientServiceApplication.class, args);
    }

}
