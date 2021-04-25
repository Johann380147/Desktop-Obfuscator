package com.sim.application.controllers.obfuscation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.sim.application.classes.JavaFile;
import com.sim.application.techniques.FailedTechniqueException;
import com.github.javaparser.ast.Modifier.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class InsertMethodVariableController
{
    protected void updateContents(Map<JavaFile, CompilationUnit> source) throws FailedTechniqueException
    {
        for(JavaFile file : source.keySet())
        {
            var currFile = file.getFileName();
            CompilationUnit unit = source.get(file);

            try {

                var types = unit.getTypes();
                for (TypeDeclaration<?> type : types)
                {
                    boolean isInterface = type instanceof ClassOrInterfaceDeclaration && ((ClassOrInterfaceDeclaration) type).isInterface();
                    boolean isEnumeration = type instanceof EnumDeclaration;
                    boolean isAnnotation = type instanceof AnnotationDeclaration;

                    boolean isClass = !isInterface && !isEnumeration && !isAnnotation;
                    boolean isInnerClass = type instanceof ClassOrInterfaceDeclaration && ((ClassOrInterfaceDeclaration) type).isInnerClass();
                    boolean isAbstract = type instanceof ClassOrInterfaceDeclaration && ((ClassOrInterfaceDeclaration) type).isAbstract();

                    var nodeList = type.getMembers();
                    var iterator = nodeList.listIterator();
                    while (iterator.hasNext())
                    {
                        var member = iterator.next();

                        if (member instanceof FieldDeclaration) {
                            try {
                                iterator.add(generateFieldDeclarators(isInterface));
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        } else if (member instanceof MethodDeclaration) {
                            ((MethodDeclaration) member).getBody().ifPresent(body -> {

                                var statements = body.getStatements();
                                var iterator2 = statements.listIterator();

                                while (iterator2.hasNext()) {
                                    var member2 = iterator2.next();

                                    if (member2 instanceof ExpressionStmt) {
                                        var expr = ((ExpressionStmt) member2).getExpression();
                                        if (expr instanceof VariableDeclarationExpr) {
                                            try {
                                                iterator2.add(StaticJavaParser.parseStatement(generateDummyVariableAsString()));
                                            } catch (NoSuchAlgorithmException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            });

                            //generate dummy methods
                            iterator.add(generateDummyMethod(isInterface));
                        }
                    }
                }
            } catch(NoSuchAlgorithmException | NoSuchElementException e) {
                throw new FailedTechniqueException(currFile + "Obfuscation failed; No such hash algorithm." + e.getMessage()).setFileName(currFile);
            }
        }
    }

    protected FieldDeclaration generateFieldDeclarators(boolean isInterface) throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();
        NodeList<Modifier> mod = new NodeList<>();


        if (!isInterface)
        {
            //select random modifier
            switch (rnd.nextInt(3)) {
                case 0:
                    mod.add(Modifier.publicModifier());
                    break;
                case 1:
                    mod.add(Modifier.privateModifier());
                    break;
                case 2:
                    mod.add(Modifier.protectedModifier());
                    break;
                default:
                    break;
            }

            if (rnd.nextBoolean())
                mod.add(Modifier.finalModifier());

            if (rnd.nextBoolean())
                mod.add(Modifier.staticModifier());
        }

        return new FieldDeclaration(mod, generateDummyVariable());
    }

    protected VariableDeclarator generateDummyVariable() throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();
        VariableDeclarator vd = new VariableDeclarator();
        vd.setName(generateString(rnd.nextInt()));

        //set variable type
        switch(rnd.nextInt(4))
        {
            case 0:
                vd.setType(int.class);
                vd.setInitializer(String.format("%s", rnd.nextInt()));
                break;
            case 1:
                vd.setType(boolean.class);
                vd.setInitializer(String.format("%s", rnd.nextBoolean()));
                break;
            case 2:
                vd.setType(char.class);
                vd.setInitializer(String.format("'%s'", (char) (rnd.nextInt('z' - 'a') + 'a')));
                break;
            case 3:
                vd.setType(String.class);
                vd.setInitializer(String.format("\"%s\"", generateString(rnd.nextInt())));
                break;
            default:
                break;
        }

        return vd;
    }

    protected String generateDummyVariableAsString() throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();

        //generate random variable name
        String name = generateString(rnd.nextInt());
        String datatype = "", value = "";

        switch (rnd.nextInt(9))
        {
            case 0:
                datatype = "byte";
                value = String.valueOf(rnd.nextInt(127 + 128) - 128);
                break;
            case 1:
                datatype = "short";
                value = String.valueOf(rnd.nextInt(1 << 16));
                break;
            case 2:
                datatype = "int";
                value = String.valueOf(rnd.nextInt());
                break;
            case 3:
                datatype = "long";
                value = String.valueOf(rnd.nextLong());
                break;
            case 4:
                datatype = "float";
                value = String.format("%.2f",rnd.nextFloat());
                break;
            case 5:
                datatype = "double";
                value = String.valueOf(rnd.nextDouble());
                break;
            case 6:
                datatype = "boolean";
                value = String.valueOf(rnd.nextBoolean());
                break;
            case 7:
                datatype = "char";
                value = String.valueOf((char) (rnd.nextInt(90 - 65) + 65));
                break;
            case 8:
                datatype = "String";
                value = generateString(rnd.nextInt());
                break;
        }

        if (datatype.equals("String"))
            return String.format("%s %s = \"%s\";",datatype, name, value);
        else if (datatype.equals("char"))
            return String.format("%s %s = '%s';",datatype, name, value);
        else if (datatype.equals("long"))
            return String.format("%s %s = %sL;",datatype, name, value);
        else if (datatype.equals("float") || datatype.equals("short") )
            return String.format("%s %s = (%s) %s;",datatype, name, datatype, value);

        return String.format("%s %s = %s;",datatype, name, value);

    }

    protected MethodDeclaration generateDummyMethod(boolean isInterface) throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();
        MethodDeclaration md = new MethodDeclaration();
        BlockStmt block = new BlockStmt();

        md.setName(generateString(rnd.nextInt()));
        String pName1 = generateString(rnd.nextInt());
        String pName2 = generateString(rnd.nextInt());
        String c = generateString(rnd.nextInt());

        if (isInterface)
        {
            md.addModifier(Keyword.DEFAULT);
        } else
        {
            //select random modifier
            switch (rnd.nextInt(3)) {
                case 0:
                    md.addModifier(Keyword.PUBLIC);
                    break;
                case 1:
                    md.addModifier(Keyword.PRIVATE);
                    break;
                case 2:
                    md.addModifier(Keyword.PROTECTED);
                    break;
                default:
                    break;
            }

            if (rnd.nextBoolean())
                md.addModifier(Keyword.STATIC);
        }

        //select random method type
        switch (rnd.nextInt(4))
        {
            case 0:
                md.setType(int.class);

                switch(rnd.nextInt(4))
                {
                    case 0:
                        md.addParameter(int.class, pName1);

                        block.addStatement(String.format("int counter = 0;"));
                        block.addStatement(String.format("while(%s > 0 && %s > %s) { %s += counter; counter ++; }", pName1, pName1, rnd.nextInt(100), pName1));
                        block.addStatement(String.format("return %s;", pName1));
                        md.setBody(block);
                        break;

                    case 1:
                        md.addParameter(int.class, pName1);
                        block.addStatement(String.format("final int %s = %s;", pName2, rnd.nextInt(100)));
                        block.addStatement(String.format("int[] %s = { %s,%s,%s,%s, %s, %s };", c,rnd.nextInt(100), rnd.nextInt(100), rnd.nextInt(100), rnd.nextInt(100), rnd.nextInt(100), rnd.nextInt(100)));
                        block.addStatement(String.format("if (%s  != 0) { for(int i = 0; i < %s.length; i++) { %s = (%s * %s[i]) %% %s; } }",pName1, c, pName1, pName1, c, pName2));
                        block.addStatement(String.format("return %s;", pName1));
                        md.setBody(block);
                        break;

                    case 2:
                        md.addParameter(int.class, pName1);
                        md.addParameter(int.class, pName2);
                        block.addStatement(String.format("if (%s < %s) { return 0; } else { return %s + %s; }", pName1, pName2, pName1, pName2));
                        md.setBody(block);
                        break;

                    case 3:
                        md.addParameter(int.class, pName1);
                        md.addParameter(int.class, pName2);
                        block.addStatement(String.format("if ( %s> 0) { return 0; }", pName1));
                        block.addStatement(String.format("if ( %s < 0) { return 1; }", pName1));
                        block.addStatement(String.format("return (%s %% %s);", pName1, pName2));
                        md.setBody(block);
                        break;
                    default:
                        break;
                }
                break;

            case 1:
                md.setType(boolean.class);

                switch(rnd.nextInt(2))
                {
                    case 0:
                        md.addParameter(int.class, pName1);
                        md.addParameter(String.class, pName2);
                        block.addStatement(String.format("if (%s > %s) { return false; }", pName1, rnd.nextInt(100)));
                        block.addStatement(String.format("if (%s.length() > %s) { return false; }", pName2, rnd.nextInt(100)));
                        block.addStatement(String.format("if (%s == %s && %s.length() == %s) { return false; }", pName1, rnd.nextInt(100), pName2, rnd.nextInt(20)));
                        block.addStatement(String.format("return true;"));
                        md.setBody(block);
                        break;

                    case 1:
                        md.addParameter(int.class, pName1);
                        md.addParameter(char.class,pName2);
                        block.addStatement(String.format("if (%s > %s && Character.compare(%s,'%s') == 1) { return true; }", pName1, rnd.nextInt(50), pName2, (char) (rnd.nextInt('z' - 'a') + 'a')));
                        block.addStatement(String.format("if (%s < %s && Character.compare(%s,'%s') == 0) { return true; }", pName1, rnd.nextInt(40), pName2, (char) (rnd.nextInt('z' - 'a') + 'a')));
                        block.addStatement(String.format("return false;"));
                        md.setBody(block);
                        break;
                    default:
                        break;
                }
                break;

            case 2:
                md.setType(char.class);
                md.addParameter(int.class, pName1);
                md.addParameter(boolean.class, pName2);

                block.addStatement(String.format("if(%s < %s && %s) { return '%s'; } else { %s = %s + '%s'; }", pName1, rnd.nextInt(30), pName2, (char) (rnd.nextInt('z' - 'a') + 'a'), pName1, pName1, (char) (rnd.nextInt('z' - 'a') + 'a') ));
                block.addStatement(String.format("return (char) %s;", pName1));
                md.setBody(block);
                break;

            case 3:
                md.setType(String.class);

               switch(rnd.nextInt(3))
                {
                    case 0:
                        md.addParameter(String.class, pName1);
                        md.addParameter(String.class, pName2);

                        block.addStatement(String.format("String %s = %s;", c, pName2));
                        block.addStatement(String.format("if (%s == \"\"){ return null; } else if (%s == \"0\") { %s = %s + \":\" + %s + \":\" + \"%s\"; } else if (%s == \"1\") { %s = %s + \":\" + %s + \":\" + \"%s\"; } else if (%s == \"2\") { %s = %s + \":\" + %s + \":\" + \"%s\"; }", pName1, pName2, c, pName1, pName2, generateString(rnd.nextInt()), pName2, c, pName1, pName2, generateString(rnd.nextInt()), pName2, c, pName1, pName2, generateString(rnd.nextInt())));
                        block.addStatement(String.format("return %s;", c));
                        md.setBody(block);
                        break;

                    case 1:
                        md.addParameter(String.class, pName1);

                        block.addStatement(String.format("int %s = 0;", c));
                        block.addStatement(String.format("for(int i = 0; i < %s.length(); i++) { %s += %s.charAt(i); }", pName1, c, pName1));
                        block.addStatement(String.format("return String.valueOf(%s);", c));
                        md.setBody(block);
                        break;

                    case 2:
                        md.addParameter(String.class, pName1);
                        md.addParameter(String.class, pName2);
                        block.addStatement(String.format("String %s = %s.charAt(0) + \".\" + %s.charAt(0);", c, pName1, pName2));
                        block.addStatement(String.format("return %s;", c));
                        md.setBody(block);
                        break;

                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return md;
    }

    private String generateString(int value) throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        md.update(String.valueOf(value).getBytes());
        byte[] byteDigest = md.digest();

        StringBuffer sb = new StringBuffer();
        for (byte b : byteDigest) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

        return (char) (rnd.nextInt(122 - 97) + 97) + sb.substring(rnd.nextInt(sb.length()/2),rnd.nextInt(sb.length()/2) + sb.length()/2);
    }
}