package zw.org.zvandiri.batch.listeners;

/**
 * @author :: codemaster
 * created on :: 2/10/2022
 * Package Name :: zw.org.zvandiri.batch
 */

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zw.org.zvandiri.business.domain.User;
import zw.org.zvandiri.business.service.UserService;
import zw.org.zvandiri.controller.progress.variables.ExportDatabaseVariables;
import zw.org.zvandiri.controller.progress.variables.ExportPatientsVariables;

/**
 * Log the count of items processed at a specified interval.
 *
 * @author Jeremy Yearron
 *
 */
@Component
@Slf4j
public class PatientChunckListener implements ChunkListener{

    //private static final Logger log = LogManager.getLogger(ExportDatabaseChunckListener.class);

    private int loggingInterval =500;
    @Autowired
    ExportPatientsVariables exportPatientsVariables;
    User user;

    @Override
    public void beforeChunk(ChunkContext context) {
        // Nothing to do here
    }

    @Override
    public void afterChunk(ChunkContext context) {

        int count = context.getStepContext().getStepExecution().getReadCount();
        exportPatientsVariables.setProgress(count);
//        ExportDatabaseVariables.progress=Double.valueOf(count).doubleValue();

        // If the number of records processed so far is a multiple of the logging interval then output a log message.
        if (count > 0 && count % loggingInterval == 0) {
            log.warn("+++++++++++ Client Detailed Report +++++++ User:: {} of District:: {} <=>  Processed {} records so far",
                    getUser().getUserName(),
                    getUser().getUserLevel().getCode()==1?"NATIONAL":
                    getUser().getUserLevel().getCode()==2?"PROVINCIAL":
                    getUser().getUserLevel().getCode()==3?getUser().getDistrict().getName():"UNKNOWN", count); ;
        }
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        // Nothing to do here
    }

    public void setLoggingInterval(int loggingInterval) {
        this.loggingInterval = loggingInterval;
    }

    public Integer getLoggingInterval(){
        return this.loggingInterval;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ExportPatientsVariables getExportPatientsVariables() {
        return exportPatientsVariables;
    }

    public void setExportPatientsVariables(ExportPatientsVariables exportPatientsVariables) {
        this.exportPatientsVariables = exportPatientsVariables;
    }
}