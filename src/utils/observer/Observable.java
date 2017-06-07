package utils.observer;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public interface Observable {
    void addObserver(Observer observer);
    void deleteObserver(Observer observer);
    void notifyObservers(int processNumber);
}
