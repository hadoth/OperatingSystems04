package pagingalgorithm;

import utils.observer.Observer;

import java.util.ArrayList;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class SecondChancePaging implements PagingAlgorithm {
    private ArrayList<Integer> queue;
    int maxSize;
    private ArrayList<Observer> observers;
    ArrayList<Boolean> secondChances;

    public SecondChancePaging(int frameNumber){
        this.maxSize = frameNumber - 1;
        this.queue = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.secondChances = new ArrayList<>();
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
        int index;
        if ((index = this.queue.indexOf(pageNumber)) >= 0){
            secondChances.set(index, true);
        } else {
            this.notifyObservers();
            if (this.queue.size() == maxSize) this.makeSpace();
            this.queue.add(pageNumber);
            this.secondChances.add(true);
        }
    }

    @Override
    public String getName() {
        return "Second Chance";
    }

    @Override
    public void clear() {
        this.queue.clear();
    }


    private void makeSpace() {
        while (this.queue.size() == maxSize){
            int i = 0;
            while (i < this.maxSize){
                if (this.secondChances.get(i)) this.secondChances.set(i, false);
                else{
                    this.secondChances.remove(i);
                    this.queue.remove(i);
                    return;
                }
            }
        }
    }
}
