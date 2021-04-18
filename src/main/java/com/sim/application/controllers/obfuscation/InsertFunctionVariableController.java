package com.sim.application.controllers.obfuscation;
import com.github.javaparser.ast.CompilationUnit;
import com.sim.application.classes.JavaFile;
import java.util.Map;

public class InsertFunctionVariableController
{

    protected void readFile(Map<JavaFile, CompilationUnit> source) {

        for (CompilationUnit unit : source.values()) {
            String[] sourceCodeString = unit.toString().split("\r?\n");

            for (int i = 0; i < sourceCodeString.length; i++) {
                String[] lines = sourceCodeString[i].split("[ ]");

                //insert dummy functions
                if ((lines[0].equals("public") || lines[0].equals("private") || lines[0].equals("protected") || sourceCodeString[i].contains("void")) && sourceCodeString[i].contains("class") && sourceCodeString[i].contains("public static void main")) {
                    CreateDummyFunctionController df = new CreateDummyFunctionController();
                    String.format(df.generateDummyFunction() + "\n\n" + sourceCodeString[i] + "\n");
                }

                // insert dummy variables
                else if ((lines[0].equals("boolean") || lines[0].equals("String") || lines[0].equals("int") || lines[0].equals("long") || lines[0].equals("char") || lines[0].equals("double") || lines[0].equals("float") || lines[0].equals("byte")) && sourceCodeString[i].contains("=")) {

                    CreateDummyVariableController dv = new CreateDummyVariableController();
                    String.format(dv.generateDummyVariable() + "\n" + sourceCodeString[i] + "\n");
                }
            }
        }
    }
}