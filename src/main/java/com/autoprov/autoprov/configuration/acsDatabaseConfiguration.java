 package com.autoprov.autoprov.configuration;

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
// @EnableJpaRepositories(entityManagerFactoryRef = "acsEntityManagerEntity", basePackages = {
//         "com.autoprov.autoprov.repositories.acsRepositories" }, transactionManagerRef = "acsTransactionManager")
// public class acsDatabaseConfiguration {

//     @Autowired
//     Environment env;

//     @Primary
//     @Bean(name = "acsDataSource")
//     public DataSource dataSource() {
//         DriverManagerDataSource ds = new DriverManagerDataSource();
//         ds.setUrl(env.getProperty("acs.datasource.url"));
//         ds.setUsername(env.getProperty("acs.datasource.username"));
//         ds.setPassword(env.getProperty("acs.datasource.password"));
//         ds.setDriverClassName(env.getProperty("acs.datasource.driver-class-name"));
//         return ds;
//     }

//     @Primary
//     @Bean(name = "acsEntityManagerEntity")
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
//         bean.setPackagesToScan("com.autoprov.autoprov.entity.acsDomain");
//         return bean;

//     }

//     @Primary
//     @Bean("acsTransactionManager")
//     public PlatformTransactionManager transactionManager(
//             @Qualifier("acsEntityManagerEntity") EntityManagerFactory entityManagerFactory) {
//         return new JpaTransactionManager(entityManagerFactory);
//     }

// }






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
    entityManagerFactoryRef = "acsEntityManagerFactory",
    basePackages = "com.autoprov.autoprov.repositories.acsRepositories",
    transactionManagerRef = "acsTransactionManager"
)
public class acsDatabaseConfiguration {

    @Autowired
    private Environment env;

    @Bean(name = "acsDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(env.getProperty("acs.datasource.url"));
        ds.setUsername(env.getProperty("acs.datasource.username"));
        ds.setPassword(env.getProperty("acs.datasource.password"));
        ds.setDriverClassName(env.getProperty("acs.datasource.driver-class-name"));
        return ds;
    }

    @Bean(name = "acsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        JpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        bean.setJpaVendorAdapter(adapter);
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", env.getProperty("acs.jpa.hibernate.ddl-auto"));
        bean.setJpaPropertyMap(properties);
        bean.setPackagesToScan("com.autoprov.autoprov.entity.acsDomain");
        return bean;
    }

    @Bean(name = "acsTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("acsEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}