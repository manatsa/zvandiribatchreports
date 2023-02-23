package zw.org.zvandiri.controller.progress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.org.zvandiri.controller.progress.variables.ExportCaseloadVariables;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

/**
 * @author :: codemaster
 * created on :: 6/10/2022
 * Package Name :: zw.org.zvandiri.controller.progress
 */

@RestController
@RequestMapping("/report/export-caseload/progress")
public class ExportCaseloadProgressController {

    @Autowired
    ExportCaseloadVariables exportCaseloadVariables;
    @GetMapping("/index")
    public double getProgress(){
       double progress=Math.round((exportCaseloadVariables.getProgress()>0 && exportCaseloadVariables.getCount()>0)?(double)((exportCaseloadVariables.getProgress()/exportCaseloadVariables.getCount())*100):0);
        return progress;
    }
}
