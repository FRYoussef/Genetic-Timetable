package model;

import aima.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): Figure 4.8, page
 * 129.<br>
 * <br>
 *
 * <pre>
 * function GENETIC-ALGORITHM(population, FITNESS-FN) returns an individual
 *   inputs: population, a set of individuals
 *           FITNESS-FN, a function that measures the fitness of an individual
 *
 *   repeat
 *     new_population &lt;- empty set
 *     for i = 1 to SIZE(population) do
 *       x &lt;- RANDOM-SELECTION(population, FITNESS-FN)
 *       y &lt;- RANDOM-SELECTION(population, FITNESS-FN)
 *       child &lt;- REPRODUCE(x, y)
 *       if (small random probability) then child &lt;- MUTATE(child)
 *       add child to new_population
 *     population &lt;- new_population
 *   until some individual is fit enough, or enough time has elapsed
 *   return the best individual in population, according to FITNESS-FN
 * --------------------------------------------------------------------------------
 * function REPRODUCE(x, y) returns an individual
 *   inputs: x, y, parent individuals
 *
 *   n &lt;- LENGTH(x); c &lt;- random number from 1 to n
 *   return APPEND(SUBSTRING(x, 1, c), SUBSTRING(y, c+1, n))
 * </pre>
 *
 * Figure 4.8 A genetic algorithm. The algorithm is the same as the one
 * diagrammed in Figure 4.6, with one variation: in this more popular version,
 * each mating of two parents produces only one offspring, not two.
 *
 * @author Ciaran O'Reilly
 * @author Mike Stampone
 * @author Ruediger Lunde
 *
 * @param <A>
 *            the type of the alphabet used in the representation of the
 *            individuals in the population (this is to provide flexibility in
 *            terms of how a problem can be encoded).
 */
public class GeneticAlgorithm<A> {
    protected static final String POPULATION_SIZE = "populationSize";
    protected static final String ITERATIONS = "iterations";
    protected static final String TIME_IN_MILLISECONDS = "timeInMSec";
    public static final int ROULETTE_SELECTION = 1;
    public static final int TOURNAMENT_SELECTION = 2;
    //
    protected Metrics metrics = new Metrics();
    //
    protected int individualLength;
    protected List<A> finiteAlphabet;
    protected double mutationProbability;
    protected double crossingProbability;
    protected boolean crossingWithAPoint;
    protected boolean aimaMutation;
    protected int selectionType;
    protected int teachersPerTurn;


    protected Random random;
    private List<ProgressTracker<A>> progressTrackers = new ArrayList<ProgressTracker<A>>();

    public GeneticAlgorithm(int individualLength, Collection<A> finiteAlphabet, double crossingProbability) {
    	this(individualLength, finiteAlphabet,crossingProbability, true,new Random());
    }
    
    public GeneticAlgorithm(int individualLength, Collection<A> finiteAlphabet, double mutationProbability,
                            double crossingProbability) {
        this(individualLength, finiteAlphabet, mutationProbability, crossingProbability, true,
                ROULETTE_SELECTION,true, 1,new Random());
    }

    public GeneticAlgorithm(int individualLength, Collection<A> finiteAlphabet, double mutationProbability,
                            double crossingProbability, boolean crossingWithAPoint, boolean aimaMutation, int selectionType
                            , int teachersPerTurn) {
        this(individualLength, finiteAlphabet, mutationProbability, crossingProbability, crossingWithAPoint,
                selectionType,aimaMutation, teachersPerTurn,new Random());
    }

    public GeneticAlgorithm(int individualLength, Collection<A> finiteAlphabet, double crossingProbability, boolean crossingWithAPoint, Random random) {
		this.individualLength = individualLength;
		this.finiteAlphabet = new ArrayList<A>(finiteAlphabet);
		this.crossingProbability = crossingProbability;
		this.crossingWithAPoint = crossingWithAPoint;
		this.random = random;

		assert (this.crossingProbability >= 0.0 && this.crossingProbability <= 1.0);
	}
    
