package ru.albemuth.jrsync;

/**
 * @author vovan
 */
public class AgentSample {

    //-javaagent:D:\Vovan\lang\albemuth\jrsync\target\jrsync-0.0.1-SNAPSHOT.jar=classpath=D:\Vovan\lang\albemuth\jrsync\data;port=1111
    public static void main(String[] args) {
        for (; true; ) {
            synchronized (AgentSample.class) {
                try {
                    AgentSample.class.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("agent iteration1: " + new AgentClient().getValue());
        }
    }
}
