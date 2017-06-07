package pagingalgorithm;

import utils.ReadInstruction;
import utils.observer.Observable;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public interface PagingAlgorithm extends Observable{
    void readFormPage(ReadInstruction instruction);
    String getName();
    void clear();
}
