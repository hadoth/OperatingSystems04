package utils.pagequeue;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * Created by KPokomeda on 10.05.2017.
 */
public class RandomPageNumberGenerator {
    public static List<Integer> read(String filePath) {
        File output = new File(filePath);
        ArrayList<Integer> result = new ArrayList<>();

        try {
            FileReader fReader = new FileReader(output);
            Scanner scanner = new Scanner(fReader);

            while (scanner.hasNextInt()) result.add(scanner.nextInt());

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void generate(String filePath, int pages, int calls) {
        File output = new File(filePath);

        try {
            FileWriter fWriter = new FileWriter(output);
            PrintWriter pWriter = new PrintWriter(fWriter);

            output.createNewFile();
            for (int i = 0; i < calls; i++) {
                pWriter.println(((int) (Math.random() * pages)));
            }

            pWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateWithProcessEqual(int processCount, String filePath) {
        List<Integer> listOfPages = RandomPageNumberGenerator.read(filePath);
        int maxValue = listOfPages.get(0);
        int pretender;
        Iterator<Integer> iterator = listOfPages.iterator();
        while (iterator.hasNext()) {
            if ((pretender = iterator.next()) > maxValue) {
                maxValue = pretender;
            }
        }

        int pagesPerProcess = maxValue / processCount;

        List<Integer> listOfProcesses = new ArrayList<>();
        for (Integer pageNumber : listOfPages) {
            listOfProcesses.add(pageNumber / pagesPerProcess);
        }

        String outputFilePath = filePath.replace(".csv", "_even.csv");
        File output = new File(outputFilePath);

        try {
            FileWriter fWriter = new FileWriter(output);
            PrintWriter pWriter = new PrintWriter(fWriter);

            output.createNewFile();
            for (int i = 0; i < listOfProcesses.size(); i++) {
                pWriter.println(listOfPages.get(i) + "," + listOfProcesses.get(i));
            }

            pWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}