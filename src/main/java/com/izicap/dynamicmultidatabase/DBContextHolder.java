package com.izicap.dynamicmultidatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBContextHolder {
    
    private static final Logger logger = LoggerFactory.getLogger(DBContextHolder.class);
    private static final ThreadLocal<DBTypeEnum> contextHolder = new ThreadLocal<>();
    
    public static void setCurrentDb(DBTypeEnum dbType) {
        logger.debug("Setting database context to: {} for thread: {}", dbType, Thread.currentThread().getName());
        contextHolder.set(dbType);
    }
    
    public static DBTypeEnum getCurrentDb() {
        DBTypeEnum currentDb = contextHolder.get();
        logger.debug("Retrieved database context: {} for thread: {}", currentDb, Thread.currentThread().getName());
        return currentDb;
    }
    
    public static void clear() {
        DBTypeEnum previousDb = contextHolder.get();
        contextHolder.remove();
        logger.debug("Cleared database context (was: {}) for thread: {}", previousDb, Thread.currentThread().getName());
    }
}