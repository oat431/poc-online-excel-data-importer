package panomete.poc.ezyxcel.service;

import com.alibaba.excel.EasyExcel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ExcelReaderServiceImpl implements ExcelReaderService {
    @Override
    public Mono<?> readExcelData(Flux<DataBuffer> dataBuffers) {
        return DataBufferUtils.join(dataBuffers) // Collect all DataBuffers into a single DataBuffer
                .flatMap(dataBuffer -> Mono.fromCallable(() -> {
                            // Get the byte array from the DataBuffer
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer); // Release the DataBuffer to prevent memory leaks

                            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                                // EasyExcel is a blocking operation, so run it on a dedicated scheduler
                                EasyExcel.read(inputStream).sheet().doRead();
                            } catch (Exception e) {
                                throw new IOException("Failed to read Excel data with EasyExcel: " + e.getMessage(), e);
                            }
                            return null;
                        })
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
