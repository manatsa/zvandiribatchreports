package zw.org.zvandiri.business.domain.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import zw.org.zvandiri.business.domain.Patient;

import java.io.Serializable;
import java.util.List;

/**
 * @author :: codemaster
 * created on :: 28/9/2022
 * Package Name :: zw.org.zvandiri.business.domain.dto
 */

@Data
public class CaseloadManagementDTO implements Serializable {

    List<Patient> patients;

    List<Patient> invalidVLs;

    List<Patient>  enhancedPatients;

    List<Patient> mhPatients;

    List<Patient> tbPatients;

}
