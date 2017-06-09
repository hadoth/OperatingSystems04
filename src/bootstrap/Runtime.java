package bootstrap;

import utils.ReadInstruction;
import utils.pagequeue.RandomPageNumberGenerator;
import vmmanager.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class Runtime {
    public static void main(String[] ags){
        int framesNumber = 1024;
        List<ReadInstruction> instructionList = RandomPageNumberGenerator.readReadInstructions("pages_uneven.csv");


        List<VMManager> managers = new ArrayList<>();

        managers.add(new EvenShareManager(instructionList, framesNumber));
        managers.add(new FairShareManager(instructionList, framesNumber));
//        managers.add(new PFFManager(instructionList, framesNumber, 100, 5, 30));
        managers.add(new PFFManager(instructionList, framesNumber, 100, 10, 30));
//        managers.add(new PFFManager(instructionList, framesNumber, 100, 15, 30));
//
//        managers.add(new PFFManager(instructionList, framesNumber, 50, 10, 30));
//        managers.add(new PFFManager(instructionList, framesNumber, 100, 10, 30));
//        managers.add(new PFFManager(instructionList, framesNumber, 150, 10, 30));
//
//
//        managers.add(new PFFManager(instructionList, framesNumber, 100, 10, 15));
//        managers.add(new PFFManager(instructionList, framesNumber, 100, 10, 30));
//        managers.add(new PFFManager(instructionList, framesNumber, 100, 10, 45));
        managers.add(new WorkingSetManager(instructionList, framesNumber, 1024, 128));

        for (VMManager manager : managers) {
            manager.run();
            System.out.println(manager.report());
        }
    }
}