package com.comisiones.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class DaoResourceManagementTest {

    private static final Path DAO_DIRECTORY = Path.of("src/main/java/com/comisiones/dao");
    private static final Pattern JDBC_MANUAL_CLOSE_PATTERN = Pattern.compile(
            "\\b(?:conn|connection|stmt|statement|pstmt|preparedStatement|rs|resultSet)\\s*\\.\\s*close\\s*\\(");
    private static final Pattern MANUAL_CONNECTION_ACQUISITION_PATTERN = Pattern.compile(
            "(?m)^\\s*Connection\\s+\\w+\\s*=\\s*DBUtil\\.getConnection\\(\\)\\s*;");

    @Test
    void daoClassesDoNotUseManualCloseOrFinallyCleanup() throws IOException {
        for (Path daoFile : getDaoFiles()) {
            String source = Files.readString(daoFile);
            assertFalse(JDBC_MANUAL_CLOSE_PATTERN.matcher(source).find(),
                    () -> daoFile.getFileName() + " should rely on try-with-resources instead of manual close()");
            assertFalse(source.contains("finally"),
                    () -> daoFile.getFileName() + " should not use finally for JDBC resource cleanup");
        }
    }

    @Test
    void daoClassesDoNotManuallyAcquireConnectionsOutsideTryWithResources() throws IOException {
        for (Path daoFile : getDaoFiles()) {
            String source = Files.readString(daoFile);
            assertFalse(MANUAL_CONNECTION_ACQUISITION_PATTERN.matcher(source).find(),
                    () -> daoFile.getFileName() + " should open connections inside try-with-resources");
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
