package com.sim.application.controllers.obfuscation;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sim.application.classes.ClassMap;
import com.sim.application.classes.JavaFile;
import com.sim.application.classes.Problem;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;

import java.awt.*;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public final class ObfuscateArtController extends Technique {
    private static ObfuscateArtController instance;
    private final String name = "Artist Obfuscation";
    private final String description = "Changes the structure of the code into an ASCII art format";

    public static ObfuscateArtController getInstance() {
        if (instance == null) {
            instance = new ObfuscateArtController();
        }
        return instance;
    }

    private ObfuscateArtController() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {
        // read the art file
        super.saveRawContent(Artist.draw(sourceFiles));
    }

    private static class Artist {
        private static BiMap<JavaFile, String> draw(Map<JavaFile, CompilationUnit> source) throws FailedTechniqueException {
            BiMap<JavaFile, String> rawOutput = HashBiMap.create();

            // the art file
            InputStream stream = ObfuscateArtController.class.getClassLoader()
                    .getResourceAsStream("text/ayaya.txt");
            ArtTemplate selectedArt = new ArtTemplate();
            selectedArt.readSketch(stream);

            // source code string
            String sourceCodeString = "";
            for(JavaFile file : source.keySet()) {
                CompilationUnit unit = source.get(file);
                sourceCodeString = "//BeginArt\n" + unit.toString();
                String[] lines = sourceCodeString.split("\r?\n");

                ArrayDeque<String> contents = new ArrayDeque<>();
                ProcessFile processor = new ProcessFile();
                // run the lines
                for (String line : lines) {
                    ArrayList<String> wordRow = processor.WordCheck(line.strip());
                    if (wordRow.size() > 1) {
                        for (int index = 0; index < wordRow.size(); index++) {
                            if (wordRow.get(index).equals("")) wordRow.remove(index);
                        }
                        contents.addAll(wordRow);
                    } else if (!wordRow.get(wordRow.size() - 1).equals("")) contents.addAll(wordRow);
                }
                processor.extractContents(contents);

                // make art using the selected template and extracted contents
                ArrayDeque<String> finalContents = processor.getFileText();
                Mapper makesArt = new Mapper(selectedArt.getListOfMaps().get(0), finalContents);

                // output is saved into an string ArrayList
                ArrayList<String> output = makesArt.getOutput();
                StringBuilder stringBuilder = new StringBuilder();
                for (String str : output) {
                    stringBuilder.append(str).append("\n");
                }
                rawOutput.put(file, stringBuilder.toString());
            }
            return rawOutput;
        }
        private static class ArtTemplate {
            private final ArrayList<ArtDetails> listOfMaps = new ArrayList<>();
            public ArtTemplate() {}
            public void readSketch (InputStream stream) {
                // Read template file
                Scanner sketchReader = new Scanner (stream);
                // Begin template selection and construction
                ArtDetails Map = new ArtSelector(sketchReader).returnTemplate();
                // Add into a list of maps
                listOfMaps.add(Map);
            }
            // Returns the list of maps
            public ArrayList<ArtDetails> getListOfMaps () { return listOfMaps; }
        }

        private static class ArtDetails {
            private final String[][] template;
            private final ArrayList<ArrayList<String>> structure = new ArrayList<>();
            private int numberOfCharacters;
            private int longestWord = 0;
            public ArtDetails(String[][] template){
                this.template = template;
                structureGenerator();
            }
            // generate structure of character made words for each row
            public void structureGenerator () {
                StringBuilder tempWord = new StringBuilder();
                for (String[] row : template) {
                    ArrayList<String> rowOfWords = new ArrayList<>();
                    // link every 'x' character slot before a <blank> together and store in the list
                    for (int character = 0; character < row.length; character++) {
                        if (row[character].equals("x")) {
                            numberOfCharacters++;
                            tempWord.append(row[character]);
                        } else {
                            if (tempWord.length() != 0) rowOfWords.add(tempWord.toString());
                            tempWord = new StringBuilder();
                        }
                        if (character == row.length - 1 && tempWord.length() != 0) {
                            rowOfWords.add(tempWord.toString());
                            tempWord = new StringBuilder();
                        }
                    }
                    // after row is completed add all contents to the structure list
                    structure.add(rowOfWords);
                }
                // calculate the longest word available
                for (ArrayList<String> row : structure)
                    for (String word : row)
                        if (word.length() > longestWord) longestWord = word.length();
            }
            public int getLongestWord () { return longestWord; }
            public int getNumberOfCharacters () { return numberOfCharacters; }
            public String[][] getTemplate () { return template; }
            public ArrayList<ArrayList<String>> getStructure () { return structure; }
        }

        private static class ArtSelector {
            private final ArrayList<String> fileContext = new ArrayList<>();
            private final ArrayStructure originalMap = new ArrayStructure();
            private int numOfWordsOriginal;
            private final ArrayStructure invertedMap  = new ArrayStructure();
            private int numOfWordsInverted;
            private ArtDetails template;

            public ArtSelector(Scanner fileReader){
                readContext(fileReader);
                createMaps();
                selectMap();
            }
            public void readContext (Scanner fileReader) {
                while (fileReader.hasNextLine()) {
                    // replace all <blanks> into 'o' characters
                    String rowContext = fileReader.nextLine().replace(" ", "o");
                    fileContext.add(rowContext);
                }
                fileReader.close();
            }
            // creates both original and inverted art at the same time
            public void createMaps () {
                for (int row = 0; row < fileContext.size(); row++){
                    // edit context to allow invert
                    StringBuilder contextEditor = new StringBuilder(fileContext.get(row));
                    for (int ch = 0; ch < contextEditor.length(); ch++) {
                        // if the character is not an 'o' character
                        if (!Character.toString(contextEditor.charAt(ch)).equals("o")) {
                            // count number of original characters
                            numOfWordsOriginal++;
                            // original row characters
                            contextEditor.setCharAt(ch, 'x');
                            // generate original map rows
                            originalMap.add(row, ch, Character.toString(contextEditor.charAt(ch)));
                            // invert row characters
                            contextEditor.setCharAt(ch, ' ');
                            // generate inverted map rows
                            invertedMap.add(row, ch, Character.toString(contextEditor.charAt(ch)));
                        } else {
                            // count number of inverted characters
                            numOfWordsInverted++;
                            // inverted row characters
                            contextEditor.setCharAt(ch, 'x');
                            // generate inverted map rows
                            invertedMap.add(row, ch, Character.toString(contextEditor.charAt(ch)));
                            // originate row characters
                            contextEditor.setCharAt(ch, ' ');
                            // generate original map rows
                            originalMap.add(row, ch, Character.toString(contextEditor.charAt(ch)));
                        }
                    }
                }
            }
            // select one of the two between original and inverted based on amount of characters
            public void selectMap () {
                String[][] newMap;
                if (numOfWordsOriginal > numOfWordsInverted) { newMap = originalMap.toArray(); }
                else { newMap = invertedMap.toArray(); }
                template = new ArtDetails(newMap);
            }
            // return selected map as a template
            private ArtDetails returnTemplate () { return template; }
        }

        private static class ArrayStructure {
            private final HashMap<Point, String> map = new HashMap<>();
            private int maxRow = 0;
            private int maxColumn = 0;
            public ArrayStructure() {}
            public void add(int row, int column, String string) {
                map.put(new Point(row, column), string);
                maxRow = Math.max(row, maxRow);
                maxColumn = Math.max(column, maxColumn);
            }

            public String[][] toArray() {
                String[][] result = new String[maxRow + 1][maxColumn + 1];
                for (int row = 0; row <= maxRow; ++row)
                    for (int column = 0; column <= maxColumn; ++column) {
                        Point p = new Point(row, column);
                        result[row][column] = map.containsKey(p) ? map.get(p) : "";
                    }
                return result;
            }
        }

        private static class ProcessFile {
            private final ArrayDeque<String> fileText = new ArrayDeque<>();
            private ProcessFile () {}

            public void extractContents (ArrayDeque<String> dummy) {
                StringBuilder doc = new StringBuilder();
                ArrayList<String> temp = new ArrayList<>();
                temp.addAll(dummy);
                // java logic checks for /, /*, */
                int javadoc = -1;
                int open = -1;
                int close = -1;
                for (int index = 0; index < temp.size(); index++) {
                    // add blanks to prevent unreadable for / (division)
                    if (temp.get(index).equals("/"))
                        temp.set(index, " " + temp.get(index) + " ");

                    // check if /** exists
                    if (temp.get(index).contains("/**")) {
                        // check if /* is before /**
                        if (temp.get(index).charAt(0) == '/' && temp.get(index).charAt(1) == '*' && temp.get(index).charAt(2) != '*')
                            open = index;
                        // check if */ does not exist
                        if (!(temp.get(index).contains("*/")))
                            javadoc = index;
                    }
                    else if (temp.get(index).contains("/*") && !temp.get(index).contains("*/") && open == -1) {
                        temp.set(index, temp.get(index) + "*/");
                        open = index;
                    }
                    else if (temp.get(index).contains("*/") && !temp.get(index).contains("/*")) {
                        if ((!(open == -1 && javadoc != -1))) temp.set(index, "/*" + temp.get(index));
                        close = index;
                    }
                    if (open > 0 && close > 0 ) {
                        if (close - open > 1) {
                            for (int comment = open+1; comment <= close-1; comment++) temp.set(comment, "/*" + temp.get(comment) + "*/" );
                            open = -1; close = -1;
                        } else open = -1; close = -1;
                    } else if (javadoc != -1) {
                        // add index to doc
                        doc.append(temp.get(index));
                        if (close != -1) { // the ending of doc
                            temp.set(index, doc.toString());
                            doc = new StringBuilder();
                            javadoc = -1; close = -1;
                        } else temp.set(index, "");
                    }
                }
                for (int index = 0; index < temp.size(); index++) { if (temp.get(index).equals("")) temp.remove(index); }
                for (int index = 0; index < temp.size(); index++) { if (temp.get(index).equals("")) temp.remove(index); }
                fileText.addAll(temp);
            }

            public ArrayList<String> WordCheck (String nextLine){
                ArrayList<Integer> quoteIndexes = new ArrayList<>();
                ArrayList<Integer> commentIndexes = new ArrayList<>();
                ArrayList<String> wordRow = new ArrayList<>();

                // check for quotations and comments
                for (int i = 0; i < nextLine.length(); i++){
                    if (i-1 >= 0 && i+1 < nextLine.length()) {
                        if (nextLine.charAt(i) == '"') {
                            if (!(nextLine.charAt(i - 1) == '\\')) quoteIndexes.add(i);
                        } else if (nextLine.charAt(i) == '/' && nextLine.charAt(i - 1) == '/') commentIndexes.add(i-1);
                    } else if (nextLine.charAt(i) == '"' && (i == nextLine.length()-1 || i == 0)) quoteIndexes.add(i);
                }

                // check position of comments with the position of quotations ie. "//", //"", ""//
                if (quoteIndexes.size() > 0) {
                    // check all quotes index
                    for (int index = 0; index < quoteIndexes.size(); index = index + 2) {
                        if (index != quoteIndexes.size()-1) {
                            // run through comment indexes
                            int counter = 0;
                            while (counter < commentIndexes.size()) {
                                // if comment is in between quotes
                                if (commentIndexes.get(counter) != null) {
                                    if (commentIndexes.get(counter) > quoteIndexes.get(index)
                                            && commentIndexes.get(counter) < quoteIndexes.get(index+1))
                                        commentIndexes.set(counter, null);
                                }
                                counter++;
                            }
                        }
                    }
                }

                // begin check and organize wording
                int firstLetter = 0;
                int endOfLine = nextLine.length()-1;
                StringBuilder bunchOfCharacters = new StringBuilder();
                if (commentIndexes.size() != 0 && commentIndexes.get(0) != null){ endOfLine = commentIndexes.get(0); }
                if (quoteIndexes.size() != 0) { // if there is a quotation
                    if (quoteIndexes.get(0) < endOfLine) { // quotation is first
                        for (int index = 0; index < quoteIndexes.size(); index = index + 2) {
                            if (index != quoteIndexes.size()-1) {
                                // add and split words before the quote
                                for (int start = firstLetter; start < quoteIndexes.get(index); start++)
                                    bunchOfCharacters.append(nextLine.charAt(start));

                                // check ' '
                                if (bunchOfCharacters.toString().contains("'")) {
                                    String[] cString = bunchOfCharacters.toString().split("'");
                                    for (int i = 0; i < cString.length; i = i + 2) {
                                        if (i != cString.length - 1) {
                                            wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                            // add ' ' to the value
                                            wordRow.add("'" + cString[i+1] + "'");
                                        } else wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                    }
                                } else wordRow.addAll(Arrays.asList(bunchOfCharacters.toString().split(" ")));
                                bunchOfCharacters = new StringBuilder();

                                // add words within the quote
                                for (int start = quoteIndexes.get(index); start <= quoteIndexes.get(index+1); start++)
                                    bunchOfCharacters.append(nextLine.charAt(start));
                                wordRow.add(bunchOfCharacters.toString());
                                bunchOfCharacters = new StringBuilder();

                                firstLetter = quoteIndexes.get(index + 1) + 1;
                            }
                        }
                        // add and split the remains
                        if (endOfLine != nextLine.length()-1) { // if there is a comment
                            for (int start = firstLetter; start < endOfLine; start++)
                                bunchOfCharacters.append(nextLine.charAt(start));

                            // check ' '
                            if (bunchOfCharacters.toString().contains("'")) {
                                String[] cString = bunchOfCharacters.toString().split("'");
                                for (int i = 0; i < cString.length; i = i + 2) {
                                    if (i != cString.length - 1) {
                                        wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                        // add ' ' to the value
                                        wordRow.add("'" + cString[i+1] + "'");
                                    } else wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                }
                            } else wordRow.addAll(Arrays.asList(bunchOfCharacters.toString().split(" ")));
                            bunchOfCharacters = new StringBuilder();

                            for (int start = endOfLine; start < nextLine.length(); start++)
                                bunchOfCharacters.append(nextLine.charAt(start));
                            wordRow.add(bunchOfCharacters.toString());
                        } else {
                            for (int start = quoteIndexes.get(quoteIndexes.size()-1) + 1; start <= endOfLine; start++)
                                bunchOfCharacters.append(nextLine.charAt(start));

                            // check ' '
                            if (bunchOfCharacters.toString().contains("'")) {
                                String[] cString = bunchOfCharacters.toString().split("'");
                                for (int i = 0; i < cString.length; i = i + 2) {
                                    if (i != cString.length - 1) {
                                        wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                        // add ' ' to the value
                                        wordRow.add("'" + cString[i+1] + "'");
                                    } else wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                }
                            } else wordRow.addAll(Arrays.asList(bunchOfCharacters.toString().split(" ")));

                        }
                    } else { // comment is first
                        // add and split words before the comment
                        for (int start = firstLetter; start < endOfLine; start++)
                            bunchOfCharacters.append(nextLine.charAt(start));

                        // check ' '
                        if (bunchOfCharacters.toString().contains("'")) {
                            String[] cString = bunchOfCharacters.toString().split("'");
                            for (int i = 0; i < cString.length; i = i + 2) {
                                if (i != cString.length - 1) {
                                    wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                    // add ' ' to the value
                                    wordRow.add("'" + cString[i+1] + "'");
                                } else wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                            }
                        } else wordRow.addAll(Arrays.asList(bunchOfCharacters.toString().split(" ")));
                        bunchOfCharacters = new StringBuilder();

                        // add all words after the comment
                        for (int start = endOfLine; start < nextLine.length(); start++)
                            bunchOfCharacters.append(nextLine.charAt(start));
                        wordRow.add(bunchOfCharacters.toString());
                    }
                } else { // if there is no quotation
                    if (endOfLine != nextLine.length()-1){ // there is a comment
                        // add and split words before the comment
                        for (int start = firstLetter; start < endOfLine; start++)
                            bunchOfCharacters.append(nextLine.charAt(start));

                        // check ' '
                        if (bunchOfCharacters.toString().contains("'")) {
                            String[] cString = bunchOfCharacters.toString().split("'");
                            for (int i = 0; i < cString.length; i = i + 2) {
                                if (i != cString.length - 1) {
                                    wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                    // add ' ' to the value
                                    wordRow.add("'" + cString[i+1] + "'");
                                } else wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                            }
                        } else wordRow.addAll(Arrays.asList(bunchOfCharacters.toString().split(" ")));
                        bunchOfCharacters = new StringBuilder();

                        // add all words after the comment
                        for (int start = endOfLine; start < nextLine.length(); start++)
                            bunchOfCharacters.append(nextLine.charAt(start));
                        wordRow.add(bunchOfCharacters.toString());
                    } else { // there is no quotation and comment
                        // add and split all normie words
                        for (int start = firstLetter; start < nextLine.length(); start++)
                            bunchOfCharacters.append(nextLine.charAt(start));

                        // check ' '
                        if (bunchOfCharacters.toString().contains("'")) {
                            String[] cString = bunchOfCharacters.toString().split("'");
                            for (int i = 0; i < cString.length; i = i + 2) {
                                if (i != cString.length - 1) {
                                    wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                                    // add ' ' to the value
                                    wordRow.add("'" + cString[i+1] + "'");
                                } else wordRow.addAll(Arrays.asList(cString[i].split(" ")));
                            }
                        } else wordRow.addAll(Arrays.asList(bunchOfCharacters.toString().split(" ")));

                    }
                }
                return wordRow;
            }

            private ArrayDeque<String> getFileText () { return fileText; }
        }

        private static class Mapper {
            private final String[][] rawTemplate;
            private final ArrayList<ArrayList<String>> rawStructure;
            private final int longestWord;
            private ArrayList<String> output;
            public Mapper (ArtDetails Map, ArrayDeque<String> fileText) {
                this.rawTemplate = Map.getTemplate();
                this.rawStructure = Map.getStructure();
                this.longestWord = Map.getLongestWord();
                this.output = new ArrayList<>();
                processContents(fileText);
            }
            public void processContents (ArrayDeque<String> fileText) {
                boolean outOfGas = false;
                ArrayList<ArrayList<String>> fullArt = new ArrayList<>();
                String nextWord = fileText.getFirst(); //TODO: may throw if fileText.size() == 0
                boolean sSlash = false;
                // check for '//'
                if (nextWord.contains("//") &&
                        (!nextWord.contains("/*") || !nextWord.contains("*/")) &&
                        !(nextWord.charAt(0) == '\"' && nextWord.charAt(nextWord.length()-1) == '\"') ) sSlash = true;
                for (int row = 0; row < rawStructure.size(); row++) {
                    ArrayList<String> rowArt = new ArrayList<>();
                    for (int word = 0; word < rawStructure.get(row).size(); word++) {
                        if (!fileText.isEmpty()) { // there are words to be filled
                            String blankWord = rawStructure.get(row).get(word);
                            int spaceLeft = blankWord.length();
                            while (spaceLeft > 0) { // there is still space left
                                if (fileText.size() > 0) { // there are still words
                                    if (nextWord.length() + 4  <= longestWord) {
                                        if (!sSlash) { // there is no single line comment
                                            if (word != rawStructure.get(row).size() - 1) { // not last word
                                                if (spaceLeft >= nextWord.length() + 4) { // if a new word can be fitted
                                                    rowArt.add(nextWord + "/**/");
                                                    spaceLeft = spaceLeft - (nextWord.length() + 4);
                                                    fileText.removeFirst();
                                                    if (!fileText.isEmpty()) {
                                                        nextWord = fileText.getFirst();
                                                        if (nextWord.contains("//") &&
                                                                (!nextWord.contains("/*") || !nextWord.contains("*/")) &&
                                                                !(nextWord.charAt(0) == '\"' && nextWord.charAt(nextWord.length()-1) == '\"') ) sSlash = true;
                                                    }
                                                } else { // no word can be fitted
                                                    if (spaceLeft >= 4) {
                                                        rowArt.add("/" + "*".repeat(spaceLeft - 2) + "/");
                                                    } else rowArt.add(" ".repeat(spaceLeft));
                                                    spaceLeft = 0;
                                                }
                                            } else { // last word
                                                if (spaceLeft >= nextWord.length() + 4) { // if a new word can be fitted
                                                    rowArt.add(nextWord + "/**/");
                                                    spaceLeft = spaceLeft - (nextWord.length() + 4);
                                                    fileText.removeFirst();
                                                    if (!fileText.isEmpty()) {
                                                        nextWord = fileText.getFirst();
                                                        if (nextWord.contains("//") &&
                                                                (!nextWord.contains("/*") || !nextWord.contains("*/")) &&
                                                                !(nextWord.charAt(0) == '\"' && nextWord.charAt(nextWord.length()-1) == '\"') ) sSlash = true;
                                                    }
                                                } else { // no word can be fitted
                                                    if (spaceLeft >= 4) {
                                                        rowArt.add("/" + "*".repeat(spaceLeft - 2) + "/");
                                                    } else if (spaceLeft == 1) {
                                                        rowArt.add(" ".repeat(spaceLeft));
                                                    } else rowArt.add("/".repeat(spaceLeft));
                                                    spaceLeft = 0;
                                                }
                                            }
                                        } else { // there is a single line comment
                                            if (word != rawStructure.get(row).size() - 1) { // not last word
                                                if (spaceLeft >= nextWord.length()) { // if a new word can be fitted
                                                    spaceLeft = spaceLeft - nextWord.length();
                                                    rowArt.add(nextWord + "*".repeat(spaceLeft));
                                                    fileText.removeFirst();
                                                    spaceLeft = 0;
                                                    for (int position = word + 1; position < rawStructure.get(row).size(); position++)
                                                        rowArt.add("*".repeat(rawStructure.get(row).get(position).length()));
                                                    word = rawStructure.get(row).size(); //break?
                                                    sSlash = false;
                                                    if (!fileText.isEmpty()) {
                                                        nextWord = fileText.getFirst();
                                                        if (nextWord.contains("//") &&
                                                                (!nextWord.contains("/*") || !nextWord.contains("*/")) &&
                                                                !(nextWord.charAt(0) == '\"' && nextWord.charAt(nextWord.length()-1) == '\"') ) sSlash = true;
                                                    }
                                                } else { // no word can be fitted
                                                    if (spaceLeft >= 4) {
                                                        rowArt.add("/" + "*".repeat(spaceLeft - 2) + "/");
                                                    } else rowArt.add(" ".repeat(spaceLeft));
                                                    spaceLeft = 0;
                                                }
                                            } else { // last word
                                                if (spaceLeft >= nextWord.length()) { // if a new word can be fitted
                                                    spaceLeft = spaceLeft - nextWord.length();
                                                    rowArt.add(nextWord + "*".repeat(spaceLeft));
                                                    fileText.removeFirst();
                                                    spaceLeft = 0;
                                                    sSlash = false;
                                                    if (!fileText.isEmpty()) {
                                                        nextWord = fileText.getFirst();
                                                        if (nextWord.contains("//") &&
                                                                (!nextWord.contains("/*") || !nextWord.contains("*/")) &&
                                                                !(nextWord.charAt(0) == '\"' && nextWord.charAt(nextWord.length()-1) == '\"') ) sSlash = true;
                                                    }
                                                } else { // no word can be fitted
                                                    if (spaceLeft >= 4) {
                                                        rowArt.add("/" + "*".repeat(spaceLeft - 2) + "/");
                                                    } else if (spaceLeft == 1) {
                                                        rowArt.add(" ".repeat(spaceLeft));
                                                    } else rowArt.add("/".repeat(spaceLeft));
                                                    spaceLeft = 0;
                                                }
                                            }
                                        }
                                    } else {
                                        if (sSlash = true) sSlash = false;
                                        rowArt.add(nextWord + "////");
                                        fileText.removeFirst();
                                        if (!fileText.isEmpty()) {
                                            nextWord = fileText.getFirst();
                                            if (nextWord.contains("//") &&
                                                    (!nextWord.contains("/*") || !nextWord.contains("*/")) &&
                                                    !(nextWord.charAt(0) == '\"' && nextWord.charAt(nextWord.length()-1) == '\"') ) sSlash = true;
                                        }
                                    }
                                } else { // fill the remains
                                    if (spaceLeft >= 4) {
                                        rowArt.add("/" + "*".repeat(spaceLeft - 2) + "/");
                                    } else
                                        rowArt.add(" ".repeat(spaceLeft));
                                    spaceLeft = 0;
                                }
                            }
                        } else { // there are no words left in the list
                            while (!outOfGas) {
                                if (rawStructure.get(row).get(word).length() >= 2) {
                                    rowArt.add("/*" + "*".repeat(rawStructure.get(row).get(word).length() - 2));
                                    outOfGas = true;
                                } else rowArt.add(" ".repeat(rawStructure.get(row).get(word).length()));
                                if (word < rawStructure.get(row).size()) word++;
                            }

                            if (row == rawStructure.size()-1) { // last row
                                if (word == rawStructure.get(row).size() - 1) // if last word
                                    rowArt.add("*".repeat(rawStructure.get(row).get(word).length() - 2) + "*/");
                                else rowArt.add("*".repeat(rawStructure.get(row).get(word).length()));
                            }
                            else {
                                for (int position = word; position < rawStructure.get(row).size(); position++)
                                    rowArt.add("*".repeat(rawStructure.get(row).get(position).length()));
                                word = rawStructure.get(row).size(); //break?
                            }
                        }
                    }
                    fullArt.add(rowArt);
                }
                shuffleArt(fullArt);
                if (!outOfGas && !fileText.isEmpty()) processContents(fileText);
            }

            public void shuffleArt (ArrayList<ArrayList<String>> Sketch) {
                ArrayDeque<String> Art = new ArrayDeque();
                for (ArrayList<String> rows : Sketch) {
                    for (String words : rows) {
                        if (words.length() <= longestWord) {
                            for (int c = 0; c < words.length(); c++) Art.add(Character.toString(words.charAt(c)));
                        } else Art.add (words);
                    }
                }

                String letter = "";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < rawTemplate.length; i++){
                    for (int j = 0; j < rawTemplate[i].length;){
                        if (rawTemplate[i][j].equals("x")) {
                            if (Art.getFirst().length() == 1) {
                                letter = Art.getFirst();
                                Art.removeFirst();
                                j++;
                                sb.append(letter);
                            } else {
                                // spam blanks to end row
                                for (int start = j; start < rawTemplate[i].length; start++) sb.append(" ");
                                sb.append("\n" + Art.getFirst() + "\n");
                                Art.removeFirst();
                                // add back taken slots to new row
                                for (int start = 0; start < j; start++) sb.append(" ");
                                // push back index by 1 to restart queue
                            }
                        } else {
                            letter = " ";
                            j++;
                            // next word
                            sb.append(letter);
                        }
                    }
                    // next line
                    output.add(sb.toString());
                    sb = new StringBuilder();
                }
            }

            private ArrayList<String> getOutput () { return output; }
        }
    }
}
