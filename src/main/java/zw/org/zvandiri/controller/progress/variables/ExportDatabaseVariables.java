package zw.org.zvandiri.controller.progress.variables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author :: codemaster
 * created on :: 6/10/2022
 * Package Name :: zw.org.zvandiri.controller.progress.variables
 */

@NoArgsConstructor
@AllArgsConstructor
public class ExportDatabaseVariables {
    public  double progress=0.0;
    public  double count=0.0;

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }
}
