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
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zw.org.zvandiri.BaseController;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.batch.listeners.PatientChunckListener;
import zw.org.zvandiri.batch.streams.PatientItemCountItemStream;
import zw.org.zvandiri.batch.writers.PatientExcelWriter;
import zw.org.zvandiri.business.domain.Cadre;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.domain.util.PatientChangeEvent;
import zw.org.zvandiri.business.domain.util.UserLevel;
import zw.org.zvandiri.business.service.*;
import zw.org.zvandiri.business.util.dto.SearchDTO;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.controller
 */

@Controller
@RequestMapping("/report/patient")
public class PatientController extends BaseController {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    PatientService patientService;
    @Autowired
    FacilityService facilityService;
    @Autowired
    DistrictService districtService;
    @Autowired
    ProvinceService provinceService;
    @Autowired
    UserService userService;
    @Autowired
    InvestigationTestService investigationTestService;
    @Autowired
    ItemProcessor<Cadre, Cadre> cadreProcessor;
    @Autowired
    JobLauncher patientJobLauncher;
    @Autowired
    JobBuilderFactory jobBuilderFactory;
    @Autowired
    StepBuilderFactory stepBuilderFactory;




    public ItemStream stream(){
        return new PatientItemCountItemStream();
    }

    public JpaPagingItemReader<Patient> patientReader(Map params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        StringBuilder sb=new StringBuilder("from Patient p where ");
        /*if(!params.isEmpty()){
            sb.append(" where ");
        }*/
        if(!params.isEmpty() && params.containsKey("facilities")){
            sb.append((tracer>0)?" and p.primaryClinic in :facilities":" p.primaryClinic in :facilities");
            tracer++;
        }
        if(!params.isEmpty() && params.containsKey("districts")){
            sb.append((tracer>0)?" and p.primaryClinic.district in :districts": " p.primaryClinic.district in :districts");
            tracer++;
        }
        if(!params.isEmpty() && params.containsKey("provinces")){
            sb.append((tracer>0)?" and p.primaryClinic.district.province in :provinces":" p.primaryClinic.district.province in :provinces");
            tracer++;
        }
        if(!params.isEmpty() && params.containsKey("statuses")){
            sb.append((tracer>0)?" and p.status in :statuses":" p.status in :statuses");
            tracer++;
        }
        else{
            sb.append((tracer>0)?" and p.status =5":" p.status = 5");
        }
        itemReader.setQueryString(sb.toString());
        itemReader.setParameterValues(params);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.PATIENT_PAGE_SIZE);
        return itemReader;
    }

    public ItemProcessor<Patient,Patient> patientItemProcessor(){
        ItemProcessor<Patient,Patient> itemProcessor=new ItemProcessor<>() {
            @Override
            public Patient process(Patient patient) throws Exception {

                return patient;
            }
        };
        return itemProcessor;
    }

    public Step exportPatientsStep(HttpServletResponse response, Map<String, Object> params,User user){
        PatientChunckListener patientChunckListener=new PatientChunckListener();
        patientChunckListener.setUser(user);
        return stepBuilderFactory.get("**EXPORT_PATIENT_STEP**")
                .<Patient, Patient>chunk(Constants.CHUNK_SIZE)
                .reader(patientReader(params))
                .processor(patientItemProcessor())
                .writer(new PatientExcelWriter(response))
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(patientChunckListener)
                .build();

    }




    public Job exportPatientsJob(HttpServletResponse response, Map<String, Object> params, User user){
        return jobBuilderFactory.get("EXPORT_PATIENT_JOB")
                .incrementer(new RunIdIncrementer())
                .start(exportPatientsStep(response, params, user))
                .build();
    }


    @GetMapping( value={"/index","/index.htm"})
    public String getPatients(Model model, HttpServletResponse response) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        model.addAttribute("pageTitle", APP_PREFIX + "Client Detailed Report");
        model.addAttribute("provinces", provinceService.getAll());
        model.addAttribute("districts", districtService.getAll());
        model.addAttribute("facilities", facilityService.getAll());
        model.addAttribute("item", new SearchDTO());
        UserLevel userLevel=userService.getCurrentUser().getUserLevel();
        model.addAttribute("userLevel",userLevel.getName().toUpperCase());
        model.addAttribute("statuses", PatientChangeEvent.values());
        return "hierarchicalDatabaseExport";
    }

    @RequestMapping(value = {"/index","index.htm"}, method = RequestMethod.POST)
    public void getPatientList(HttpServletResponse response, @ModelAttribute("item") SearchDTO dto) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        long start=System.currentTimeMillis();
        User user=userService.getCurrentUser();
        System.err.println("---- Client Detailed Report ==> Current User: "+userService.getCurrentUsername()+
                " Parameters::"+((dto.getFacilities()!=null && !dto.getFacilities().isEmpty())?"Facilities:["+dto.getFacilities()+"]":
                dto.getDistricts()!=null && !dto.getDistricts().isEmpty()?"Districts["+dto.getDistricts()+"]":
                        dto.getProvinces()!=null && !dto.getProvinces().isEmpty()?"Provinces["+dto.getProvinces()+"]":"National Database"));

        Map<String, Object> params= new HashMap<>();
        if(dto.getStatuses()!=null && !dto.getStatuses().isEmpty()){
            params.put("statuses", dto.getStatuses());
        }
        if(dto.getFacilities()!=null && !dto.getFacilities().isEmpty()){
            params.put("facilities", dto.getFacilities());
        }
        if(dto.getDistricts()!=null && !dto.getDistricts().isEmpty()){
            params.put("districts", dto.getDistricts());
        }
        if(dto.getProvinces()!=null && !dto.getProvinces().isEmpty()){
            params.put("provinces", dto.getProvinces());
        }
        List<PatientChangeEvent> statuses=Arrays.asList(PatientChangeEvent.DECEASED, PatientChangeEvent.GRADUATED).stream().collect(Collectors.toList());
        dto.setStatuses(statuses);
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        JobExecution jobExecution = patientJobLauncher.run(exportPatientsJob(response, params, user), jobParameters);
        BatchStatus batchStatus = jobExecution.getStatus();
        long time=System.currentTimeMillis()-start;
        System.err.println("EXPORT PATIENTS JOB STATUS :: "+batchStatus+".USER:: "+user.getUserName()+". IT TOOK :: "+
                ((time<120)?((time/1000)+" SECONDS."):((time/60000)+" MINUTES.")));

    }
}
