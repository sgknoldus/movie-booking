package com.moviebooking.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.moviebooking.search.repository")
public class ElasticsearchConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }
    
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager() {
        return new ElasticsearchTransactionManager();
    }
    
    private static class ElasticsearchTransactionManager extends AbstractPlatformTransactionManager {
        
        @Override
        protected Object doGetTransaction() throws TransactionException {
            return new Object();
        }
        
        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
            // Elasticsearch doesn't support traditional transactions
            // This is a placeholder for potential future transaction support
        }
        
        @Override
        protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
            // No-op for Elasticsearch
        }
        
        @Override
        protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
            // No-op for Elasticsearch
        }
    }
}