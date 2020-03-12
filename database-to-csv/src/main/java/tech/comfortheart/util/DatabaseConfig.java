package tech.comfortheart.util;

import tech.comfortheart.DatabaseToCsvApp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getSimpleName());
    private String jdbcUrl;
    private String username;
    private String password;
    private String databaseType;
    private String csvLocation;
    private List<Table> tables;

    public void setVariable(String key, String value) {
        if (notEmpty(key) && notEmpty(value)) {
            key = key.trim();
            value = value.trim();
            switch (key.trim()) {
                case "JDBC_URL":
                    setJdbcUrl(value);
                    break;
                case "USERNAME":
                    setUsername(value);
                    break;
                case "PASSWORD":
                    setPassword(value);
                    break;
                case "DB_TYPE":
                    setDatabaseType(value);
                    break;
                case "CSV_LOCATION":
                    setCsvLocation(value);
                    break;
                case "TABLES":
                    setTables(value);
                    break;
                default:
                    if (key.toLowerCase().endsWith("sql")) {
                        customizeSql(key.substring(0, key.lastIndexOf('.')), value);
                    }
            }
        }
    }

    public static class Table {
        private String tableName = null;
        private String customSql = null;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getCustomSql() {
            return customSql;
        }

        public void setCustomSql(String customSql) {
            this.customSql = customSql;
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        String tmp = password;
        if (password.startsWith("{cipher}")) {
            logger.info("Password is encrypted, now trying to decrypt it..");
            tmp = password.substring("{cipher}".length());
            try {
                EncryptUtil.KeystoreAndCert keystoreAndCert = EncryptUtil.getKeystoreAndCert(DatabaseToCsvApp.APP_ID_FILE, DatabaseToCsvApp.KEYSTORE_FILE_NAME, DatabaseToCsvApp.CERT_FILENAME);
                tmp = EncryptUtil.decrypt(keystoreAndCert.getKeystorePath(), DatabaseToCsvApp.ALIAS_NAME, keystoreAndCert.getPassword(), keystoreAndCert.getPassword(), tmp);

                logger.info("Password decrypted successfully!");
            } catch (Exception e) {
                logger.info("FATAL ERROR: Password decrypt failed!");
                throw new RuntimeException(e);
            }
        } else {
            logger.warning("Your password is stored in plain text, which is not save! Please try to encrypt it!");
        }
        this.password = tmp;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getCsvLocation() {
        return csvLocation;
    }

    public void setCsvLocation(String csvLocation) {
        this.csvLocation = massageVariables(csvLocation);
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public void setTables(String tablesStr) {
        if (this.tables == null) {
            this.tables = new ArrayList<>();
        } else {
            this.tables.clear();
        }

        if (tablesStr != null && !tablesStr.trim().equals("")) {
            String[] tables = tablesStr.split(",");
            for (String table: tables) {
                Table t = new Table();
                t.tableName = table.trim();
                this.tables.add(t);
            }
        }
    }

    public void customizeSql(String tableName, String customSql) {
        if (notEmpty(tableName) && notEmpty(customSql)) {
            for (int i=0; i < tables.size(); i++) {
                Table table = tables.get(i);
                if (table.getTableName().toUpperCase().equals(tableName.trim().toUpperCase())) {
                    table.customSql = massageVariables(customSql.trim());
                }
            }
        }
    }

    public static final boolean notEmpty(final String str) {
        return str!=null && !str.trim().equals("");
    }

    public static final String massageVariables(final String source) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String businessDate = System.getProperty("businessDate");
        LocalDate date;
        if (notEmpty(businessDate)) {
            date = LocalDate.parse(businessDate, formatter);
            logger.info("Using business date " + date.toString() + " as today!");
        } else {
            date = LocalDate.now();
        }

        String target = source;

        if (source.indexOf("$today") >= 0) {
            String today = date.format(formatter);
            target = target.replace("$today", today);
        }

        if (source.indexOf("$yesterday") >= 0) {
            String yesterday = date.minusDays(1).format(formatter);
            target = target.replace("$yesterday", yesterday);
        }

        if (source.indexOf("$tomorrow") >= 0) {
            String tomorrow = date.plusDays(1).format(formatter);
            target = target.replace("$tomorrow", tomorrow);
        }

        return target;
    }
}
