package com.sim.application.controllers.obfuscation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.sim.application.classes.*;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.StringUtil;
import javafx.util.Pair;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObfuscateConstantController extends Technique {
    private static ObfuscateConstantController instance;
    private String name = "Encrypt Constants";
    private String description = "Encrypts constants using aes128";

    public static ObfuscateConstantController getInstance() {
        if (instance == null) {
            instance = new ObfuscateConstantController();
        }
        return instance;
    }

    private ObfuscateConstantController() {}

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void execute(Map<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {

        var decrypterUnitMap = new HashMap<String, CompilationUnit>();
        String currFile = "";
        for (var file : sourceFiles.keySet()) {
            currFile = file.getFileName();

            var unit = sourceFiles.get(file);
            var storage = unit.getStorage().get();
            var sourceRoot = Parser.getSourceRoot(storage.getSourceRoot());
            var sourceRootPath = sourceRoot.getRoot().toAbsolutePath().toString();

            boolean isNew = false;
            AtomicInteger constantCount = new AtomicInteger();
            var changeList = new ArrayList<Pair<Expression, MethodCallExpr>>();
            CompilationUnit decrypterUnit;
            if (decrypterUnitMap.containsKey(sourceRootPath)) {
                decrypterUnit = decrypterUnitMap.get(sourceRootPath);
            } else {
                var unitPackageDeclr = unit.getPackageDeclaration().isEmpty() ? null : unit.getPackageDeclaration().get().getNameAsString();
                var packageName = getPackageName(unitPackageDeclr);
                decrypterUnit = getNewDecrypterUnit(packageName);
                decrypterUnit.setStorage(Paths.get(sourceRootPath, getPackageFolder(packageName), "Decrypter.java"));
                decrypterUnitMap.put(sourceRootPath, decrypterUnit);
                isNew = true;
            }

            try {
                var constantVisitor = new ConstantVisitor(unit, decrypterUnit, constantCount, changeList);
                constantVisitor.visit(unit, classMap);
            } catch (Exception e) {
                e.printStackTrace();
                throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
            }

            if (constantCount.get() > 0) {
                decrypterUnit.findFirst(ClassOrInterfaceDeclaration.class)
                        .flatMap(ClassOrInterfaceDeclaration::getFullyQualifiedName)
                        .ifPresent(unit::addImport);
                for (var pair : changeList) {
                    Expression expr = pair.getKey();
                    MethodCallExpr method = pair.getValue();
                    expr.replace(method);
                }

                if (isNew) {
                    Parser.addCompilationUnit(sourceRootPath, decrypterUnit);
                }
            }
        }
    }

    private static String getPackageFolder(String packageName) {
        return packageName.replace(".", File.separator);
    }

    private static String getPackageName(String unitPackage) {
        if (unitPackage == null) return null;

        var unitPackageArray = unitPackage.split("\\.");
        StringBuilder packageName = new StringBuilder();
        int size = Math.min(unitPackageArray.length, 3);
        for (int i = 0; i < size; i++) {
            packageName.append(unitPackageArray[i]);
            if (i != size - 1) {
                packageName.append(".");
            }
        }
        return packageName.toString();
    }

    private static CompilationUnit getNewDecrypterUnit(String packageName) throws FailedTechniqueException {
        if (packageName == null || packageName.length() == 0) throw new FailedTechniqueException("Decrypter's package name cannot be empty");

        var str = "package " + packageName + ";\n" +
                "import javax.crypto.*;\n" +
                "import javax.crypto.spec.IvParameterSpec;\n" +
                "import javax.crypto.spec.SecretKeySpec;\n" +
                "import java.lang.reflect.Array;\n" +
                "import java.security.InvalidAlgorithmParameterException;\n" +
                "import java.security.InvalidKeyException;\n" +
                "import java.security.NoSuchAlgorithmException;\n" +
                "import java.util.Base64;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public class Decrypter {\n" +
                "    private static boolean isInitialized = false;\n" +
                "    private static Map<String, String> globalConstants;\n" +
                "\n" +
                "    private static void init() {\n" +
                "        if (isInitialized) return;\n" +
                "        addConstants();\n" +
                "        isInitialized = true;\n" +
                "    }\n" +
                "\n" +
                "    private static void addConstants() {\n" +
                "        globalConstants = new HashMap<>();\n" +
                "    }\n" +
                "\n" +
                "    public static <T> T getConstant(String key, Class<T> type) {\n" +
                "        init();\n" +
                "        String constant = key == null ? null : globalConstants.get(key);\n" +
                "        if (constant == null) return getDefaultValue(type);\n" +
                "        try {\n" +
                "            String decrypted = decrypt(key, constant);\n" +
                "            return toType(decrypted, type);\n" +
                "        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {\n" +
                "            return getDefaultValue(type);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static <T> T toType(String value, Class<T> type) {\n" +
                "        if (type.isPrimitive()) {\n" +
                "            if (type.equals(char.class)) return (T) Character.valueOf(value.charAt(0));\n" +
                "            else if (type.equals(int.class)) return (T) Integer.valueOf(value);\n" +
                "            else if (type.equals(double.class)) return (T) Double.valueOf(value);\n" +
                "            else if (type.equals(long.class)) return (T) Long.valueOf(value);\n" +
                "           else if (type.equals(boolean.class)) return (T) Boolean.valueOf(value);\n" +
                "            else return getDefaultValue(type);\n" +
                "        } else {\n" +
                "            if (type.equals(String.class)) return type.cast(value);\n" +
                "            else if (type.equals(Character.class)) return type.cast(value.charAt(0));\n" +
                "            else if (type.equals(Integer.class)) return type.cast(Integer.valueOf(value));\n" +
                "            else if (type.equals(Double.class)) return type.cast(Double.valueOf(value));\n" +
                "            else if (type.equals(Long.class)) return type.cast(Long.valueOf(value));\n" +
                "            else if (type.equals(Boolean.class)) return type.cast(Boolean.valueOf(value));\n" +
                "            else return getDefaultValue(type);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static <T> T getDefaultValue(Class<T> type) {\n" +
                "        if (type.isPrimitive()) return (T)Array.get(Array.newInstance(type, 1), 0);\n" +
                "        else if (type.equals(String.class)) return type.cast(\"\");\n" +
                "        else if (type.equals(Character.class)) return type.cast('\\0');\n" +
                "        else if (type.equals(Integer.class)) return type.cast(0);\n" +
                "        else if (type.equals(Double.class)) return type.cast(0.0);\n" +
                "        else if (type.equals(Long.class)) return type.cast(0l);\n" +
                "        else if (type.equals(Boolean.class)) return type.cast(false);\n" +
                "        else return null;\n" +
                "    }\n" +
                "\n" +
                "    private static String decrypt(String key, String encryptedString) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidAlgorithmParameterException {\n" +
                "        byte[] keyIV = Base64.getDecoder().decode(key);\n" +
                "        byte[] decodedEncryptedString = Base64.getDecoder().decode(encryptedString);\n" +
                "        Cipher cipher = Cipher.getInstance(\"AES/CBC/PKCS5Padding\");\n" +
                "        SecretKey originalKey = new SecretKeySpec(keyIV, 0, 16, \"AES\");\n" +
                "        IvParameterSpec iv = new IvParameterSpec(keyIV, 16, 16);\n" +
                "       cipher.init(Cipher.DECRYPT_MODE, originalKey, iv);\n" +
                "        byte[] decrypted = cipher.doFinal(decodedEncryptedString);\n" +
                "\n" +
                "        return new String(decrypted);\n" +
                "    }\n" +
                "}";

        return StaticJavaParser.parse(str);
    }

    private class ConstantVisitor extends ModifierVisitor<ClassMap> {

        private final CompilationUnit unit;
        private final CompilationUnit decrypterUnit;
        private AtomicInteger constantCount;
        private final ArrayList<Pair<Expression, MethodCallExpr>> changeList;
        private final StringEncrypt stringEncrypt;
        private final MethodDeclaration constantsInitMethod;

        private ConstantVisitor(CompilationUnit unit,
                                CompilationUnit decrypterUnit,
                                AtomicInteger constantCount,
                                ArrayList<Pair<Expression, MethodCallExpr>> changeList) {
            this.unit = unit;
            this.decrypterUnit = decrypterUnit;
            this.constantCount = constantCount;
            this.changeList = changeList;
            this.stringEncrypt = new StringEncrypt();
            this.constantsInitMethod = getConstantsInitMethod();
        }

        private MethodDeclaration getConstantsInitMethod() {
            return decrypterUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .filter(methodDeclaration ->
                            methodDeclaration.getNameAsString().equals("addConstants"))
                                .findFirst()
                                .orElseThrow();

        }

        public CharLiteralExpr visit(CharLiteralExpr cl, ClassMap classMap) {
            super.visit(cl, classMap);

            try {
                if (!isValidExprToReplace(cl)) return cl;
                var keyConstantPair = stringEncrypt.encrypt(String.valueOf(cl.getValue()));
                replaceWithEncryptedString(cl, keyConstantPair, "Character.class");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return cl;
        }

        public StringLiteralExpr visit(StringLiteralExpr sl, ClassMap classMap) {
            super.visit(sl, classMap);

            try {
                if (!isValidExprToReplace(sl)) return sl;
                var keyConstantPair = stringEncrypt.encrypt(String.valueOf(sl.getValue()));
                replaceWithEncryptedString(sl, keyConstantPair, "String.class");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return sl;
        }

        public IntegerLiteralExpr visit(IntegerLiteralExpr il, ClassMap classMap) {
            super.visit(il, classMap);

            try {
                if (!isValidExprToReplace(il)) return il;
                var value = getNumberValue(il);
                var keyConstantPair = stringEncrypt.encrypt(value);
                replaceWithEncryptedString(il, keyConstantPair, "Integer.class");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return il;
        }

        public DoubleLiteralExpr visit(DoubleLiteralExpr dl, ClassMap classMap) {
            super.visit(dl, classMap);

            try {
                if (!isValidExprToReplace(dl)) return dl;
                var value = getNumberValue(dl);
                var keyConstantPair = stringEncrypt.encrypt(value);
                replaceWithEncryptedString(dl, keyConstantPair, "Double.class");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return dl;
        }

        public LongLiteralExpr visit(LongLiteralExpr ll, ClassMap classMap) {
            super.visit(ll, classMap);

            try {
                if (!isValidExprToReplace(ll)) return ll;
                var value = getNumberValue(ll);
                var keyConstantPair = stringEncrypt.encrypt(value);
                replaceWithEncryptedString(ll, keyConstantPair, "Long.class");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ll;
        }

        public BooleanLiteralExpr visit(BooleanLiteralExpr bl, ClassMap classMap) {
            super.visit(bl, classMap);

            try {
                if (!isValidExprToReplace(bl)) return bl;
                var keyConstantPair = stringEncrypt.encrypt(String.valueOf(bl.getValue()));
                replaceWithEncryptedString(bl, keyConstantPair, "Boolean.class");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bl;
        }

        private String getNumberValue(Expression expr) {
            if (expr.getParentNode().isPresent()) {
                if(expr.getParentNode().get().getClass().equals(UnaryExpr.class)) {
                    var unary = (UnaryExpr)expr.getParentNode().get();
                    if (unary.getOperator() == UnaryExpr.Operator.MINUS) {
                        if (expr.getClass().equals(IntegerLiteralExpr.class))
                            return String.valueOf(((IntegerLiteralExpr)expr).asNumber().intValue() * -1);
                        else if (expr.getClass().equals(DoubleLiteralExpr.class))
                            return String.valueOf(((DoubleLiteralExpr)expr).asDouble() * -1.0);
                        else if (expr.getClass().equals(LongLiteralExpr.class))
                            return String.valueOf(((LongLiteralExpr)expr).asNumber().longValue() * -1l);
                    }
                }
            }
            if (expr.getClass().equals(IntegerLiteralExpr.class))
                return String.valueOf(((IntegerLiteralExpr)expr).asNumber().intValue());
            else if (expr.getClass().equals(DoubleLiteralExpr.class))
                return String.valueOf(((DoubleLiteralExpr)expr).asDouble());
            else if (expr.getClass().equals(LongLiteralExpr.class))
                return String.valueOf(((LongLiteralExpr)expr).asNumber().longValue());
            else
                return null;
        }

        private String addEscapeChars(String str) {
            final String[] metaCharacters = {"\\"};
            String newStr = str;
            for (String character : metaCharacters) {
                newStr = newStr.replace(character, "\\" + character);
            }
            return newStr;
        }

        private boolean isValidExprToReplace(Expression expr) {
            return expr.findAncestor(AnnotationDeclaration.class).isEmpty() &&
                   expr.findAncestor(NormalAnnotationExpr.class).isEmpty() &&
                   expr.findAncestor(SingleMemberAnnotationExpr.class).isEmpty();
        }

        private void replaceWithEncryptedString(Expression expr, Pair<String, String> keyConstantPair, String type) {
            constantsInitMethod.getBody().ifPresent(blockStmt -> {
                blockStmt.addStatement(
                        "globalConstants.put(" +
                                "\"" + addEscapeChars(keyConstantPair.getKey()) + "\"," +
                                "\"" + addEscapeChars(keyConstantPair.getValue()) + "\"" +
                                ");"
                );

                var scope = new NameExpr("Decrypter");
                var method = new MethodCallExpr(scope, "getConstant");
                method.addArgument("\"" + keyConstantPair.getKey() + "\"");
                method.addArgument(type);
                changeList.add(new Pair<>(expr, method));
                constantCount.incrementAndGet();
            });
        }

        private class StringEncrypt {
            private final int AES_KEY_SIZE = 128;
            private final KeyGenerator keyGenerator;
            private final SecureRandom rng;

            private StringEncrypt() {
                try {
                    this.keyGenerator = KeyGenerator.getInstance("AES");
                } catch (final NoSuchAlgorithmException e) {
                    throw new RuntimeException("AES key generator should always be available in a Java runtime", e);
                }
                try {
                    this.rng = SecureRandom.getInstanceStrong();
                } catch (final NoSuchAlgorithmException e) {
                    throw new RuntimeException("No strong secure random available to generate strong AES key", e);
                }
                keyGenerator.init(AES_KEY_SIZE, rng);
            }

            private SecretKey generateAESKey() {
                return keyGenerator.generateKey();
            }

            private IvParameterSpec generateIV(Cipher cipher) {
                byte[] iv = new byte[cipher.getBlockSize()];
                rng.nextBytes(iv);
                return new IvParameterSpec(iv);
            }

            public Pair<String, String> encrypt(String constant) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
                byte[] input = constant.getBytes();
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKey key = generateAESKey();
                IvParameterSpec iv = generateIV(cipher);
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
                byte[] encryptedOutput = cipher.doFinal(input);

                byte[] keyIV = StringUtil.appendByteArray(key.getEncoded(), iv.getIV());
                String encodedKey = Base64.getEncoder().encodeToString(keyIV);
                String output = Base64.getEncoder().encodeToString(encryptedOutput);

                return new Pair<>(encodedKey, output);
            }
        }
    }
}
