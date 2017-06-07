package pagingalgorithm;

import utils.observer.Observer;

import java.util.ArrayList;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class RandPaging implements PagingAlgorithm {
    private ArrayList<Integer> queue;
    int maxSize;
    private ArrayList<Observer> observers;

    public RandPaging(int frameNumber){
        this.maxSize = frameNumber - 1;
        this.queue = new ArrayList<>();
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
            if (this.queue.size() == maxSize) this.queue.remove((int)(Math.random()*maxSize));
            this.queue.add(pageNumber);
        }
    }

    @Override
    public String getName() {
        return "Randomized";
    }

    @Override
    public void clear() {
        this.queue.clear();
    }
}
