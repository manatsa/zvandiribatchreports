
package zw.org.zvandiri.controller;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import zw.org.zvandiri.BaseController;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.batch.listeners.ExportDatabaseChunckListener;
import zw.org.zvandiri.batch.listeners.PatientChunckListener;
import zw.org.zvandiri.batch.streams.PatientItemCountItemStream;
import zw.org.zvandiri.batch.writers.ExportDatabaseExcelWriter;
import zw.org.zvandiri.batch.writers.PatientExcelWriter;
import zw.org.zvandiri.business.domain.*;
import zw.org.zvandiri.business.domain.dto.ExportDatabaseDTO;
import zw.org.zvandiri.business.domain.util.PatientChangeEvent;
import zw.org.zvandiri.business.domain.util.TestType;
import zw.org.zvandiri.business.service.*;
import zw.org.zvandiri.business.util.DateUtil;
import zw.org.zvandiri.business.util.dto.SearchDTO;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author manatsachinyeruse@gmail.com
 */
@Controller
@RequestMapping("/report/hierarchical-database-export")
public class HierarchicalDatabaseExportController extends BaseController {

    @Autowired
    JobBuilderFactory jobBuilderFactory;
    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Resource
    private DistrictService districtService;
    @Resource
    ProvinceService provinceService;
    @Resource
    FacilityService facilityService;
    @Autowired
    PatientReportService patientReportService;
    @Resource
    UserService userService;
    @Autowired
    PatientService patientService;
    @Autowired
    ContactService contactService;
    @Autowired
    ReferralService referralService;
    @Autowired
    InvestigationTestService investigationTestService;
    @Autowired
    TbIptService tbIptService;
    @Autowired
    MentalHealthScreeningService mentalHealthScreeningService;
    @Autowired
    JobLauncher exportDatabaseJobLauncher;
    @Autowired
    EntityManagerFactory entityManagerFactory;
    @Autowired
    ExportDatabaseVariables exportDatabaseVariables;


    public ItemStream stream(){
        return new PatientItemCountItemStream();
    }

    public JpaPagingItemReader<Patient> patientsReader(Map params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        StringBuilder sb=new StringBuilder("select p from Patient p where ");
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
        }else{
            sb.append((tracer>0)?" and p.status=5":" p.status=5");
        }

        itemReader.setQueryString(sb.toString());
        itemReader.setParameterValues(params);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.EXPORT_DATABASE_PAGE_SIZE);
        return itemReader;
    }

    public ItemProcessor<Patient, Patient> patientItemProcessor(){
        ExportDatabaseDTO exportDatabaseDTO= new ExportDatabaseDTO();
        ItemProcessor<Patient,Patient> itemProcessor= (ItemProcessor) patient -> patient;
        return itemProcessor;
    }

    public Step exportDatabaseStep(HttpServletResponse response, Map<String, Object> params, User user){
        SimpleAsyncTaskExecutor executor= new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(3);
        executor.setDaemon(true);
        executor.setThreadNamePrefix("PATIENT_THR_");
        ExportDatabaseChunckListener exportDatabaseChunckListener= new ExportDatabaseChunckListener();
        exportDatabaseChunckListener.setUser(user);
        exportDatabaseChunckListener.setExportDatabaseVariables(exportDatabaseVariables);
        return stepBuilderFactory.get("**EXPORT_DB_STEP**")
                .<Patient, Patient>chunk(Constants.EXPORT_DATABASE_PAGE_SIZE)
                .reader(patientsReader(params))
                //.processor(patientItemProcessor())
                .writer(new ExportDatabaseExcelWriter(response))
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .skip(EntityNotFoundException.class)
                .skip(JpaObjectRetrievalFailureException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(exportDatabaseChunckListener)
                .build();

    }

    public Job exportPatientsJob(HttpServletResponse response, Map<String, Object> params, User user){
        return jobBuilderFactory.get("EXPORT_DATABASE_JOB")
                .incrementer(new RunIdIncrementer())
                .start(exportDatabaseStep(response, params, user))
                .build();
    }

    public void setUpModel(ModelMap model, SearchDTO item) {
        item = getUserLevelObjectState(item);
        model.addAttribute("item", item);
        model.addAttribute("pageTitle", APP_PREFIX + "Hierarchical Database Export");

        model.addAttribute("districts", districtService.getAll());
        model.addAttribute("provinces", provinceService.getAll());
        model.addAttribute("facilities", facilityService.getAll());
        model.addAttribute("statuses", PatientChangeEvent.values());


    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String getExportDatabaseIndex(ModelMap model) {
        setUpModel(model, new SearchDTO());
        return "hierarchicalDatabaseExport";
    }

    @RequestMapping(value = "/index", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DATA_CLERK') or hasRole('ROLE_ZM') or hasRole('ROLE_ZI') or hasRole('ROLE_M_AND_E_OFFICER') or hasRole('ROLE_HOD_M_AND_E')")
    public void getExcelExport(ModelMap model,HttpServletResponse response, @ModelAttribute("item") SearchDTO dto) {
        try{
            long startTime=System.currentTimeMillis();
            User currentUser=userService.getCurrentUser();
            dto = getUserLevelObjectState(dto);
            dto.setUserLevel(currentUser.getUserLevel());
            System.err.println("---- Export Database ==> Current User: "+userService.getCurrentUsername()+
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
            long count=patientReportService.getCount(dto);
            exportDatabaseVariables.setCount(Double.valueOf(count).doubleValue());
            exportDatabaseVariables.setProgress(0);
            List<PatientChangeEvent> statuses= Arrays.asList(PatientChangeEvent.DECEASED, PatientChangeEvent.GRADUATED).stream().collect(Collectors.toList());
            dto.setStatuses(statuses);
            JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
            JobExecution jobExecution = exportDatabaseJobLauncher.run(exportPatientsJob(response, params, currentUser), jobParameters);
            BatchStatus batchStatus = jobExecution.getStatus();
            long time=System.currentTimeMillis()-startTime;
            System.err.println("EXPORT PATIENTS JOB STATUS :: "+batchStatus+".USER:: "+currentUser.getUserName()+". IT TOOK :: "+
                    ((time<120)?((time/1000)+" SECONDS."):((time/60000)+" MINUTES."+
                            " PARAMETERS ::"+((dto.getFacilities()!=null && !dto.getFacilities().isEmpty())?" Facilities:["+dto.getFacilities()+"]":
                            dto.getDistricts()!=null && !dto.getDistricts().isEmpty()?" Districts["+dto.getDistricts()+"]":
                                    dto.getProvinces()!=null && !dto.getProvinces().isEmpty()?" Provinces["+dto.getProvinces()+"]":" National Database"))));

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
