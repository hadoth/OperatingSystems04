package pagingalgorithm;

import utils.observer.Observer;

import java.util.ArrayList;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class OptPaging implements PagingAlgorithm {
    private ArrayList<Integer> future;
    private ArrayList<Integer> queue;
    int maxSize;
    private ArrayList<Observer> observers;

    public OptPaging(int frameNumber, ArrayList<Integer> future){
        this.maxSize = frameNumber;
        this.queue = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.future = (ArrayList<Integer>)(future.clone());
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
        this.future.remove(0);
        if (!this.queue.contains(pageNumber)){
            this.notifyObservers();
            if (this.queue.size() == this.maxSize) this.makeSpace();
            this.queue.add(pageNumber);
        }
    }

    @Override
    public String getName() {
        return "Optimal";
    }

    @Override
    public void clear() {
        this.queue.clear();
    }

    private void makeSpace(){
        Integer lastIndex = this.future.indexOf(this.queue.get(0));
        if (lastIndex == -1){
            this.queue.remove(0);
        }
        Integer contender;
        for (int i = 1; i < this.queue.size(); i++){
            contender = this.future.indexOf(this.queue.get(i));
            if (contender == -1){
                this.queue.remove(i);
                return;
            }
            if (contender > lastIndex) lastIndex = contender;
        }
        if (lastIndex > -1) this.queue.remove(this.queue.indexOf(this.future.get(lastIndex)));
    }
}
