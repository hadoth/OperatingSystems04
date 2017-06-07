package vmmanager;

import pagingalgorithm.PagingAlgorithm;
import utils.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class VMManager implements Observer {
    private PagingAlgorithm algorithm;
    private List<Integer> callQueue;
    private int errorCount;

    public VMManager(PagingAlgorithm algorithm, List<Integer> callQueue){
        this.algorithm = algorithm;
        this.callQueue = new ArrayList<>(callQueue);
        this.errorCount = 0;
        this.algorithm.addObserver(this);
    }

    public void run(){
        for (Integer pageNumber : this.callQueue) this.algorithm.readFormPage(pageNumber);
    }

    @Override
    public void update() {
        this.errorCount++;
    }

    public String report(){
        StringBuilder result = new StringBuilder();
        result.append("Paging algorithm ");
        result.append(this.algorithm.getName());
        result.append(" executed ");
        result.append(this.callQueue.size());
        result.append(" page calls, resulting with ");
        result.append(this.errorCount);
        result.append(" page faults.");
        return result.toString();
    }

    public int getFaultPageCont(){
        return this.errorCount;
    }
}
