package com.noloverme.npromo.data;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface Database {
    void init() throws SQLException;
    void close();
    void activateCode(UUID uuid, String ip, String code);
    boolean hasActivatedCode(UUID uuid, String code);
    boolean hasActivatedAnyCode(UUID uuid);
    boolean hasIpActivatedAnyCode(String ip);
    int getCodeActivations(String code);
    List<String> getActivatedCodes(UUID uuid);
    void importData(String fileName);
    void exportData(String fileName);
}
