package zw.org.zvandiri.batch.writers;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.batchboot.batch
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.business.domain.Cadre;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.service.PatientService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@Component("cadreExcelWriter")
//@Scope("step")
public class CadreExcelWriter implements ItemWriter<Cadre> {

   @Autowired
    PatientService patientService;
    private static final String[] HEADERS = { "ID", "First Name", "Last Name","Address",
            "Mobile Number", "Date Of Birth", "Gender","Facility","District","Province" };

    private String outputFilename;
    private  HttpServletResponse response;
    private Workbook workbook;
    private CellStyle dataCellStyle;
    private int currRow = 0;

    public CadreExcelWriter(HttpServletResponse response) {
        this.response = response;
    }

    private void addHeaders(Sheet sheet) {
        Workbook wb = sheet.getWorkbook();

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();

        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setBold(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);

        Row row = sheet.createRow(0);
        int col = 0;

        for (String header : HEADERS) {
            Cell cell = row.createCell(col);
            cell.setCellValue(header);
            cell.setCellStyle(style);
            col++;
        }
//        currRow++;
    }


    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        try(ServletOutputStream myOut = response.getOutputStream()) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "filename=Cadres.xlsx");
            workbook.write(myOut);
            myOut.flush();
        } catch (IOException e) {
            System.err.println("EXPORTING WORKBOOK TO EXCEL::");
            e.printStackTrace();
        }

    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        workbook = new SXSSFWorkbook(Constants.CADRE_PAGE_SIZE);
        Sheet sheet = workbook.createSheet("cadres");
        sheet.setDefaultColumnWidth(20);
        addHeaders(sheet);
        initDataStyle();

    }

    private void initDataStyle() {
        dataCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setFont(font);
    }


    @Override
    public void write(List<? extends Cadre> items) throws Exception {

        System.err.println(items);;
        System.err.println("The chunk size is ::"+items.size());
        Sheet sheet = workbook.getSheetAt(0);

        for (Cadre data : items) {
                Row row = sheet.createRow(++currRow);
                createStringCell(row, data.getId(), 0);
                createStringCell(row, data.getFirstName(), 1);
                createStringCell(row, data.getLastName(), 2);
                createStringCell(row, data.getAddress(), 3);
                createStringCell(row, data.getMobileNumber(), 4);
                createStringCell(row, data.getDateOfBirth().toString(), 5);
                createStringCell(row, data.getGender().getName(), 6);
                createStringCell(row, data.getPatient()!=null?data.getPatient().getPrimaryClinic().getName():"", 7);
                createStringCell(row, data.getPatient()!=null?data.getPatient().getPrimaryClinic().getDistrict().getName():"", 8);
                createStringCell(row, data.getPatient()!=null?data.getPatient().getPrimaryClinic().getDistrict().getProvince().getName():"", 9);
        }
    }

    private void createStringCell(Row row, String val, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(val);
    }

    private void createNumericCell(Row row, Double val, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(val);
    }

}