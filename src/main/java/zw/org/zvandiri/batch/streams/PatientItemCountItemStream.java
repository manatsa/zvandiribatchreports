package zw.org.zvandiri.batch.streams;

/**
 * @author :: codemaster
 * created on :: 2/10/2022
 * Package Name :: zw.org.zvandiri.batch.streams
 */

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;

public class PatientItemCountItemStream implements ItemStream {

    public void open(ExecutionContext executionContext) throws ItemStreamException {
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("ItemCount: "+executionContext.get("ItemReader.read.count"));
    }

    public void close() throws ItemStreamException {
    }
}