    public GeneticAlgorithm(int individualLength, Collection<A> finiteAlphabet, double mutationProbability,
                            double crossingProbability, boolean crossingWithAPoint, int selectionType,boolean aimaMutation,
                            int teachersPerTurn, Random random) {
        this.individualLength = individualLength;
        this.finiteAlphabet = new ArrayList<A>(finiteAlphabet);
        this.mutationProbability = mutationProbability;
        this.crossingProbability = crossingProbability;
        this.crossingWithAPoint = crossingWithAPoint;
        this.aimaMutation = aimaMutation;
        this.selectionType = selectionType;
        this.teachersPerTurn = teachersPerTurn;

        assert (this.mutationProbability >= 0.0 && this.mutationProbability <= 1.0);
        assert (this.crossingProbability >= 0.0 && this.crossingProbability <= 1.0);
    }

    /** Progress tracers can be used to display progress information. */
    public void addProgressTracer(ProgressTracker<A> pTracer) {
        progressTrackers.add(pTracer);
    }

    /**
     * Starts the genetic algorithm and stops after a specified number of
     * iterations.
     */
    public Individual<A[]> geneticAlgorithm(Collection<Individual<A[]>> initPopulation,
                                            FitnessFunction<A[]> fitnessFn, final int maxIterations) {
        GoalTest<Individual<A[]>> goalTest = state -> getIterations() >= maxIterations;
        return geneticAlgorithm(initPopulation, fitnessFn, goalTest, 0L);
    }

    /**
     * Template method controlling search. It returns the best individual in the
     * specified population, according to the specified FITNESS-FN and goal
     * test.
     *
     * @param initPopulation
     *            a set of individuals
     * @param fitnessFn
     *            a function that measures the fitness of an individual
     * @param goalTest
     *            test determines whether a given individual is fit enough to
     *            return. Can be used in subclasses to implement additional
     *            termination criteria, e.g. maximum number of iterations.
     * @param maxTimeMilliseconds
     *            the maximum time in milliseconds that the algorithm is to run
     *            for (approximate). Only used if > 0L.
     * @return the best individual in the specified population, according to the
     *         specified FITNESS-FN and goal test.
     */
    // function GENETIC-ALGORITHM(population, FITNESS-FN) returns an individual
    // inputs: population, a set of individuals
    // FITNESS-FN, a function that measures the fitness of an individual
    public Individual<A[]> geneticAlgorithm(Collection<Individual<A[]>> initPopulation, FitnessFunction<A[]> fitnessFn,
                                          GoalTest<Individual<A[]>> goalTest, long maxTimeMilliseconds) {
        Individual<A[]> bestIndividual = null;

        // Create a local copy of the population to work with
        List<Individual<A[]>> population = new ArrayList<>(initPopulation);
        // Validate the population and setup the instrumentation
        validatePopulation(population);
        updateMetrics(population, 0, 0L);
        
        long startTime = System.currentTimeMillis();

        // repeat
        int itCount = 0;
        do {
            population = nextGeneration(population, fitnessFn, goalTest);
            bestIndividual = retrieveBestIndividual(population, fitnessFn);

            updateMetrics(population, ++itCount, System.currentTimeMillis() - startTime);

            // until some individual is fit enough, or enough time has elapsed
            if (maxTimeMilliseconds > 0L && (System.currentTimeMillis() - startTime) > maxTimeMilliseconds)
                break;
            if (Tasks.currIsCancelled())
                break;
        } while (!goalTest.test(bestIndividual));

        notifyProgressTrackers(itCount, population);
        // return the best individual in population, according to FITNESS-FN
        return bestIndividual;
    }

    public Individual<A[]> retrieveBestIndividual(Collection<Individual<A[]>> population, FitnessFunction<A[]> fitnessFn) {
        Individual<A[]> bestIndividual = null;
        double bestSoFarFValue = Double.NEGATIVE_INFINITY;

        for (Individual<A[]> individual : population) {
            double fValue = fitnessFn.apply(individual);
            if (fValue > bestSoFarFValue) {
                bestIndividual = individual;
                bestSoFarFValue = fValue;
            }
        }

        return bestIndividual;
    }

