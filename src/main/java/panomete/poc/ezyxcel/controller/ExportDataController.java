package panomete.poc.ezyxcel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import panomete.poc.ezyxcel.entity.AccessPage;
import panomete.poc.ezyxcel.entity.Role;
import panomete.poc.ezyxcel.service.ExcelReaderService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/mock")
public class ExportDataController {

    @Value("${datasource.mock}")
    String excelUrl;

    @Autowired
    private ExcelReaderService  excelReaderService;

    private final WebClient webClient;

    public ExportDataController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("role")
    public Mono<List<Role>> getRoleData() {
        if (excelUrl == null || excelUrl.trim().isEmpty()) {
            return Mono.just(new ArrayList<>());
        }

        log.info("Attempting to download Excel from URL: {}", excelUrl);

        Flux<DataBuffer> excelFileStream = webClient.get()
                .uri(URI.create(excelUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.info("WebClient error during download: {}",e.getMessage()));

        return excelReaderService.getAllRoles(excelFileStream)
                .map(rowDataList -> {
                    rowDataList.forEach(System.out::println); // Print each row data to console
                    return rowDataList; // Return list as JSON
                })
                .onErrorResume(RuntimeException.class, e -> Mono.just(new ArrayList<>()));
    }

    @GetMapping("access-page")
    public Mono<List<AccessPage>> getAccessPage() {
        if (excelUrl == null || excelUrl.trim().isEmpty()) {
            return Mono.just(new ArrayList<>());
        }

        log.info("Attempting to download Excel from URL: {}", excelUrl);

        Flux<DataBuffer> excelFileStream = webClient.get()
                .uri(URI.create(excelUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.info("WebClient error during download: {}",e.getMessage()));

        return excelReaderService.getAccessPage(excelFileStream)
                .map(rowDataList -> {
                    rowDataList.forEach(System.out::println); // Print each row data to console
                    return rowDataList; // Return list as JSON
                })
                .onErrorResume(RuntimeException.class, e -> Mono.just(new ArrayList<>()));
    }

}
