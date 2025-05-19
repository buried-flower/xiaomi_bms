package org.example.mi_bms;

import org.example.mi_bms.util.RocketMQHelper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@MapperScan("org.example.mi_bms.mapper")
@Slf4j
@EnableCaching
@EnableScheduling
public class MiBmsApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MiBmsApplication.class);
        Environment environment = application.run(args).getEnvironment();
        log.info("启动成功！！！");
        log.info("地址：\thttp://127.0.0.1:{}",environment.getProperty("server.port"));
    }

    /**
     * 应用启动后执行，仅在RocketMQHelper存在时才创建
     */
    @Bean
    @ConditionalOnBean(RocketMQHelper.class)
    public ApplicationRunner rocketMQApplicationRunner(RocketMQHelper rocketMQHelper) {
        return args -> {
            try {
                // 打印RocketMQ配置
                rocketMQHelper.printRocketMQConfig();
                
                // 检查RocketMQ连接
                if (rocketMQHelper.checkConnection()) {
                    log.info("RocketMQ连接正常，预警功能已启用");
                } else {
                    log.warn("RocketMQ连接异常，预警功能可能无法正常工作");
                }
            } catch (Exception e) {
                log.error("初始化RocketMQ失败", e);
            }
        };
    }
}
