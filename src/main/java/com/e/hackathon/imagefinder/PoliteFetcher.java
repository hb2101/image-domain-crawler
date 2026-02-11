package com.e.hackathon.imagefinder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PoliteFetcher {
    private final CrawlConfig config;
    private final Map<String, Long> lastFetchAtMs = new ConcurrentHashMap<>();
    private final Map<String, Object> hostLocks = new ConcurrentHashMap<>();

    public PoliteFetcher(CrawlConfig config) {
        this.config = config;
    }

    public Document fetch(String url, String host) throws IOException {
        Object lock = hostLocks.computeIfAbsent(host, h -> new Object());
        synchronized (lock) {
            long now = System.currentTimeMillis();
            Long last = lastFetchAtMs.get(host);
            if (last != null) {
                long wait = config.minDelayPerHostMs - (now - last);
                if (wait > 0) {
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            lastFetchAtMs.put(host, System.currentTimeMillis());
        }

        Connection conn = Jsoup.connect(url)
                .userAgent(config.userAgent)
                .timeout(config.timeoutMs)
                .followRedirects(true)
                .ignoreContentType(true);

        return conn.get();
    }
}
