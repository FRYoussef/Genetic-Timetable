package control;

import aima.FitnessFunction;
import aima.GeneticAlgorithm;
import aima.GoalTest;
import aima.Individual;
import model.TimetableFileReader;
import model.TimetableGenAlgoUtil;

import java.util.*;

public class MainConsole {

    private static Scanner sc;
    private static int turns = 0;
    private static HashSet<String> hsAlphabet;
    private static HashMap<String, HashSet<Integer>> hmRestrictions;
    private static HashMap<String, HashSet<Integer>> hmPreferences;
    private static HashMap<String, Boolean> hmConsecutive;
    private static HashMap<String, Integer> hmTurns;
    private static double mutationProbability = 0.15;
    private static double crossingProbability = 0.7;
    private static boolean crossingWithAPoint = true;
    private static boolean aimaMutation = true;
    private static int selectionType = GeneticAlgorithm.ROULETTE_SELECTION;
    private static int teachersPerTurn = 1;



    public static void main(String[] args) {
        sc = new Scanner(System.in);

        while(!loadAtrb()){ }

        try{
            String lin = "";
            while(!lin.equals("0")) {
            	System.out.print("\n0. Exit\n" +
            			"1. Introduce the crossing Probability(default 0.7)\n" +
            			"2. Introduce mutation Probability(default 0.15)\n" +
                        "3. Switch the points of crossing (default 1)\n" +
                        "4. Switch the mutation type (default a random gen)\n" +
                        "5. Switch the selection type (default roulette)\n" +
                        "6. Switch teachers per turn (default 1)\n" +
                        "7. Run\n");
            	lin = sc.nextLine();
            	switch (lin){
            	    case "1":
                        do {
                            System.out.print("A value between 0.0 and 1.0 = ");
                            crossingProbability = Double.parseDouble(sc.nextLine());
                        } while(crossingProbability < 0.0 || crossingProbability > 1.0 );
                        break;
                    case "2":
                        do {
                            System.out.print("A value between 0.0 and 1.0 = ");
                            mutationProbability = Double.parseDouble(sc.nextLine());
                        } while(mutationProbability < 0.0 || mutationProbability > 1.0 );
                        break;
                    case "3":
                        System.out.println("OK!!! Switched\n");
                        crossingWithAPoint = !crossingWithAPoint;
                        break;
                    case "4":
                        System.out.println("OK!!! Switched\n");
                        aimaMutation = !aimaMutation;
                        break;
                    case "5":
                        System.out.println("OK!!! Switched\n");
                        if(selectionType == GeneticAlgorithm.ROULETTE_SELECTION)
                            selectionType = GeneticAlgorithm.TOURNAMENT_SELECTION;
                        else
                            selectionType = GeneticAlgorithm.ROULETTE_SELECTION;
                        break;
                    case "6":
                        System.out.println("OK!!! Switched\n");
                        if(teachersPerTurn == 1)
                            teachersPerTurn = 2;
                        else
                            teachersPerTurn = 1;
                        break;
                    case "7":
                        runGen();
                        break;
                default:
                    break;
            	}
        }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void runGen(){
        FitnessFunction<String[]> fitnessFunction = TimetableGenAlgoUtil.getFitnessFunction(turns, hmRestrictions,
                hmPreferences, hmConsecutive, hmTurns);
        GoalTest<Individual<String[]>> goalTest = TimetableGenAlgoUtil.getGoalTest(hmRestrictions, turns);
        // Generate an initial population
        Set<Individual<String[]>> population = new HashSet<>();
        ArrayList<String> alpha = new ArrayList<>(hsAlphabet);
        for (int i = 0; i < 50; i++) {
            population.add(TimetableGenAlgoUtil.generateRandomIndividual(turns, alpha, hmRestrictions, teachersPerTurn));
        }

        GeneticAlgorithm<String> ga = new GeneticAlgorithm<String>(TimetableGenAlgoUtil.MAX_TURNS, alpha,
                            mutationProbability, crossingProbability, crossingWithAPoint, aimaMutation, selectionType, teachersPerTurn);
        // Run for a set amount of time
        Individual<String[]> bestIndividual = ga.geneticAlgorithm(population, fitnessFunction, goalTest, 1000L);
        for (int i = 0; i < bestIndividual.length(); i++) {

            if(bestIndividual.getRepresentation().get(i)[0] != null){
                System.out.print("Turn" + (i+1) + ": " + bestIndividual.getRepresentation().get(i)[0]);
                for(int j = 1; j < bestIndividual.getRepresentation().get(i).length; j++){
                    System.out.print(", " + bestIndividual.getRepresentation().get(i)[j]);
                }
                System.out.println();
            }

            else
                System.out.println("Turn" + (i+1) + ": ");
        }
        System.out.println("Fitness         = " + fitnessFunction.apply(bestIndividual));
        System.out.println("Is Goal         = " + goalTest.test(bestIndividual));
        System.out.println("Population Size = " + ga.getPopulationSize());
        System.out.println("Iterations      = " + ga.getIterations());
        System.out.println("Took            = " + ga.getTimeInMilliseconds() + "ms.");
    }

    private static boolean loadAtrb(){
        System.out.print("Please, introduce the configuration file: ");

        try {
            TimetableFileReader reader = new TimetableFileReader(sc.nextLine());
            turns = reader.getTurns();
            hsAlphabet = reader.getTeachers();
            hmRestrictions = reader.getTeacherRestrictions(hsAlphabet.size());
            hmPreferences = reader.getTeacherPreferences(hsAlphabet.size());
            hmConsecutive = reader.getTeacherConsecutivePreferences(hsAlphabet.size());
            hmTurns = reader.getHmTurns(hsAlphabet.size());
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}