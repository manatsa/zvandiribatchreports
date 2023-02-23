package zw.org.zvandiri;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableBatchProcessing
public class ZvandiriReportsApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ZvandiriReportsApplication.class);
    }


    public static void main(String[] args) {
        SpringApplication.run(ZvandiriReportsApplication.class, args);
    }

}
