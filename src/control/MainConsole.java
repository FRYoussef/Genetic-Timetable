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

    public static void main(String[] args) {
        sc = new Scanner(System.in);

        while(!loadAtrb()){ }

        try{
            GeneticAlgorithm<String> ga = null;   ///?????
            double mutationProbability = 0.0;
            double crossingProbability = 0.0;
            String lin = "";
            while(!lin.equals("0")) {
            	
            	 FitnessFunction<String> fitnessFunction = TimetableGenAlgoUtil.getFitnessFunction(turns, hmRestrictions,
                         hmPreferences, hmConsecutive, hmTurns);
                 GoalTest<Individual<String>> goalTest = TimetableGenAlgoUtil.getGoalTest(hmRestrictions, turns);
                 // Generate an initial population
                 Set<Individual<String>> population = new HashSet<>();
                 ArrayList<String> alpha = new ArrayList<>(hsAlphabet);
                 for (int i = 0; i < 50; i++) {
                     population.add(TimetableGenAlgoUtil.generateRandomIndividual(turns, alpha, hmRestrictions));
                 }
                 
            	System.out.print("\nMenu \n" + 
            			"0. Exit\n" +
            			"1. Introduce crossingProbability\n" + 
            			"2. Introduce mutationProbability and crossingProbability\n");
            	lin = sc.nextLine();
            	switch (lin){
                case "1":
                	do {
                		System.out.print("crossingProbability(value between 0.0 and 1.0) = ");
                		crossingProbability = Double.parseDouble(sc.nextLine());
                	} while(crossingProbability < 0.0 || crossingProbability > 1.0 );
                	ga = new GeneticAlgorithm<>(TimetableGenAlgoUtil.MAX_TURNS, new ArrayList<>(hsAlphabet), crossingProbability);
                    break;
                case "2":
                	do {
                		System.out.print("mutationProbability(value between 0.0 and 1.0) = ");
                		mutationProbability = Double.parseDouble(sc.nextLine());
                	} while(mutationProbability < 0.0 || mutationProbability > 1.0 );
                	do {
                		System.out.print("crossingProbability(value between 0.0 and 1.0) = ");
                		crossingProbability = Double.parseDouble(sc.nextLine());
                	} while(crossingProbability < 0.0 || crossingProbability > 1.0 );
                	 ga = new GeneticAlgorithm<>(TimetableGenAlgoUtil.MAX_TURNS, new ArrayList<>(hsAlphabet), mutationProbability, crossingProbability);
                    break;
                default:
                    break;
            	}
            	
            
            /*
            System.out.println("Introduce mutationProbability: ");
            double mutationProbability = Double.parseDouble(sc.nextLine());
            System.out.println("Introduce crossingProbability: ");
            double crossingProbability = Double.parseDouble(sc.nextLine());
			
            GeneticAlgorithm<String> ga = new GeneticAlgorithm<>(TimetableGenAlgoUtil.MAX_TURNS,
                    new ArrayList<>(hsAlphabet), mutationProbability, crossingProbability);
            */
            	
            	
            // Run for a set amount of time
            Individual<String> bestIndividual = ga.geneticAlgorithm(population, fitnessFunction, goalTest, 1000L);
            String nom = null;
            for (int i = 0; i < bestIndividual.length(); i++) {

                if((nom=bestIndividual.getRepresentation().get(i)) != null)
                    System.out.println("Turn" + (i+1) + ": " + nom);
                else
                    System.out.println("Turn" + (i+1) + ": ");
            }
        }
        }catch (Exception e){
            e.printStackTrace();
        }

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