package ru.albemuth.jrsync.watcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class ClasspathWatcher implements Runnable {

    private ClassFileQueue classFileQueue;
    private List<File> classpath;
    private long period;
    private boolean running;
    private Map<File, WatchDir> dirs;

    public ClasspathWatcher(ClassFileQueue classFileQueue, List<String> rootNames, long period) {
        this.classFileQueue = classFileQueue;
        this.classpath = rootNames.stream().map(File::new).collect(Collectors.toList());
        this.period = period;
        this.dirs = new HashMap<>();
    }

    @Override
    public void run() {
        running = true;
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            for (File root: classpath) {
                addDir(watcher, root, root, false);
            }

            for (; running; ) {
                WatchKey key = watcher.poll(period, TimeUnit.MILLISECONDS);
                if (!running) break;
                if (key == null) continue;

                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    Path watchablePath = (Path) key.watchable();
                    Path filePath = ev.context();
                    File watchableFile = watchablePath.toFile();
                    File file = new File(watchableFile, filePath.toFile().getName());
                    WatchDir wd = dirs.get(watchableFile);

                    if (dirs.containsKey(file) || file.isDirectory()) {
                        if (kind == ENTRY_CREATE) {
                            addDir(watcher, wd.root, file, true);
                        } else if (kind == ENTRY_MODIFY) {
                            //???
                        } else if (kind == ENTRY_DELETE) {
                            removeDir(file);
                        }
                    } else {
                        if (kind == ENTRY_CREATE) {
                            classFileQueue.classFileCreated(wd.root, file);
                        } else if (kind == ENTRY_MODIFY) {
                            classFileQueue.classFileModified(wd.root, file);
                        } else if (kind == ENTRY_DELETE) {
                            classFileQueue.classFileDeleted(wd.root, file);
                        }
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    key.cancel(); //???
                }
            }

            watcher.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            running = false;
        }
    }

    public Thread start() {
        Thread thread = new Thread(this);
        thread.start();
        return thread;
    }

    public synchronized void stop() {
        running = false;
    }

    private void addDir(WatchService watcher, File root, File dir, boolean addFiles) throws IOException {
        WatchKey key = dir.toPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        dirs.put(dir, new WatchDir(key, root, dir));

        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file: files) {
            if (file.isDirectory()) {
                addDir(watcher, root, file, addFiles);
            } else if (addFiles) {
                classFileQueue.classFileCreated(root, file);
            }
        }
    }

    private void removeDir(File dir) throws IOException {
        WatchDir wd = dirs.remove(dir);
        wd.key.cancel();

        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file: files) {
            if (file.isDirectory()) {
                removeDir(file);
            } else {
                classFileQueue.classFileDeleted(wd.root, file);
            }
        }
    }

    private static class WatchDir {

        private final WatchKey key;
        private final File root;
        private final File dir;

        WatchDir(WatchKey key, File root, File dir) {
            this.key = key;
            this.root = root;
            this.dir = dir;
        }

        public WatchKey getKey() {
            return key;
        }

        public File getRoot() {
            return root;
        }

        public File getDir() {
            return dir;
        }
    }

}
