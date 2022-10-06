package zw.org.zvandiri.batch.configs;

import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.business.domain.Cadre;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.service.PatientService;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.batch.configs
 */

@Configuration
public class CadreJobConfiguration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    PatientService patientService;



    @Bean(name = "cadreReader")
    public JpaPagingItemReader<Cadre> cadreReader(){
        JpaPagingItemReader<Cadre> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        itemReader.setQueryString("from Cadre c");
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.CADRE_PAGE_SIZE);
        //itemReader.setMaxItemCount(Constants.CADRE_PAGE_SIZE);
        return itemReader;
    }



    @Bean
    public ItemProcessor<Cadre,Cadre> cadreItemProcessor(){
        ItemProcessor<Cadre,Cadre> itemProcessor=new ItemProcessor<Cadre, Cadre>() {
            @Override
            public Cadre process(Cadre cadre) throws Exception {

                return cadre;
            }
        };
        return itemProcessor;
    }





    public DelimitedLineAggregator<Cadre> getDelimitedLineAggregator() {
        BeanWrapperFieldExtractor<Cadre> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<Cadre>();
        beanWrapperFieldExtractor.setNames(new String[] {"id","firstName","lastName","gender","address","mobileNumber","dateOfBirth","primaryClinic","district","province"});

        DelimitedLineAggregator<Cadre> delimitedLineAggregator = new DelimitedLineAggregator<Cadre>();
        delimitedLineAggregator.setDelimiter(",");
        delimitedLineAggregator.setFieldExtractor(beanWrapperFieldExtractor);
        return delimitedLineAggregator;

    }
}
