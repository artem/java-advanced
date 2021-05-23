package info.kgeorgiy.ja.labazov.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int downloaders;
    private final int extractors;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
    }

    private static void usage() {
        System.err.println("Usage: url [depth [downloads [extractors [perHost]]]]");
    }

    public static void main(String[] args) {
        int depth = 0;
        int downloads = 0;
        int extractors = 0;
        int perHost = 0;

        if (args.length < 1) {
            usage();
            return;
        }

        try {
            switch (args.length) {
                case 5:
                    perHost = Integer.parseUnsignedInt(args[4]);
                case 4:
                    extractors = Integer.parseUnsignedInt(args[3]);
                case 3:
                    downloads = Integer.parseUnsignedInt(args[2]);
                case 2:
                    depth = Integer.parseUnsignedInt(args[1]);
                    break;
                default:
                    usage();
                    return;
            }
        } catch (NumberFormatException ex) {
            System.err.println(ex.getMessage());
        }

        try {
            new WebCrawler(new CachingDownloader(), downloads, extractors, perHost).download(args[0], depth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> downloadImpl() {
        Set<String> downloaded = new HashSet<>();

        return downloaded;
    }

    @Override
    public Result download(String url, int depth) {
        //List.copyOf();
        try {
            downloader.download(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;//ew Result();
    }

    @Override
    public void close() {

    }
}
