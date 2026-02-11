package com.e.hackathon.imagefinder;

import java.util.List;

public interface ImageCrawlService {
    List<String> crawlImages(String seedUrl);
}
