package com.example.jobrunr.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration.JobRunrConfigurationResult;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class JobRunrConfiguration {

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:jobrunr.db");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found", e);
        }

        return new HikariDataSource(config);
    }

    @Bean
    public StorageProvider storageProvider(DataSource dataSource) {
        return SqlStorageProviderFactory.using(dataSource);
    }

    // Khởi tạo JobRunr đúng 1 lần, bật BackgroundJobServer và Dashboard
    @Bean
    public JobRunrConfigurationResult jobRunr(StorageProvider storageProvider) {
        return JobRunr.configure()
                .useStorageProvider(storageProvider)
                .useBackgroundJobServer()   // bật server
                .useDashboard(8000)         // bật dashboard
                .initialize();
    }

    // Lấy JobScheduler từ kết quả khởi tạo
    @Bean
    public JobScheduler jobScheduler(JobRunrConfigurationResult result) {
        return result.getJobScheduler();
    }
}
