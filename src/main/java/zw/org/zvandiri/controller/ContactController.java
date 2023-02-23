package zw.org.zvandiri.controller;

import org.apache.commons.math3.analysis.function.Exp;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import zw.org.zvandiri.BaseController;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.batch.listeners.ContactChunckListener;
import zw.org.zvandiri.batch.listeners.ExportContactJobListener;
import zw.org.zvandiri.batch.listeners.PatientChunckListener;
import zw.org.zvandiri.batch.streams.PatientItemCountItemStream;
import zw.org.zvandiri.batch.writers.ContactExcelWriter;
import zw.org.zvandiri.batch.writers.PatientExcelWriter;
import zw.org.zvandiri.business.domain.Cadre;
import zw.org.zvandiri.business.domain.Contact;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.domain.util.PatientChangeEvent;
import zw.org.zvandiri.business.domain.util.UserLevel;
import zw.org.zvandiri.business.service.*;
import zw.org.zvandiri.business.util.dto.SearchDTO;
import zw.org.zvandiri.controller.progress.variables.ExportContactsVariables;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.controller
 */

@Controller
@RequestMapping("/report/contact")
public class ContactController extends BaseController {

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
    JobLauncher contactobLauncher;
    @Autowired
    JobBuilderFactory jobBuilderFactory;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    PatientReportService patientReportService;
    @Autowired
    ContactService contactService;

    @Autowired
    ExportContactsVariables exportContactsVariables;



    public JpaPagingItemReader<Contact> contactReader(Map params){
        JpaPagingItemReader<Contact> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        StringBuilder sb=new StringBuilder("from Contact c left join fetch c.patient p where ");
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
        if(!params.isEmpty() && params.containsKey("startDate") && params.containsKey("endDate")){
            sb.append((tracer>0)?" and c.contactDate between :startDate and :endDate":" c.contactDate between :startDate and :endDate");
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
        itemReader.setPageSize(Constants.CONTACT_PAGE_SIZE);
        return itemReader;
    }

    public ItemProcessor<Contact,Contact> contactItemProcessor(){
        ItemProcessor<Contact, Contact> itemProcessor= (ItemProcessor) contact -> contact;
        return itemProcessor;
    }

    public Step exportContactsStep(HttpServletResponse response, Map<String, Object> params, User user){
        ContactChunckListener contactChunckListener=new ContactChunckListener();
        contactChunckListener.setUser(user);
        contactChunckListener.setExportContactsVariables(exportContactsVariables);
        return stepBuilderFactory.get("**EXPORT_CONTACTS_STEP**")
                .<Contact, Contact>chunk(Constants.CONTACT_PAGE_SIZE)
                .reader(contactReader(params))
                //.processor(contactItemProcessor())
                .writer(new ContactExcelWriter(response))
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(contactChunckListener)
                .build();

    }

    public Job exportContactsJob(HttpServletResponse response, Map<String, Object> params, User user){
        return jobBuilderFactory.get("EXPORT_CONTACTS_JOB")
                .incrementer(new RunIdIncrementer())
                .start(exportContactsStep(response, params,user))
                .build();
    }


    @GetMapping( value={"/index","/index.htm"})
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DATA_CLERK') or hasRole('ROLE_ZM') or hasRole('ROLE_ZI') or hasRole('ROLE_M_AND_E_OFFICER') or hasRole('ROLE_HOD_M_AND_E')")
    public String getPatients(Model model, HttpServletResponse response) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        model.addAttribute("pageTitle", APP_PREFIX + "Contact Detailed Report");
        model.addAttribute("provinces", provinceService.getAll());
        model.addAttribute("districts", districtService.getAll());
        model.addAttribute("facilities", facilityService.getAll());
        model.addAttribute("item", new SearchDTO());
        UserLevel userLevel=userService.getCurrentUser().getUserLevel();
        model.addAttribute("userLevel",userLevel.getName().toUpperCase());
        model.addAttribute("statuses", PatientChangeEvent.values());
        return "contactsExportDates";
    }

    @RequestMapping(value = {"/index","index.htm"}, method = RequestMethod.POST)
    public void getPatientList(HttpServletResponse response, @ModelAttribute("item") SearchDTO dto) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        long start=System.currentTimeMillis();
        User currentUser=userService.getCurrentUser();
        System.err.println("---- Contact Detailed Report ==> Current User: "+userService.getCurrentUsername()+
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
        if(dto.getStartDate()!=null){
            params.put("startDate", dto.getStartDate());
        }
        if(dto.getEndDate()!=null){
            params.put("endDate", dto.getEndDate());
        }
        long contactCount=contactService.getSelectedContacts(dto);
        System.err.println("__________________ extracting :: "+contactCount+" contacts as requested ______________");
        exportContactsVariables.setCount(Double.valueOf(contactCount).doubleValue());
        //System.err.println("Total Contacts::"+contactCount);
        exportContactsVariables.setProgress(0);

        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        JobExecution jobExecution = contactobLauncher.run(exportContactsJob(response, params, currentUser), jobParameters);
        BatchStatus batchStatus = jobExecution.getStatus();
        long time=System.currentTimeMillis()-start;
        System.err.println("EXPORT PATIENTS JOB STATUS :: "+batchStatus+".USER:: "+currentUser.getUserName()+". IT TOOK :: "+
                ((time<120)?((time/1000)+" SECONDS."):((time/60000)+" MINUTES."+
                        " PARAMETERS ::"+((dto.getFacilities()!=null && !dto.getFacilities().isEmpty())?" Facilities:["+dto.getFacilities()+"]":
                        dto.getDistricts()!=null && !dto.getDistricts().isEmpty()?" Districts["+dto.getDistricts()+"]":
                                dto.getProvinces()!=null && !dto.getProvinces().isEmpty()?" Provinces["+dto.getProvinces()+"]":" National Database"))));

    }
}
