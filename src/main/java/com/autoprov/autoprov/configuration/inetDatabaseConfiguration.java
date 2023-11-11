package com.autoprov.autoprov.configuration;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "inetEntityManagerEntity", basePackages = {
        "com.autoprov.autoprov.repositories.inetRepositories" }, transactionManagerRef = "inetTransactionManager")
public class inetDatabaseConfiguration {

    @Autowired
    Environment env;

    @Primary
    @Bean(name = "inetDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(env.getProperty("inet.datasource.url"));
        ds.setUsername(env.getProperty("inet.datasource.username"));
        ds.setPassword(env.getProperty("inet.datasource.password"));
        ds.setDriverClassName(env.getProperty("inet.datasource.driver-class-name"));
        return ds;
    }

    @Primary
    @Bean(name = "inetEntityManagerEntity")
    public LocalContainerEntityManagerFactoryBean entityManager() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(adapter);
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        // properties.put("hibernate.dialect",
        // "org.hibernate.dialect.PostgreSQLDialect");
        bean.setJpaPropertyMap(properties);
        bean.setPackagesToScan("com.autoprov.autoprov.entity.inetDomain");
        return bean;

    }

    @Primary
    @Bean("inetTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("inetEntityManagerEntity") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}