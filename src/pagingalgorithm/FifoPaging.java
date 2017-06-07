package pagingalgorithm;

import utils.observer.Observer;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class FifoPaging implements PagingAlgorithm {
    private ArrayBlockingQueue<Integer> queue;
    private ArrayList<Observer> observers;

    public FifoPaging(int frameNumber){
        this.queue = new ArrayBlockingQueue<>(frameNumber);
        this.observers = new ArrayList<>();
    }

    @Override
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void deleteObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : this.observers) observer.update();
    }

    @Override
    public void readFormPage(int pageNumber) {
        if (!this.queue.contains(pageNumber)){
            this.notifyObservers();
            if (this.queue.remainingCapacity() == 0) this.queue.remove();
            this.queue.add(pageNumber);
        }
    }

    @Override
    public String getName() {
        return "FIFO";
    }

    @Override
    public void clear() {
        this.queue.clear();
    }
}
