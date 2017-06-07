package bootstrap;

import pagingalgorithm.PagingAlgorithm;
import pagingalgorithm.RandPaging;
import utils.pagequeue.RandomPageNumberGenerator;
import vmmanager.VMManager;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by KPokomeda on 11.05.2017.
 */
public class RandomTester {
    public static void main(String[] ags){
        int framesNumber = 1024;
        int repetitions = 50000;
//        ArrayList<Integer> callQueue = RandomPageNumberGenerator.read("pages_85_10.csv");
        ArrayList<Integer> callQueue = RandomPageNumberGenerator.read("pages.csv");

        ArrayList<PagingAlgorithm> algorithms = new ArrayList<>();

        PagingAlgorithm algorithm = new RandPaging(framesNumber);

        int faultCount = 0;
        for (int i = 0; i < repetitions; i++) {
            VMManager manager = new VMManager(algorithm, callQueue);
            manager.run();
            faultCount += manager.getFaultPageCont();
            if (i%1000 == 0) System.out.println(manager.report() + " (" + faultCount/(i+1) + ") " + " " + new Date());
            algorithm.clear();
        }

        System.out.println(faultCount/repetitions);
    }
}
