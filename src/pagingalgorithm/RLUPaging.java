package pagingalgorithm;

import utils.observer.Observer;

import java.util.ArrayList;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class RLUPaging implements PagingAlgorithm {
    private ArrayList<Integer> queue;
    int maxSize;
    private ArrayList<Observer> observers;

    public RLUPaging(int frameNumber){
        this.maxSize = frameNumber;
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
        if (this.queue.contains(pageNumber)){
            this.queue.remove(this.queue.indexOf(pageNumber));
            this.queue.add(pageNumber);
//            System.out.print(Arrays.toString(queue.toArray()));
//            System.out.println("\t read: " + pageNumber);
        } else {
            this.notifyObservers();
            if (this.queue.size() == this.maxSize) this.queue.remove(0);
            this.queue.add(pageNumber);
//            System.out.print(Arrays.toString(queue.toArray()));
//            System.out.println("\t add new: " + pageNumber);
        }
    }

    @Override
    public String getName() {
        return "LRU";
    }

    @Override
    public void clear() {
        this.queue.clear();
    }
}
