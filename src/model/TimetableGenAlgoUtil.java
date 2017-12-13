package model;

import aima.FitnessFunction;
import aima.GoalTest;
import aima.Individual;

import java.util.*;

public class TimetableGenAlgoUtil {

    public static final int MAX_TURNS = 16;
    private static Random r = new Random();

    /**
     * It returns the goal test associated
     * @param hmRestrictions
     * @return
     */
    public static GoalTest<Individual<String[]>> getGoalTest(HashMap<String, HashSet<Integer>> hmRestrictions, int turns)
    {
        return new TimetableGoalTest(hmRestrictions, turns);
    }

    public static FitnessFunction<String[]> getFitnessFunction(int turns, HashMap<String, HashSet<Integer>> hmRestrictions,
                                                                        HashMap<String, HashSet<Integer>> hmPreferences,
                                                                        HashMap<String, Boolean> hmConsecutive,
                                                                        HashMap<String, Integer> hmTurnsMade)
    {
        return new TimetableFitnessFunction(hmRestrictions, hmPreferences,hmConsecutive,hmTurnsMade,turns);
    }


    public static Individual<String[]> generateRandomIndividual(int turns, ArrayList<String> alphabet,
                                                              HashMap<String, HashSet<Integer>> hmRestrictions,
                                                              int teacherPerTurn)
    {
        Individual<String[]> indi = getRandomIndividual(turns, alphabet, teacherPerTurn);

        while(!testRestrictions(indi, hmRestrictions))
            indi = getRandomIndividual(turns, alphabet, teacherPerTurn);
        
        return indi;
    }

    /**
     * It returns a random individual
     * @param turns to create an individual
     * @return the individual
     */
    private static Individual<String[]> getRandomIndividual(int turns, ArrayList<String> alphabet, int teacherPerTurn){
        ArrayList<String[]> indi = new ArrayList<>(MAX_TURNS);
        for (int i = 0; i < MAX_TURNS; i++)
            indi.add(new String[teacherPerTurn]);

        int pos;
        int []posAlphabet = new int[teacherPerTurn];
        int elems = 0;
        while(elems != turns){
            pos = r.nextInt(MAX_TURNS);

            //checking different names per turn
            for (int i = 0; i < posAlphabet.length; i++){
                posAlphabet[i] = r.nextInt(alphabet.size());
                for (int j = 0; j < i; j++) {
                    if(posAlphabet[i] == posAlphabet[j])
                        i--;
                }
            }

            if(indi.get(pos)[0] == null) {
                indi.set(pos, new String[teacherPerTurn]);

                for (int i = 0; i < teacherPerTurn; i++)
                    indi.get(pos)[i] = alphabet.get(posAlphabet[i]);

                elems++;
            }
        }

        return new Individual<>(indi);
    }

    /**
     * A test to verify if an individual satisfy the restrictions
     * @param ind
     * @param hmRestrictions
     * @return
     */
    private static boolean testRestrictions(Individual<String[]> ind, HashMap<String, HashSet<Integer>> hmRestrictions){
        for (int i = 0; i < ind.length(); i++) {
            if(ind.getRepresentation().get(i)[0] != null){
                for (int j = 0; j < ind.getRepresentation().get(i).length; j++) {
                    if((hmRestrictions.get(ind.getRepresentation().get(i)[j])).contains(i+1))
                        return false;
                }
            }
        }
        return true;
    }

    /** 
     * It returns the number of strings repeated in an individual, or -1 if
     * it is not satisfy turns
     * @param ind
     * @param turns
     * @return
     */
    private static int testRepetitions(Individual<String[]> ind, int turns){
        HashMap<String, Integer> hmRepetitions = new HashMap<>();
        int counter = 0;
        int counterTurns = 0;
        for (String[] s : ind.getRepresentation()){
            if(s[0] != null){
                counterTurns++;
                for (String te : s) {
                    if(!hmRepetitions.containsKey(te))
                        hmRepetitions.put(te, 1);
                    else
                        hmRepetitions.replace(te, hmRepetitions.get(te)+1);
                }
            }
        }

        for(String s : hmRepetitions.keySet())
            counter += hmRepetitions.get(s) - 1;

        if(counterTurns != turns)
            return -1;
        return counter;
    }

