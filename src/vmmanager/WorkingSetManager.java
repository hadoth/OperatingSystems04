package vmmanager;

import pagingalgorithm.PagingAlgorithm;
import pagingalgorithm.RLUPaging;
import utils.ReadInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Karol on 2017-06-08.
 */
public class WorkingSetManager implements VMManager {
    private int framesAssigned;           // number of frames which have been assigned to the process
    private List<ReadInstruction> callQueue;    // queue of read instructions
    private Map<Integer, Integer> faultCountGlobal;             // list of  page fault errors recorded in a whole run per process
    private int framesNumber;                   // number of frames available in the memory
    private int initialFrameShare;              // number of frames assigned to program at the start ot its existence
    private int recordingTime;                  // time of single recording session
    private List<Integer> processesPresent;     // list of active processes in the run
    private List<Integer> processesWaiting;    // list of processes waiting for process time
    private List<PagingAlgorithm> algorithms;   // list of paging algorithms - one per process
    private List<List<ReadInstruction>> waitingQueues;      // list of lists of read instructions for waiting processes
    private List<List<Integer>> listOfSets;                 // list of workingSets
    private List<PagingAlgorithm> waitingAlgorithms;
    private int queueSize;
    private int masterFault;

    public WorkingSetManager(List<ReadInstruction> callQueue, int framesNumber, int recordingTime, int initialFrameShare){
        // assign arguments to variables
        this.callQueue = new ArrayList<>(callQueue);
        this.framesNumber = framesNumber;
        this.initialFrameShare = initialFrameShare;
        this.recordingTime = recordingTime;
        this.queueSize = this.callQueue.size();

        // initialize internal data
        this.framesAssigned = 0;
        this.masterFault = 0;

        // initialization of internally used collections
        this.processesPresent = new ArrayList<>();
        this.processesWaiting = new ArrayList<>();
        this.algorithms = new ArrayList<>();
        this.waitingQueues = new ArrayList<>();
        this.waitingAlgorithms = new ArrayList<>();
        this.listOfSets = new ArrayList<>();
        this.faultCountGlobal = new HashMap<>();
    }

    public void run(){
        int counter = 0;            // read instruction counter

        // iterate over the queue and process the read instructions
        while (!this.callQueue.isEmpty()) {
            ReadInstruction instruction = this.callQueue.remove(0);
            if (this.processesPresent.contains(instruction.getProcessId())){
                // if process is on a list of active processes use paging algorithm to process read instruction
                int processIndex = this.processesPresent.indexOf(instruction.getProcessId());
                this.algorithms.get(processIndex).readFormPage(instruction);
                if (!this.listOfSets.get(processIndex).contains(instruction.getPageNumber())){
                    this.listOfSets.get(processIndex).add(instruction.getPageNumber());
                }

                // increment counter
                counter++;
            } else if((this.framesNumber - this.framesAssigned) >= this.initialFrameShare){
                // if process is not active, but number of unassigned frames is greater than initial share of frames
                // then add process to list of active processes and handle read instruction
                this.processesPresent.add(instruction.getProcessId());
                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(instruction.getPageNumber());
                this.listOfSets.add(temp);
                PagingAlgorithm algorithm = new RLUPaging(this.initialFrameShare);
                algorithm.addObserver(this);
                this.algorithms.add(algorithm);
                this.algorithms.get(this.processesPresent.indexOf(instruction.getProcessId())).readFormPage(instruction);
                this.framesAssigned += this.initialFrameShare;

                // increment counter
                counter++;
            } else if (this.processesWaiting.contains(instruction.getProcessId())){
                // if process is inactive and there is no space for it and it is already waiting in the queue
                this.waitingQueues.get(this.processesWaiting.indexOf(instruction.getProcessId())).add(instruction);
            } else {
                // process is not on waiting or active list and there is no memory to initialize;
                List<ReadInstruction> temp = new ArrayList<>();
                temp.add(instruction);
                this.waitingQueues.add(temp);
                this.waitingAlgorithms.add(new RLUPaging(this.initialFrameShare));
                this.processesWaiting.add(instruction.getProcessId());
            }

            // at the end of each session
            if (counter == this.recordingTime){
                // iterate over active processes and update working sets size
                for (int i = 0; i < this.processesPresent.size(); i++){
                    int framesOld = this.algorithms.get(i).getFrameCount();
                    int framesNew = this.listOfSets.get(i).size() >= 10 ? this.listOfSets.get(i).size() : 10;
                    this.listOfSets.get(i).clear();
                    this.framesAssigned -= framesOld;
                    this.framesAssigned += framesNew;
                    this.algorithms.get(i).setFrameCount(framesNew);
                }

                // if all active processes require more space than what is available, then remove first one and check again
                while (this.framesAssigned > this.framesNumber){
                    this.processesWaiting.add(this.processesPresent.remove(0));
                    PagingAlgorithm algorithm = this.algorithms.remove(0);
                    this.framesAssigned -= algorithm.getFrameCount();
                    this.waitingAlgorithms.add(algorithm);
                }

                // if there is more space than the initial frame share, then add process from waiting list
                boolean hasChanged = true;           // flag which states if all processes have been set
                while (this.framesNumber - this.framesAssigned >= this.initialFrameShare && hasChanged){
                    hasChanged = false;
                    for (int i = 0; i < this.processesWaiting.size(); i++){
                        if (this.waitingAlgorithms.get(i).getFrameCount() <= this.framesNumber - this.framesAssigned){
                            hasChanged = true;
                            List<ReadInstruction> listToAdd = this.waitingQueues.remove(0);
                            int processToAdd = this.processesWaiting.remove(0);
                            PagingAlgorithm algorithmToAdd = this.waitingAlgorithms.remove(0);
                            while (!listToAdd.isEmpty()){
                                this.callQueue.add(0, listToAdd.remove(listToAdd.size()-1));
                            }
                            this.processesPresent.add(processToAdd);
                            this.algorithms.add(algorithmToAdd);
                            this.framesAssigned += algorithmToAdd.getFrameCount();
                        }
                    }
                }

                counter = 0;
            }
        }
    }

    @Override
    public void update(int processNumber) {
        this.masterFault++;
        this.faultCountGlobal.put(processNumber, this.faultCountGlobal.getOrDefault(processNumber,0) + 1);
    }

    public String report(){
        StringBuilder result = new StringBuilder();
        result.append("Paging algorithm executed ");
        result.append(this.queueSize);
        result.append(" page calls, resulting with ");
        result.append(this.getTotalFaultPageCount());
        result.append(" total page faults.");
        return result.toString();
    }

    @Override
    public int[] getFaultPageCount() {
        int[] result = new int[this.faultCountGlobal.size()];
        for (int i = 0; i <this.processesPresent.size(); i++){
            result[i] = this.faultCountGlobal.get(this.processesPresent.get(i));
        }
        return result;
    }

    @Override
    public int getTotalFaultPageCount() {
        int result = 0;
        for (int key : this.faultCountGlobal.keySet()){
            result += this.faultCountGlobal.get(key);
        }
        return result;
    }
}