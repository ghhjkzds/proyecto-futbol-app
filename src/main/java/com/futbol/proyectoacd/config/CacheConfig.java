package com.futbol.proyectoacd.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para optimizar llamadas a APIs externas
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configuración de CacheManager con Caffeine
     * - scheduledFixtures: caché de partidos programados (TTL: 10 minutos)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("scheduledFixtures");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuración de Caffeine
     * - maximumSize: 100 entradas
     * - expireAfterWrite: 10 minutos (no gastar cuota innecesariamente)
     */
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
