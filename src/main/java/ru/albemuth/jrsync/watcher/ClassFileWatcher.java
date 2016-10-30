package ru.albemuth.jrsync.watcher;

import java.io.File;
import java.nio.file.WatchEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static ru.albemuth.jrsync.watcher.ClassUtils.readClassContent;

/**
 * @author vovan
 */
public class ClassFileWatcher implements Runnable {

    private EventHandler eventHandler;
    private long period;
    private long threshhold;
    private boolean running;
    private PriorityQueue<ClassFile> filesQueue;
    private Map<ClassFileKey, ClassFile> filesMap;

    public ClassFileWatcher(EventHandler eventHandler, long period, long threshhold) {
        this.eventHandler = eventHandler;
        this.period = period;
        this.threshhold = threshhold;
        this.filesQueue = new PriorityQueue<>();
        this.filesMap = new HashMap<>();
    }

    public void classFileCreated(File root, File file) {
        addToQueue(root, file, ENTRY_CREATE);
    }

    public void classFileModified(File root, File file) {
        addToQueue(root, file, ENTRY_MODIFY);
    }

    public void classFileDeleted(File root, File file) {
        addToQueue(root, file, ENTRY_DELETE);
    }

    @Override
    public void run() {
        running = true;

        for (; running; ) {
            waitForUpdates();
            if (!running) break;

            while (processOldestClassFile()) {
                //do nothing
            }
        }
    }

    private synchronized boolean processOldestClassFile() {
        if (filesQueue.isEmpty()) return false;
        ClassFile classFile = filesQueue.peek();
        if (!classFile.isExpired()) return false;
        try {
            String classname = ClassUtils.className(classFile.root, classFile.file);
            if (classFile.action == ENTRY_CREATE) {
                eventHandler.addClass(classname, ClassUtils.readClassContent(classFile.file));
            } else if (classFile.action == ENTRY_MODIFY) {
                eventHandler.modifyClass(classname, ClassUtils.readClassContent(classFile.file));
            } else if (classFile.action == ENTRY_DELETE) {
                eventHandler.deleteClass(classname);
            }
        } catch (Exception e) {
            System.err.println("Exception while processing file " + classFile.file);
            e.printStackTrace();
        }
        filesQueue.poll();
        filesMap.remove(new ClassFileKey(classFile.root, classFile.file));
        return true;
    }

    private synchronized void addToQueue(File root, File file, WatchEvent.Kind action) {
        ClassFileKey key = new ClassFileKey(root, file);
        ClassFile classFile = filesMap.get(key);
        if (classFile == null) {
            classFile = new ClassFile(root, file, action, System.currentTimeMillis() + threshhold);
            filesMap.put(key, classFile);
            filesQueue.add(classFile);
        } else {
            try {
                classFile.action = getAction(classFile.action, action);
                classFile.expires = System.currentTimeMillis() + threshhold;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                classFile.action = null; //this removes classFile from queue on next iteration
            }
        }
    }

    public Thread start() {
        Thread thread = new Thread(this);
        thread.start();
        return thread;
    }

    public synchronized void stop() {
        running = false;
        notify();
    }

    private synchronized void waitForUpdates() {
        try {
            wait(period);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private WatchEvent.Kind getAction(WatchEvent.Kind prev, WatchEvent.Kind next) {
        WatchEvent.Kind action = null;
        if (prev == ENTRY_CREATE) {
            if (next == ENTRY_CREATE) {
                throw new IllegalStateException("illegal state - create after create");
            } else if (next == ENTRY_MODIFY) {
                action = ENTRY_CREATE;
            } else if (next == ENTRY_DELETE) {
                action = null;
            }
        } else if (prev == ENTRY_MODIFY) {
            if (next == ENTRY_CREATE) {
                throw new IllegalStateException("illegal state - create after modify");
            } else if (next == ENTRY_MODIFY) {
                action = ENTRY_MODIFY;
            } else if (next == ENTRY_DELETE) {
                action = ENTRY_DELETE;
            }
        } else if (prev == ENTRY_DELETE) {
            if (next == ENTRY_CREATE) {
                action = ENTRY_MODIFY;
            } else if (next == ENTRY_MODIFY) {
                throw new IllegalStateException("illegal state - modify after delete");
            } else if (next == ENTRY_DELETE) {
                throw new IllegalStateException("illegal state - delete after delete");
            }
        }
        return action;
    }

    private static class ClassFile implements Comparable<ClassFile> {
        private File root;
        private File file;
        private WatchEvent.Kind action;
        private long expires;

        public ClassFile(File root, File file, WatchEvent.Kind action, long expires) {
            this.root = root;
            this.file = file;
            this.action = action;
            this.expires = expires;
        }

        public File getRoot() {
            return root;
        }

        public File getFile() {
            return file;
        }

        public WatchEvent.Kind getAction() {
            return action;
        }

        public void setAction(WatchEvent.Kind action) {
            this.action = action;
        }

        public long getExpires() {
            return expires;
        }

        public void setExpires(long expires) {
            this.expires = expires;
        }

        public boolean isExpired() {
            return expires < System.currentTimeMillis();
        }

        @Override
        public int compareTo(ClassFile o) {
            return (int)(o.expires - expires);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassFile that = (ClassFile) o;
            return Objects.equals(root, that.root) &&
                    Objects.equals(file, that.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(root, file);
        }
    }

    private static class ClassFileKey {
        private File root;
        private File file;

        public ClassFileKey(File root, File file) {
            this.root = root;
            this.file = file;
        }

        public File getRoot() {
            return root;
        }

        public File getFile() {
            return file;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassFileKey that = (ClassFileKey) o;
            return Objects.equals(root, that.root) &&
                    Objects.equals(file, that.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(root, file);
        }
    }
}
