package zw.org.zvandiri.business.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import zw.org.zvandiri.business.domain.District;
import zw.org.zvandiri.business.domain.Facility;
import zw.org.zvandiri.business.domain.Province;
import zw.org.zvandiri.business.domain.util.PatientChangeEvent;
import zw.org.zvandiri.business.util.dto.SearchDTO;

import java.util.Date;
import java.util.List;

/**
 * @author :: codemaster
 * created on :: 8/10/2022
 * Package Name :: zw.org.zvandiri.business.domain.dto
 */


@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DataDTO {
    private List<Province> provinces;
    private List<District> districts;
    private List<Facility> facilities;
    private List<PatientChangeEvent> statuses;
    private Date startDate;
    private Date endDate;

    public SearchDTO toSearchDTO(){
        SearchDTO searchDTO= new SearchDTO();
        searchDTO.setFacilities(this.facilities);
        searchDTO.setDistricts(this.districts);
        searchDTO.setProvinces(this.provinces);
        searchDTO.setStartDate(this.startDate);
        searchDTO.setEndDate(this.endDate);
        return searchDTO;
    }

    public List<Province> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<Province> provinces) {
        this.provinces = provinces;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }

    public List<Facility> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<Facility> facilities) {
        this.facilities = facilities;
    }

    public List<PatientChangeEvent> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<PatientChangeEvent> statuses) {
        this.statuses = statuses;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
