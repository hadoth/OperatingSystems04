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
    private List<ReadInstruction> callQueue;
    private int[] errorCount;
    private List<Integer> processesPresent;
    private List<PagingAlgorithm> algorithms;

    public PFFManager(List<ReadInstruction> callQueue, int framesNumber){
        int processCount = 0;
        this.processesPresent = new ArrayList<>();
        this.algorithms = new ArrayList<>();
        for (ReadInstruction instruction : callQueue) {
            if (!processesPresent.contains(instruction.getProcessId())){
                this.processesPresent.add(instruction.getProcessId());
                processCount++;
            }
        }
        this.callQueue = new ArrayList<>(callQueue);
        this.errorCount = new int[processCount];

        for (int i = 0; i < this.processesPresent.size(); i++){
            int evenShare = framesNumber/(this.processesPresent.size() - i);
            this.algorithms.add(new RLUPaging(evenShare));
            this.algorithms.get(i).addObserver(this);
            framesNumber -= evenShare;
        }
        if (framesNumber != 0){
            throw new AssertionError("residue frame number must equal 0: " + framesNumber);
        }
    }

    public void run(){
        int counter = 0;
        for (ReadInstruction instruction : this.callQueue) {
            this.algorithms.get(this.processesPresent.indexOf(instruction.getProcessId())).readFormPage(instruction);
            counter++;
            if (counter == 100){
                counter = 0;
                for (int i = 0; i < this.processesPresent.size(); i++) {
                    System.out.println("process " + this.processesPresent.get(i) + " generated " + this.errorCount[i] + "page faults");
                    this.errorCount[i] = 0;
                }
            }
        }
    }

    @Override
    public void update(int processNumber) {
        this.errorCount[this.processesPresent.indexOf(processNumber)]++;
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
        return this.errorCount;
    }

    @Override
    public int getTotalFaultPageCount() {
        int result = 0;
        for (int count : this.errorCount){
            result += count;
        }
        return result;
    }
}