package control;

import aima.FitnessFunction;
import model.GeneticAlgorithm;
import aima.GoalTest;
import aima.Individual;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.TimetableFileReader;
import model.TimetableGenAlgoUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Controller {

    private final HashMap<Integer, Pair<Integer, Integer>> hmRepresentation;
    private final FileChooser fileChooser = new FileChooser();
    private final Stage stage;

    @FXML
    private GridPane _gpTimetable;
    @FXML
    private TextField _tfPath;
    @FXML
    private TextArea _taLog;
    @FXML
    private Button _btFileFinder;
    @FXML
    private Button _btGenerate;
    @FXML
    private TextField _tfMutation;
    @FXML
    private TextField _tfCrossing;
    @FXML
    private MenuButton _btmTypeMutation;
    @FXML
    private MenuButton _btmCrossing;
    @FXML
    private MenuButton _btmSelection;
    @FXML
    private MenuButton _btmTechTurn;


    private int turns = 0;
    private HashSet<String> hsAlphabet = null;
    private HashMap<String, HashSet<Integer>> hmRestrictions = null;
    private HashMap<String, HashSet<Integer>> hmPreferences = null;
    private HashMap<String, Boolean> hmConsecutive = null;
    private HashMap<String, Integer> hmTurns = null;
    private Individual<String[]> bestIndividual = null;
    private double mutationProbability = 0.15d;
    private double crossingProbability = 0.70d;
    private boolean aimaMutation = true;
    private boolean crossingWithAPoint = true;
    private int selectionType = GeneticAlgorithm.ROULETTE_SELECTION;
    private int teachersPerTurn = 1;

    public Controller() {
        this.stage = new Stage();
        hmRepresentation = new HashMap<>();
        hmRepresentation.put(1, new Pair<>(1, 1));
        hmRepresentation.put(2, new Pair<>(2, 1));
        hmRepresentation.put(3, new Pair<>(3, 1));
        hmRepresentation.put(4, new Pair<>(4, 1));
        hmRepresentation.put(5, new Pair<>(1, 2));
        hmRepresentation.put(6, new Pair<>(2, 2));
        hmRepresentation.put(7, new Pair<>(3, 2));
        hmRepresentation.put(8, new Pair<>(4, 2));
        hmRepresentation.put(9, new Pair<>(1, 3));
        hmRepresentation.put(10, new Pair<>(2, 3));
        hmRepresentation.put(11, new Pair<>(3, 3));
        hmRepresentation.put(12, new Pair<>(4, 3));
        hmRepresentation.put(13, new Pair<>(1, 4));
        hmRepresentation.put(14, new Pair<>(2, 4));
        hmRepresentation.put(15, new Pair<>(3, 4));
        hmRepresentation.put(16, new Pair<>(4, 4));
    }

    public void onClickFileFinder(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            _btFileFinder.setDisable(true);
            File file = fileChooser.showOpenDialog(stage);
            if (file != null)
                _tfPath.setText(file.getPath());
            _btFileFinder.setDisable(false);
        });
    }

    public void onClickGenerate(ActionEvent actionEvent) {
        if(_tfPath.getText().equals("") || _tfPath.getText() == null)
        {
            writeTA("You should to introduce a path");
            return;
        }
        String nom = _tfPath.getText();

        try{
            if(_tfMutation.getText() != null)
                mutationProbability = Double.parseDouble(_tfMutation.getText());
            if(_tfCrossing.getText() != null)
                crossingProbability = Double.parseDouble(_tfCrossing.getText());
            if(crossingProbability < 0 || crossingProbability > 1
                    || mutationProbability < 0 || mutationProbability > 1)
            {
                throw new Exception("The probability should be between 0 and 1");
            }
        }catch (Exception e){
            e.printStackTrace();
            writeTA(e.getMessage());
            return;
        }
        _btGenerate.setDisable(true);
        Thread th = new Thread(new RnGenetic(nom));
        th.setDaemon(true);

        th.start();
    }
    
    private void clearTimeTable(){
        Platform.runLater(() -> {
            for (int i = 1; i < 5; i++) {
                for (int j = 1; j < 5; j++) {
                    ((Label)_gpTimetable.getChildren().get(j*5+i)).setText("");
                }
            }
        });
    }

    private void showResult(){
        Platform.runLater(() -> {
            for (int i = 0; i < bestIndividual.length(); i++) {
                if(bestIndividual.getRepresentation().get(i) != null){
                    int row = hmRepresentation.get(i+1).getKey();
                    int col = hmRepresentation.get(i+1).getValue();
                    StringBuilder text = new StringBuilder();
                    for (String te : bestIndividual.getRepresentation().get(i))
                        if(te != null)
                            text.append(te).append("\n");
                    ((Label)_gpTimetable.getChildren().get(row*5+col)).setText(text.toString());
                }
            }
        });
    }

    private void writeTA(String text){
        Platform.runLater(() -> _taLog.appendText(text + "\n"));
    }

    public void onClickItemMutation(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            String str = ((MenuItem)actionEvent.getSource()).getText();
            _btmTypeMutation.setText(str);
            if(str.equals("Aima Mutation"))
                aimaMutation = true;
            else
                aimaMutation = false;

        });
    }

    public void onClickPointCrossing(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            String str = ((MenuItem)actionEvent.getSource()).getText();
            _btmCrossing.setText(str);
            if(str.equals("Crossing With 1 Point"))
                crossingWithAPoint = true;
            else
                crossingWithAPoint = false;

        });
    }

    public void onClickSelection(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            String str = ((MenuItem)actionEvent.getSource()).getText();
            _btmSelection.setText(str);
            if(str.equals("Selection: Roulette"))
                selectionType = GeneticAlgorithm.ROULETTE_SELECTION;
            else
                selectionType = GeneticAlgorithm.TOURNAMENT_SELECTION;

        });
    }

    public void onClickTeachTurn(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            String str = ((MenuItem)actionEvent.getSource()).getText();
            _btmTechTurn.setText(str);
            if(str.equals("Teachers Per Turn: 1"))
                teachersPerTurn = 1;
            else
                teachersPerTurn = 2;

        });
    }

    private class RnGenetic implements Runnable{

        private String nom;

        public RnGenetic(String nom) {
            this.nom = nom;
        }

        @Override
        public void run() {
            try{
                //file read
                TimetableFileReader reader = new TimetableFileReader(nom);
                turns = reader.getTurns();
                hsAlphabet = reader.getTeachers();
                hmRestrictions = reader.getTeacherRestrictions(hsAlphabet.size());
                hmPreferences = reader.getTeacherPreferences(hsAlphabet.size());
                hmConsecutive = reader.getTeacherConsecutivePreferences(hsAlphabet.size());
                hmTurns = reader.getHmTurns(hsAlphabet.size());
                reader.close();

                FitnessFunction<String[]> fitnessFunction = TimetableGenAlgoUtil.getFitnessFunction(turns, hmRestrictions,
                        hmPreferences, hmConsecutive, hmTurns);
                GoalTest<Individual<String[]>> goalTest = TimetableGenAlgoUtil.getGoalTest(hmRestrictions, turns);
                // Generate an initial population
                Set<Individual<String[]>> population = new HashSet<>();
                ArrayList<String> alpha = new ArrayList<>(hsAlphabet);
                for (int i = 0; i < 50; i++) {
                    population.add(TimetableGenAlgoUtil.generateRandomIndividual(turns, alpha, hmRestrictions, teachersPerTurn));
                }

                GeneticAlgorithm<String> ga = new GeneticAlgorithm<>(TimetableGenAlgoUtil.MAX_TURNS, alpha,
                        mutationProbability, crossingProbability, crossingWithAPoint, aimaMutation, selectionType, teachersPerTurn);

                // Run for a set amount of time
                bestIndividual = ga.geneticAlgorithm(population, fitnessFunction, goalTest, 2000L);
                clearTimeTable();
                showResult();
                Platform.runLater(() -> _btGenerate.setDisable(false));
                StringBuilder sb = new StringBuilder();
                      sb.append("Fitness = ").append(fitnessFunction.apply(bestIndividual)).append("\n")
                        .append("Is Goal = ")
                        .append(goalTest.test(bestIndividual))
                        .append("\n")
                        .append("Population Size = ")
                        .append(ga.getPopulationSize()).append("\n")
                        .append("Iterations = ")
                        .append(ga.getIterations()).append("\n")
                        .append("Took = ")
                        .append(ga.getTimeInMilliseconds()).append("ms.\n");
                writeTA(sb.toString());
            }catch (Exception e){
                e.printStackTrace();
                Platform.runLater(() -> _btGenerate.setDisable(false));
                writeTA(e.getMessage());
            }
        }
    }
}
