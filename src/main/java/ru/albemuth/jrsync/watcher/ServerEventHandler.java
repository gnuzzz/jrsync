package ru.albemuth.jrsync.watcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static ru.albemuth.jrsync.watcher.ClassUtils.fileName;

/**
 * @author vovan
 */
public class ServerEventHandler implements EventHandler {

    private File root;

    public ServerEventHandler(String rootName) {
        this.root = new File(rootName);
        System.out.println("Event handler root: " + root.getAbsolutePath());
    }

    @Override
    public void addClass(String className, byte[] classContent) {
        storeFile(fileName(className), classContent);
    }

    @Override
    public void modifyClass(String className, byte[] classContent) {
        storeFile(fileName(className), classContent);
    }

    @Override
    public void deleteClass(String className) {
        deleteFile(fileName(className));
    }

    private void storeFile(String fileName, byte[] content) {
        int parentIndex = fileName.lastIndexOf(File.separatorChar);
        if (parentIndex >= 0) {
            File fileDir = new File(root, fileName.substring(0, parentIndex));
            if (!fileDir.exists()) {
                if (!fileDir.mkdirs()) {
                    System.err.println("Can't create parent dir " + fileDir);
                }
            }
        }
        File file = new File(root, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteFile(String fileName) {
        File file = new File(root, fileName);
        if (!file.delete()) {
            System.out.println("Warning: file " + fileName + " wasn't deleted");
        }
    }

}
