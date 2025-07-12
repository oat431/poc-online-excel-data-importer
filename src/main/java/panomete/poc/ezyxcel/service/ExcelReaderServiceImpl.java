package panomete.poc.ezyxcel.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import panomete.poc.ezyxcel.entity.Role;
import panomete.poc.ezyxcel.utility.ExcelReader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelReaderServiceImpl implements ExcelReaderService {

    @Override
    public Mono<List<String>> getAllSheetName(Flux<DataBuffer> excelFileBuffers) {
        return DataBufferUtils.join(excelFileBuffers)
                .flatMap(dataBuffer -> Mono.fromCallable(() -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            System.out.println("DataBuffer readableByteCount: " + dataBuffer.read());

                            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                                return EasyExcel.read(inputStream)
                                        .build()
                                        .excelExecutor()
                                        .sheetList()
                                        .stream()
                                        .map(ReadSheet::getSheetName)
                                        .collect(Collectors.toList());
                            } catch (Exception e) {
                                throw new IOException("Failed to get sheet names from Excel file: " + e.getMessage(), e);
                            }
                        })
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<List<Role>> getAllRoles(Flux<DataBuffer> excelFileBuffers) {
        return DataBufferUtils.join(excelFileBuffers)
                .flatMap(dataBuffer -> Mono.fromCallable(() -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);

                            ExcelReader<Role> listener = new ExcelReader<>();
                            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                                EasyExcel.read(inputStream, Role.class, listener)
                                        .sheet("Role")
                                        .doRead();
                            } catch (Exception e) {
                                throw new IOException("Failed to read data from sheet '" + "Role" + "': " + e.getMessage(), e);
                            }
                            return listener.getAllData();
                        })
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<List<Role>> readSheetDataFromStream(InputStream inputStream) {
        return Mono.fromCallable(() -> {

                    ExcelReader<Role> listener = new ExcelReader<>();
                    try (InputStream is = inputStream) { // Use try-with-resources to ensure stream is closed
                        EasyExcel.read(is, Role.class, listener)
                                .sheet("Role")
                                .doRead();
                    } catch (Exception e) {
                        throw new IOException("Failed to read data from sheet '" + "Role" + "' (from InputStream): " + e.getMessage(), e);
                    }
                    return listener.getAllData();
                })
                .subscribeOn(Schedulers.boundedElastic()); // IMPORTANT: Offload blocking operation
    }
}
