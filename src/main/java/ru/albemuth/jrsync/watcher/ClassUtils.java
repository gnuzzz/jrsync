package ru.albemuth.jrsync.watcher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public final class ClassUtils {

    private ClassUtils() {
        //do nothing
    }

    public static byte[] readClassContent(InputStream in) throws IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream(in.available());
        byte[] buf = new byte[1024];
        for (int read = in.read(buf); read >= 0; read = in.read(buf)) {
            content.write(buf, 0, read);
        }
        return content.toByteArray();
    }

    public static byte[] readClassContent(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return readClassContent(in);
        }
    }

    public static byte[] readClassContent(Class clazz) throws IOException {
        try (InputStream in = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class")) {
            return readClassContent(in);
        }
    }

    public static void defineClass(Instrumentation instrumentation, Class clazz, byte[] classContent) throws IOException {
        try {
            ClassDefinition classDef = new ClassDefinition(clazz, classContent);
            instrumentation.redefineClasses(classDef);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            System.err.println("Can't modify class " + clazz.getName());
            e.printStackTrace();
        }
    }

    public static String className(File root, File classFile) {
        String rootName = root.getAbsolutePath();
        String classFileName = classFile.getAbsolutePath();
        if (!classFileName.contains(rootName)) {
            throw new IllegalArgumentException("Can't resolve class name for " + classFileName + " relative to " + rootName);
        }
        return classFileName.substring(rootName.length() + 1, classFileName.lastIndexOf('.'))
                .replace(File.separatorChar, '.').replace('$', '.');
    }

    public static String fileName(String className) {
        return className.replace('.', File.separatorChar) + ".class";
    }

    public static Map<File, String> existingClasses(List<String> roots) {
        Map<File, String> ret = new HashMap<>();
        roots.stream().map(File::new).forEach(r -> ret.put(r, existingClass(r, r)));
        return ret;
    }

    public static String existingClass(File root, File dir) {
        File[] files = dir.listFiles();
        if (files == null) return null;
        String className = null;
        for (File file: files) {
            if (!file.isDirectory()) {
                if (file.getName().endsWith(".class")) {
                    className = className(root, file);
                    break;
                }
            } else {
                className = existingClass(root, file);
                if (className != null) {
                    break;
                }
            }
        }
        return className;
    }

}
