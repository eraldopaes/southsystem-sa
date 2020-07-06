package br.com.southsystem.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static br.com.southsystem.utils.Constants.*;
import static java.nio.file.FileSystems.getDefault;

@Component
public class LocalStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorage.class);

    private final Path inputPath;
    private final Path outputPath;

    public LocalStorage() {
        Path inputPath = getDefault().getPath(System.getenv(HOME_PATH), DATA_FOLDER, INPUT_FOLDER);
        Path outputPath = getDefault().getPath(System.getenv(HOME_PATH), DATA_FOLDER, OUTPUT_FOLDER);
        
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        
        createDirectories();
    }

    public void delete(File file, String path) {
        try {
            Files.delete(Paths.get(System.getenv(HOME_PATH) + path + file.getName()));
        } catch (IOException e) {
            LOGGER.error("Arquivo não existe!");
        }
    }

    public void renameFile(String filename, String newFilename, String path) {

        try {
            Path source = Paths.get(System.getenv(HOME_PATH) + path + filename);
            Files.move(source, source.resolveSibling(newFilename));
        } catch (IOException e) {
            LOGGER.error("Arquivo não existe!");
        }
    }

    private void createDirectories() {
        try {
            Files.createDirectories(this.inputPath);
            LOGGER.info("Pasta de input criada em: {}", this.inputPath);
            Files.createDirectories(this.outputPath);
            LOGGER.info("Pasta de output criada em: {}", this.outputPath);
        } catch (IOException e) {
            LOGGER.info("Ocorreu um erro ao criar pastas de input/output");
        }
    }
}
