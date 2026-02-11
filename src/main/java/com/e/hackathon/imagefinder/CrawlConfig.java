package com.e.hackathon.imagefinder;

public class CrawlConfig {

    public int maxThreads = 8;
    public int maxPages = 40;
    public int maxDepth = 1;
    public int timeoutMs = 5000;
    public int minDelayPerHostMs = 50;

    public String userAgent =
            "ImageFinderBot/1.0 (+https://example.com) Java/8 (Eulerity TakeHome)";
}
