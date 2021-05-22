package info.kgeorgiy.ja.labazov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;

public class WebCrawler implements AdvancedCrawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHost;
    private final Map<String, PageQueue> limitedQueues;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        limitedQueues = new ConcurrentHashMap<>();
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
            /* Switch technology */
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
            Crawler crowler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost);
            crowler.download(args[0], depth);
            crowler.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Result download(String url, int depth) {
        return download(url, depth, host -> true);
    }

    private void downloadUrl(int depth, Set<String> successful, Map<String, IOException> failed, Phaser phaser, Queue<String> newQueue, String url, Predicate<String> allowed) {
        try {
            final String hostName = URLUtils.getHost(url);
            if (!allowed.test(hostName)) {
                return;
            }
            final PageQueue pageQueue = limitedQueues.computeIfAbsent(hostName, host -> new PageQueue());

            phaser.register();

            pageQueue.submit(() -> {
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
                    pageQueue.next();
                }
            });
        } catch (MalformedURLException e) {
            failed.put(url, e);
        }
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

    @Override
    public Result download(String url, int depth, List<String> hosts) {
        final Set<String> allowed = new HashSet<>(hosts);
        return download(url, depth, allowed::contains);
    }

    private Result download(String url, int depth, Predicate<String> allowed) {
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

                downloadUrl(depth, successful, failed, phaser, newQueue, currentUrl, allowed);
            }
            phaser.arriveAndAwaitAdvance();
            queue = newQueue;
        }

        return new Result(new ArrayList<>(successful), failed);
    }

    private class PageQueue {
        private final Queue<Runnable> queue = new ArrayDeque<>();
        private int busy;

        public synchronized void submit(final Runnable runnable) {
            if (busy < perHost) {
                busy++;
                downloaders.submit(runnable);
            } else {
                queue.add(runnable);
            }
        }

        public synchronized void next() {
            final Runnable runnable = queue.poll();
            if (runnable == null) {
                busy--;
            } else {
                downloaders.submit(runnable);
            }
        }
    }
}
