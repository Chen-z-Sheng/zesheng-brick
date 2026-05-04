package com.zesheng.admin;

import com.zesheng.common.kuaidi100.Kuaidi100Properties;
import com.zesheng.common.config.SystemConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan({"com.zesheng.admin.mapper", "com.zesheng.sys.mapper"})
@SpringBootApplication(scanBasePackages = {"com.zesheng.admin", "com.zesheng.common", "com.zesheng.sys.service"})
@EnableConfigurationProperties({SystemConfig.class, Kuaidi100Properties.class})
@EnableScheduling
public class ZeshengAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZeshengAdminServiceApplication.class, args);
	}

}