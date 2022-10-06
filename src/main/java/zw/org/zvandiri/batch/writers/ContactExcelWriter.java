package zw.org.zvandiri.batch.writers;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.batchboot.batch
 */

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.DatabaseHeader;
import zw.org.zvandiri.business.domain.Contact;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.service.PatientService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component("contactExcelWriter")
public class ContactExcelWriter implements ItemWriter<Contact> {

    @Autowired
    PatientService patientService;

    private HttpServletResponse response;
    private Workbook workbook;
    private CellStyle dataCellStyle;
    private int currRow = 0;

    public ContactExcelWriter(HttpServletResponse response) {
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
        for (String header : DatabaseHeader.CONTACT_HEADER) {
            Cell cell = row.createCell(col);
            cell.setCellValue(header);
            cell.setCellStyle(style);
            col++;
        }
    }


    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        try (ServletOutputStream myOut = response.getOutputStream()) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "filename=Contact_Detailed_Report_" + suffix + ".xlsx");
            workbook.write(myOut);
            myOut.flush();
        } catch (IOException e) {
            System.err.println("ForceDOWNLOAD Method: ");
            e.printStackTrace();
        }

    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        workbook = new SXSSFWorkbook(Constants.CONTACT_PAGE_SIZE);
        Sheet sheet = workbook.createSheet("contacts");
        sheet.setDefaultColumnWidth(20);
        addHeaders(sheet);
        initDataStyle();
    }

    private void initDataStyle() {
        dataCellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setFont(font);
    }


    @Override
    public void write(List<? extends Contact> items) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        for (Contact data : items) {
            Row row = sheet.createRow(++currRow);
            createStringCell(row, data.getPatient().getPatientNumber(), 0);
            createStringCell(row, data.getPatient().getName(), 1);
            createStringCell(row, data.getPatient().getDateOfBirth() != null ? data.getPatient().getDateOfBirth().toString() : "", 2);
            createNumericCell(row, (double) data.getPatient().getAge(), 3);
            createStringCell(row, data.getPatient().getGender() != null ? data.getPatient().getGender().getName() : "", 4);
            createStringCell(row, data.getPatient().getPrimaryClinic().getDistrict().getProvince().getName(), 5);
            createStringCell(row, data.getPatient().getPrimaryClinic().getDistrict().getName(), 6);
            createStringCell(row, data.getPatient().getPrimaryClinic().getName(), 7);
            createStringCell(row, data.getDateCreated() != null ? data.getDateCreated().toString() : "", 8);
            createStringCell(row, data.getContactDate() != null ? data.getContactDate().toString() : "", 9);
            createStringCell(row, data.getCareLevel() != null ? data.getCareLevel().getName() : "", 10);
            createStringCell(row, data.getLocation() != null ? data.getLocation().getName() : "", 11);
            createStringCell(row, data.getPosition() != null ? data.getPosition().getName() : "", 12);
            createStringCell(row, data.getCareLevelAfterAssessment() != null ? data.getCareLevelAfterAssessment().getName() : "", 13);
            createStringCell(row, data.getLastClinicAppointmentDate() != null ? data.getLastClinicAppointmentDate().toString() : "", 14);
            createStringCell(row, data.getAttendedClinicAppointment() != null ? data.getAttendedClinicAppointment().getName() : "", 15);
            createStringCell(row, data.getNextClinicAppointmentDate() != null ? data.getNextClinicAppointmentDate().toString() : "", 16);
            createStringCell(row, data.getContactMadeBy(), 17);
            createStringCell(row, data.getEac() != null ? data.getEac().getName() : "", 18);
            if (data.getEac1() != null && data.getEac1() == 1) {
                createNumericCell(row, Integer.valueOf(1).doubleValue(), 19);
            } else if (data.getEac2() != null && data.getEac2() == 1) {
                createNumericCell(row, Integer.valueOf(2).doubleValue(), 19);
            } else if (data.getEac3() != null && data.getEac3() == 1) {
                createNumericCell(row, Integer.valueOf(3).doubleValue(), 19);
            } else {
                createBlankCell(row, 19);
            }
            createStringCell(row, data.getPatient().getCat() != null ? data.getPatient().getCat().getName() : "", 20);
            createStringCell(row, data.getPatient().getYoungMumGroup() != null ? data.getPatient().getYoungMumGroup().getName() : "", 21);
            createStringCell(row, data.getPatient().getYoungDadGroup() != null ? data.getPatient().getYoungDadGroup().getName() : "", 22);
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

    private void createBlankCell(Row row, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(CellType.BLANK);
    }


}