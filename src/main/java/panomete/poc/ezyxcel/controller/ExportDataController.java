package panomete.poc.ezyxcel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/v1/mock")
public class ExportDataController {

    @Value("${datasource.mock}")
    String datasourceMock;

    private final WebClient webClient;

    public ExportDataController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping
    public Mono<String> downloadExcelRaw() {
        String excelUrl = datasourceMock;
        if (excelUrl == null || excelUrl.trim().isEmpty()) {
            return Mono.just("Please provide an Excel file URL.");
        }

        log.info("Attempting to download Excel from URL: {}", excelUrl);

        return webClient.get()
                .uri(URI.create(excelUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.info("WebClient error during download: {}",e.getMessage()))
                .collectList()
                .map(dataBuffers -> {
                    DataBuffer joinedBuffer = dataBuffers.stream()
                            .reduce(DataBuffer::write)
                            .orElse(null);
                    if (joinedBuffer != null) {
                        int byteCount = joinedBuffer.readableByteCount();
                        DataBufferUtils.release(joinedBuffer);
                        log.info("Successfully downloaded Excel file. Total bytes: {}", byteCount);
                        return "Successfully downloaded Excel file. Total bytes: " + byteCount;
                    } else {
                        log.info("Downloaded empty content from URL.");
                        return "Downloaded empty content.";
                    }
                })
                .onErrorResume(RuntimeException.class, e -> Mono.just("Failed to download or process file: " + e.getMessage()));
    }
}
