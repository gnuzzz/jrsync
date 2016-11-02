package ru.albemuth.jrsync;

import org.junit.Test;
import org.zeroturnaround.javarebel.IntegrationFactory;
import ru.albemuth.jrsync.watcher.ClassUtils;

import java.io.File;
import java.lang.instrument.ClassDefinition;

/**
 * @author Vladimir Kornyshev {@literal <vkornyshev@at-consulting.ru>}
 */
public class TestSimple {

    @Test
    public void test() throws Exception {
        AgentClient client = new AgentClient();
        System.out.println(client.getValue());
        ClassDefinition cd = new ClassDefinition(AgentClient.class, ClassUtils.readClassContent(new File("C:/Users/VKornyshev/temp/AgentClient.class")));
        IntegrationFactory.getInstance().redefineClasses(cd);
//        ReloaderFactory.getInstance().
        System.out.println(client.getValue());
        System.out.println(new AgentClient().getValue());
        System.err.println("error1");
    }
}
