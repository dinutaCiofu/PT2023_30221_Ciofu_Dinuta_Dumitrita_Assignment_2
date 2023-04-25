package org.example.business_logic;

import org.example.model.Server;
import org.example.model.Task;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private List<Server> servers;
    private Integer maxNoServers;
    private Integer maxTasksPerServer;
    private List<Thread> threads;

    public Scheduler(Integer maxNoServers, Integer maxTasksPerServer) {
        this.servers = new ArrayList<Server>(maxNoServers);
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
        this.threads = new ArrayList<Thread>(maxNoServers);

        for (int i = 0; i < maxNoServers; i++) {
            servers.add(new Server(i+1, maxNoServers));
            //pentru fiecare coada pornim un fir de executie
            threads.add(new Thread(servers.get(i)));
            threads.get(i).start();
        }
    }

    public String toString() {
        String result = "";
        for (Server server : servers) {
            result += "Queue " + server.getID() + ": " + server.toString() + "\n";
        }
        return result;
    }
    public void killThreads(){
        for(Server server : servers){
            server.setOpen(false);
        }
    }

    public Integer getPeakHour(){
        Integer maxWaitingPeriod = 0;
        for (Server server : servers){
            if(server.getWaitingPeriod() > maxWaitingPeriod){
                maxWaitingPeriod = server.getWaitingPeriod();
            }
        }
        return maxWaitingPeriod;
    }

    public void dispatchTask(Task t) {
        // adauga clientul cu cel mai mic timp de asteptare in coada

        //se cauta server-ul (coada) cu cel mai mic timp de asteptare
        Integer minQueueID = 0;
        Integer minWaitingTime = 999;

        for (int i = 0; i < maxNoServers; i++) {
            if(servers.size() < maxTasksPerServer-1){
                if (servers.get(i).getWaitingPeriod() < minWaitingTime) {
                    minWaitingTime = servers.get(i).getWaitingPeriod();
                    minQueueID = i;

                }
                if (minWaitingTime == 0) {
                    break;
                }
            }
        }

        // adaugam task-ul la serverul cu cel mai mic timp de asteptare
        try {
            servers.get(minQueueID).addTask(t);
        }catch (IllegalStateException ex){

        }
        if (servers.get(minQueueID).isOpen() != true) {
            // marcam server-ul ca fiind deschis
            servers.get(minQueueID).setOpen(true);
            threads.set(minQueueID, new Thread(servers.get(minQueueID)));
            threads.get(minQueueID).start();
        }
    }
}
