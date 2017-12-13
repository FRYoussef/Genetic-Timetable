package model;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import aima.Individual;

public class TimetableFileReader {

    private static final String REGEX1 = ",";
    private static final String REGEX2 = ":";
    private static final String CONSECUTIVE = "+";
    private static final String NOT_CONSECUTIVE = "-";
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

    public HashMap<String, Integer> getHmTurns(int numTeachers) throws Exception {
    	HashMap<String, Integer> hm = new HashMap<>(numTeachers);
        
         String aux[];
         
         for (int i = 0; i < numTeachers; i++) {
             aux = simplifyString(br.readLine());
             if(aux == null)
                 throw new Exception("Error in the input file structure");
             if(aux.length == 2) {
            	 Integer a = Integer.parseInt(aux[1]);
            	 hm.put(aux[0], a);
             }
                 else
                     throw new Exception("Error in the input file structure");
         }
        return hm;
    }
    
    public HashSet<String> getTeachers() throws IOException {
        String teachers[] = br.readLine().replaceAll("\\s+","").split(REGEX1);
        return new HashSet<>(Arrays.asList(teachers));
    }

    /**
     * It returns the teacher´s restrictions from the file
     * @param numTeachers number of teachers
     * @return hashMap with the restrictions
     *  @throws Exception
     */
    public HashMap<String, HashSet<Integer>> getTeacherRestrictions(int numTeachers) throws Exception {
        HashMap<String, HashSet<Integer>> hm = new HashMap<>(numTeachers);
        HashSet<Integer> hs;
        String aux[];
        int rest[];
        for (int i = 0; i < numTeachers; i++) {
            aux = simplifyString(br.readLine());
            if(aux == null)
                throw new Exception("Error in the input file structure");
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

    /**
     * It returns the teacher´s preferences from the file
     * @param numTeachers
     * @return hashMap with the preferences
     * @throws Exception
     */
    public HashMap<String, HashSet<Integer>> getTeacherPreferences(int numTeachers) throws Exception {
        HashMap<String, HashSet<Integer>> hm = new HashMap<>(numTeachers);
        HashSet<Integer> hs;
        String aux[];
        int rest[];
        for (int i = 0; i < numTeachers; i++) {
            aux = simplifyString(br.readLine());
            if(aux == null)
                throw new Exception("Error in the input file structure");
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

    /**
     * It reads from the file if the teacher wants to a consecutive turns "+"
     * or not "-", default is not consecutive
     * @param numTeachers
     * @return
     * @throws Exception
     */
    public HashMap<String, Boolean> getTeacherConsecutivePreferences(int numTeachers) throws Exception {
        HashMap<String, Boolean> hm = new HashMap<>(numTeachers);
        String aux[];
        
        for (int i = 0; i < numTeachers; i++) {
            aux = simplifyString(br.readLine());
            if(aux == null)
                throw new Exception("Error in the input file structure");
            if(aux.length == 2){
                if(aux[1].equals(CONSECUTIVE) || aux[1].equals(NOT_CONSECUTIVE))
                    hm.put(aux[0], aux[1].equals(CONSECUTIVE)?true:false);
                else
                    throw new Exception("Error in the input file structure");

            }
            else
                hm.put(aux[0], false);

        }
        return hm;
    }

    private String[] simplifyString(String str){
       return str == null ? null:str.replaceAll("\\s+","").split(REGEX2);
    }

    public void close() throws IOException {
        br.close();
    }
}
