package org.example.businessLogic;

import org.example.model.Server;
import org.example.model.Task;

import java.util.List;

public class ConcreteStrategyQueue implements Strategy{
    @Override
    public void addTask(List<Server> servers, Task t) {
        Integer ID = 0;
        Integer waitingTasks = 999;

        for(Server server : servers){
            if(server.getWaitingTasks() < waitingTasks){
                waitingTasks = server.getWaitingTasks();
                ID = server.getID();
            }
        }

        for(Server server : servers){
            if(server.getID() == ID){
                server.setOpen(true);
                server.addTask(t);
            }
        }
    }
}
