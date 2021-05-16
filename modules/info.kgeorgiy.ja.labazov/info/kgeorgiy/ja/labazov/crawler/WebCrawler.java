package info.kgeorgiy.ja.labazov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
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
            Crawler kralya = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost);
            kralya.download(args[0], depth);
            kralya.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> visited = ConcurrentHashMap.newKeySet();
        final Set<String> successful = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> failed = new ConcurrentHashMap<>();
        Queue<String> queue = new ConcurrentLinkedQueue<>();
        final Phaser phaser = new Phaser(1);

        queue.add(url);

        while (phaser.getPhase() < depth && !queue.isEmpty()) {
            final Queue<String> newQueue = new ConcurrentLinkedQueue<>();

            while (!queue.isEmpty()) {
                String currentUrl = queue.poll();

                if (!visited.add(currentUrl)) {
                    continue;
                }

                downloadUrl(depth, successful, failed, phaser, newQueue, currentUrl);
            }
            phaser.arriveAndAwaitAdvance();
            queue = newQueue;
        }

        return new Result(new ArrayList<>(successful), failed);
    }

    private void downloadUrl(int depth, Set<String> successful, Map<String, IOException> failed, Phaser phaser, Queue<String> newQueue, String url) {
        phaser.register();
        downloaders.submit(() -> {
            try {
                Document res = downloader.download(url);
                if (phaser.getPhase() + 1 < depth) {
                    extractUrl(successful, failed, phaser, newQueue, url, res);
                } else {
                    successful.add(url);
                }
            } catch (IOException e) {
                failed.put(url, e);
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    private void extractUrl(Set<String> successful, Map<String, IOException> failed, Phaser phaser, Queue<String> newQueue, String url, Document document) {
        phaser.register();
        extractors.submit(() -> {
            try {
                newQueue.addAll(document.extractLinks());
                successful.add(url);
            } catch (IOException e) {
                failed.put(url, e);
            } finally {
                phaser.arriveAndDeregister();
            }
        });
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }
}
