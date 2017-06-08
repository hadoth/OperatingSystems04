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
    private final int framesAssigned;           // number of frames which have been assigned to the process
    private List<ReadInstruction> callQueue;    // queue of read instructions
    private int[] faultCountGlobal;             // list of  page fault errors recorded in a whole run per process
    private int framesNumber;                   // number of frames available in the memory
    private int initialFrameShare;              // number of frames assigned to program at the start ot its existence
    private int recordingTime;                  // time of single recording session
    private List<Integer> processesPresent;     // list of active processes in the run
    private List<Integer> processesWaiiting;    // list of processes waiting for process time
    private List<PagingAlgorithm> algorithms;   // list of paging algorithms - one per process
    private List<List<ReadInstruction>> waitingQueues;

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
        this.processesWaiiting = new ArrayList<>();
        this.algorithms = new ArrayList<>();
        this.waitingQueues = new ArrayList<>();

        // find the number of processes in the run
        int processCount = 0;                                   // process counter
        for (ReadInstruction instruction : callQueue) {
            if (!processesPresent.contains(instruction.getProcessId())){
                this.processesPresent.add(instruction.getProcessId());
                processCount++;
            }
        }

        // initialization of internally used collections
        this.faultCountGlobal = new int[processCount];

        // initial assignment of fair shares
        for (int i = 0; i < this.processesPresent.size(); i++){
            int evenShare = framesNumber/(this.processesPresent.size() - i);
            this.algorithms.add(new RLUPaging(evenShare));
            this.algorithms.get(i).addObserver(this);
            framesNumber -= evenShare;
        }

        // assertion
        if (framesNumber != 0){
            throw new AssertionError("residue frame number must equal 0: " + framesNumber);
        }
    }

    public void run(){
        int counter = 0;            // read instruction counter

        // iterate over the queue and process the read instructions
        for (ReadInstruction instruction : this.callQueue) {

            if (this.processesPresent.contains(instruction.getProcessId())){
                // if process is on a list of active processes use paging algorithm to process read instruction
                this.algorithms.get(this.processesPresent.indexOf(instruction.getProcessId())).readFormPage(instruction);
            } else if((this.framesNumber - this.framesAssigned) >= this.initialFrameShare){
                // if process is not active, but number of unassigned frames is greater than initial share of frames
                // then add process to list of active processes and handle read instruction
                this.processesPresent.add(instruction.getProcessId());
                this.algorithms.get(this.processesPresent.indexOf(instruction.getProcessId())).readFormPage(instruction);
            } else if (this.processesWaiiting.contains(instruction.getProcessId())){
                // if process is inactive and there is no space for it and it is already waiting in the queue
                this.waitingQueues.get(this.processesWaiiting.indexOf(instruction.getProcessId())).add(instruction);
            } else {
                // process is not on waiting or active list and there is no memory to initialize;
                List<ReadInstruction> temp = new ArrayList<>();
                temp.add(instruction);
                this.waitingQueues.add(temp);
                this.processesPresent.add(instruction.getProcessId());
            }

            // TODO: create HashMap which stores data where key is counter value, and map value is page address; each time you read something, add it to the hash map, every check, delete values with read numbers smaller than

            // increment counter
            counter++;

            // if counter value is greater than or equal to the duration of recording session check local page fault counters and act accordingly
            if (counter >= this.recordingTime){


                // restart recording session
                counter = 0;
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