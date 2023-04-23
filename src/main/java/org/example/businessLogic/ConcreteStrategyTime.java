package org.example.businessLogic;

import org.example.model.Server;
import org.example.model.Task;

import java.util.List;

public class ConcreteStrategyTime implements Strategy{
    @Override
    public void addTask(List<Server> servers, Task t) {
        Integer ID = 0;
        Integer waitingPeriod = 999;

        for(Server server : servers){
            if(server.getWaitingPeriod() < waitingPeriod){
                waitingPeriod=server.getWaitingPeriod();
                ID=server.getID();
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