    /**
     * Sets the population size and number of iterations to zero.
     */
    public void clearInstrumentation() {
        updateMetrics(new ArrayList<Individual<A[]>>(), 0, 0L);
    }

    /**
     * Returns all the metrics of the genetic algorithm.
     *
     * @return all the metrics of the genetic algorithm.
     */
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Returns the population size.
     *
     * @return the population size.
     */
    public int getPopulationSize() {
        return metrics.getInt(POPULATION_SIZE);
    }

    /**
     * Returns the number of iterations of the genetic algorithm.
     *
     * @return the number of iterations of the genetic algorithm.
     */
    public int getIterations() {
        return metrics.getInt(ITERATIONS);
    }

    /**
     *
     * @return the time in milliseconds that the genetic algorithm took.
     */
    public long getTimeInMilliseconds() {
        return metrics.getLong(TIME_IN_MILLISECONDS);
    }

    /**
     * Updates statistic data collected during search.
     *
     * @param itCount
     *            the number of iterations.
     * @param time
     *            the time in milliseconds that the genetic algorithm took.
     */
    protected void updateMetrics(Collection<Individual<A[]>> population, int itCount, long time) {
        metrics.set(POPULATION_SIZE, population.size());
        metrics.set(ITERATIONS, itCount);
        metrics.set(TIME_IN_MILLISECONDS, time);
    }

    //
    // PROTECTED METHODS
    //
    // Note: Override these protected methods to create your own desired
    // behavior.
    //
    /**
     * Primitive operation which is responsible for creating the next
     * generation. Override to get progress information!
     */
    protected List<Individual<A[]>> nextGeneration(List<Individual<A[]>> population, FitnessFunction<A[]> fitnessFn
                                                    , GoalTest<Individual<A[]>> goalTest) {
        List<Individual<A[]>> newPopulation = new ArrayList<Individual<A[]>>(population.size());
        Random random;
    	random = new Random();
        Individual<A[]> x;
        Individual<A[]> y;
        for (int i = 0; i < population.size(); i++) {

            if (selectionType == TOURNAMENT_SELECTION){
                x = tournamentSelection(population, fitnessFn);
                y = tournamentSelection(population, fitnessFn);
            }
            else{
                x = randomSelection(population, fitnessFn);
                y = randomSelection(population, fitnessFn);
            }


            if (random.nextDouble() <= crossingProbability) {
                List<Individual<A[]>> children;
                if (crossingWithAPoint)
                    children = reproduce(x, y, goalTest);
                else
                    children = reproduceTwoPoints(x, y, goalTest);

                //Individual<A> child = reproduce(x, y);
                if (random.nextDouble() <= mutationProbability) {
                    if (aimaMutation) {
                        children.set(0, mutate(children.get(0), goalTest));
                        children.set(1, mutate(children.get(1), goalTest));
                    } else {
                        for (int j = 0; j < children.size(); j++)
                            children.set(j, mutateTwoPoints(children.get(j)));
                    }
                    //child = mutate(child);
                }
                double xValue = fitnessFn.apply(x);
                double yValue = fitnessFn.apply(y);
                double higgherValue = xValue >= yValue ? xValue : yValue;
                double nextHighValue = xValue >= yValue ? yValue : xValue;
                double fChild1 = fitnessFn.apply(children.get(0));
                double fChild2 = fitnessFn.apply(children.get(1));
                //3.3
                if (fChild1 >= higgherValue || fChild2 >= higgherValue) {
                    // add child to new_population
                    Individual<A[]> c;
                    if (fChild1 >= fChild2) {
                        c = children.get(0);
                        fChild1 = 0d;
                    } else {
                        c = children.get(1);
                        fChild2 = 0d;
                    }
                    newPopulation.add(c);
                } else {
                    Individual<A[]> c;
                    if (xValue >= yValue) {
                        c = x;
                        xValue = 0;
                    } else {
                        c = y;
                        yValue = 0;
                    }
                    newPopulation.add(c);
                }

                if (fChild1 >= nextHighValue || fChild2 >= nextHighValue) {
                    // add child to new_population
                    newPopulation.add(fChild1 >= fChild2 ? children.get(0) : children.get(1));
                } else {
                    newPopulation.add(xValue >= yValue ? x : y);
                }
            }
            else {
                newPopulation.add(x);
                newPopulation.add(y);
            }
        }
        notifyProgressTrackers(getIterations(), population);
        return newPopulation;
    }

