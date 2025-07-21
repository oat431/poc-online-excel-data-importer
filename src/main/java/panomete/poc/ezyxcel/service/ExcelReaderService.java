package panomete.poc.ezyxcel.service;

import org.springframework.core.io.buffer.DataBuffer;
import panomete.poc.ezyxcel.entity.AccessPage;
import panomete.poc.ezyxcel.entity.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;

public interface ExcelReaderService {
    Mono<List<Role>> getAllRoles(Flux<DataBuffer> inputStream);
    Mono<List<AccessPage>> getAccessPage(Flux<DataBuffer> inputStream);

}
