package com.sim.application.controllers.obfuscation;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;

public class CreateDummyVariableController
{

    protected String generateDummyVariable()
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

    private String generateString(int value)
    {
        SecureRandom rnd = new SecureRandom();
        MessageDigest md = null;
        try { md = MessageDigest.getInstance("SHA-1"); }
        catch(NoSuchAlgorithmException N) { System.out.println(N); System.exit(1); }

        md.update(String.valueOf(value).getBytes());
        byte[] byteDigest = md.digest();

        StringBuffer sb = new StringBuffer();
        for (byte b : byteDigest) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

        return (char) (rnd.nextInt(122 - 97) + 97) + sb.substring(rnd.nextInt(sb.length()/2),rnd.nextInt(sb.length()/2) + sb.length()/2);
    }
}