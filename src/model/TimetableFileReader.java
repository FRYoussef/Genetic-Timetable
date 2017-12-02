package model;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimetableFileReader {

    private static final String REGEX1 = ",";
    private static final String REGEX2 = ":";
    private String file = null;
    private BufferedReader br = null;

    public TimetableFileReader(String file) throws FileNotFoundException {
        this.file = file;
        br = new BufferedReader(new FileReader(new File(file)));
    }

    public int getTurns() throws Exception{
        int turns = Integer.parseInt(br.readLine());
        if(turns < 1 || turns > TimetableGenAlgoUtil.MAX_TURNS)
            throw new Exception("Error: the number of turns should be between 1 and 16");
        return turns;
    }

    public HashSet<String> getTeachers() throws IOException {
        String teachers[] = br.readLine().replaceAll("\\s+","").split(REGEX1);
        return new HashSet<>(Arrays.asList(teachers));
    }

    /**
     * It returns the teacher´s restrictions from the file
     * @param numTeachers number of teachers
     * @return hashMap with the restrictions
     *  @throws IOException
     */
    public HashMap<String, HashSet<Integer>> getTeacherRestrictions(int numTeachers) throws IOException {
        HashMap<String, HashSet<Integer>> hm = new HashMap<>(numTeachers);
        HashSet<Integer> hs;
        String aux[];
        int rest[];
        for (int i = 0; i < numTeachers; i++) {
            aux = br.readLine().replaceAll("\\s+","").split(REGEX2);
            if(aux.length == 2) {
                rest = Arrays.stream(aux[1].split(REGEX1)).mapToInt(Integer::parseInt).toArray();
                hs = IntStream.of(rest).boxed().collect(Collectors.toCollection(HashSet::new));
            }
            else
                hs = new HashSet<>(0);

            hm.put(aux[0], hs);
        }
        return hm;
    }

    /**
     * It returns the teacher´s preferences from the file
     * @param numTeachers
     * @return hashMap with the preferences
     * @throws IOException
     */
    public HashMap<String, HashSet<Integer>> getTeacherPreferences(int numTeachers) throws IOException {
        HashMap<String, HashSet<Integer>> hm = new HashMap<>(numTeachers);
        HashSet<Integer> hs;
        String aux[];
        int rest[];
        for (int i = 0; i < numTeachers; i++) {
            aux = br.readLine().replaceAll("\\s+","").split(REGEX2);
            if(aux.length == 2){
                rest = Arrays.stream(aux[1].split(REGEX1)).mapToInt(Integer::parseInt).toArray();
                hs = IntStream.of(rest).boxed().collect(Collectors.toCollection(HashSet::new));
            }
            else
                hs = new HashSet<>(0);

            hm.put(aux[0], hs);
        }
        return hm;
    }


    public void close() throws IOException {
        br.close();
    }
}
