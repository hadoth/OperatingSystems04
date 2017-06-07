package bootstrap;

import pagingalgorithm.PagingAlgorithm;
import utils.ReadInstruction;
import utils.pagequeue.RandomPageNumberGenerator;
import vmmanager.EvenShareManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class Runtime {
    public static void main(String[] ags){
        int framesNumber = 1024;
        List<ReadInstruction> instructionList = RandomPageNumberGenerator.readReadInstructions("pages_even.csv");


        List<PagingAlgorithm> algorithms = new ArrayList<>();

        EvenShareManager manager = new EvenShareManager(instructionList, framesNumber);
        manager.run();
        System.out.println(manager.report());
    }
}