package zw.org.zvandiri.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.batch.writers.CadreExcelWriter;
import zw.org.zvandiri.business.domain.Cadre;
import zw.org.zvandiri.business.service.CadreService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.controller
 */

@Controller
@RequestMapping("/report/cadre")
public class CadreController {

    @Autowired
    CadreService cadreService;
    @Qualifier("cadreReader")
    @Autowired
    JpaPagingItemReader<Cadre> caderReader;
    @Autowired
    ItemProcessor<Cadre, Cadre> cadreProcessor;
    @Autowired
    CadreExcelWriter cadreExcelWriter;
    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    JobBuilderFactory jobBuilderFactory;
    @Autowired
    StepBuilderFactory stepBuilderFactory;


    public Step exportCadreStep(HttpServletResponse response){
        return stepBuilderFactory.get("**step1**")
                .<Cadre, Cadre>chunk(Constants.CADRE_PAGE_SIZE)
                .reader(caderReader)
                //.processor(cadreProcessor)
                .writer(new CadreExcelWriter(response))
                .faultTolerant()
                .allowStartIfComplete(true)
                .build();
    }




    public Job exportCadreJob(HttpServletResponse response){
        return jobBuilderFactory.get("EXPORT_CADRE_JOB")
                .incrementer(new RunIdIncrementer())
                .start(exportCadreStep(response))
                .build();
    }


    @GetMapping(value={"/index", "index.htm"})
    @ResponseBody
    public String getCadre(Model model, HttpServletResponse response) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {

        JobParameters jobParameters = new JobParametersBuilder().addLong("start_time",System.currentTimeMillis()).toJobParameters();
        JobExecution jobExecution = jobLauncher.run(exportCadreJob(response), jobParameters);
        BatchStatus batchStatus = jobExecution.getStatus();
        System.err.println("JOB STATUS :: "+batchStatus);
        return "index";
    }
}
