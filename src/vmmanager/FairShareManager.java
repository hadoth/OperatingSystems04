package vmmanager;

import pagingalgorithm.PagingAlgorithm;
import pagingalgorithm.RLUPaging;
import utils.ReadInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Karol Pokomeda on 2017-06-08.
 */
public class FairShareManager implements VMManager {
    private List<ReadInstruction> callQueue;
    private int[] errorCount;
    private List<Integer> processesPresent;
    private List<PagingAlgorithm> algorithms;

    public FairShareManager(List<ReadInstruction> callQueue, int framesNumber){
        int processCount = 0;
        List<Integer> pageInstructions = new ArrayList<>();
        List<Integer> processSize = new ArrayList<>();
        this.processesPresent = new ArrayList<>();
        this.algorithms = new ArrayList<>();
        for (ReadInstruction instruction : callQueue) {
            if (!processesPresent.contains(instruction.getProcessId())){
                this.processesPresent.add(instruction.getProcessId());
                processCount++;
                processSize.add(0);
            }
            processSize.set(this.processesPresent.indexOf(instruction.getProcessId()), processSize.get(this.processesPresent.indexOf(instruction.getProcessId()))+1);
        }
        this.callQueue = new ArrayList<>(callQueue);
        this.errorCount = new int[processCount];
        int totalSize = this.callQueue.size();

        for (int i = 0; i < this.processesPresent.size(); i++){
            int fairShare = framesNumber * (processSize.get(i)) / totalSize;
            this.algorithms.add(new RLUPaging(fairShare));
            this.algorithms.get(i).addObserver(this);
            totalSize -= processSize.get(i);
            framesNumber -= fairShare;
        }

        if (framesNumber != 0){
            throw new AssertionError("residue frame number must equal 0: " + framesNumber);
        }
    }

    public void run(){
        for (ReadInstruction instruction : this.callQueue) this.algorithms.get(this.processesPresent.indexOf(instruction.getProcessId())).readFormPage(instruction);
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