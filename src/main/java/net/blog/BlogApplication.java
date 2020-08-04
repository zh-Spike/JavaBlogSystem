package net.blog;

import lombok.extern.slf4j.Slf4j;
import net.blog.utils.RedisUtils;
import net.blog.utils.SnowflakeIdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Bean
    public BCryptPasswordEncoder createPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RedisUtils createRedisUtils(){
        return new RedisUtils();
    }
}
