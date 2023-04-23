package org.example.business_logic;

import org.example.model.Task;
import org.example.single_point_access.GUIFrameSinglePointAccess;
import org.example.view.UserInterface;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SimulationManager implements Runnable {

    // data read from UI
    public Integer timeLimit = 0;
    public Integer maxProcessingTime = 0;
    public Integer minProcessingTime = 0;
    public Integer numberOfServers = 0;
    public Integer minArrivalTime = 0;
    public Integer maxArrivalTime = 0;
    public Integer numberOfClients = 0;
    //
    private Integer avgTime = 0;
    private Integer peakHour = 0;
    private Integer avgServiceTime = 0;
    private File resultsFile;
    public SelectionPolicy selectionPolicy = SelectionPolicy.SHORTEST_TIME;
    //entitate responsabila cu management-ul cozilor si distribuirea clientilor
    private Scheduler scheduler;
    //frame pentru afisarea simularii
    private UserInterface userInterface;
    //pool of tasks
    private List<Task> generatedTasks;
    private boolean validare = false;
    private boolean isRunning = false; // flag pentru a indica daca simularea este in desfasurare

    public SimulationManager(File output) {
        this.resultsFile = output;
        //initialise frame to display simulation
        userInterface = new UserInterface();
        GUIFrameSinglePointAccess.changePanel(userInterface.getMainPanel(), "QUEUES MANAGEMENT APPLICATION");

        //set up a semaphore with initial count 0
        Semaphore semaphore = new Semaphore(0);
        Semaphore startSemaphore = new Semaphore(0);

        // add action listener on validare button
        userInterface.getValidareBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isRunning = false;

                String nbOfClientsStr = userInterface.getClientiTextField().getText();
                String nbOfQueuesStr = userInterface.getCoziTextField().getText();
                String simulationTimeStr = userInterface.getSimulareTextField().getText();
                String minArrivalTimeStr = userInterface.getMinSosireTextField().getText();
                String maxArrivalTimeStr = userInterface.getMaxSosireTextField().getText();
                String minProcessingTimeStr = userInterface.getMinServireTextField().getText();
                String maxProcessingTimeStr = userInterface.getMaxServireTextField().getText();

//                if(nbOfClientsStr.isEmpty()){
//                    System.out.println("wtf "+nbOfClientsStr);
//                }

                if (!nbOfClientsStr.isEmpty() && !nbOfQueuesStr.isEmpty() && !simulationTimeStr.isEmpty() && !minArrivalTimeStr.isEmpty() && !maxArrivalTimeStr.isEmpty() && !minProcessingTimeStr.isEmpty() && !maxProcessingTimeStr.isEmpty()) {
                    validare = true;
                    System.out.println("Ajunge aici");
                    numberOfClients = Integer.parseInt(nbOfClientsStr);
                    numberOfServers = Integer.parseInt(nbOfQueuesStr);
                    timeLimit = Integer.parseInt(simulationTimeStr);
                    minArrivalTime = Integer.parseInt(minArrivalTimeStr);
                    maxArrivalTime = Integer.parseInt(maxArrivalTimeStr);
                    minProcessingTime = Integer.parseInt(minProcessingTimeStr);
                    maxProcessingTime = Integer.parseInt(maxProcessingTimeStr);
                } else {
                    validare = false;
                    GUIFrameSinglePointAccess.showDialogMessage("Invalid input");
                }
                semaphore.release();
            }
        });

        // add action listener on start button
        userInterface.getStartBtn().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(validare == true){
                    isRunning = true;
                    startSemaphore.release();
                }else{
                    GUIFrameSinglePointAccess.showDialogMessage("Valideaza datele");
                }

            }
        });

        try {
            //astept pana cand semaforul este eliberat
            semaphore.acquire();
            startSemaphore.acquire();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        //   System.out.println(validare);

        System.out.println("Inputs in SimulationManager");
        System.out.println("Nb of clients: " + numberOfClients);
        System.out.println("Nb of servers: " + numberOfServers);
        System.out.println("Time limit: " + timeLimit);
        System.out.println("min arrival time: " + minArrivalTime);
        System.out.println("max arrival time: " + maxArrivalTime);
        System.out.println("min processing time: " + minProcessingTime);
        System.out.println("max processing time: " + maxProcessingTime);

        if (validare) {
            try {
                this.resultsFile.createNewFile();
            } catch (Exception ex) {
                System.out.println("File could not be created");
            }
        }
        //generate numberOfClients clients and store them to generatedTasks
        generateNRandomTasks();
        //initialize the scheduler
        scheduler = new Scheduler(numberOfServers, numberOfClients);
    }

    private void generateNRandomTasks() {
        generatedTasks = new ArrayList<Task>(numberOfClients);
        for (int i = 0; i < numberOfClients; i++) {
            Integer randArrivingTime = minArrivalTime + (int) (Math.random() * (maxArrivalTime - minArrivalTime));
            Integer randServiceTime = minProcessingTime + (int) (Math.random() * (maxProcessingTime - minProcessingTime));
//            System.out.println(i + " randArrivingTime: " + randArrivingTime);
//            System.out.println(i + " randServiceTime: " + randServiceTime);
            Task t = new Task(i + 1, randArrivingTime, randServiceTime);
            System.out.println("Task " + (i + 1) + " created");
            generatedTasks.add(t);
        }

        //sort the list based on arrival time
        Collections.sort(generatedTasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return Integer.compare(o1.getArrivalTime(), o2.getArrivalTime());
            }
        });
    }

    public String displayResult(Integer currentTime) {
        String result = "\nTime: " + currentTime + "\nWaiting clients: ";
        for (Task task : generatedTasks) {
            result += task.toString();
        }
        result += "\n" + scheduler.toString();

        return result;
    }

    private Integer getMaxWaitingTime(Integer maxWaitingTime) {
        if (generatedTasks.isEmpty()) {
            maxWaitingTime = scheduler.getPeakHour();
        } else {
            maxWaitingTime--;
        }
        return maxWaitingTime;
    }

    @Override
    public void run() {
        if (validare == true && isRunning == true) {
            FileWriter resultsWriter = null;
            try {
                resultsWriter = new FileWriter(this.resultsFile.toString());
            } catch (Exception ex) {
                System.out.println("Exception from FileWriter.");
                System.out.println(ex.getMessage());
            }

            Integer currentTime = 0;
            Integer maxWaitingTimeTmp = 0;

            while (currentTime < timeLimit && (!generatedTasks.isEmpty() || maxWaitingTimeTmp > 0)) {
                //iterate generatedTasks list and pick tasks that have the
                //arrivalTime equal with the current time

                while (!generatedTasks.isEmpty() && generatedTasks.get(0).getArrivalTime() == currentTime) {
                    scheduler.dispatchTask(generatedTasks.get(0));
                    if (currentTime + generatedTasks.get(0).getServiceTime() < timeLimit) {
                        System.out.println("*******************************\nA intrat in if");
                        avgTime += generatedTasks.get(0).getArrivalTime();
                        avgServiceTime += generatedTasks.get(0).getServiceTime();
                        System.out.println("ln 184: avgTime is: " + avgTime);
                    }
                    generatedTasks.remove(0);
                }
                //update the UI frame
                String result = displayResult(currentTime);
                userInterface.getResult().setText(userInterface.getResult().getText() + "\n" + result);
                System.out.println(result);

                try {
                    resultsWriter.write(result);
                } catch (Exception ex) {
                    System.out.println("Exception from resultWriter");
                    System.out.println(ex.getMessage());
                }
                // timpul de asteptare maxim pentru ca un client sa intre intr-o coada
                maxWaitingTimeTmp = getMaxWaitingTime(maxWaitingTimeTmp);
                System.out.println("LN 202: maxWaitingTime " + maxWaitingTimeTmp);
                currentTime++;
                System.out.println("currentTime: " + currentTime);
                if (maxWaitingTimeTmp < 0) {
                    Integer temp = maxWaitingTimeTmp * (-1);
                    if (temp == currentTime && temp > peakHour) {
                        peakHour = temp;
                    }
                }
                //wait an interval of 1 second
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    System.out.println("Exception from Thread.sleep(1000)");
                    System.out.println(ex.getMessage());
                }
            }
            scheduler.killThreads();
            try {
                resultsWriter.write("Average waiting time: " + (avgTime / numberOfClients));
                resultsWriter.write("Average service time: " + (avgServiceTime / numberOfClients));
                resultsWriter.write("\nPeak hour: " + peakHour + "\n");
                String peakHourStr = "\nPeak hour: " + peakHour + "\n";
                String avgTimeStr = "Average waiting time: " + (avgTime / numberOfClients) + "\n";
                String avgServiceTimeStr = "Average service time: " + (avgServiceTime / numberOfClients) + "\n";
                userInterface.getResult().setText(userInterface.getResult().getText().concat(avgTimeStr));
                userInterface.getResult().setText(userInterface.getResult().getText().concat(avgServiceTimeStr));
                userInterface.getResult().setText(userInterface.getResult().getText().concat(peakHourStr));

            } catch (Exception ex) {
                System.out.println("Exception from resultWriter");
                System.out.println(ex.getMessage());
            }

            try {
                resultsWriter.close();
            } catch (Exception ex) {
                System.out.println("Exception from closing the file.");
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void main(String args[]) {
        try {
            SimulationManager manager = new SimulationManager(new File("results.txt"));
            Thread simulationThread = new Thread(manager);
            simulationThread.start();
            try {
                simulationThread.join();
            } catch (Exception ex) {
                System.out.println("Exception from join");
            }
        } catch (Exception ex) {
            System.out.println("Exception from start");
            System.out.println(ex.getMessage());
        }
    }
}
