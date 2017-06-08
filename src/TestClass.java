import utils.pagequeue.RandomPageNumberGenerator;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class TestClass {
    public static void main(String[] args){
//        List<Integer> callQueue = RandomPageNumberGenerator.read("pages.csv");
//        int current = callQueue.get(0);
//        int count = 1;
//        for (int i = 1; i < callQueue.size(); i++){
//            int next = callQueue.get(i);
//            if (next != current){
//                count++;
//                current = next;
//            }
//        }
//        System.out.println(count);
        RandomPageNumberGenerator.generateWithProcessEqual(8, "pages_85_10.csv");
        RandomPageNumberGenerator.generateWithProcessUnequal(8, "pages_85_10.csv");
    }
}
