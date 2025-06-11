package com.izicap.dynamicmultidatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiRoutingDataSource extends AbstractRoutingDataSource {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiRoutingDataSource.class);
    
    @Override
    protected Object determineCurrentLookupKey() {
        DBTypeEnum currentDb = DBContextHolder.getCurrentDb();
        logger.debug("Determining datasource lookup key: {} for thread: {}", currentDb, Thread.currentThread().getName());
        
        if (currentDb == null) {
            logger.warn("No database context found, using default datasource");
            return DBTypeEnum.MAIN;
        }
        
        logger.info("Routing to database: {}", currentDb);
        return currentDb;
    }
}