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
@EnableJpaRepositories(
    entityManagerFactoryRef = "subscriberEntityManagerFactory",
    basePackages = "com.autoprov.autoprov.repositories.subscriberRepositories",
    transactionManagerRef = "subscriberTransactionManager"
)
public class subscriberDatabaseConfiguration {

    @Autowired
    private Environment env;

    @Primary
    @Bean(name = "subscriberDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(env.getProperty("clientdb.datasource.url"));
        ds.setUsername(env.getProperty("clientdb.datasource.username"));
        ds.setPassword(env.getProperty("clientdb.datasource.password"));
        ds.setDriverClassName(env.getProperty("clientdb.datasource.driver-class-name"));
        return ds;
    }

    @Primary
    @Bean(name = "subscriberEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(adapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("clientdb.jpa.hibernate.ddl-auto"));
        bean.setJpaPropertyMap(properties);
        bean.setPackagesToScan("com.autoprov.autoprov.entity.subscriberDomain");
        return bean;
    }

    @Primary
    @Bean(name = "subscriberTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("subscriberEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
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
// @EnableJpaRepositories(entityManagerFactoryRef = "subscriberEntityManagerEntity", basePackages = {
//         "com.autoprov.autoprov.repositories.subscriberRepositories" }, transactionManagerRef = "subscriberTransactionManager")
// public class subscriberDatabaseConfiguration {

//     @Autowired
//     Environment env;

//     @Primary
//     @Bean(name = "subscriberDataSource")
//     public DataSource dataSource() {
//         DriverManagerDataSource ds = new DriverManagerDataSource();
//         ds.setUrl(env.getProperty("clientdb.datasource.url"));
//         ds.setUsername(env.getProperty("clientdb.datasource.username"));
//         ds.setPassword(env.getProperty("clientdb.datasource.password"));
//         ds.setDriverClassName(env.getProperty("clientdb.datasource.driver-class-name"));
//         return ds;
//     }

//     @Primary
//     @Bean(name = "subscriberEntityManagerEntity")
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
//         bean.setPackagesToScan("com.autoprov.autoprov.entity.hiveDomain");
//         return bean;

//     }

//     @Primary
//     @Bean("subscriberTransactionManager")
//     public PlatformTransactionManager transactionManager(
//             @Qualifier("subscriberEntityManagerEntity") EntityManagerFactory entityManagerFactory) {
//         return new JpaTransactionManager(entityManagerFactory);
//     }

// }