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

    public static void main(String[] args) {
        sc = new Scanner(System.in);

        while(!loadAtrb()){ }

        try{
            FitnessFunction<String> fitnessFunction = TimetableGenAlgoUtil.getFitnessFunction(turns, hmRestrictions,
                    hmPreferences);
            GoalTest<Individual<String>> goalTest = TimetableGenAlgoUtil.getGoalTest(hmRestrictions, turns);
            // Generate an initial population
            Set<Individual<String>> population = new HashSet<>();
            ArrayList<String> alpha = new ArrayList<>(hsAlphabet);
            for (int i = 0; i < 50; i++) {
                population.add(TimetableGenAlgoUtil.generateRandomIndividual(turns, alpha, hmRestrictions));
            }

            GeneticAlgorithm<String> ga = new GeneticAlgorithm<>(TimetableGenAlgoUtil.MAX_TURNS,
                    new ArrayList<>(hsAlphabet), 0.15);

            // Run for a set amount of time
            Individual<String> bestIndividual = ga.geneticAlgorithm(population, fitnessFunction, goalTest, 1000L);
            String nom = null;
            for (int i = 0; i < bestIndividual.length(); i++) {

                if((nom=bestIndividual.getRepresentation().get(i)) != null)
                    System.out.println("Turn" + (i+1) + ": " + nom);
                else
                    System.out.println("Turn" + (i+1) + ": ");
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
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
