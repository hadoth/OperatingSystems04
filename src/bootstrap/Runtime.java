package bootstrap;

import pagingalgorithm.*;
import utils.pagequeue.RandomPageNumberGenerator;
import vmmanager.VMManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class Runtime {
    public static void main(String[] ags){
        int framesNumber = 1024;
        RandomPageNumberGenerator.generate("test.csv", 5, 10);
//        List<Integer> callQueue = RandomPageNumberGenerator.read("test.csv");
//        List<Integer> callQueue = RandomPageNumberGenerator.read("pages.csv");
        List<Integer> callQueue = RandomPageNumberGenerator.read("pages_85_10.csv");

//        callQueue = new ArrayList<>();
//        for (int i = 0; i < 10; i++){
//            for (int j = 0; j < 10; j++) callQueue.add(i);
//        }

        List<PagingAlgorithm> algorithms = new ArrayList<>();

        algorithms.add(new FifoPaging(framesNumber));
        algorithms.add(new OptPaging(framesNumber, callQueue));
        algorithms.add(new RLUPaging(framesNumber));
        algorithms.add(new SecondChancePaging(framesNumber));
        algorithms.add(new RandPaging(framesNumber));

        for (PagingAlgorithm algorithm : algorithms) {
            VMManager manager = new VMManager(algorithm, callQueue);
            manager.run();
            System.out.println(manager.report());
        }
    }
}