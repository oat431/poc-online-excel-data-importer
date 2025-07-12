package panomete.poc.ezyxcel.service;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExcelReaderService {
    Mono<?> readExcelData(Flux<DataBuffer> flux);
}
