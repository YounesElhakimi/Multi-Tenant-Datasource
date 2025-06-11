package com.izicap.dynamicmultidatabase;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.izicap.dynamicmultidatabase",
        entityManagerFactoryRef = "multiEntityManager",
        transactionManagerRef = "multiTransactionManager"
)
public class PersistenceConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(PersistenceConfiguration.class);
    private final String PACKAGE_SCAN = "com.izicap.dynamicmultidatabase";
    
    @Primary
    @Bean(name = "mainDataSource")
    @ConfigurationProperties("app.datasource.main")
    public DataSource mainDataSource() {
        logger.info("Configuring main datasource");
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean(name = "clientADataSource")
    @ConfigurationProperties("app.datasource.clienta")
    public DataSource clientADataSource() {
        logger.info("Configuring client A datasource");
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean(name = "clientBDataSource")
    @ConfigurationProperties("app.datasource.clientb")
    public DataSource clientBDataSource() {
        logger.info("Configuring client B datasource");
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }
    
    @Bean(name = "multiRoutingDataSource")
    public DataSource multiRoutingDataSource() {
        logger.info("Configuring multi-routing datasource");
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DBTypeEnum.MAIN, mainDataSource());
        targetDataSources.put(DBTypeEnum.CLIENT_A, clientADataSource());
        targetDataSources.put(DBTypeEnum.CLIENT_B, clientBDataSource());
        
        MultiRoutingDataSource multiRoutingDataSource = new MultiRoutingDataSource();
        multiRoutingDataSource.setDefaultTargetDataSource(mainDataSource());
        multiRoutingDataSource.setTargetDataSources(targetDataSources);
        
        logger.info("Multi-routing datasource configured with {} target datasources", targetDataSources.size());
        return multiRoutingDataSource;
    }
    
    @Bean(name = "multiEntityManager")
    public LocalContainerEntityManagerFactoryBean multiEntityManager() {
        logger.info("Configuring multi-entity manager");
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(multiRoutingDataSource());
        em.setPackagesToScan(PACKAGE_SCAN);
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());
        
        logger.info("Multi-entity manager configured for packages: {}", PACKAGE_SCAN);
        return em;
    }
    
    @Bean(name = "multiTransactionManager")
    public PlatformTransactionManager multiTransactionManager() {
        logger.info("Configuring multi-transaction manager");
        
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(multiEntityManager().getObject());
        
        logger.info("Multi-transaction manager configured");
        return transactionManager;
    }
    
    @Primary
    @Bean(name = "dbSessionFactory")
    public LocalSessionFactoryBean dbSessionFactory() {
        logger.info("Configuring database session factory");
        
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(multiRoutingDataSource());
        sessionFactoryBean.setPackagesToScan(PACKAGE_SCAN);
        sessionFactoryBean.setHibernateProperties(hibernateProperties());
        
        logger.info("Database session factory configured");
        return sessionFactoryBean;
    }
    
    private Properties hibernateProperties() {
        logger.debug("Configuring Hibernate properties");
        
        Properties properties = new Properties();
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        
        logger.debug("Hibernate properties configured");
        return properties;
    }
}