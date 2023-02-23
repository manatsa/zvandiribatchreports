package zw.org.zvandiri.batch.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.service.UserService;
import zw.org.zvandiri.controller.progress.variables.ExportCaseloadVariables;
import zw.org.zvandiri.controller.progress.variables.ExportContactsVariables;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;
import zw.org.zvandiri.controller.progress.variables.ExportPatientsVariables;

/**
 * @author :: codemaster
 * created on :: 4/10/2022
 * Package Name :: zw.org.zvandiri.batch.configs
 */

@Configuration
public class BatchConfiguration {

    @Bean("exportDatabaseVars")
    @SessionScope
    public ExportDatabaseVariables exportDatabaseVariables(){
        return new ExportDatabaseVariables();
    }

    @Bean("exportContactVars")
    @SessionScope
    public ExportContactsVariables exportContactsVariables(){
        return new ExportContactsVariables();
    }

    @Bean("exportPatientVars")
    @SessionScope
    public ExportPatientsVariables exportPatientsVariables(){
        return new ExportPatientsVariables();
    }

    @Bean("exportCaseloadVars")
    @SessionScope
    public ExportCaseloadVariables exportCaseloadVariables(){
        return new ExportCaseloadVariables();
    }


}
