package com.autoprov.autoprov.configuration;


import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableJpaRepositories(
    entityManagerFactoryRef = "oltEntityManagerFactory",
    basePackages = "com.autoprov.autoprov.repositories.oltRepositories",
    transactionManagerRef = "oltTransactionManager"
)
public class oltDatabaseConfiguration {

    @Autowired
    private Environment env;

    @Bean(name = "oltDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(env.getProperty("oltDB.datasource.url"));
        ds.setUsername(env.getProperty("oltDB.datasource.username"));
        ds.setPassword(env.getProperty("oltDB.datasource.password"));
        ds.setDriverClassName(env.getProperty("oltDB.datasource.driver-class-name"));
        return ds;
    }

    @Bean(name = "oltEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(adapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("oltDB.jpa.hibernate.ddl-auto"));
        bean.setJpaPropertyMap(properties);
        bean.setPackagesToScan("com.autoprov.autoprov.entity.oltDomain");
        return bean;
    }

    @Bean(name = "oltTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("oltEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}


// import java.util.HashMap;

// import javax.sql.DataSource;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import org.springframework.core.env.Environment;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
// import org.springframework.jdbc.datasource.DriverManagerDataSource;
// import org.springframework.orm.jpa.JpaTransactionManager;
// import org.springframework.orm.jpa.JpaVendorAdapter;
// import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
// import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
// import org.springframework.transaction.PlatformTransactionManager;
// import org.springframework.transaction.annotation.EnableTransactionManagement;

// import jakarta.persistence.EntityManagerFactory;

// @Configuration
// @EnableTransactionManagement
// @EnableJpaRepositories(entityManagerFactoryRef = "oltEntityManagerEntity", basePackages = {
//         "com.autoprov.autoprov.repositories.oltRepositories" }, transactionManagerRef = "oltransactionManager")
// public class oltDatabaseConfiguration {

//     @Autowired
//     Environment env;

//     @Primary
//     @Bean(name = "oltDataSource")
//     public DataSource dataSource() {
//         DriverManagerDataSource ds = new DriverManagerDataSource();
//         ds.setUrl(env.getProperty("oltDB.datasource.url"));
//         ds.setUsername(env.getProperty("oltDB.datasource.username"));
//         ds.setPassword(env.getProperty("oltDB.datasource.password"));
//         ds.setDriverClassName(env.getProperty("oltDB.datasource.driver-class-name"));
//         return ds;
//     }

//     @Primary
//     @Bean(name = "oltEntityManagerEntity")
//     public LocalContainerEntityManagerFactoryBean entityManager() {
//         LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
//         bean.setDataSource(dataSource());
//         JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
//         bean.setJpaVendorAdapter(adapter);
//         HashMap<String, Object> properties = new HashMap<String, Object>();
//         properties.put("hibernate.hbm2ddl.auto", "update");
//         // properties.put("hibernate.dialect",
//         // "org.hibernate.dialect.PostgreSQLDialect");
//         bean.setJpaPropertyMap(properties);
//         bean.setPackagesToScan("com.autoprov.autoprov.entity.oltDomain");
//         return bean;

//     }

//     @Primary
//     @Bean("oltTransactionManager")
//     public PlatformTransactionManager transactionManager(
//             @Qualifier("oltEntityManagerEntity") EntityManagerFactory entityManagerFactory) {
//         return new JpaTransactionManager(entityManagerFactory);
//     }

// }