    private static class TimetableGoalTest implements GoalTest<Individual<String[]>>{
        private HashMap<String, HashSet<Integer>> hmRestrictions;
        private int turns;
        
        public TimetableGoalTest(HashMap<String, HashSet<Integer>> hmRestrictions, int turns) {
            this.hmRestrictions = hmRestrictions;
            this.turns = turns;
        }

        @Override
        public boolean test(Individual<String[]> individual) {
            return testRestrictions(individual, hmRestrictions) && testRepetitions(individual, turns) != -1;
        }
    }

    private static class TimetableFitnessFunction implements FitnessFunction<String[]>{
        private HashMap<String, HashSet<Integer>> hmRestrictions;
        private HashMap<String, HashSet<Integer>> hmPreferences;
        private HashMap<String, Boolean> hmConsecutive;
        private int turns;
        HashMap<String, Integer> hmTurnsMade;
        
        public TimetableFitnessFunction(HashMap<String, HashSet<Integer>> hmRestrictions,
                                        HashMap<String, HashSet<Integer>> hmPreferences,
                                        HashMap<String, Boolean> hmConsecutive,
                                        HashMap<String, Integer> hmTurnsMade,
                                        int turns)
        {
            this.hmRestrictions = hmRestrictions;
            this.hmPreferences = hmPreferences;
            this.hmConsecutive = hmConsecutive;
            this.hmTurnsMade = hmTurnsMade;
            this.turns = turns;
        }

        @Override
        public double apply(Individual<String[]> individual) {
            double remainder = (countPreferences(individual)+0.5d) - (testRepetitions(individual, turns) * 0.5d)
                                + countConsecutives(individual) + countPrefTurns(individual)*0.1d;
            return remainder < 0 ? 0: remainder;
        }

        private int countPreferences(Individual<String[]> indi){
            int preferences = 0;
            for (int i = 0; i < indi.length(); i++) {
                if(indi.getRepresentation().get(i)[0] != null){
                    for(String te : indi.getRepresentation().get(i)){
                        if((hmPreferences.get(te)).contains(i+1))
                            preferences++;
                    }
                }
            }
            return preferences;
        }
        
        private double countPrefTurns(Individual<String[]> indi) {
        	int numTeacher = hmTurnsMade.size();

        	int totalTurnsMade = 0;

        	HashMap<String, Integer> newHmTurnsMade = (HashMap<String, Integer>)hmTurnsMade.clone();
        	for(String[] s : indi.getRepresentation()){
        	    if(s[0] != null){
                    for (String te : s){
                        newHmTurnsMade.replace(te, newHmTurnsMade.get(te) + 1);
                        totalTurnsMade += newHmTurnsMade.get(te);
                    }
                }
            }

        	int turnsPerTeacher = Math.round(totalTurnsMade / numTeacher);
            int count = 0;
            for (String te : newHmTurnsMade.keySet())
                count -= Math.abs(turnsPerTeacher - newHmTurnsMade.get(te));

        	return count;
        }
      
        private double countConsecutives(Individual<String[]> indi){
            double counter = 0.0d;
            HashMap<String, HashSet<Integer>> hmIndi = new HashMap<>(indi.length());
            HashSet<Integer> hs;
            for (int i = 0; i < indi.length(); i++) {
                if(indi.getRepresentation().get(i)[0] != null){
                    for(String te : indi.getRepresentation().get(i)){
                        if(hmIndi.containsKey(te))
                            hmIndi.get(te).add(i);
                        else{
                            hs = new HashSet<>();
                            hs.add(i);
                            hmIndi.put(te, hs);
                        }
                    }
                }
            }

            for(String str: hmIndi.keySet()){
                if(hmIndi.get(str).size() > 1){
                    int consec = 0;
                    int prev = -1;
                    for(Integer turn: hmIndi.get(str)){
                        if(prev != -1 && turn-prev == 1)
                            consec++;
                        prev = turn;
                    }

                    if(hmConsecutive.get(str) && consec == 0) counter -= 0.3d;
                    else if(hmConsecutive.get(str) && consec > 0) counter += 0.3d;
                    else if(!hmConsecutive.get(str) && consec == 0) counter += 0.3d;
                    else if(!hmConsecutive.get(str) && consec > 0) counter -= 0.3d;
                }
            }

            return counter;
        }
    }
}
