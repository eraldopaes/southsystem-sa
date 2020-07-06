package br.com.southsystem.service;

import br.com.southsystem.domain.Client;
import br.com.southsystem.domain.Sale;
import br.com.southsystem.domain.Salesman;
import br.com.southsystem.mapper.ClientMapper;
import br.com.southsystem.mapper.SaleMapper;
import br.com.southsystem.mapper.SalesmanMapper;
import br.com.southsystem.storage.LocalStorage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.southsystem.utils.Constants.*;

@Service
public class FileProcessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessService.class);

    private final LocalStorage localStorage;
    private final SaleMapper saleMapper;
    private final ClientMapper clientMapper;
    private final SalesmanMapper salesmanMapper;

    @Autowired
    public FileProcessService(LocalStorage localStorage,
                              ClientMapper clientMapper,
                              SalesmanMapper salesmanMapper,
                              SaleMapper saleMapper) {
        this.localStorage = localStorage;
        this.clientMapper = clientMapper;
        this.salesmanMapper = salesmanMapper;
        this.saleMapper = saleMapper;
    }

    @Async
    public void readFile(File file) {

        Integer numberOfClients = 0;
        Integer numberOfSalesman = 0;

        BigDecimal biggestSale = BigDecimal.ZERO;
        String biggestSaleId = "";

        BigDecimal slowestSale = BigDecimal.valueOf(Long.MAX_VALUE);
        String worstSaleName = "";

        try (InputStream is = new FileInputStream(file)) {

            List<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList());

            for (String line : lines) {

                if (line.startsWith(PREFIX_SALESMAN)) {

                    Salesman salesman = salesmanMapper.lineToSalesman(line);
                    numberOfSalesman++;
                    LOGGER.info("Vendedor processado: {}", salesman);

                } else if (line.startsWith(PREFIX_CLIENT)) {

                    Client client = clientMapper.lineToClient(line);
                    numberOfClients++;
                    LOGGER.info("Cliente processado: {}", client);

                } else if (line.startsWith(PREFIX_SALE)) {

                    Sale sale = saleMapper.lineToSale(line);
                    BigDecimal totalSale = calculeTotalSale(sale);
                    if (totalSale.compareTo(biggestSale) >= 0) {
                        biggestSale= totalSale;
                        biggestSaleId = sale.getId();
                    }
                    if (totalSale.compareTo(slowestSale) <= 0) {
                        slowestSale = totalSale;
                        worstSaleName = sale.getSalesmanName();
                    }
                    LOGGER.info("Venda processado: {}", sale);

                } else {
                    LOGGER.error("Entrada não esperada");
                    throw new RuntimeException("Entrada não esperada");
                }

            }

            writeFile(file, numberOfClients, numberOfSalesman, biggestSaleId, worstSaleName);
            localStorage.delete(file, DATA_INPUT_PATH);

        } catch (Exception e) {
            localStorage.renameFile(file.getName(), file.getName() + ERROR_EXTENSION, DATA_INPUT_PATH);
            throw new RuntimeException(e.getMessage());
        }
    }

    private BigDecimal calculeTotalSale(Sale sale) {
        return sale.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private void writeFile(File file, Integer numberOfClients, Integer numberOfSalesman, String biggestSaleId, String worstSaleName) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(System.getenv(HOME_PATH) + DATA_OUTPUT_PATH + renameToProcessedFilename(file.getName())));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(CLIENTS, SALESMAN, ID_BIGGEST_SALE, WORST_SALESMAN));
        csvPrinter.printRecord(numberOfClients, numberOfSalesman, biggestSaleId, worstSaleName);
        csvPrinter.flush();
    }

    private String renameToProcessedFilename(String filename) {
        String filenameWithoutExtension = filename.replace(DAT_EXTENSION, "");
        return filenameWithoutExtension + DONE_EXTENSION;
    }
}
