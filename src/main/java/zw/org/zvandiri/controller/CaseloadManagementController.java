package zw.org.zvandiri.controller;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.joda.time.LocalDate;
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
import org.springframework.batch.item.database.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import zw.org.zvandiri.BaseController;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.batch.listeners.CaseloadJobExcutionListener;
import zw.org.zvandiri.batch.listeners.CaseloadManagementChunckListener;
import zw.org.zvandiri.batch.writers.CaseloadManagementExcelWriter;
import zw.org.zvandiri.business.domain.Cadre;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.domain.util.PatientChangeEvent;
import zw.org.zvandiri.business.domain.util.UserLevel;
import zw.org.zvandiri.business.service.*;
import zw.org.zvandiri.business.util.dto.SearchDTO;
import zw.org.zvandiri.controller.progress.variables.ExportCaseloadVariables;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

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
@RequestMapping("/report/case-load-management")
public class CaseloadManagementController extends BaseController {
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
    ExportCaseloadVariables exportCaseloadVariables;

    SXSSFWorkbook workbook;

    LocalDate now = LocalDate.now();
    LocalDate vlDate=now.minusMonths(12);
    LocalDate mhDate=now.minusMonths(6);
    LocalDate tbDate=now.minusMonths(1);


    public JpaPagingItemReader<Patient> uncontactReader(Map<String, Object> params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;
        Map<String, Object> params2=new HashMap<>();
        for (String key: params.keySet()) {
            if(!key.equals("start") && !key.equals("end")){
                params2.put(key, params.get(key));
            }
        }

        StringBuilder sb=new StringBuilder("from Patient p where ");

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
            sb.append((tracer>0)?" and p.id not in (select t.patient from Contact t where t.contactDate between :startDate and :endDate)":
                    " p.id not in (select t.id from Contact t where t.contactDate between :startDate and :endDate)");
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

        itemReader.setParameterValues(params2);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.CONTACT_PAGE_SIZE);
        return itemReader;
    }

    public JpaPagingItemReader<Patient> invalidVlsReader(Map<String, Object> params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        Map<String, Object> params2=new HashMap<>();
        for (String key: params.keySet()) {
            if(!key.equals("startDate") && !key.equals("endDate")){
                params2.put(key, params.get(key));
            }
        }

        params2.put("start", vlDate.toDate());
        params2.put("end", new Date());

        StringBuilder sb=new StringBuilder("from Patient p where ");

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
        sb.append((tracer>0)?" and p.id not in (select i.patient from InvestigationTest i where i.dateTaken between :start and :end) ":
                " p.id not in (select i.patient from InvestigationTest i where i.dateTaken between :start and :end)");
        itemReader.setQueryString(sb.toString());
        itemReader.setParameterValues(params2);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.CONTACT_PAGE_SIZE);
        return itemReader;
    }

    public JpaPagingItemReader<Patient> enhancedReader(Map<String, Object> params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        Map<String, Object> params2=new HashMap<>();
        for (String key: params.keySet()) {
            if(!key.equals("startDate") && !key.equals("endDate")){
                params2.put(key, params.get(key));
            }
        }
        params2.remove("startDate", vlDate.toDate());
        params2.remove("endDate", new Date());

        StringBuilder sb=new StringBuilder("from Patient p where ");

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
        sb.append(" and p.id in " +
                "(SELECT a.patient FROM Contact a\n" +
                "LEFT OUTER JOIN Contact b\n" +
                "ON a.id = b.id \n" +
                "AND a.dateCreated > b.dateCreated\n" +
                "WHERE a.patient =p.id and a.careLevelAfterAssessment=1\n" +
                "ORDER BY a.dateCreated desc )");

        itemReader.setQueryString(sb.toString());
        itemReader.setParameterValues(params2);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.CONTACT_PAGE_SIZE);
        return itemReader;
    }

    public JpaPagingItemReader<Patient> tbCandidatesReader(Map<String,Object> params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        Map<String, Object> params2=new HashMap<>();
        for (String key: params.keySet()) {
            if(!key.equals("startDate")){
                params2.put(key, params.get(key));
            }
        }
        params2.put("endDate", tbDate.toDate());

        StringBuilder sb=new StringBuilder("from Patient p where ");

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
        sb.append((tracer>0)?" and p not in (select distinct t.patient from TbIpt t where t.dateScreened < :endDate)":
                " p not in (select distinct t.patient from TbIpt t where t.dateScreened < :endDate)");
        itemReader.setQueryString(sb.toString());
        itemReader.setParameterValues(params2);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.CONTACT_PAGE_SIZE);
        itemReader.setMaxItemCount(Constants.CONTACT_PAGE_SIZE);
        return itemReader;
    }

    public JpaPagingItemReader<Patient> mhCandidatesReader(Map<String,Object> params){
        JpaPagingItemReader<Patient> itemReader= new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        HashMap sorts=new HashMap();
        sorts.put("id", Order.ASCENDING);
        int tracer=0;

        Map<String, Object> params2=new HashMap<>();
        for (String key: params.keySet()) {
            if(!key.equals("startDate") && !key.equals("endDate")){
                params2.put(key, params.get(key));
            }
        }
        params2.put("startDate", mhDate.toDate());

        StringBuilder sb=new StringBuilder("from Patient p where ");

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
        sb.append((tracer>0)?" and p.id not in ( select m.patient from MentalHealthScreening m where m.dateScreened < :startDate )":
                " p.id not in ( select m.patient from MentalHealthScreening m where m.dateScreened < :startDate ))");
        itemReader.setQueryString(sb.toString());
        itemReader.setParameterValues(params2);
        itemReader.setSaveState(false);
        itemReader.setPageSize(Constants.CONTACT_PAGE_SIZE);
        return itemReader;
    }

    public Step exportUncontactedStep(HttpServletResponse response, Map<String, Object> params,User user){
        CaseloadManagementExcelWriter caseloadManagementExcelWriter =new CaseloadManagementExcelWriter(response);
        caseloadManagementExcelWriter.setSheet(workbook.createSheet("uncontacted"));
        caseloadManagementExcelWriter.setExportCaseloadVariables(exportCaseloadVariables);
        CaseloadManagementChunckListener caseloadManagementChunckListener= new CaseloadManagementChunckListener();
        caseloadManagementChunckListener.setUser(user);
        return stepBuilderFactory.get("**EXPORT_UNCONTACTED_STEP**")
                .<Patient, Patient>chunk(Constants.CHUNK_SIZE)
                .reader(uncontactReader(params))
                .writer(caseloadManagementExcelWriter)
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(caseloadManagementChunckListener)
                .build();

    }

    public Step exportInvalidVlStep(HttpServletResponse response, Map<String, Object> params, User user){
        CaseloadManagementExcelWriter caseloadManagementExcelWriter =new CaseloadManagementExcelWriter(response);
        caseloadManagementExcelWriter.setSheet(workbook.createSheet("invalidVLs"));
        caseloadManagementExcelWriter.setExportCaseloadVariables(exportCaseloadVariables);
        CaseloadManagementChunckListener caseloadManagementChunckListener= new CaseloadManagementChunckListener();
        caseloadManagementChunckListener.setUser(user);
        return stepBuilderFactory.get("**EXPORT_INVALID_VL_STEP**")
                .<Patient, Patient>chunk(Constants.CHUNK_SIZE)
                .reader(invalidVlsReader(params))
                .writer(caseloadManagementExcelWriter)
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(caseloadManagementChunckListener)
                .build();

    }

    public Step exportEnhancedStep(HttpServletResponse response, Map<String, Object> params, User user){
        CaseloadManagementExcelWriter caseloadManagementExcelWriter =new CaseloadManagementExcelWriter(response);
        caseloadManagementExcelWriter.setSheet(workbook.createSheet("enhanced"));
        caseloadManagementExcelWriter.setExportCaseloadVariables(exportCaseloadVariables);
        CaseloadManagementChunckListener caseloadManagementChunckListener= new CaseloadManagementChunckListener();
        caseloadManagementChunckListener.setUser(user);
        return stepBuilderFactory.get("**EXPORT_ENHANCED_STEP**")
                .<Patient, Patient>chunk(Constants.CHUNK_SIZE)
                .reader(enhancedReader(params))
                .writer(caseloadManagementExcelWriter)
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(caseloadManagementChunckListener)
                .build();

    }

    public Step exportTBScreeningStep(HttpServletResponse response, Map<String, Object> params, User user){
        CaseloadManagementExcelWriter caseloadManagementExcelWriter =new CaseloadManagementExcelWriter(response);
        caseloadManagementExcelWriter.setSheet(workbook.createSheet("tb_candidates"));
        caseloadManagementExcelWriter.setExportCaseloadVariables(exportCaseloadVariables);
        CaseloadManagementChunckListener caseloadManagementChunckListener= new CaseloadManagementChunckListener();
        caseloadManagementChunckListener.setUser(user);
        return stepBuilderFactory.get("**EXPORT_TB_CANDIDATES_STEP**")
                .<Patient, Patient>chunk(Constants.CHUNK_SIZE)
                .reader(tbCandidatesReader(params))
                .writer(caseloadManagementExcelWriter)
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                //.skip(NegativeAmountException.class)
                .allowStartIfComplete(true)
                .listener(caseloadManagementChunckListener)
                //.taskExecutor(new SimpleAsyncTaskExecutor("contacts_task_executor"))
                .build();

    }

    public Step exportMHScreeningStep(HttpServletResponse response, Map<String, Object> params, User user){
        CaseloadManagementExcelWriter caseloadManagementExcelWriter =new CaseloadManagementExcelWriter(response);
        caseloadManagementExcelWriter.setSheet(workbook.createSheet("mh_candidates"));
        caseloadManagementExcelWriter.setExportCaseloadVariables(exportCaseloadVariables);
        CaseloadManagementChunckListener caseloadManagementChunckListener= new CaseloadManagementChunckListener();
        caseloadManagementChunckListener.setUser(user);

        return stepBuilderFactory.get("**EXPORT_MH_CANDIDATES_STEP**")
                .<Patient, Patient>chunk(Constants.CHUNK_SIZE)
                .reader(mhCandidatesReader(params))
                .writer(caseloadManagementExcelWriter)
                .faultTolerant()
                .skipLimit(3)
                .skip(NullPointerException.class)
                .throttleLimit(8)
                .allowStartIfComplete(true)
                .listener(caseloadManagementChunckListener)
                .build();

    }


    public Job exportContactsJob(HttpServletResponse response, Map<String, Object> params, User user){
        CaseloadJobExcutionListener caseloadJobExcutionListener= new CaseloadJobExcutionListener(response);
        caseloadJobExcutionListener.setWorkbook(workbook);
        return jobBuilderFactory.get("EXPORT_CASELOAD_MANAGEMENT_JOB")
                .incrementer(new RunIdIncrementer())
                .start(exportUncontactedStep(response, params, user))
                .next(exportInvalidVlStep(response,params, user))
                .next(exportEnhancedStep(response, params, user))
                .next(exportTBScreeningStep(response,params, user))
                .next(exportMHScreeningStep(response,params, user))
                .listener(caseloadJobExcutionListener)
                .build();
    }


    @GetMapping( value={"/index","/index.htm"})
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DATA_CLERK') or hasRole('ROLE_ZM') or hasRole('ROLE_ZI') or hasRole('ROLE_M_AND_E_OFFICER') or hasRole('ROLE_HOD_M_AND_E')")
    public String getPatients(Model model, HttpServletResponse response) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        model.addAttribute("pageTitle", APP_PREFIX + "Caseload Management Plan Report");
        model.addAttribute("provinces", provinceService.getAll());
        model.addAttribute("districts", districtService.getAll());
        model.addAttribute("facilities", facilityService.getAll());
        model.addAttribute("item", new SearchDTO());
        UserLevel userLevel=userService.getCurrentUser().getUserLevel();
        model.addAttribute("userLevel",userLevel.getName().toUpperCase());
        model.addAttribute("statuses", PatientChangeEvent.values());
        return "hierarchicalDatabaseExportDates";
    }

    @RequestMapping(value = {"/index","index.htm"}, method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or hasRole('ROLE_DATA_CLERK') or hasRole('ROLE_ZM') or hasRole('ROLE_ZI') or hasRole('ROLE_M_AND_E_OFFICER') or hasRole('ROLE_HOD_M_AND_E')")
    public void startJob(HttpServletResponse response, @ModelAttribute("item") SearchDTO dto) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, IOException {
        long start=System.currentTimeMillis();
        User user=userService.getCurrentUser();
        System.err.println("---- Caseload Management ==> Current User: "+userService.getCurrentUsername()+
                " Parameters::"+((dto.getFacilities()!=null && !dto.getFacilities().isEmpty())?"Facilities:["+dto.getFacilities()+"]":
                dto.getDistricts()!=null && !dto.getDistricts().isEmpty()?"Districts["+dto.getDistricts()+"]":
                        dto.getProvinces()!=null && !dto.getProvinces().isEmpty()?"Provinces["+dto.getProvinces()+"]":"National Database"));

        workbook=new SXSSFWorkbook(Constants.EXPORT_DATABASE_PAGE_SIZE);

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
        if(dto.getStartDate()!=null && dto.getEndDate()!=null){
            params.put("startDate", dto.getStartDate());
            params.put("endDate", dto.getEndDate());
        }
        exportCaseloadVariables.setCount(5);
        exportCaseloadVariables.setProgress(0);
        List<PatientChangeEvent> statuses=Arrays.asList(PatientChangeEvent.DECEASED, PatientChangeEvent.GRADUATED).stream().collect(Collectors.toList());
        dto.setStatuses(statuses);
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
        JobExecution jobExecution = contactobLauncher.run(exportContactsJob(response, params,user), jobParameters);
        long time=System.currentTimeMillis()-start;
        System.err.println("EXPORT PATIENTS JOB STATUS :: "+jobExecution.getStatus()+".USER:: "+user.getUserName()+". IT TOOK :: "+
                ((time<120)?((time/1000)+" SECONDS."):((time/60000)+" MINUTES."+
                        " PARAMETERS ::"+((dto.getFacilities()!=null && !dto.getFacilities().isEmpty())?" Facilities:["+dto.getFacilities()+"]":
                        dto.getDistricts()!=null && !dto.getDistricts().isEmpty()?" Districts["+dto.getDistricts()+"]":
                                dto.getProvinces()!=null && !dto.getProvinces().isEmpty()?" Provinces["+dto.getProvinces()+"]":" National Database"))));

    }


}
