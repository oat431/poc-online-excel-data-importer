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

    @GetMapping
    public Mono<String> downloadExcelRaw() {
        if (excelUrl == null || excelUrl.trim().isEmpty()) {
            return Mono.just("Please provide an Excel file URL.");
        }

        log.info("Attempting to download Excel from URL: {}", excelUrl);

        Flux<DataBuffer> excelFileStream = webClient.get()
                .uri(URI.create(excelUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/octet-stream")
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.info("WebClient error during download: {}",e.getMessage()));

        return excelReaderService.getAllSheetName(excelFileStream)
                .map(sheetNames -> {
                    System.out.println("Successfully retrieved sheet names: " + sheetNames);
                    return "Successfully retrieved sheet names: " + String.join(", ", sheetNames);
                })
                .onErrorResume(RuntimeException.class, e -> {
                    System.err.println("Error in getExcelSheetNames: " + e.getMessage());
                    return Mono.just("Failed to get sheet names: " + e.getMessage());
                });
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

    @GetMapping("/role2")
    public Mono<List<Role>> readExcelFromResources() { // Default sheet name

        try {
            // Load the file from the resources folder
            ClassPathResource resource = new ClassPathResource("MockupCheckRoom.xlsx");
            if (!resource.exists()) {
                return Mono.just(new ArrayList<>());
            }
            InputStream inputStream = resource.getInputStream(); // Get the InputStream

            // Call the service to read data from the InputStream
            return excelReaderService.readSheetDataFromStream(inputStream)
                    .map(rowDataList -> {
                        System.out.println("Successfully read " + rowDataList.size() + " rows from resource sheet '" + "Role" + "'.");
                        rowDataList.forEach(System.out::println);
                        return rowDataList;
                    });
        } catch (IOException e) {
            return Mono.just(new ArrayList<>());
        }
    }
}
