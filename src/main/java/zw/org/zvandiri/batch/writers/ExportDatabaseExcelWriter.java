package zw.org.zvandiri.batch.writers;

/**
 * @author :: codemaster
 * created on :: 30/9/2022
 * Package Name :: zw.org.zvandiri.batchboot.batch
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import zw.org.zvandiri.Constants;
import zw.org.zvandiri.DatabaseHeader;
import zw.org.zvandiri.business.domain.*;
import zw.org.zvandiri.business.domain.dto.ExportDatabaseDTO;
import zw.org.zvandiri.business.service.PatientService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component("exportDatabaseExcelWriter")
@Slf4j
public class ExportDatabaseExcelWriter implements ItemWriter<Patient> {
    private  HttpServletResponse response;
    private Workbook workbook;
    private CellStyle dataCellStyle;
    private int patientCurrRow = 0;
    private int contactCurrRow = 0;
    private int contactPage=0;
    private int referralCurrRow = 0;
    private int viralCurrRow = 0;
    private int tbCurrRow = 0;
    private int mhCurrRow = 0;

    Sheet contactSheet1=null;

    public ExportDatabaseExcelWriter(HttpServletResponse response) {
        this.response = response;
    }

    private void addHeaders(Sheet sheet, String[] headers, int currRow) {

        Workbook wb = sheet.getWorkbook();
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(font);

        Row row = sheet.createRow(0);
        int col = 0;

        for (String header : headers) {
            Cell cell = row.createCell(col);
            cell.setCellValue(header);
            cell.setCellStyle(style);
            col++;
        }
        //currRow++;
    }


    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        String suffix= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        try(ServletOutputStream myOut = response.getOutputStream()) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "filename=Zvandiri_Hierarchical_Database_Export_"+suffix+".xlsx");
            workbook.write(myOut);
            myOut.flush();
        } catch (IOException e) {
            System.err.println("ForceDOWNLOAD Method: ");
            e.printStackTrace();
        }

    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("************* Now beginning the export ***********************************");
        workbook = new SXSSFWorkbook(Constants.EXPORT_DATABASE_PAGE_SIZE);
        Sheet patientSheet = workbook.createSheet("patients");
        addHeaders(patientSheet, DatabaseHeader.PATIENT_HEADER, patientCurrRow);
        patientSheet.setDefaultColumnWidth(20);

        Sheet contactSheet = workbook.createSheet("contacts");
        addHeaders(contactSheet, DatabaseHeader.CONTACT_HEADER, contactCurrRow);
        contactSheet.setDefaultColumnWidth(20);

        Sheet referralSheet = workbook.createSheet("referrals");
        addHeaders(referralSheet, DatabaseHeader.REFERRAL_HEADER, referralCurrRow);
        referralSheet.setDefaultColumnWidth(20);

        Sheet vlSheet = workbook.createSheet("viral_Load");
        addHeaders(vlSheet, DatabaseHeader.CD4_COUNT_HEADER, viralCurrRow);
        vlSheet.setDefaultColumnWidth(20);

        Sheet tbSheet = workbook.createSheet("tb_screening");
        addHeaders(tbSheet, DatabaseHeader.TB_IPT_HEADER, tbCurrRow);
        tbSheet.setDefaultColumnWidth(20);

        Sheet mhSheet = workbook.createSheet("mh_screening");
        addHeaders(mhSheet, DatabaseHeader.MENTAL_HEALTH_SCREENING_HEADER, mhCurrRow);
        mhSheet.setDefaultColumnWidth(20);

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
    public void write(List<? extends Patient> items) throws Exception {
        Sheet patientSheet = workbook.getSheetAt(0);
        Sheet contactSheet = workbook.getSheetAt(1);
        Sheet referralSheet = workbook.getSheetAt(2);
        Sheet vlSheet = workbook.getSheetAt(3);
        Sheet tbSheet = workbook.getSheetAt(4);
        Sheet mhSheet = workbook.getSheetAt(5);



        // Write patient details to excel sheet
        items.stream().forEach(data -> {
            Row row = patientSheet.createRow(++patientCurrRow);
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
            createStringCell(row, data.getClientType()!=null?data.getClientType().getName() : "", 14);
            createStringCell(row, data.getArtRegimen(), 15);
            createStringCell(row, data.getReferer()!=null?data.getReferer().getName():"", 16);
            createStringCell(row, data.getPrimaryClinic().getDistrict().getProvince().getName(), 17);
            createStringCell(row, data.getPrimaryClinic().getDistrict().getName(), 18);
            createStringCell(row, data.getPrimaryClinic().getName(), 19);
            createStringCell(row, data.getSupportGroup()!=null?data.getSupportGroup().getName():"", 20);
            createStringCell(row, data.getDateTested()!=null?data.getDateTested().toString():"", 21);
            createStringCell(row, data.gethIVDisclosureLocation()!=null?data.gethIVDisclosureLocation().getName():"", 22);
            createStringCell(row, data.getDisclosureType()!=null?data.getDisclosureType().getName():"", 23);
            createStringCell(row, data.getIsKeypopulation()!=null?data.getIsKeypopulation().getName():"", 24);
            createStringCell(row, data.getKeyPopulation()!=null?data.getKeyPopulation().getName():"", 25);
            createStringCell(row, data.getHaveBirthCertificate()!=null?data.getHaveBirthCertificate().getName():"", 26);
            createStringCell(row, data.getDisability()!=null?data.getDisability().getName():"", 27);
            createStringCell(row, data.getDisablityType(), 28);
            createStringCell(row, data.getCat()!=null?data.getCat().getName():"", 29);
            createStringCell(row, data.getYoungMumGroup()!=null?data.getYoungMumGroup().getName():"", 30);
            createStringCell(row, data.getYoungDadGroup()!=null?data.getYoungDadGroup().getName() : "", 31);
            createStringCell(row, data.getTransmissionMode()!=null?data.getTransmissionMode().getName():"", 32);
            createStringCell(row, data.getArtRegimen(), 33);
            createStringCell(row, data.getHivStatusKnown()!=null?data.getHivStatusKnown().getName():"", 34);
            createStringCell(row, data.getStatus().getName(), 35);
            if(data.getLastVl()!=null && data.getLastVl().getResult()!=null){
                createNumericCell(row, data.getLastVl().getResult().doubleValue(), 36);
            }else{
                createBlankCell(row,36);
            }
            createStringCell(row, data.getLastVl()!=null?data.getLastVl().getTnd():"", 37);
            createStringCell(row, data.getLastVl()!=null && data.getLastVl().getDateTaken()!=null?data.getLastVl().getDateTaken().toString():"", 38);

            //writing contacts for the client
            data.getContacts().stream().forEach(contact -> {
                if(contactPage==0 && contactCurrRow>1400000){
                    System.err.println("^^^^^^^^^^^^^^^^^^ Now creating another sheet for contacts ^^^^^^^^^^^^^^^^^^");
                    contactSheet1=workbook.createSheet("contacts-2");
                    addHeaders(contactSheet1, DatabaseHeader.CONTACT_HEADER, 0);
                    contactSheet1.setDefaultColumnWidth(20);
                    contactPage++;
                }
                contactCurrRow++;
                Row contactRow = (contactCurrRow<=1400000)?contactSheet.createRow(contactCurrRow):contactSheet1.createRow((++contactCurrRow-1400000)+1);
                createStringCell(contactRow, contact.getPatient().getPatientNumber(), 0);
                createStringCell(contactRow, contact.getPatient().getName(), 1);
                createStringCell(contactRow, contact.getPatient().getDateOfBirth() != null ? contact.getPatient().getDateOfBirth().toString() : "", 2);
                createNumericCell(contactRow, (double) contact.getPatient().getAge(), 3);
                createStringCell(contactRow, contact.getPatient().getGender() != null ? contact.getPatient().getGender().getName() : "", 4);
                createStringCell(contactRow, contact.getPatient().getPrimaryClinic().getDistrict().getProvince().getName(), 5);
                createStringCell(contactRow, contact.getPatient().getPrimaryClinic().getDistrict().getName(), 6);
                createStringCell(contactRow, contact.getPatient().getPrimaryClinic().getName(), 7);
                createStringCell(contactRow, contact.getDateCreated() != null ? contact.getDateCreated().toString() : "", 8);
                createStringCell(contactRow, contact.getContactDate() != null ? contact.getContactDate().toString() : "", 9);
                createStringCell(contactRow, contact.getCareLevel() != null ? contact.getCareLevel().getName() : "", 10);
                createStringCell(contactRow, contact.getLocation() != null ? contact.getLocation().getName() : "", 11);
                createStringCell(contactRow, contact.getPosition() != null ? contact.getPosition().getName() : "", 12);
                createStringCell(contactRow, contact.getCareLevelAfterAssessment() != null ? contact.getCareLevelAfterAssessment().getName() : "", 13);
                createStringCell(contactRow, contact.getLastClinicAppointmentDate() != null ? contact.getLastClinicAppointmentDate().toString() : "", 14);
                createStringCell(contactRow, contact.getAttendedClinicAppointment() != null ? contact.getAttendedClinicAppointment().getName() : "", 15);
                createStringCell(contactRow, contact.getNextClinicAppointmentDate() != null ? contact.getNextClinicAppointmentDate().toString() : "", 16);
                createStringCell(contactRow, contact.getContactMadeBy(), 17);
                createStringCell(contactRow, contact.getEac() != null ? contact.getEac().getName() : "", 18);
                if (contact.getEac1() != null && contact.getEac1() == 1) {
                    createNumericCell(contactRow, Integer.valueOf(1).doubleValue(), 19);
                } else if (contact.getEac2() != null && contact.getEac2() == 1) {
                    createNumericCell(contactRow, Integer.valueOf(2).doubleValue(), 19);
                } else if (contact.getEac3() != null && contact.getEac3() == 1) {
                    createNumericCell(contactRow, Integer.valueOf(3).doubleValue(), 19);
                } else {
                    createBlankCell(contactRow, 19);
                }
                createStringCell(contactRow, contact.getPatient().getCat() != null ? contact.getPatient().getCat().getName() : "", 20);
                createStringCell(contactRow, contact.getPatient().getYoungMumGroup() != null ? contact.getPatient().getYoungMumGroup().getName() : "", 21);
                createStringCell(contactRow, contact.getPatient().getYoungDadGroup() != null ? contact.getPatient().getYoungDadGroup().getName() : "", 22);
            });

            //writing referrals for the client
            data.getReferrals().stream().forEach(referral -> {
                Row referralRow = referralSheet.createRow(++referralCurrRow);
                createStringCell(referralRow, referral.getPatient().getPatientNumber(), 0);
                createStringCell(referralRow, referral.getPatient().getName(), 1);
                createStringCell(referralRow, referral.getPatient().getDateOfBirth()!=null?referral.getPatient().getDateOfBirth().toString():"", 2);
                createNumericCell(referralRow, (double) referral.getPatient().getAge(), 3);
                createStringCell(referralRow, referral.getPatient().getGender()!=null?referral.getPatient().getGender().getName():"", 4);
                createStringCell(referralRow, referral.getPatient().getPrimaryClinic().getDistrict().getProvince().getName(), 5);
                createStringCell(referralRow, referral.getPatient().getPrimaryClinic().getDistrict().getName(), 6);
                createStringCell(referralRow, referral.getPatient().getPrimaryClinic().getName(), 7);
                createStringCell(referralRow, referral.getDateCreated()!=null?referral.getDateCreated().toString():"", 8);
                createStringCell(referralRow, referral.getReferralDate()!=null?referral.getReferralDate().toString():"", 9);
                createStringCell(referralRow, referral.getExpectedVisitDate()!=null?referral.getExpectedVisitDate().toString():"", 10);
                createStringCell(referralRow, referral.getOrganisation()!=null?referral.getOrganisation():"", 11);
                createStringCell(referralRow, referral.getDesignation()!=null?referral.getDesignation():"", 12);
                createStringCell(referralRow, referral.getAttendingOfficer()!=null?referral.getAttendingOfficer():"", 13);
                createStringCell(referralRow, referral.getDateAttended()!=null?referral.getDateAttended().toString():"", 14);
                createStringCell(referralRow, referral.getActionTaken()!=null?referral.getActionTaken().getName():"", 15);
                createStringCell(referralRow, referral.getHivStiServicesReq()!=null?referral.getHivStiServicesReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 16);
                createStringCell(referralRow, referral.getHivStiServicesAvailed()!=null?referral.getHivStiServicesAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 17);
                createStringCell(referralRow, referral.getOiArtReq()!=null?referral.getOiArtReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 18);
                createStringCell(referralRow, referral.getOiArtAvailed()!=null?referral.getOiArtAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 19);
                createStringCell(referralRow, referral.getSrhReq()!=null?referral.getSrhReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 20);
                createStringCell(referralRow, referral.getSrhAvailed()!=null?referral.getSrhAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 21);
                createStringCell(referralRow, referral.getLaboratoryReq()!=null?referral.getLaboratoryReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 22);
                createStringCell(referralRow, referral.getLaboratoryAvailed()!=null?referral.getLaboratoryAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 23);
                createStringCell(referralRow, referral.getTbReq()!=null?referral.getTbReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 24);
                createStringCell(referralRow, referral.getTbAvailed()!=null?referral.getTbAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 25);
                createStringCell(referralRow, referral.getPsychReq()!=null?referral.getPsychReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 26);
                createStringCell(referralRow, referral.getPsychAvailed()!=null?referral.getPsychAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 27);
                createStringCell(referralRow, referral.getLegalReq()!=null?referral.getLegalReq().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 28);
                createStringCell(referralRow, referral.getLegalAvailed()!=null?referral.getLegalAvailed().stream().map(service -> service.getName()).collect(Collectors.joining(",")):"", 29);

            });

            //writing vls for the client
            data.getInvestigationTests().stream().forEach(vl -> {
                Row vlRow = vlSheet.createRow(++viralCurrRow);
                createStringCell(vlRow, vl.getPatient().getPatientNumber(), 0);
                createStringCell(vlRow, vl.getPatient().getName(), 1);
                createStringCell(vlRow, vl.getPatient().getDateOfBirth()!=null?vl.getPatient().getDateOfBirth().toString():"", 2);
                createNumericCell(vlRow, (double) vl.getPatient().getAge(), 3);
                createStringCell(vlRow, vl.getPatient().getGender()!=null?vl.getPatient().getGender().getName():"", 4);
                createStringCell(vlRow, vl.getPatient().getPrimaryClinic().getDistrict().getProvince().getName(), 5);
                createStringCell(vlRow, vl.getPatient().getPrimaryClinic().getDistrict().getName(), 6);
                createStringCell(vlRow, vl.getPatient().getPrimaryClinic().getName(), 7);
                createStringCell(vlRow, vl.getDateCreated()!=null?vl.getDateCreated().toString():"", 8);
                createStringCell(vlRow, vl.getTestType()!=null?vl.getTestType().getName():"", 9);
                createStringCell(vlRow, vl.getDateTaken()!=null?vl.getDateTaken().toString():"", 10);
                createStringCell(vlRow, vl.getSource()!=null?vl.getSource().getName():"", 12);
                createStringCell(vlRow, vl.getNextTestDate()!=null?vl.getNextTestDate().toString():"", 13);
                createStringCell(vlRow, (vl.getResult()!=null&& vl.getResult()<1000) || (vl.getTnd()!=null)?"Suppressed":"Unsupressed", 14);
                if(vl.getResult()!=null){
                    createNumericCell(vlRow, vl.getResult().doubleValue(), 11);
                }else{
                    createBlankCell(vlRow, 11);
                }
                createStringCell(vlRow, vl.getTnd()!=null?vl.getTnd():"", 15);
                createStringCell(vlRow, vl.getPatient().getCat()!=null?vl.getPatient().getCat().getName():"", 16);
                createStringCell(vlRow, vl.getPatient().getYoungMumGroup()!=null?vl.getPatient().getYoungMumGroup().getName():"", 17);
                createStringCell(vlRow, vl.getPatient().getYoungDadGroup()!=null?vl.getPatient().getYoungDadGroup().getName():"", 18);
            });

            //writing TB Screening items for the client
            data.getTbIpts().stream().forEach(tb->{
                Row tbRow = tbSheet.createRow(++tbCurrRow);
                createStringCell(tbRow, tb.getPatient().getPatientNumber(), 0);
                createStringCell(tbRow, tb.getPatient().getName(), 1);
                createStringCell(tbRow, tb.getPatient().getDateOfBirth()!=null?tb.getPatient().getDateOfBirth().toString():"", 2);
                createNumericCell(tbRow, (double) tb.getPatient().getAge(), 3);
                createStringCell(tbRow, tb.getPatient().getGender()!=null?tb.getPatient().getGender().getName():"", 4);
                createStringCell(tbRow, tb.getPatient().getPrimaryClinic().getDistrict().getProvince().getName(), 5);
                createStringCell(tbRow, tb.getPatient().getPrimaryClinic().getDistrict().getName(), 6);
                createStringCell(tbRow, tb.getPatient().getPrimaryClinic().getName(), 7);
                createStringCell(tbRow, tb.getDateCreated()!=null?tb.getDateCreated().toString():"", 8);
                createStringCell(tbRow, tb.getScreenedForTb()!=null?tb.getScreenedForTb().getName():"", 9);
                createStringCell(tbRow, tb.getDateScreened()!=null?tb.getDateScreened().toString():"", 10);
                createStringCell(tbRow, tb.getIdentifiedWithTb()!=null?tb.getIdentifiedWithTb().getName():"", 11);
                createStringCell(tbRow, tb.getDateStartedTreatment()!=null?tb.getDateStartedTreatment().toString():"", 12);
                createStringCell(tbRow, tb.getReferredForIpt()!=null?tb.getReferredForIpt().getName():"", 13);
                createStringCell(tbRow, tb.getOnIpt()!=null?tb.getOnIpt().getName():"", 14);
                createStringCell(tbRow, tb.getDateStartedIpt()!=null?tb.getDateStartedIpt().toString():"", 15);
                createStringCell(tbRow, tb.getPatient().getCat()!=null?tb.getPatient().getCat().getName():"", 16);
                createStringCell(tbRow, tb.getPatient().getYoungMumGroup()!=null?tb.getPatient().getYoungMumGroup().getName():"", 17);
                createStringCell(tbRow, tb.getPatient().getYoungDadGroup()!=null?tb.getPatient().getYoungDadGroup().getName():"", 18);
            });

            //writing MH Screening items for the client
            data.getMentalHealthScreenings().stream().forEach(mh->{
                Row mhRow = mhSheet.createRow(++mhCurrRow);
                createStringCell(mhRow, mh.getPatient().getPatientNumber(), 0);
                createStringCell(mhRow, mh.getPatient().getName(), 1);
                createStringCell(mhRow, mh.getPatient().getDateOfBirth()!=null?mh.getPatient().getDateOfBirth().toString():"", 2);
                createNumericCell(mhRow, (double) mh.getPatient().getAge(), 3);
                createStringCell(mhRow, mh.getPatient().getGender()!=null?mh.getPatient().getGender().getName():"", 4);
                createStringCell(mhRow, mh.getPatient().getPrimaryClinic().getDistrict().getProvince().getName(), 5);
                createStringCell(mhRow, mh.getPatient().getPrimaryClinic().getDistrict().getName(), 6);
                createStringCell(mhRow, mh.getPatient().getPrimaryClinic().getName(), 7);
                createStringCell(mhRow, mh.getDateCreated()!=null?mh.getDateCreated().toString():"", 8);
                createStringCell(mhRow, mh.getScreenedForMentalHealth()!=null?mh.getScreenedForMentalHealth().getName():"", 9);
                createStringCell(mhRow, mh.getDateScreened()!=null?mh.getDateScreened().toString():"", 10);
                createStringCell(mhRow, mh.getRisk()!=null?mh.getRisk().getName():"", 11);
                createStringCell(mhRow, mh.getIdentifiedRisks().stream().map(r->r.getName()).collect(Collectors.joining(",")), 12);
                createStringCell(mhRow, mh.getSupport()!=null?mh.getSupport().getName():"", 13);
                createStringCell(mhRow, mh.getSupports().stream().map(s->s.getName()).collect(Collectors.joining(",")), 14);
                createStringCell(mhRow, mh.getReferral()!=null?mh.getReferral().getName():"", 15);
                createStringCell(mhRow, mh.getPatient().getCat()!=null?mh.getPatient().getCat().getName():"", 16);
                createStringCell(mhRow, mh.getPatient().getYoungMumGroup()!=null?mh.getPatient().getYoungMumGroup().getName():"", 17);
                createStringCell(mhRow, mh.getPatient().getYoungDadGroup()!=null?mh.getPatient().getYoungDadGroup().getName():"", 18);

            });

        });

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




}