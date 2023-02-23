package zw.org.zvandiri.batch.listeners;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.annotation.AfterStep;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author :: codemaster
 * created on :: 5/10/2022
 * Package Name :: zw.org.zvandiri.batch.listeners
 */

public class CaseloadJobExcutionListener implements JobExecutionListener {
    HttpServletResponse response;
    Workbook workbook;
    private int progress=0;

    public CaseloadJobExcutionListener(HttpServletResponse response) {
        this.response = response;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.err.println("--------------- starting caseload management plan export ---------------");
    }


    @Override
    public void afterJob(JobExecution jobExecution) {
        String suffix= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        try(ServletOutputStream myOut = response.getOutputStream()) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "filename=Caseload_Management_Planner_"+suffix+".xlsx");
            workbook.write(myOut);
            myOut.flush();
        } catch (IOException e) {
            System.err.println("ForceDOWNLOAD Method: ");
            e.printStackTrace();
        }
    }
}
