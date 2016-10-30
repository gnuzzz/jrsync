package ru.albemuth.jrsync;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static ru.albemuth.jrsync.watcher.ClassUtils.className;

/**
 * @author vovan
 */
public class TestClassUtils {

    @Test
    public void testClassName() {
        assertEquals(
                TestClassUtils.class.getName(),
                className(
                        new File("src\\test\\java"),
                        new File("src\\test\\java\\ru\\albemuth\\jrsync\\TestClassUtils.java")));

        assertEquals(
                TestClassUtils.class.getName(),
                className(
                        new File("src\\test\\java\\"),
                        new File("src\\test\\java\\ru\\albemuth\\jrsync\\TestClassUtils.java")));

        assertEquals(
                TestClassUtils.class.getName(),
                className(
                        new File("src\\test\\java\\"),
                        new File(new File("src\\test\\java\\"),
                                "ru\\albemuth\\jrsync\\TestClassUtils.java")));

        assertEquals(
                TestClassUtils.class.getName(),
                className(
                        new File("src/test/java"),
                        new File("src/test/java/ru/albemuth/jrsync/TestClassUtils.java")));
    }

}
