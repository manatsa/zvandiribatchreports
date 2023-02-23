package zw.org.zvandiri;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.service.UserService;

@SpringBootApplication
@EnableBatchProcessing
public class ZvandiriBatchReporterApplication {


    public static void main(String[] args) {
        SpringApplication.run(ZvandiriBatchReporterApplication.class, args);
    }

}
