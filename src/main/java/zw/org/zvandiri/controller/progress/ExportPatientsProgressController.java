package zw.org.zvandiri.controller.progress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;
import zw.org.zvandiri.controller.progress.variables.ExportPatientsVariables;

/**
 * @author :: codemaster
 * created on :: 6/10/2022
 * Package Name :: zw.org.zvandiri.controller.progress
 */

@RestController
@RequestMapping("/report/export-patients/progress")
public class ExportPatientsProgressController {

    @Autowired
    ExportPatientsVariables exportPatientsVariables;

    @GetMapping("/index")
    public double getProgress(){
        double progress=Math.round((exportPatientsVariables.getProgress()>0 && exportPatientsVariables.getCount()>0)?(double)((exportPatientsVariables.getProgress()/exportPatientsVariables.getCount())*100):0);
        return progress;
    }
}
