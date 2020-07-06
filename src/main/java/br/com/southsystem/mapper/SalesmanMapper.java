package br.com.southsystem.mapper;

import br.com.southsystem.domain.Salesman;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SalesmanMapper {

    public static final String TOKENIZER = "ç";

    public Salesman lineToSalesman(String line) {
        String[] lineSplited = line.split(TOKENIZER);
        return new Salesman(lineSplited[1], lineSplited[2], new BigDecimal(lineSplited[3]));
    }
}
