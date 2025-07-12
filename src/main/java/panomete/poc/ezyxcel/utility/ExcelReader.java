package panomete.poc.ezyxcel.utility;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelReader<T> implements ReadListener<T> {
    private static final int BATCH_COUNT = 100;
    private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    @Getter
    private final List<T> allData = new ArrayList<>();

    @Override
    public void invoke(T data, AnalysisContext context) {
        log.debug("Parsed data: {}", data);
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData(); // Process any remaining data
        log.info("All Excel data analysis finished for sheet.");
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        log.error("Error during Excel parsing: {}", exception.getMessage(), exception);
        throw new RuntimeException("Failed to parse Excel file row: " + exception.getMessage(), exception);
    }

    private void saveData() {
        log.debug("{} pieces of data were saved to list. Total processed so far: {}", cachedDataList.size(), allData.size() + cachedDataList.size());
        allData.addAll(cachedDataList);
    }

}