    protected Individual<A[]> randomSelection(List<Individual<A[]>> population, FitnessFunction<A[]> fitnessFn) {
        // Default result is last individual
        Individual<A[]> selected = population.get(population.size() - 1);
        Random random;
    	random = new Random();
        // Determine all of the fitness values
        double[] fValues = new double[population.size()];
        for (int i = 0; i < population.size(); i++) {
            fValues[i] = fitnessFn.apply(population.get(i));
        }
        // Normalize the fitness values
        fValues = Util.normalize(fValues);
        double prob = 0.0;
        //while(prob == 0.0) prob = random.nextDouble();
        double totalSoFar = 0.0;
        for (int i = 0; i < fValues.length; i++) {
            // Are at last element so assign by default
            // in case there are rounding issues with the normalized values
            totalSoFar += fValues[i];
            if (prob <= totalSoFar) {
                selected = population.get(i);
                break;
            }
        }

        selected.incDescendants();
        return selected;
    }
    
    //Seleccion por torneo(Determin�stica)
    protected Individual<A[]> tournamentSelection(List<Individual<A[]>> population, FitnessFunction<A[]> fitnessFn) {
    	Individual<A[]> selected = population.get(population.size() - 1);
    	int p = 2;

    	Random random = new Random();
    	double[] fValues = new double[p];
    	int r1 = random.nextInt(population.size() - 1);
    	fValues[0] = fitnessFn.apply(population.get(r1));
    	int r2 = random.nextInt(population.size() - 1);
    	fValues[1] = fitnessFn.apply(population.get(r2));
    	
        fValues = Util.normalize(fValues);
        selected = fValues[0] > fValues[1] ? population.get(r1):population.get(r2);
        selected.incDescendants();
    	return selected;
    }
    
    
    // function REPRODUCE(x, y) returns an individual
    // inputs: x, y, parent individuals
    protected List<Individual<A[]>> reproduce(Individual<A[]> x, Individual<A[]> y, GoalTest<Individual<A[]>> goalTest) {
        List<Individual<A[]>> a = new ArrayList<Individual<A[]>>(2);
        do{
            int c = randomOffset(individualLength);
            Individual<A[]> child1Representation = this.reproduceAux(x,y,c);
            Individual<A[]> child2Representation = this.reproduceAux(y,x,c);
            a.add(0, child1Representation);
            a.add(1, child2Representation);
        }while (!goalTest.test(a.get(0)) && !goalTest.test(a.get(1)));

        return a;
    }

    protected Individual<A[]> reproduceAux(Individual<A[]> x, Individual<A[]> y, int c) {
        // return APPEND(SUBSTRING(x, 1, c), SUBSTRING(y, c+1, n))
        List<A[]> childRepresentation = new ArrayList<A[]>();
        childRepresentation.addAll(x.getRepresentation().subList(0, c));
        childRepresentation.addAll(y.getRepresentation().subList(c, individualLength));

        return new Individual<A[]>(childRepresentation);
    }
    
    protected Individual<A[]> reproduceTwoPointsAux(Individual<A[]> x, Individual<A[]> y, int c, int d) {
    	List<A[]> childRepresentation = new ArrayList<A[]>();
    	childRepresentation.addAll(x.getRepresentation().subList(0, c));
    	childRepresentation.addAll(y.getRepresentation().subList(c, d));
    	childRepresentation.addAll(x.getRepresentation().subList(d, individualLength));
    
    	return new Individual<A[]>(childRepresentation);
    }
    
