package net.blog;

import lombok.extern.slf4j.Slf4j;
import net.blog.utils.SnowflakeIdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Slf4j
@SpringBootApplication
public class BlogApplication {
    public static void main(String[] args) {
        log.info("BlogApplication run...");
        SpringApplication.run(BlogApplication.class, args);
    }

    @Bean
    public SnowflakeIdWorker creatIdWorker(){
        return new SnowflakeIdWorker(0,0);
    }
}
