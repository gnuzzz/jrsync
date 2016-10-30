package ru.albemuth.jrsync;

/**
 * @author vovan
 */
public class AgentClient {

    public String getValue() {
//        processInternal();
//        return "value333";
        return processInternal2();
    }

//    private void processInternal() {
//        System.out.println("agent client internal");
//    }

    private String processInternal2() {
        return new AgentClient4().getValue();
    }

}
