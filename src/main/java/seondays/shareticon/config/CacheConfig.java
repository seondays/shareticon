package seondays.shareticon.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import seondays.shareticon.utils.CacheType;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caffeineCaches = Arrays.stream(CacheType.values())
                .map(cache -> new CaffeineCache(
                        cache.getCacheName(),
                        caffeineCacheBuilder(cache.getExpireAfterWrite(), cache.getMaximumSize()))
                ).toList();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caffeineCaches);
        return cacheManager;
    }

    private Cache<Object, Object> caffeineCacheBuilder(int duration, int size) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration, TimeUnit.SECONDS)
                .maximumSize(size)
                .recordStats()
                .build();
    }
}
