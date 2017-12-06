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
    public static GoalTest<Individual<String>> getGoalTest(HashMap<String, HashSet<Integer>> hmRestrictions, int turns)
    {
        return new TimetableGoalTest(hmRestrictions, turns);
    }

    public static FitnessFunction<String> getFitnessFunction(int turns, HashMap<String, HashSet<Integer>> hmRestrictions,
                                                                        HashMap<String, HashSet<Integer>> hmPreferences,
                                                                        HashMap<String, Boolean> hmConsecutive)
    {
        return new TimetableFitnessFunction(hmRestrictions, hmPreferences,hmConsecutive,turns);
    }


    public static Individual<String> generateRandomIndividual(int turns, ArrayList<String> alphabet,
                                                              HashMap<String, HashSet<Integer>> hmRestrictions)
    {
        Individual<String> indi = getRandomIndividual(turns, alphabet);

        while(!testRestrictions(indi, hmRestrictions))
            indi = getRandomIndividual(turns, alphabet);

        return indi;
    }

    /**
     * It returns a random individual
     * @param turns to create an individual
     * @return the individual
     */
    private static Individual<String> getRandomIndividual(int turns, ArrayList<String> alphabet){
        String [] indi = new String[MAX_TURNS];
        int pos;
        int posAlphabet;
        int elems = 0;
        while(elems != turns){
            pos = r.nextInt(MAX_TURNS);
            posAlphabet = r.nextInt(alphabet.size());
            if(indi[pos] == null || indi[pos].equals("")) {
                indi[pos] = alphabet.get(posAlphabet);
                elems++;
            }
        }

        return new Individual<>(new ArrayList<>(Arrays.asList(indi)));
    }

    /**
     * A test to verify if an individual satisfy the restrictions
     * @param ind
     * @param hmRestrictions
     * @return
     */
    private static boolean testRestrictions(Individual<String> ind, HashMap<String, HashSet<Integer>> hmRestrictions){
        for (int i = 0; i < ind.length(); i++) {
            if(ind.getRepresentation().get(i) != null){
                if((hmRestrictions.get(ind.getRepresentation().get(i))).contains(i+1))
                    return false;
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
    private static int testRepetitions(Individual<String> ind, int turns){
        HashMap<String, Integer> hmRepetitions = new HashMap<>();
        int counter = 0;
        for (String s : ind.getRepresentation()){
            if(s != null && !hmRepetitions.containsKey(s))
                hmRepetitions.put(s, 1);
            else if(s != null)
                hmRepetitions.replace(s, hmRepetitions.get(s), hmRepetitions.get(s)+1);
        }

        for(String s : hmRepetitions.keySet())
            counter += hmRepetitions.get(s) - 1;

        if(hmRepetitions.size() + counter != turns)
            return -1;
        return counter;
    }

    private static class TimetableGoalTest implements GoalTest<Individual<String>>{
        private HashMap<String, HashSet<Integer>> hmRestrictions;
        private int turns;

        public TimetableGoalTest(HashMap<String, HashSet<Integer>> hmRestrictions, int turns) {
            this.hmRestrictions = hmRestrictions;
            this.turns = turns;
        }

        @Override
        public boolean test(Individual<String> individual) {
            return testRestrictions(individual, hmRestrictions) && testRepetitions(individual, turns) != -1;
        }
    }

    private static class TimetableFitnessFunction implements FitnessFunction<String>{
        private HashMap<String, HashSet<Integer>> hmRestrictions;
        private HashMap<String, HashSet<Integer>> hmPreferences;
        private HashMap<String, Boolean> hmConsecutive;
        private int turns;

        public TimetableFitnessFunction(HashMap<String, HashSet<Integer>> hmRestrictions,
                                        HashMap<String, HashSet<Integer>> hmPreferences,
                                        HashMap<String, Boolean> hmConsecutive,
                                        int turns)
        {
            this.hmRestrictions = hmRestrictions;
            this.hmPreferences = hmPreferences;
            this.hmConsecutive = hmConsecutive;
            this.turns = turns;
        }

        @Override
        public double apply(Individual<String> individual) {
            if(!testRestrictions(individual, hmRestrictions))
                return 0.0d;
            int repetitions = testRepetitions(individual, turns);
            if(repetitions == -1)
                return 0.0d;
            double remainder = (countPreferences(individual)+0.5d) - (repetitions * 0.5d) + countConsecutives(individual);
            return remainder < 0 ? 0: remainder;
        }

        private int countPreferences(Individual<String> indi){
            int preferences = 0;
            for (int i = 0; i < indi.length(); i++) {
                if(indi.getRepresentation().get(i) != null){
                    if((hmPreferences.get(indi.getRepresentation().get(i))).contains(i+1))
                        preferences++;
                }
            }
            return preferences;
        }

        private double countConsecutives(Individual<String> indi){
            double counter = 0.0d;
            HashMap<String, HashSet<Integer>> hmIndi = new HashMap<>(indi.length());
            HashSet<Integer> hs;
            for (int i = 0; i < indi.length(); i++) {
                if(indi.getRepresentation().get(i) != null){
                    if(hmIndi.containsKey(indi.getRepresentation().get(i)))
                        hmIndi.get(indi.getRepresentation().get(i)).add(i);
                    else{
                        hs = new HashSet<>();
                        hs.add(i);
                        hmIndi.put(indi.getRepresentation().get(i), hs);
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
