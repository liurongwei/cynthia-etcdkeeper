package com.cynthia.etcdkeeper.caching;

import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

@Slf4j
public class CacheEventLogger implements CacheEventListener {
    @Override
    public void onEvent(CacheEvent cacheEvent) {
        log.info("key %s, old value %s, new value %s", cacheEvent.getKey(), cacheEvent.getOldValue(),
                cacheEvent.getNewValue());
    }
}
