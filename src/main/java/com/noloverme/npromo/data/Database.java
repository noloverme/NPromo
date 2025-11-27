package com.noloverme.npromo.data;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {

    void init() throws SQLException;

    void close();

    CompletableFuture<Void> activateCode(UUID uuid, String ip, String code);

    CompletableFuture<Boolean> hasActivatedCode(UUID uuid, String code);

    CompletableFuture<Boolean> hasActivatedAnyCode(UUID uuid);

    CompletableFuture<Boolean> hasIpActivatedAnyCode(String ip);

    CompletableFuture<Integer> getCodeActivations(String code);

    CompletableFuture<List<String>> getActivatedCodes(UUID uuid);

    void importData(String fileName);

    void exportData(String fileName);
}
