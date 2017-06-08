package vmmanager;

import pagingalgorithm.PagingAlgorithm;
import pagingalgorithm.RLUPaging;
import utils.ReadInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karol Pokomeda on 2017-06-08.
 */
public class PFFManager implements VMManager {
    private List<ReadInstruction> callQueue;    // queue of read instructions
    private int[] faultCountLocal;              // list of  page fault errors recorded per process in single recording session
    private int[] faultCountGlobal;             // list of  page fault errors recorded in a whole run per process
    private int framesNumber;                   // number of frames available in the memory
    private List<Integer> processesPresent;     // list of all processes in the run
    private List<PagingAlgorithm> algorithms;   // list of paging algorithms - one per process
    private int recordingTime;                  // time of single recording session
    private int notEnough;                      // number of page faults which result in frame number decrease
    private int tooMany;                        // number of page faults which result in frame number increase

    public PFFManager(List<ReadInstruction> callQueue, int framesNumber, int recordingTime, int notEnough, int tooMany){
        // assign arguments to variables
        this.recordingTime = recordingTime;
        this.notEnough = notEnough;
        this.tooMany = tooMany;
        this.callQueue = new ArrayList<>(callQueue);
        this.framesNumber = framesNumber;

        // initialization of internally used collections
        this.processesPresent = new ArrayList<>();
        this.algorithms = new ArrayList<>();

        // find the number of processes in the run
        int processCount = 0;                                   // process counter
        for (ReadInstruction instruction : callQueue) {
            if (!processesPresent.contains(instruction.getProcessId())){
                this.processesPresent.add(instruction.getProcessId());
                processCount++;
            }
        }

        // initialization of internally used collections
        this.faultCountLocal = new int[processCount];
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

            // use paging algorithm to process read instruction
            this.algorithms.get(this.processesPresent.indexOf(instruction.getProcessId())).readFormPage(instruction);

            // increment counter
            counter++;

            // if counter value is greater than or equal to the duration of recording session check loal page fault counters and act accordingly
            if (counter >= this.recordingTime){
                int tooMany = 0;            // number of processes which produced too many page faults
                int framesToSpare = 0;      // number of framesToSpare

                // count processes which are thrashing
                for (int i = 0; i <this.faultCountLocal.length; i++){
                    if (this.faultCountLocal[i] >= this.tooMany){
                        tooMany++;
                    }
                }

                // if there are processes thrashing, then make space for them
                if (tooMany > 0) {
                    for (int i = 0; i < this.faultCountLocal.length; i++) {
                        if (this.faultCountLocal[i] <= this.notEnough) {
                            int framesToCut = this.algorithms.get(i).getFrameCount();
                            int newFrames = framesToCut / 2 > 0 ? framesToCut / 2 : 1;
                            framesToSpare += (framesToCut - newFrames);
                            this.algorithms.get(i).setFrameCount(newFrames);
                        }
                    }
                }

                int totalFrames = 0;
                // assign new space to the processes and assert full use of memory
                for (int i = 0; i < this.processesPresent.size(); i++) {
                    if (this.faultCountLocal[i] >= this.tooMany){
                        int evenShare = framesToSpare/(tooMany);
                        this.algorithms.get(i).setFrameCount(this.algorithms.get(i).getFrameCount()+evenShare);
                        framesToSpare -= evenShare;
                        tooMany--;
                    }
                    totalFrames += this.algorithms.get(i).getFrameCount();
                    this.faultCountLocal[i] = 0;
                }

                if (totalFrames != this.framesNumber){
                    throw new AssertionError("residue frame number must equal 0: " + (this.framesNumber - totalFrames));
                }

                // restart recording session
                counter = 0;
            }
        }
    }

    @Override
    public void update(int processNumber) {
        this.faultCountLocal[this.processesPresent.indexOf(processNumber)]++;
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