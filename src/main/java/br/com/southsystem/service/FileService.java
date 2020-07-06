package br.com.southsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.southsystem.utils.Constants.*;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    private final FileProcessService fileProcessService;

    @Autowired
    public FileService(FileProcessService fileProcessService) {
        this.fileProcessService = fileProcessService;
    }

    @Scheduled(fixedDelay = 60000)
    public void findFilesToProcess() throws IOException {
        List<File> filesToProcess = Files.walk(Paths.get(System.getenv(HOME_PATH) + DATA_INPUT_PATH))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(DAT_EXTENSION))
                .limit(2)
                .collect(Collectors.toList());

        if (filesToProcess.isEmpty()) {
            LOGGER.info("Nenhum arquivo para processar!");
            return;
        }

        filesToProcess.forEach(file -> {
            LOGGER.info("Iniciando processamento assíncrono do arquivo {}", file.getName());
            fileProcessService.readFile(file);
        });
    }
}