    protected List<Individual<A[]>> reproduceTwoPoints(Individual<A[]> x, Individual<A[]> y, GoalTest<Individual<A[]>> goalTest) {
        List<Individual<A[]>> a = new ArrayList<Individual<A[]>>(2);
        do{
            int c = randomOffset(individualLength);
            int d = randomOffset(individualLength);

            Individual<A[]> child1Representation;
            Individual<A[]> child2Representation;

            while(c == d)
                d = randomOffset(individualLength);
            if(c < d) {
                child1Representation = this.reproduceTwoPointsAux(x,y,c,d);
                child2Representation = this.reproduceTwoPointsAux(y,x,c,d);
            }
            else {
                child1Representation = this.reproduceTwoPointsAux(x,y,d,c);
                child2Representation = this.reproduceTwoPointsAux(y,x,d,c);
            }

            a.add(0, child1Representation);
            a.add(1, child2Representation);
        }while (!goalTest.test(a.get(0)) && !goalTest.test(a.get(1)));

    	return a;
    }

    protected Individual<A[]> mutate(Individual<A[]> child, GoalTest<Individual<A[]>> goalTest) {
        Individual<A[]> childMutated;
        do{
            int mutateOffset = randomOffset(individualLength);
            int alphaOffset[] = new int[teachersPerTurn];

            for (int i = 0; i < alphaOffset.length; i++) {
                alphaOffset[i] = randomOffset(finiteAlphabet.size());
                for (int j = 0; j < i; j++) {
                    if(alphaOffset[i] == alphaOffset[j])
                        i--;
                }
            }

            String[] mutation = new String[teachersPerTurn];
            for (int i = 0; i < alphaOffset.length; i++)
                mutation[i] = (String) finiteAlphabet.get(alphaOffset[i]);

            List<A[]> mutatedRepresentation = new ArrayList<A[]>(child.getRepresentation());
            mutatedRepresentation.set(mutateOffset, (A[]) mutation);
            childMutated = new Individual<A[]>(mutatedRepresentation);
        }while (!goalTest.test(childMutated));

        return childMutated;
    }

    /**
     * It selects two random points to changed
     * @param child
     */
    protected Individual<A[]> mutateTwoPoints(Individual<A[]> child){
        int firstGen = randomOffset(individualLength);
        int secondGen = randomOffset(individualLength);
        List<A[]> mutated = new ArrayList<A[]>(child.getRepresentation());

        A[] genAux = mutated.get(firstGen);
        mutated.set(firstGen, mutated.get(secondGen));
        mutated.set(secondGen, genAux);

        return new Individual<>(mutated);
    }

    protected int randomOffset(int length) {
    	Random random = new Random();
        return random.nextInt(length);
    }

    protected void validatePopulation(Collection<Individual<A[]>> population) {
        // Require at least 1 individual in population in order
        // for algorithm to work
        if (population.size() < 1) {
            throw new IllegalArgumentException("Must start with at least a population of size 1");
        }
        // String lengths are assumed to be of fixed size,
        // therefore ensure initial populations lengths correspond to this
        for (Individual<A[]> individual : population) {
            if (individual.length() != this.individualLength) {
                throw new IllegalArgumentException("Individual [" + individual
                        + "] in population is not the required length of " + this.individualLength);
            }
        }
    }

    private void notifyProgressTrackers(int itCount, Collection<Individual<A[]>> generation) {
        for (ProgressTracker<A> tracer : progressTrackers)
            tracer.trackProgress(getIterations(), generation);
    }

    /**
     * Interface for progress tracers.
     *
     * @author Ruediger Lunde
     */
    public interface ProgressTracker<A> {
        void trackProgress(int itCount, Collection<Individual<A[]>> population);
    }
}