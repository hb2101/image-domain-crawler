# Image Domain Crawler

This project is a web-based application that crawls a provided URL and extracts all image resources found within the same domain. It demonstrates multithreaded crawling, domain restriction, thread-safe architecture, and clean frontend integration.

This README provides an overview of the project goals, structure, and setup instructions.

## Project Goal

The goal of this project is to build a domain-restricted web crawler that accepts a URL from the user, crawls that website (including sub-pages within the same domain), and returns a JSON array containing the URLs of all discovered images.

The crawler is implemented using Java 8 and JSoup for HTML parsing, along with a multithreaded architecture to improve performance.

## Core Functionality

This project implements the following features:

A web crawler that extracts all image URLs from a given website.

Crawling of sub-pages within the same domain.

Multithreaded execution to process multiple pages concurrently.

Domain restriction to prevent crawling external sites.

Prevention of revisiting previously crawled pages.

URL normalization and deduplication.

Bounded crawl limits to ensure deterministic termination.

## Additional Enhancements

The project also includes:
Rate-limited fetching to avoid aggressive crawling.

Configurable crawl limits (depth, page count, thread count).

Thread-safe data structures.

Modern responsive frontend UI.

Loading indicators and empty-state handling.

Clean separation of concerns between servlet and crawler logic.

## Structure

The core servlet entry point is located at:
src/main/java/com/e/hackathon/imagefinder/ImageFinder.java

The crawling logic is implemented in supporting classes under:
src/main/java/com/e/hackathon/imagefinder/crawler/

These include:

WebCrawlerService — Core crawling logic

PoliteFetcher — Rate-limited HTTP fetcher

CrawlConfig — Crawl configuration settings

Supporting utility classes for URL tracking and depth control

The frontend interface is located at:
src/main/webapp/index.html

This page provides a clean UI for entering a URL and displaying extracted images.

Project configuration and dependencies are defined in:
pom.xml

## Requirements

Before beginning, ensure the following are installed:
Maven 3.5 or higher
Java 8
Exact version required. Newer Java versions may cause build or runtime issues.
Setup
Navigate to the root directory of the project.

To build the project:
mvn package
If successful, you will see:
BUILD SUCCESS
To clean previously compiled files:
mvn clean
To run the application:
mvn clean package jetty:run
Once the server starts, open your browser and navigate to:
http://localhost:8080

You should see the web interface where you can enter a URL and begin crawling.

Example URLs to Test

You can test the crawler using the following:

https://www.wikipedia.org

https://www.bbc.com

https://www.apple.com

https://example.com

## Configuration

Crawler limits and behavior can be modified in:

CrawlConfig.java

Example configuration:

public int maxThreads = 6;

public int maxPages = 35;

public int maxDepth = 1;

public int timeoutMs = 5000;

public int minDelayPerHostMs = 75;

These settings control concurrency, crawl depth, politeness delay, and request timeouts.

## Design Principles

This project demonstrates:

Concurrent programming using ExecutorService

Thread-safe collections for visited URLs and image storage

Domain-scoped crawling logic

Defensive programming and input validation

Deterministic crawl termination

Clean separation between backend logic and frontend UI

Production-style project structure

## Final Notes

The crawler is intentionally bounded to avoid infinite crawling.

The architecture allows easy extension for additional features such as robots.txt parsing, crawl statistics, or image classification.

The frontend is intentionally lightweight and framework-free for clarity and portability.

The project is structured for readability, maintainability, and scalability.
