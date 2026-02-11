package com.e.hackathon.imagefinder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawlerService implements ImageCrawlService {

    private final CrawlConfig config;
    private final PoliteFetcher fetcher;

    public WebCrawlerService() {
        this(new CrawlConfig());
    }

    public WebCrawlerService(CrawlConfig config) {
        this.config = config;
        this.fetcher = new PoliteFetcher(config);
    }

    @Override
    public List<String> crawlImages(String seedUrl) {
        if (seedUrl == null || seedUrl.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedSeed = normalizeUrl(seedUrl);
        if (normalizedSeed == null) return Collections.emptyList();

        URI seedUri;
        try {
            seedUri = new URI(normalizedSeed);
        } catch (URISyntaxException e) {
            return Collections.emptyList();
        }

        String allowedHost = seedUri.getHost();
        if (allowedHost == null) return Collections.emptyList();

        ConcurrentLinkedQueue<UrlDepth> frontier = new ConcurrentLinkedQueue<>();
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> images = ConcurrentHashMap.newKeySet();

        frontier.add(new UrlDepth(normalizedSeed, 0));
        visited.add(normalizedSeed);

        ExecutorService pool = Executors.newFixedThreadPool(config.maxThreads);
        CompletionService<Void> ecs = new ExecutorCompletionService<>(pool);

        AtomicInteger inFlight = new AtomicInteger(0);
        AtomicInteger pagesCrawled = new AtomicInteger(0);

        try {
            while (true) {
                while (inFlight.get() < config.maxThreads) {
                    UrlDepth next = frontier.poll();
                    if (next == null) break;
                    if (pagesCrawled.get() >= config.maxPages) break;

                    inFlight.incrementAndGet();
                    ecs.submit(() -> {
                        try {
                            crawlOne(next, allowedHost, visited, frontier, images, pagesCrawled);
                        } finally {
                            inFlight.decrementAndGet();
                        }
                        return null;
                    });
                }

                if (frontier.isEmpty() && inFlight.get() == 0) break;

                Future<Void> done = ecs.poll(250, TimeUnit.MILLISECONDS);
                if (done != null) {
                    try { done.get(); } catch (ExecutionException ignored) {}
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }

        ArrayList<String> out = new ArrayList<>(images);
        Collections.sort(out);
        return out;
    }

    private void crawlOne(
            UrlDepth current,
            String allowedHost,
            Set<String> visited,
            ConcurrentLinkedQueue<UrlDepth> frontier,
            Set<String> images,
            AtomicInteger pagesCrawled
    ) {
        if (pagesCrawled.incrementAndGet() > config.maxPages) return;

        URI uri;
        try {
            uri = new URI(current.url);
        } catch (URISyntaxException e) {
            return;
        }

        String host = uri.getHost();
        if (host == null) return;
        if (!host.equalsIgnoreCase(allowedHost)) return;

        Document doc;
        try {
            doc = fetcher.fetch(current.url, host);
        } catch (Exception e) {
            return;
        }

        collectImages(doc, images);

        if (current.depth >= config.maxDepth) return;

        Elements links = doc.select("a[href]");
        for (Element a : links) {
            String abs = a.absUrl("href");
            String normalized = normalizeUrl(abs);
            if (normalized == null) continue;

            URI linkUri;
            try {
                linkUri = new URI(normalized);
            } catch (URISyntaxException e) {
                continue;
            }

            String linkHost = linkUri.getHost();
            if (linkHost == null) continue;
            if (!linkHost.equalsIgnoreCase(allowedHost)) continue;

            if (visited.add(normalized)) {
                frontier.add(new UrlDepth(normalized, current.depth + 1));
            }
        }
    }

    private void collectImages(Document doc, Set<String> images) {
        Elements imgs = doc.select("img[src]");

        for (Element img : imgs) {
            String abs = img.absUrl("src");

            if (abs != null && !abs.isEmpty()
                && (abs.startsWith("http://") || abs.startsWith("https://"))) {
                images.add(abs);
            }
        }
    }


    private String firstSrcFromSrcset(String srcset) {
        String[] parts = srcset.split(",");
        if (parts.length == 0) return null;
        String[] tokens = parts[0].trim().split("\\s+");
        return tokens.length > 0 ? tokens[0].trim() : null;
    }

    private String resolveAgainstBase(String base, String relative) {
        try {
            return new URI(base).resolve(relative).toString();
        } catch (Exception e) {
            return relative;
        }
    }

    private void addIfHttp(Set<String> images, String url) {
        if (url == null) return;
        String u = url.trim();
        if (u.startsWith("http://") || u.startsWith("https://")) {
            String normalized = normalizeUrl(u);
            if (normalized != null) images.add(normalized);
        }
    }

    private String normalizeUrl(String url) {
        if (url == null) return null;
        String u = url.trim();
        if (u.isEmpty()) return null;
        if (!(u.startsWith("http://") || u.startsWith("https://"))) return null;

        try {
            URI uri = new URI(u);
            uri = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    null
            );
            String out = uri.normalize().toString();
            if (out.endsWith("/") && out.length() > (uri.getScheme().length() + 3 + uri.getHost().length() + 1)) {
                out = out.substring(0, out.length() - 1);
            }
            return out;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
