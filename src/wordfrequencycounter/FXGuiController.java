/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wordfrequencycounter;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import fabFileLib.fabIO;
import hagumafab.FrequencyMap;
import javafx.event.Event;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;

public class FXGuiController implements Initializable {
    
    @FXML private Label label, statusLabel;
    @FXML private TextField topHowManyTextField;
    @FXML private TextArea textArea, resultArea;
    @FXML private RadioButton rbTextOrFile;
    @FXML private RadioButton rbTopOrWord;
    
    private String textToAnalyze;
    private int givenTopHowMany;
    boolean isAlreadyAnalyzed;
    
    fabIO myIO = new fabIO();
    java.util.List<FrequencyMap> listOfFrequencies;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listOfFrequencies = new java.util.ArrayList<>();
        isAlreadyAnalyzed = false;
        textToAnalyze = "";
        topHowManyTextField.setOnKeyPressed(event ->{
            if (event.getCode() == KeyCode.ENTER) analyzeGivenText(event);
        });
    }    
    
    @FXML
    private void analyzeGivenText(Event event) {
        long startTime = 0, endTime = 0;
        
        switch (pickOperation()) {
            case 0: /* FOR TEXT IN TEXTAREA SEARCH TOP HOW MANY */
                if (!textToAnalyze.equals(textArea.getText())) {
                    textToAnalyze = textArea.getText();
                }
                if(textToAnalyze.isEmpty()) return;
                
                inCaseOf_Text(0, startTime, endTime);
                break;
            case 1: /* FOR TEXT IN TEXTAREA SEARCH WORD FREQUENCY */
                if (!textToAnalyze.equals(textArea.getText())) {
                    textToAnalyze = textArea.getText();
                }
                if(textToAnalyze.isEmpty()) return; 
                
                inCaseOf_Text(1, startTime, endTime); 
                break;
                
            case 2: /* FOR FILE LOADED SEARCH TOP HOW MANY */ 
                inCaseOf_File(2, startTime, endTime); 
                break;
                
            case 3:  /* FOR FILE LOADED SEARCH WORD FREQUENCY */ 
                inCaseOf_File(3, startTime, endTime); 
                break;    
            default: /* BALANCA A FEW HOW MANY */
                revealTopHowMany(listOfFrequencies, givenTopHowMany);
                break;
        }
        statusLabel.setText("Millis: " + String.valueOf(endTime - startTime)); 
    }
    
    private int pickOperation(){
        
        if (rbTextOrFile.isSelected()) {
            if (rbTopOrWord.isSelected()) {
                return 0;    /* FOR TEXT IN TEXTAREA SEARCH TOP HOW MANY */
            }else return 1;  /* FOR TEXT IN TEXTAREA SEARCH WORD FREQUENCY */
        } else{
            if (rbTopOrWord.isSelected()) {
                return 2;    /* FOR FILE LOADED SEARCH TOP HOW MANY */
            }else return 3;  /* FOR FILE LOADED SEARCH WORD FREQUENCY */
        }
    }
    
    private void inCaseOf_File(int whichCase, long startTime, long endTime){
        startTime = System.currentTimeMillis();
        if (whichCase == 2) {
            if (!isAlreadyAnalyzed) {
                if(textToAnalyze.isEmpty()) return;
                listOfFrequencies = wordFrequencyAnalyzor(textToAnalyze);
                isAlreadyAnalyzed = true;
            }
            if (myIsNumber(topHowManyTextField.getText())) {
                givenTopHowMany = Integer.parseInt(topHowManyTextField.getText());
            } else givenTopHowMany = 10;
            revealTopHowMany(listOfFrequencies, givenTopHowMany);
        }else{
            if (!isAlreadyAnalyzed) {
                if(textToAnalyze.isEmpty()) return;
                listOfFrequencies = wordFrequencyAnalyzor(textToAnalyze);
                isAlreadyAnalyzed = true;
            }
            revealFrequencyOfWord(listOfFrequencies, topHowManyTextField.getText());
        }
        endTime = System.currentTimeMillis();
    }
    private void inCaseOf_Text(int whichCase, long startTime, long endTime){
        startTime = System.currentTimeMillis();
        if (whichCase == 0) {
            if (!isAlreadyAnalyzed) {
                listOfFrequencies = wordFrequencyAnalyzor(textToAnalyze);
                isAlreadyAnalyzed = true;
            }
            if (myIsNumber(topHowManyTextField.getText())) {
                givenTopHowMany = Integer.parseInt(topHowManyTextField.getText());
            } else givenTopHowMany = 10;
            revealTopHowMany(listOfFrequencies, givenTopHowMany);
        }else{
            if (!isAlreadyAnalyzed) {
                listOfFrequencies = wordFrequencyAnalyzor(textToAnalyze);
                isAlreadyAnalyzed = true;
            }
            revealFrequencyOfWord(listOfFrequencies, topHowManyTextField.getText());
        }
        endTime = System.currentTimeMillis();
    }
    
    @FXML
    private void chooseAndReadFile(Event event){
        FileChooser fc = new FileChooser();
        java.io.File leFile = fc.showOpenDialog(null);
        
        if (leFile != null) {
            label.setText(leFile.getName());
            try {
                textToAnalyze = myIO.readFile_asString(leFile);
            } catch (Exception e) { label.setText("Error with File/Folder");
            }
            isAlreadyAnalyzed = false;
        }
    }
    
    @FXML
    private void clearAll(Event event){
        label.setText("");
        resultArea.setText("");
        textArea.setText("");
        isAlreadyAnalyzed = false;
        textToAnalyze = "";
        topHowManyTextField.setText(""); 
        listOfFrequencies.clear();
        
    }
    private void revealFrequencyOfWord(java.util.List<FrequencyMap> list, String textString){
        String result = ""; 
        for(int i=0 ; i<list.size() ; i++){
            if (list.get(i).getObject().equals(textString)) {
                result += list.get(i).getObject().toString();
                result += " : ";
                result += list.get(i).getFrequency();
                break;
            }
        }
        resultArea.setText(result + "\n\n\n\nDistinct words Count: " + list.size());
    }
    
    private static java.util.List<FrequencyMap> wordFrequencyAnalyzor(String aFileString){
        String[] array = arrayOfWordsNoSymbol(aFileString);
        int sz = array.length;
        java.util.List<FrequencyMap> unList = new java.util.ArrayList<>();
        
        for(int i=0 ; i<sz ; i++){
            int found = freqListContains(unList, array[i]);
            if (found >= 0) {
                unList.get(found).addFrequency();
            } else {
                unList.add(new FrequencyMap(array[i]));
            }
        }
        java.util.Collections.sort(unList, FrequencyMap.compareByFrequency);
        return unList;
    }
    
    private void revealTopHowMany(java.util.List<FrequencyMap> frequencyMapList, int topHowMany){
        if(topHowMany > frequencyMapList.size()) topHowMany = frequencyMapList.size();
        StringBuilder sb = new StringBuilder();
        
        for(int i=0 ; i<topHowMany ; i++){   // (int i=sz -1 ; i<=sz -topHowMany ; i--)
            sb.append(i + 1).append(")  ");
            sb.append(frequencyMapList.get(i).getObject());
            sb.append(" : ").append(frequencyMapList.get(i).getFrequency());
            sb.append(System.lineSeparator());
        }
        resultArea.setText(sb.toString());
    }
    private static int freqListContains(java.util.List<FrequencyMap> list, Object obj){
        for(int i=0 ; i<list.size() ; i++){
            if(list.get(i).isObject(obj)) return i;
        } return -3;
    }
    
    private static String[] arrayOfWordsNoSymbol(String anyString){
        java.util.List<String> aList = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int sz = anyString.length();

        for(int i=0 ; i<sz ; i++){
            if (Character.isLetterOrDigit(anyString.charAt(i))) {
                sb.append(anyString.charAt(i));
            } else {
                if(sb.length() < 1) continue;
                aList.add(sb.toString());
                sb.delete(0, sb.length());
            }
        }
        if (sb.length() > 0) aList.add(sb.toString());
        return aList.toArray(new String[aList.size()]);
    }
    
    private boolean myIsNumber(Object thing){ 
        String str = String.valueOf(thing);
        if(str.isEmpty()) return false;
        int start = 0, size = str.length();
        if (str.charAt(0) == 43 || str.charAt(0) == 45) 
            start = 1;  // in case there is plus or minus
        if (str.charAt(start) == 46) return false; // a dot at the beginning is bad !!
        for (int i=start ; i<size ; i++){
            if (str.charAt(i) < 46 || str.charAt(i) > 57) return false;
            if (str.charAt(i) == 47) return false;
        }
        return true;
    }
    
}
