package zw.org.zvandiri.business.domain.dto;

import lombok.Data;
import lombok.ToString;
import zw.org.zvandiri.business.domain.*;

import java.util.List;

/**
 * @author :: codemaster
 * created on :: 3/10/2022
 * Package Name :: zw.org.zvandiri.business.domain.dto
 */


public class ExportDatabaseDTO {

    List<Patient> patients;
    List<Contact> contacts;
    List<Referral> referrals;
    List<InvestigationTest> investigationTests;
    List<TbIpt> tbIpts;
    List<MentalHealthScreening> mentalHealthScreenings;

    public ExportDatabaseDTO() {
    }

    public ExportDatabaseDTO(List<Patient> patients, List<Contact> contacts, List<Referral> referrals, List<InvestigationTest> investigationTests, List<TbIpt> tbIpts, List<MentalHealthScreening> mentalHealthScreenings) {
        this.patients = patients;
        this.contacts = contacts;
        this.referrals = referrals;
        this.investigationTests = investigationTests;
        this.tbIpts = tbIpts;
        this.mentalHealthScreenings = mentalHealthScreenings;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Referral> getReferrals() {
        return referrals;
    }

    public void setReferrals(List<Referral> referrals) {
        this.referrals = referrals;
    }

    public List<InvestigationTest> getInvestigationTests() {
        return investigationTests;
    }

    public void setInvestigationTests(List<InvestigationTest> investigationTests) {
        this.investigationTests = investigationTests;
    }

    public List<TbIpt> getTbIpts() {
        return tbIpts;
    }

    public void setTbIpts(List<TbIpt> tbIpts) {
        this.tbIpts = tbIpts;
    }

    public List<MentalHealthScreening> getMentalHealthScreenings() {
        return mentalHealthScreenings;
    }

    public void setMentalHealthScreenings(List<MentalHealthScreening> mentalHealthScreenings) {
        this.mentalHealthScreenings = mentalHealthScreenings;
    }
}
