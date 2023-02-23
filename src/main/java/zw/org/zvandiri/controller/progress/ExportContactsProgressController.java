package zw.org.zvandiri.controller.progress;

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

    @GetMapping("/index")
    public double getProgress(){
        double progress=Math.round((ExportDatabaseVariables.progress>0 && ExportDatabaseVariables.count>0)?(double)((ExportDatabaseVariables.progress/ExportDatabaseVariables.count)*100):0);
        return progress;
    }
}
