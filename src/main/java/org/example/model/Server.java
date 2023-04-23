package org.example.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private Integer ID;
    private BlockingQueue<Task> tasks;
    //durata cozii
    private AtomicInteger waitingPeriod;
    private boolean open = true;
    private Integer waitingTasks = 0;

    public Server(Integer ID, Integer N) {
        this.tasks = new ArrayBlockingQueue<Task>(N);
        this.waitingPeriod = new AtomicInteger(0);
        this.ID = ID;
    }

    public Integer getWaitingPeriod() {
        return waitingPeriod.get();
    }

    public void setWaitingPeriod(Integer waitingPeriod) {
        this.waitingPeriod = new AtomicInteger(waitingPeriod);
    }

    public void addTask(Task newTask) {
        tasks.add(newTask);
        //increment the waiting period
        waitingPeriod.set(waitingPeriod.addAndGet(newTask.getServiceTime()));
//        System.out.println("LN 40 Server: waitingPeriod is: "+waitingPeriod + " from server "+ this.ID);
//        System.out.println("The tasks from server "+this.ID + " are: ");
//        for (Task task : tasks){
//            System.out.println(task.toString());
//        }
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public Integer getID() {
        return ID;
    }

    @Override
    public void run() {
        while (open) {
            //peek returneaza primul element din coada fara a-l elimina
            while (tasks.peek() != null) {
                try {
                    Integer clientServiceTime = tasks.peek().getServiceTime();
                    Thread.sleep(1000);
                    // decrementeaza timpul de asteptare al cozii
                    waitingPeriod.set(waitingPeriod.decrementAndGet());
                    // decrementeaza timpul de servire al clientului din coada
                    clientServiceTime--;
                    tasks.peek().setServiceTime(clientServiceTime);

                    //daca un client a fost servit
                    if (clientServiceTime == 0) {
                        tasks.peek().setServiceTime(0);
                        tasks.poll(); // extrage un client din coada si il elimina din coada
                    }
                } catch (Exception ex) {
                    System.out.println("Ceva eroare pe aici, of");
                }
            }
            // dupa ce am procesat toti clientii, inchidem coada
            setOpen(false);
        }
    }


    public String toString(){
        if(tasks.peek()==null || this.tasks.isEmpty() || tasks.peek().getServiceTime()==0){
            return "closed";
        }
        return tasks.toString();
    }
    public Integer getWaitingTasks() {
        return waitingTasks;
    }
}
