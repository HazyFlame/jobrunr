package vn.vnptit.billing.service;

import com.fss.dictionary.Dictionary;
import com.fss.sql.Database;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.context.JobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ExportDataService {

    private static final Logger logger = LoggerFactory.getLogger(ExportDataService.class);

    public static final String DB_CONFIG = "conf/DBConfig.txt";
    String vdirectory = "/app/billing/data";

    @Job(name = "Export Data Job", retries = 2)
    public void run(JobContext ctx) {
        logger.info("Bắt đầu export dữ liệu...");
        ctx.logger().info("Bắt đầu export dữ liệu...");

        Connection conn = null;
        try {
            Dictionary dicDB = new Dictionary(DB_CONFIG);
            String url = dicDB.getString("JDBC_CONNECTION_URL");
            String user = dicDB.getString("USER");
            String password = dicDB.getString("PASSWORD");

            DateFormat fmyyyyMM = new SimpleDateFormat("yyyyMM");
            Calendar now = Calendar.getInstance();
            now.add(Calendar.MONTH, -1);
            String mstrFileName = fmyyyyMM.format(now.getTime());

            conn = DriverManager.getConnection(url, user, password);
            String strPath = vdirectory + "/Billing_" + mstrFileName + ".csv";
            FileWriter fw = new FileWriter(strPath);
            List<String> vlstSchema = getSchema(url, user, password);
            int amount = 0;
            for (String schema : vlstSchema) {
                if (schema.equals("CCS_GPC."))
                    continue;
                logger.info("Chay toi schema {}", schema);
                ctx.logger().info("Chay toi schema " + schema);
                String mstrDataTime = "102017";
                String strQuery = "select ma_tb,cuoc_tt,ma_kh From " + schema + "hoadon_" + mstrDataTime + " where is_group = 1 ";
                PreparedStatement stmt = conn.prepareStatement(strQuery);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String strSdt = "0" + rs.getString(1).substring(2, 11);
                    fw.append(strSdt);
                    fw.append(',');
                    fw.append(rs.getString(2));
                    fw.append(',');
                    String yyyyMM = getLastDayOfMonth(mstrDataTime);
                    fw.append(yyyyMM);
                    fw.append(',');
                    SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
                    Date date = new Date();
                    fw.append(sdf.format(date));
                    fw.append(",");
                    fw.append("BIL_").append(rs.getString(3));
                    fw.append('\r');
                    fw.append('\n');
                    amount++;
                    if (amount % 100000 == 0) {
                        logger.info("Chay toi dong thu {} ", amount);
                    }
                }
                stmt.close();
                rs.close();
            }
            fw.flush();
            fw.close();
            logger.info("CSV File is created successfully at {}", LocalDateTime.now());
            ctx.logger().info("CSV File is created successfully at " + LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Job was interrupted", e);
            ctx.logger().info("Job was interrupted: " + e.getMessage());
            throw new RuntimeException("Job execution failed", e);
        } catch (Exception e) {
            logger.error("error", e);
            ctx.logger().info("Job execution error: " + e.getMessage());
            throw new RuntimeException("Job execution error", e);
        } finally {
            if (conn != null) Database.closeObject(conn);
        }
    }

    public List<String> getSchema(String url, String user, String password) {
        List<String> listSchema = null;
        Connection connection = null;
        PreparedStatement pstm = null;
        ResultSet rsData = null;
        try {
            connection = DriverManager.getConnection(url, user, password);
            pstm = connection.prepareStatement("select schema from ccs_common.agent");
            rsData = pstm.executeQuery();
            listSchema = new ArrayList<>();
            while (rsData.next())
                listSchema.add(rsData.getString(1));
        } catch (Exception ex) {
            logger.info("Lay ra ham getSchema: {}", ex.getMessage());
        } finally {
            if (rsData != null) Database.closeObject(rsData);
            if (pstm != null) Database.closeObject(pstm);
            if (connection != null) Database.closeObject(connection);
        }

        return listSchema;
    }

    public static String getLastDayOfMonth(String dateInString) throws Exception {
        String month = dateInString.substring(0, 2);
        String year = dateInString.substring(2, 6);
        String returnStr = year + month;
        SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy");
        Date date = sdf.parse(dateInString);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int lastDate = calendar.getActualMaximum(Calendar.DATE);
        return returnStr + lastDate;
    }
}
