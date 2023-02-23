package zw.org.zvandiri.controller.progress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;

/**
 * @author :: codemaster
 * created on :: 6/10/2022
 * Package Name :: zw.org.zvandiri.controller.progress
 */

@RestController
@RequestMapping("/report/export-database/progress")
public class ExportDatabaseProgressController {

    @Autowired
    ExportDatabaseVariables exportDatabaseVariables;

    @GetMapping("/index")
    public double getProgress(){
        double progress=Math.round((exportDatabaseVariables.getProgress()>0 && exportDatabaseVariables.getCount()>0)?(double)((exportDatabaseVariables.getProgress()/exportDatabaseVariables.getCount())*100):0);
        return progress;
    }
}
