package tech.comfortheart.app;

import org.apache.poi.ss.usermodel.*;
import tech.comfortheart.util.CommandRunner;
import tech.comfortheart.util.EncryptUtil;
import tech.comfortheart.util.StringUtility;

import java.io.*;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JobRunnerConfig {
    private static final Logger logger = Logger.getLogger(JobRunnerConfig.class.getSimpleName());
    private static final int CELL_JOB_GROUP = 0;
    private static final int CELL_JOB_SEQ = 1;
    private static final int CELL_JOB_NAME = 2;
    private static final int CELL_ABORT_FAIL = 3;
    private static final int CELL_STATUS = 4;
    private static final int CELL_COMMAND = 5;

    private List<JobRow> jobs = new LinkedList<>();

    private File jobConfig;
    private String jobGroupName;
    private Variables variables = new Variables();

    public JobRunnerConfig(final File originalJobConfig, final String jobGroupName) throws IOException {
        String newJobConfig = StringUtility.replaceSuffix(originalJobConfig.getAbsolutePath(),
                ".xlsx",
                "-" + jobGroupName + ".xlsx");
        this.jobConfig = new File(newJobConfig);
        this.jobGroupName = jobGroupName;

        if (!jobConfig.exists()) {
            Files.copy(originalJobConfig.toPath(), jobConfig.toPath());
        }

        try (InputStream ins = new FileInputStream(jobConfig);
             Workbook wb = WorkbookFactory.create(ins)){

            /**
             * Reading variables from config.
             */
            Sheet sheetConfig = wb.getSheet("Config");
            sheetConfig.forEach(row -> {
                Cell keyCell = row.getCell(0);
                if (keyCell!= null) {
                    Cell valueCell = row.getCell(1);
                    String key = keyCell.getStringCellValue();
                    String value = valueCell.getStringCellValue();
                    if (StringUtility.notEmpty(key) && StringUtility.notEmpty(value)) {
                        variables.set(key, value);
                    }
                }
            });

            Sheet sheet = wb.getSheet("Jobs");
            List<Row> toBeRemovedRows = new LinkedList<>();
            sheet.forEach(row -> {
                Cell groupCell = row.getCell(CELL_JOB_GROUP);
                String group = groupCell==null? null: groupCell.getStringCellValue();
                if (StringUtility.equalRegardlessCaseOrRoundingSpaces(group, jobGroupName)) {
                    Cell cellJobSequence = row.getCell(CELL_JOB_SEQ);
                    Cell cellJobName = row.getCell(CELL_JOB_NAME);
                    Cell cellAbortFailure = row.getCell(CELL_ABORT_FAIL);
                    Cell cellJobStatus = row.getCell(CELL_STATUS);
                    Cell cellCommand = row.getCell(CELL_COMMAND);
                    int jobSequence = (int) cellJobSequence.getNumericCellValue();
                    String jobName = cellJobName.getStringCellValue();
                    String abortOnFailureStr = cellAbortFailure==null?null:cellAbortFailure.getStringCellValue();
                    String jobStatus = cellJobStatus == null? "" : cellJobStatus.getStringCellValue();
                    String command = cellCommand == null ? null : cellCommand.getStringCellValue();
                    boolean abortOnFailure = false;

                    if (StringUtility.notEmpty(abortOnFailureStr)) {
                        if (abortOnFailureStr.trim().toUpperCase().equals("Y")) {
                            abortOnFailure = true;
                        }
                    }

                    if (StringUtility.isEmpty(command)) {
                        return;
                    } else {
                        JobRow jobRow = new JobRow();
                        jobRow.setJobGroup(jobGroupName);
                        jobRow.setJobSequence(jobSequence);
                        jobRow.setJobName(jobName);
                        jobRow.setStatus(jobStatus);
                        jobRow.setAbortForFailure(abortOnFailure);
                        jobRow.setCommandStr(StringUtility.replaceVariable(command, variables.getVariables()));
                        jobRow.setRowInExcel(row.getRowNum());
                        jobs.add(jobRow);
                    }
                } else {
                    if (!StringUtility.equalRegardlessCaseOrRoundingSpaces(group, "Job Group")) {
                        toBeRemovedRows.add(row);
                    }
                }
            });

            toBeRemovedRows.forEach(row -> sheet.removeRow(row));

            jobs.sort(Comparator.comparingInt(JobRow::getJobSequence));
            try(OutputStream ops = new FileOutputStream(jobConfig)) {
                ins.close();
                wb.write(ops);
                ops.flush();
            }

        }
    }

    public void runJobs() throws IOException{
        boolean allSuccess = true;

        for(JobRow job : jobs) {
            if (StringUtility.equalRegardlessCaseOrRoundingSpaces(job.status, "DONE")) {
                logger.info("Skipping job: " + job.jobName + " for it is done last time!");
                continue;
            }

            try {
                int result = CommandRunner.runCommand(job.commandStr, variables.getVariables());
                if (result != 0) {
                    throw new IOException("Command exit not zero: " + result);
                }

                writeStatus(job, "DONE");
            } catch (IOException|InterruptedException e) {
                allSuccess = false;
                if (job.abortForFailure) {
                    logger.info("FATAL ERROR: for job " + jobGroupName + "." + job.jobSequence + " " + job.jobName + " with error message: " + e.getMessage());
                    writeStatus(job, "FATAL: " + e.getMessage());
                    throw new RuntimeException(e);
                } else {
                    logger.info("WARNING ERROR: for job " + jobGroupName + "." + job.jobSequence + " " + job.jobName + " with error message: " + e.getMessage());
                    e.printStackTrace();
                    writeStatus(job, "WARNING: " + e.getMessage());
                }
            }
        };

        if (allSuccess) {
            logger.info("All jobs completed successfully! Remove the temp config!");
            jobConfig.delete();
        }

    }

    public List<JobRow> getJobs() {
        return jobs;
    }

    private void writeStatus(JobRow job, String status) throws IOException {
        try (InputStream ins = new FileInputStream(jobConfig);
             Workbook wb = WorkbookFactory.create(ins)){
            Sheet sheet = wb.getSheet("Jobs");
            Row row = sheet.getRow(job.getRowInExcel());
            Cell cellStatus = row.getCell(CELL_STATUS);
            if (cellStatus == null) {
                cellStatus = row.createCell(CELL_STATUS);
            }
            cellStatus.setCellValue(status);
            try(OutputStream ops = new FileOutputStream(jobConfig)) {
                ins.close();
                wb.write(ops);
                ops.flush();
            }
        }
    }

    public static class JobRow {
        private String jobGroup;
        private int jobSequence;
        private String jobName;
        private boolean abortForFailure;
        private String status;
        private String commandStr;

        public int getRowInExcel() {
            return rowInExcel;
        }

        public void setRowInExcel(int rowInExcel) {
            this.rowInExcel = rowInExcel;
        }

        private int rowInExcel;

        public String getJobGroup() {
            return jobGroup;
        }

        public void setJobGroup(String jobGroup) {
            this.jobGroup = jobGroup;
        }

        public int getJobSequence() {
            return jobSequence;
        }

        public void setJobSequence(int jobSequence) {
            this.jobSequence = jobSequence;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public boolean isAbortForFailure() {
            return abortForFailure;
        }

        public void setAbortForFailure(boolean abortForFailure) {
            this.abortForFailure = abortForFailure;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCommandStr() {
            return commandStr;
        }

        public void setCommandStr(String commandStr) {
            this.commandStr = commandStr;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Job Group: ").append(jobGroup).append("|")
                    .append("Job Sequence: ").append(jobSequence).append("|")
                    .append("Job Name: ").append(jobName).append("|")
                    .append("Abort on Failure: ").append(abortForFailure).append("|")
                    .append("Status: ").append(status).append("|")
                    .append("Row in Excel: ").append(rowInExcel).append("|")
                    .append("Command: ").append(commandStr);
            return sb.toString();
        }
    }

    public static class Variables {
        private Map<String, Object> variables = new ConcurrentHashMap<>();
        public Variables() {
            variables.putAll(System.getenv());
            System.getProperties().forEach((key, value) -> variables.put(key.toString(), value.toString()));
        }

        private void set(String key, String value) {
            String tmp = value;
            if (value.startsWith("{cipher}")) {
                logger.info("Password is encrypted, now trying to decrypt it..");
                tmp = value.substring("{cipher}".length()).trim();
                try {
                    EncryptUtil.KeystoreAndCert keystoreAndCert = EncryptUtil.getKeystoreAndCert(JobRunner.APP_ID_FILE, JobRunner.KEYSTORE_FILE_NAME, JobRunner.CERT_FILENAME);
                    tmp = EncryptUtil.decrypt(keystoreAndCert.getKeystorePath(), JobRunner.ALIAS_NAME, keystoreAndCert.getPassword(), keystoreAndCert.getPassword(), tmp);

                    logger.info("Password decrypted successfully!");
                } catch (Exception e) {
                    logger.info("FATAL ERROR: Password decrypt failed!");
                    throw new RuntimeException(e);
                }
            } else {
                logger.warning("Your password is stored in plain text, which is not save! Please try to encrypt it!");
            }
            variables.put(key, tmp);
        }

        public Map<String, Object> getVariables() {
            return variables;
        }
    }
}
