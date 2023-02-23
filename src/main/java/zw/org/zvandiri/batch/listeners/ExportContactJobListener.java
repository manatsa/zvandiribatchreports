package zw.org.zvandiri.batch.listeners;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import zw.org.zvandiri.controller.progress.variables.ExportContactsVariables;

/**
 * @author :: codemaster
 * created on :: 7/10/2022
 * Package Name :: zw.org.zvandiri.batch.listeners
 */

public class ExportContactJobListener implements JobExecutionListener {
    ExportContactsVariables exportContactsVariables;

    public ExportContactsVariables getExportContactsVariables() {
        return exportContactsVariables;
    }

    public void setExportContactsVariables(ExportContactsVariables exportContactsVariables) {
        this.exportContactsVariables = exportContactsVariables;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        exportContactsVariables.setProgress(exportContactsVariables.getCount());
    }
}
