package vn.vnpt.billing.service;

import com.fss.dictionary.Dictionary;
import org.apache.commons.net.ftp.FTPClient;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@Service
public class UploadFTPService {

    private static final Logger logger = LoggerFactory.getLogger(UploadFTPService.class);

    public static final String FTP_CONFIG = "conf/FTPConfig.txt";
    String nextServerFtpFolder = "/CuocDiDong";
    String vdirectory = "/app/billing/data";

    @Job(name = "Upload FTP Job", retries = 2)
    public void run(JobContext ctx) {
        logger.info("Bắt đầu upload lên FTP...");
        ctx.logger().info("Bắt đầu upload lên FTP...");

        FTPClient nextFtpClient = null;
        try {
            Dictionary dicDB = new Dictionary(FTP_CONFIG);
            String nextFtpIp = dicDB.getString("NextFtpIp");
            String nextFtpUser = dicDB.getString("NextFtpUser");
            String nextFtpPassword = dicDB.getString("NextFtpPassword");

            logger.info("Dia chi ftp can ket noi: {}", nextFtpIp);
            logger.info("Bat dau ket noi");
            ctx.logger().info("Dia chi ftp can ket noi: " + nextFtpIp);
            ctx.logger().info("Bat dau ket noi");
            nextFtpClient = new FTPClient();
            nextFtpClient.connect(nextFtpIp);
            nextFtpClient.setFileType(2);
            nextFtpClient.enterLocalPassiveMode();
            boolean nextLogin = nextFtpClient.login(nextFtpUser, nextFtpPassword);
            if (nextLogin) {
                logger.info("Da ket noi server vpoint thanh cong");
                ctx.logger().info("Da ket noi server vpoint thanh cong");
                try {
                    File directory = new File(this.vdirectory);
                    File[] fList = directory.listFiles();
                    if (fList == null) {
                        logger.info("Thu muc {} khong ton tai.", this.vdirectory);
                        ctx.logger().info("Da ket noi server vpoint thanh cong");
                    } else if (fList.length == 0) {
                        logger.info("Danh sach file tu thu muc {} rong.", this.vdirectory);
                        ctx.logger().info("Danh sach file tu thu muc " + this.vdirectory + " rong");
                    } else {
                        byte b;
                        int i;
                        File[] arrayOfFile;
                        for (i = (arrayOfFile = fList).length, b = 0; b < i; ) {
                            File file = arrayOfFile[b];
                            if (file.isFile()) {
                                System.out.println(file.getName());
                                String fileName = file.getName();
                                nextFtpClient.enterLocalPassiveMode();
                                nextFtpClient.setFileType(2);
                                File firstLocalFile = new File(this.vdirectory + "/" + fileName);
                                String firstRemoteFile = this.nextServerFtpFolder + "/" + fileName;
                                InputStream inputStream = Files.newInputStream(firstLocalFile.toPath());
                                logger.info("Start uploading first file");
                                ctx.logger().info("Start uploading first file");
                                boolean done = nextFtpClient.storeFile(firstRemoteFile, inputStream);
                                nextFtpClient.sendSiteCommand("chmod 777 " + firstRemoteFile);
                                inputStream.close();
                                if (done) {
                                    logger.info("The first file is uploaded successfully.");
                                    ctx.logger().info("The first file is uploaded successfully.");
                                }
                                firstLocalFile.delete();
                                logger.info("File {} done.", fileName);
                                ctx.logger().info("File " + fileName + " done");
                            }
                            b++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Loi xay ra: {}", e.getMessage());
                    ctx.logger().info("Loi xay ra: " + e.getMessage());
                }
            } else {
                logger.error("Ket noi server vpoint khong thanh cong");
                ctx.logger().info("Ket noi server vpoint khong thanh cong");
            }
        } catch (Exception e) {
            logger.error("error", e);
            ctx.logger().info("Job execution error: " + e.getMessage());
            throw new RuntimeException("Job execution error", e);
        } finally {
            if (nextFtpClient != null && nextFtpClient.isConnected()) {
                try {
                    nextFtpClient.logout();
                    nextFtpClient.disconnect();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
