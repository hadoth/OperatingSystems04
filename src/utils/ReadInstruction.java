package utils;

/**
 * Created by Karol on 2017-06-07.
 */
public class ReadInstruction {
    private int pageNumber;
    private int processId;

    public ReadInstruction(int pageNumber, int processId){
        this.pageNumber = pageNumber;
        this.processId = processId;

    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getProcessId() {
        return processId;
    }
}
