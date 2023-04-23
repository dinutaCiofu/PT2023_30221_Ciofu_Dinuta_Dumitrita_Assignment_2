package org.example.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Runnable {
    private Integer ID;
    private BlockingQueue<Task> tasks;
    //timpul de asteptare al ultimului client pana termina (durata cozii)
    private AtomicInteger waitingPeriod;
    private boolean open = true;
    private Integer waitingTasks = 0;

    public Server(Integer ID, Integer N) {
        this.tasks = new ArrayBlockingQueue<Task>(N);
        this.waitingPeriod = new AtomicInteger(0);
        this.ID = ID;
    }

    public BlockingQueue<Task> getTasks() {
        return tasks;
    }

    public void setTasks(BlockingQueue<Task> tasks) {
        this.tasks = tasks;
    }

    public Integer getWaitingPeriod() {
        return waitingPeriod.get();
    }

    public void setWaitingPeriod(Integer waitingPeriod) {
        this.waitingPeriod = new AtomicInteger(waitingPeriod);
    }

    public void addTask(Task newTask) {
        tasks.add(newTask);
        waitingPeriod.set(waitingPeriod.addAndGet(newTask.getServiceTime()));
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
                    waitingPeriod.set(waitingPeriod.decrementAndGet());
                    clientServiceTime--;
                    tasks.peek().setServiceTime(clientServiceTime);

                    if (clientServiceTime == 0) {
                        tasks.peek().setServiceTime(0);
                        tasks.poll(); // extrage un client din coada si il elimina din coada
                    }
                } catch (Exception ex) {

                }
            }
            setOpen(false);
        }
    }


    public String toString(){
        if(tasks.peek()==null || getTasks().isEmpty() || tasks.peek().getServiceTime()==0){
            return "closed";
        }
        return tasks.toString();
    }
    public Integer getWaitingTasks() {
        return waitingTasks;
    }
}
