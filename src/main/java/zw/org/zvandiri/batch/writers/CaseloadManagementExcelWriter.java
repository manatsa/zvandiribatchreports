package zw.org.zvandiri.batch.writers;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.batchboot.batch
 */

import org.apache.poi.ss.usermodel.*;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zw.org.zvandiri.DatabaseHeader;
import zw.org.zvandiri.business.domain.Patient;
import zw.org.zvandiri.business.service.PatientService;
import zw.org.zvandiri.controller.progress.variables.ExportCaseloadVariables;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component("uncontactExcelWriter")
public class CaseloadManagementExcelWriter implements ItemWriter<Patient> {

   @Autowired
    PatientService patientService;

    private  HttpServletResponse response;
    private Workbook workbook;
    private CellStyle dataCellStyle;
    private int currRow = 0;
    Sheet sheet;

    @Autowired
    ExportCaseloadVariables exportCaseloadVariables;


    public CaseloadManagementExcelWriter(HttpServletResponse response ) {
        this.response = response;
    }

    public void setSheet(Sheet sheet){
        this.sheet=sheet;
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

        for (String header : DatabaseHeader.PATIENT_HEADER) {
            Cell cell = row.createCell(col);
            cell.setCellValue(header);
            cell.setCellStyle(style);
            col++;
        }
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        sheet.setDefaultColumnWidth(20);
        addHeaders(sheet);
        initDataStyle();
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution){
        switch(stepExecution.getStepName().toUpperCase())
        {
            case "**EXPORT_UNCONTACTED_STEP**":
                exportCaseloadVariables.setProgress(1);
                break;
            case "**EXPORT_INVALID_VL_STEP**":
                exportCaseloadVariables.setProgress(2);
                break;
            case "**EXPORT_ENHANCED_STEP**":
                exportCaseloadVariables.setProgress(3);
                break;
            case "**EXPORT_TB_CANDIDATES_STEP**":
                exportCaseloadVariables.setProgress(4);
                break;
            case "**EXPORT_MH_CANDIDATES_STEP**":
                exportCaseloadVariables.setProgress(5);
                break;
        }
    }

    private void initDataStyle() {
        dataCellStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Arial");
        dataCellStyle.setAlignment(HorizontalAlignment.LEFT);
        dataCellStyle.setFont(font);
    }


    @Override
    public void write(List<? extends Patient> items) throws Exception {
        for (Patient data : items) {
                Row row = sheet.createRow(++currRow);
                createStringCell(row, data.getPatientNumber(), 0);
                createStringCell(row, data.getName(), 1);
                createStringCell(row, data.getClientType()!=null?data.getClientType().getName():"", 2);
                createStringCell(row, data.getoINumber(), 3);
                createStringCell(row, data.getDateOfBirth()!=null?data.getDateOfBirth().toString():"", 4);
                createNumericCell(row, (double) data.getAge(), 5);
                createStringCell(row, data.getDateCreated()!=null?data.getDateCreated().toString():"", 6);
                createStringCell(row, data.getDateJoined()!=null?data.getDateJoined().toString():"", 7);
                createStringCell(row, data.getGender()!=null?data.getGender().getName():"", 8);
                createStringCell(row, data.getAddress(), 9);
                createStringCell(row, data.getMobileNumber(), 10);
                createStringCell(row, data.getConsentToMHealth()!=null?data.getConsentToMHealth().getName():"", 11);
                createStringCell(row, data.getEducation()!=null?data.getEducation().getName():"", 12);
                createStringCell(row, data.getEducationLevel()!=null?data.getEducationLevel().getName():"", 13);
                createStringCell(row, data.getArtRegimen(), 14);
                createStringCell(row, data.getReferer()!=null?data.getReferer().getName():"", 15);
                createStringCell(row, data.getPrimaryClinic().getDistrict().getProvince().getName(), 16);
                createStringCell(row, data.getPrimaryClinic().getDistrict().getName(), 17);
                createStringCell(row, data.getPrimaryClinic().getName(), 18);
                createStringCell(row, data.getSupportGroup()!=null?data.getSupportGroup().getName():"", 19);
                createStringCell(row, data.getDateTested()!=null?data.getDateTested().toString():"", 20);
                createStringCell(row, data.gethIVDisclosureLocation()!=null?data.gethIVDisclosureLocation().getName():"", 21);
                createStringCell(row, data.getDisclosureType()!=null?data.getDisclosureType().getName():"", 22);
                createStringCell(row, data.getIsKeypopulation()!=null?data.getIsKeypopulation().getName():"", 23);
                createStringCell(row, data.getKeyPopulation()!=null?data.getKeyPopulation().getName():"", 24);
                createStringCell(row, data.getHaveBirthCertificate()!=null?data.getHaveBirthCertificate().getName():"", 25);
                createStringCell(row, data.getDisability()!=null?data.getDisability().getName():"", 26);
                createStringCell(row, data.getDisablityType(), 27);
                createStringCell(row, data.getCat()!=null?data.getCat().getName():"", 28);
                createStringCell(row, data.getYoungMumGroup()!=null?data.getYoungMumGroup().getName():"", 29);
                createStringCell(row, data.getYoungDadGroup()!=null?data.getYoungDadGroup().getName() : "", 30);
                createStringCell(row, data.getTransmissionMode()!=null?data.getTransmissionMode().getName():"", 31);
                createStringCell(row, data.getHivStatusKnown()!=null?data.getHivStatusKnown().getName():"", 32);
                createStringCell(row, data.getStatus().getName(), 33);
                if(data.getLastVl()!=null && data.getLastVl().getResult()!=null){
                    createNumericCell(row, data.getLastVl().getResult().doubleValue(), 34);
                }else{
                    createBlankCell(row,34);
                }
                createStringCell(row, data.getLastVl()!=null?data.getLastVl().getTnd():"", 35);
                createStringCell(row, data.getLastVl()!=null && data.getLastVl().getDateTaken()!=null?data.getLastVl().getDateTaken().toString():"", 36);

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

    private void createBlankCell(Row row,  int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(CellType.BLANK);
    }

    public ExportCaseloadVariables getExportCaseloadVariables() {
        return exportCaseloadVariables;
    }

    public void setExportCaseloadVariables(ExportCaseloadVariables exportCaseloadVariables) {
        this.exportCaseloadVariables = exportCaseloadVariables;
    }
}