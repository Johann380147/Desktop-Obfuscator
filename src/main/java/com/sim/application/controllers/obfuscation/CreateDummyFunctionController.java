package com.sim.application.controllers.obfuscation;

import com.sim.application.techniques.FailedTechniqueException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CreateDummyFunctionController
{

    protected String generateDummyFunction() throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();

        String  datatype = "", value = "",
                fName = generateString(rnd.nextInt());
        int type = 0;

        //generate data type
        switch (rnd.nextInt(9)) {
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
                value = String.valueOf(rnd.nextFloat());
                break;
            case 5:
                datatype = "double";
                value = String.valueOf(rnd.nextDouble());
                break;
            case 6:
                datatype = "boolean";
                value = String.valueOf(rnd.nextBoolean());
                type = 1;
                break;
            case 7:
                datatype = "char";
                value = String.valueOf((char) (rnd.nextInt(90 - 65) + 65));
                type = 2;
                break;
            case 8:
                datatype = "String";
                value = generateString(rnd.nextInt());
                type = 3;
        }

        return generateContents(fName, datatype, value, type);
    }

    private String generateString(int value) throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();
        MessageDigest md = null;
        md = MessageDigest.getInstance("SHA-1");

        md.update(String.valueOf(value).getBytes());
        byte[] byteDigest = md.digest();

        StringBuffer sb = new StringBuffer();
        for (byte b : byteDigest) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

        return (char) (rnd.nextInt(122 - 97) + 97) + sb.substring(rnd.nextInt(sb.length()/2),rnd.nextInt(sb.length()/2) + sb.length()/2);
    }

    private String generateContents(String fName, String datatype, String value, int type) throws NoSuchAlgorithmException
    {
        SecureRandom rnd = new SecureRandom();

        String visibility = rnd.nextBoolean() ? "public " : "private ";
        String staticity = rnd.nextBoolean() ? "static " : "";

        String argName1 = generateString(rnd.nextInt()), argName2 = generateString(rnd.nextInt());
        String a, b, c, d;

        String arguments = "";
        String header = "";

        /*
            type 0: int/short/byte/long/float/double
                 1: boolean
                 2: char
                 3: String
         */

        if (type == 0) //  int/short/byte/long/float/double
        {
            switch(rnd.nextInt(4))
            {
                case 0:
                    a = generateString(rnd.nextInt());
                    arguments = String.format("%s %s", datatype, argName1);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("int %s = 0; while (%s > 0 && %s > %s) { %s += %s; %s ++; } return %s;\n}",
                            a, argName1, argName1, a, argName1, a, a, argName1);
                case 1:
                    a = generateString(rnd.nextInt());
                    b = generateString(rnd.nextInt());
                    arguments = String.format("%s %s", "int", argName1);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("final int %s = %s; int[] %s = {	%s, %s, %s, %s, %s, %s }; if (%s != 0) { for(int i = 0; i < %s.length; i++) { %s = (%s * %s[i]) - %s; } } return (%s) %s;\n}",
                     a, rnd.nextInt(100), b, rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), argName1, b, argName1, argName1, b, a, datatype, argName1);
                case 2:
                    arguments = String.format("%s %s, %s %s", "int", argName1, "int", argName2);
                    header = String.format("%s%s%s %s(%s) throws Exception, IllegalStateException {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("if(%s > 0) { if (%s >= (%s - %s)) { throw new Exception(\"%s\"); } %s(%s, %s); } if ( %s < 0) { throw new IllegalStateException(\"%s\"); } return (%s) (%s + %s);\n}",
                    argName1, argName2, argName1, rnd.nextInt(100), generateString(rnd.nextInt()), fName, argName2, argName1, argName1, generateString(rnd.nextInt()), datatype, argName1, argName2);
                case 3:
                    arguments = String.format("%s %s, %s %s", "int", argName1, "int", argName2);
                    header = String.format("%s%s%s %s(%s) throws IllegalArgumentException {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("if (%s < %s) { throw new IllegalArgumentException(\"%s\"); } return (%s) (%s - %s);\n}",
                            argName1, argName2, generateString(rnd.nextInt()), datatype, argName1, argName2);
            }
        }
        if (type == 1) // boolean
        {
            switch(rnd.nextInt(2))
            {
                case 0:
                    arguments = String.format("%s %s, %s %s", "int", argName1, "String", argName2);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("if (%s > 3) { return false; } if (%s.length() > %s) { return false; } if (%s == %s && %s.length() == %s) { return false; } return true;\n}",
                            argName1, argName2, String.valueOf(rnd.nextInt(100)), argName1, String.valueOf(rnd.nextInt(100)), argName2, String.valueOf(rnd.nextInt(100)));
                case 1:
                    a = String.valueOf((char) rnd.nextInt(122 - 97) + 97);
                    arguments = String.format("%s %s, %s %s", "int", argName1, "char", argName2);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("if (%s > 18 && Character.compare(%s,(char) %s) == 0) { return true; } if (%s < 18 && Character.compare(%s, (char) %s) == 0) { return true; } return false;\n}",
                    argName1, argName2, a, argName1, argName2, a);
            }
        }

        if (type == 2) // char
        {
            switch(rnd.nextInt(2))
            {
                case 0:
                    arguments = String.format("%s %s, %s %s", "int", argName1, "boolean", argName2);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("if(%s < 18 && %s) { return (char) %s; } else { %s = %s + (char) %s; } return (char) %s;\n}",
                            argName1, argName2, rnd.nextInt(122-97) + 97, argName1, argName1, rnd.nextInt(122-97) + 97, argName1);
                case 1:
                    a = String.valueOf( (char) (rnd.nextInt(122-97) + 97));
                    b = String.valueOf((char) (rnd.nextInt(122-97) + 97));
                    c = String.valueOf(rnd.nextInt(14999) + 30000);
                    d = String.valueOf(rnd.nextInt(1000) + 40000);
                    arguments = String.format("%s %s, %s %s", "int", argName1, "boolean", argName2);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("if (%s > %s && %s < %s && %s) { %s -= 2500; return '%s'; } else if (%s > %s && %s < %s && %s) { %s -= 2000; return '%s'; } else if (%s < %s && %s) { %s -= 1000; return '%s'; } %s -= 500; return '%s';\n}",
                            argName1, c, argName1, d, argName2, argName1, a, argName1, c, argName1, d, argName2, argName1, a, argName1, c, argName2, argName1, b, argName1, b);
            }
        }

        if (type == 3) // String
        {
            switch(rnd.nextInt(3))
            {
                case 0:
                    a = generateString(rnd.nextInt());
                    b = generateString(rnd.nextInt());
                    c = generateString(rnd.nextInt());
                    d = generateString(rnd.nextInt());

                    arguments = String.format("%s %s, %s %s", datatype, argName1, datatype, argName2);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("String %s = \"%s\"; String %s =  %s; String %s = %s; String %s = %s +\" \" +  %s + \" \" + %s; return %s; \n}",
                            a, generateString(rnd.nextInt()), b, argName1, c, argName2, d, a, b, c, d);
                case 1:
                    a = generateString(rnd.nextInt());
                    arguments = String.format("%s %s, %s %s", datatype, argName1, datatype, argName2);
                    header = String.format("%s%s%s %s(%s) throws Exception {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("String %s = \"\"; if (%s == \"\") throw new Exception(\"%s\"); else if (%s == \"0\") { %s = %s + \":\" + %s + \":\" + \"%s\"; } else if (%s == \"1\") { %s = %s + \":\" + %s + \":\" + \"%s\"; } else if (%s == \"2\") { %s = %s + \":\" + %s + \":\" + \"%s\"; } return %s;\n}",
                            a, argName1, generateString(rnd.nextInt()),
                            argName2, a, argName1, argName2, generateString(rnd.nextInt()),
                            argName2, a, argName1, argName2, generateString(rnd.nextInt()),
                            argName2, a, argName1, argName2, generateString(rnd.nextInt()), a);
                case 2:
                    a = generateString(rnd.nextInt());
                    arguments = arguments = String.format("%s %s", datatype, argName1);
                    header = String.format("%s%s%s %s(%s) {\n", visibility, staticity, datatype, fName, arguments);

                    return header + String.format("int %s = %s; for(int i = 0; i < %s.length(); i++) { %s += %s.charAt(i); } return String.valueOf(%s);\n}",
                            a, rnd.nextInt(69), argName1, a, argName1, a);
            }
        }

        return "";

    }


}