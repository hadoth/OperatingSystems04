package vmmanager;

import utils.observer.Observer;

/**
 * Created by Karol on 2017-06-07.
 */
public interface VMManager extends Observer {
    void run();
    String report();
    int[] getFaultPageCount();
}
