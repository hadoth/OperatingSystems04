package pagingalgorithm;

import utils.observer.Observable;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public interface PagingAlgorithm extends Observable{
    void readFormPage(int pageNumber);
    String getName();
    void clear();
}
