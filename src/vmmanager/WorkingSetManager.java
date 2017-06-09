package vmmanager;

import pagingalgorithm.PagingAlgorithm;
import pagingalgorithm.RLUPaging;
import utils.ReadInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karol on 2017-06-08.
 */
public class WorkingSetManager implements VMManager {
    private int framesAssigned;           // number of frames which have been assigned to the process
    private List<ReadInstruction> callQueue;    // queue of read instructions
    private int[] faultCountGlobal;             // list of  page fault errors recorded in a whole run per process
    private int framesNumber;                   // number of frames available in the memory
    private int initialFrameShare;              // number of frames assigned to program at the start ot its existence
    private int recordingTime;                  // time of single recording session
    private List<Integer> processesPresent;     // list of active processes in the run
    private List<Integer> processesWaiting;    // list of processes waiting for process time
    private List<PagingAlgorithm> algorithms;   // list of paging algorithms - one per process
    private List<List<ReadInstruction>> waitingQueues;      // list of lists of read instructions for waiting processes
    private List<List<Integer>> listOfSets;                 // list of workingSets
    private List<PagingAlgorithm> waitingAlgorithms;

    public WorkingSetManager(List<ReadInstruction> callQueue, int framesNumber, int recordingTime, int initialFrameShare){
        // assign arguments to variables
        this.callQueue = new ArrayList<>(callQueue);
        this.framesNumber = framesNumber;
        this.initialFrameShare = initialFrameShare;
        this.recordingTime = recordingTime;

        // initialize internal data
        this.framesAssigned = 0;

        // initialization of internally used collections
        this.processesPresent = new ArrayList<>();
        this.processesWaiting = new ArrayList<>();
        this.algorithms = new ArrayList<>();
        this.waitingQueues = new ArrayList<>();
        this.waitingAlgorithms = new ArrayList<>();

        // find the number of processes in the run
        int processCount = 0;                                   // process counter
        for (ReadInstruction instruction : callQueue) {
            if (!processesPresent.contains(instruction.getProcessId())){
                this.processesPresent.add(instruction.getProcessId());
                processCount++;
            }
        }
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
                this.algorithms.add(new RLUPaging(this.initialFrameShare));
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
                this.processesPresent.add(instruction.getProcessId());
            }

            // at the end of each session
            if ((counter%this.recordingTime) == 0){
                // iterate over active processes and update working sets size
                for (int i = 0; i < this.processesPresent.size(); i++){
                    int framesOld = this.algorithms.get(i).getFrameCount();
                    int framesNew = this.listOfSets.get(i).size();
                    this.listOfSets.get(i).clear();
                    this.framesAssigned -= framesOld;
                    this.framesAssigned += framesNew;
                    this.algorithms.get(i).setFrameCount(framesNew);
                }

                // if there active processes require more space than available, then remove first one and check again
                while (this.framesAssigned > this.framesNumber){
                    this.processesWaiting.add(this.processesPresent.remove(0));
                    PagingAlgorithm algorithm = this.algorithms.remove(0);
                    this.framesAssigned -= algorithm.getFrameCount();
                    this.waitingAlgorithms.add(algorithm);
                }

                boolean flag = false;
                while (this.framesNumber -this.framesAssigned >= this.initialFrameShare && !flag){
                    for (int i = 0; i < this.processesWaiting.size(); i++){
                        if (this.waitingAlgorithms.get(i).getFrameCount() < this.framesNumber){

                        }
                    }
                }
            }
        }
    }

    @Override
    public void update(int processNumber) {
        this.faultCountGlobal[this.processesPresent.indexOf(processNumber)]++;
    }

    public String report(){
        StringBuilder result = new StringBuilder();
        result.append("Paging algorithm executed ");
        result.append(this.callQueue.size());
        result.append(" page calls, resulting with ");
        result.append(this.getTotalFaultPageCount());
        result.append(" total page faults.");
        return result.toString();
    }

    @Override
    public int[] getFaultPageCount() {
        return this.faultCountGlobal;
    }

    @Override
    public int getTotalFaultPageCount() {
        int result = 0;
        for (int count : this.faultCountGlobal){
            result += count;
        }
        return result;
    }
}