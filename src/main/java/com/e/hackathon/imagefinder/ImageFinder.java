package com.e.hackathon.imagefinder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
        name = "ImageFinder",
        urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected static final Gson GSON = new GsonBuilder().create();

    public static final String[] testImages = {
            "https://images.pexels.com/photos/545063/pexels-photo-545063.jpeg?auto=compress&format=tiny",
            "https://images.pexels.com/photos/464664/pexels-photo-464664.jpeg?auto=compress&format=tiny",
            "https://images.pexels.com/photos/406014/pexels-photo-406014.jpeg?auto=compress&format=tiny",
            "https://images.pexels.com/photos/1108099/pexels-photo-1108099.jpeg?auto=compress&format=tiny"
    };

    private final ImageCrawlService crawler;

    public ImageFinder() {
        this(new WebCrawlerService());
    }

    ImageFinder(ImageCrawlService crawler) {
        this.crawler = crawler;
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");

        String url = req.getParameter("url");

        if (url == null || url.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print(GSON.toJson(Collections.emptyList()));
            return;
        }

        List<String> images = crawler.crawlImages(url);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().print(GSON.toJson(images));
    }
}
