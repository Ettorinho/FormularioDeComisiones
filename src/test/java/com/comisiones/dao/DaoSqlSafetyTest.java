package com.comisiones.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class DaoSqlSafetyTest {

    private static final Path DAO_DIRECTORY = Path.of("src/main/java/com/comisiones/dao");

    @Test
    void daoClassesDoNotUseCreateStatement() throws IOException {
        for (Path daoFile : getDaoFiles()) {
            String source = Files.readString(daoFile);
            assertFalse(source.contains("createStatement("),
                    () -> daoFile.getFileName() + " should use PreparedStatement instead of createStatement()");
        }
    }

    @Test
    void daoSqlDefinitionsDoNotUsePlusConcatenation() throws IOException {
        for (Path daoFile : getDaoFiles()) {
            List<String> sqlDefinitionLines = Files.readAllLines(daoFile).stream()
                    .filter(line -> line.contains("String sql =") || line.contains("String query ="))
                    .collect(Collectors.toList());

            for (String line : sqlDefinitionLines) {
                assertFalse(line.contains("+"),
                        () -> daoFile.getFileName() + " should not concatenate SQL with '+': " + line.trim());
            }
        }
    }

    private List<Path> getDaoFiles() throws IOException {
        try (var stream = Files.list(DAO_DIRECTORY)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith("DAO.java"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
