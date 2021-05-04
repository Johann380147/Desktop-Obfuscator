package com.sim.application.controllers.obfuscation;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.collect.BiMap;
import com.sim.application.classes.*;
import com.sim.application.controllers.AddFileToDirectoryController;
import com.sim.application.techniques.FailedTechniqueException;
import com.sim.application.techniques.Technique;
import com.sim.application.utils.StringEncryption;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class ObfuscateConstantController extends Technique {
    private static ObfuscateConstantController instance;
    private final String name = "Encrypt Constants";
    private final String description = "Encrypts constants using aes128";

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
    public void execute(BiMap<JavaFile, CompilationUnit> sourceFiles, ClassMap classMap, List<Problem> problemList) throws FailedTechniqueException {

        StringEncryption stringEncryption = new StringEncryption();
        var decrypterPathUnitMap = new HashMap<String, CompilationUnit>();
        var decrypterFileUnitList = new ArrayList<Pair<JavaFile, CompilationUnit>>();
        String currFile = "";
        for (var file : sourceFiles.keySet()) {
            currFile = file.getFileName();

            var unit = sourceFiles.get(file);
            var storage = unit.getStorage().get();
            var sourceRoot = Parser.getSourceRoot(storage.getSourceRoot());
            if (sourceRoot == null) {
                throw new FailedTechniqueException(currFile + " failed to obfuscate. Please ensure the file's package name matches its containing directory.").setFileName(currFile);
            }
            var sourceRootPath = sourceRoot.getRoot().toAbsolutePath().toString();

            boolean isNew = false;
            AtomicInteger constantCount = new AtomicInteger();
            var changeList = new ArrayList<Pair<Expression, Expression>>();
            CompilationUnit decrypterUnit;

            // Check if decrypter class already exists for current source path
            if (decrypterPathUnitMap.containsKey(sourceRootPath)) {
                decrypterUnit = decrypterPathUnitMap.get(sourceRootPath);
            } else {
                decrypterUnit = getDecrypterUnit(unit, stringEncryption.getEncryptedVariableName(), sourceRootPath, sourceRoot);
                isNew = true;
            }

            // Gather all constants that need to be changed
            try {
                var constantVisitor = new ConstantVisitor(decrypterUnit, constantCount, changeList, stringEncryption);
                constantVisitor.visit(unit, null);
            } catch (Exception e) {
                e.printStackTrace();
                throw new FailedTechniqueException(currFile + " failed to obfuscate. " + e.getMessage()).setFileName(currFile);
            }

            // Make the changes
            if (constantCount.get() > 0) {
                decrypterUnit.findFirst(ClassOrInterfaceDeclaration.class)
                        .flatMap(ClassOrInterfaceDeclaration::getFullyQualifiedName)
                        .ifPresent(unit::addImport);
                for (var pair : changeList) {
                    Expression expr = pair.getKey();
                    Expression method = pair.getValue();
                    expr.replace(method);
                }

                if (isNew) {
                    var javaFile = new JavaFile(
                            file.getRootPath(),
                            decrypterUnit.getStorage().get().getPath().toString(),
                            "",
                            true);
                    decrypterFileUnitList.add(new Pair<>(javaFile, decrypterUnit));
                    decrypterPathUnitMap.put(sourceRootPath, decrypterUnit);
                    Parser.addCompilationUnit(decrypterUnit);
                    AddFileToDirectoryController.addFile(javaFile);
                }
            }
        }

        // Update decrypter file's content
        for (var pair : decrypterFileUnitList) {
            var file = pair.getKey();
            var unit = pair.getValue();
            var content = unit.toString();

            file.setContent(content);
            file.setObfuscatedContent(content);
        }
    }

    private static CompilationUnit getDecrypterUnit(CompilationUnit unit, String className, String srcPath, SourceRoot sourceRoot) throws FailedTechniqueException {
        var unitPackageDeclr = unit.getPackageDeclaration().isEmpty() ? null : unit.getPackageDeclaration().get().getNameAsString();
        var packageName = getPackageName(unitPackageDeclr);
        var decrypterUnit = createDecrypterUnit(packageName, className, sourceRoot.getParserConfiguration());
        String packageFolder = getPackageFolder(packageName);
        packageFolder = packageFolder == null ? "" : packageFolder;
        decrypterUnit.setStorage(Paths.get(srcPath, packageFolder, className + ".java"));
        return decrypterUnit;
    }

    private static String getPackageFolder(String packageName) {
        if (packageName == null) return null;
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

    private static CompilationUnit createDecrypterUnit(String packageName, String className, ParserConfiguration parserConfiguration) throws FailedTechniqueException {
        String packageString = "";
        if (packageName != null && packageName.length() > 0) {
            packageString = "package " + packageName + ";\n";
        }

        var str = packageString +
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
                "public class " + className + " {\n" +
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
                "            if (type.equals(char.class)) return (T) getCharFromString(value);\n" +
                "            else if (type.equals(int.class)) return (T) Integer.valueOf(value);\n" +
                "            else if (type.equals(float.class)) return (T) Float.valueOf(value);\n" +
                "            else if (type.equals(double.class)) return (T) Double.valueOf(value);\n" +
                "            else if (type.equals(long.class)) return (T) Long.valueOf(value);\n" +
                "           else if (type.equals(boolean.class)) return (T) Boolean.valueOf(value);\n" +
                "            else return getDefaultValue(type);\n" +
                "        } else {\n" +
                "            if (type.equals(String.class)) return type.cast(value);\n" +
                "            else if (type.equals(Character.class)) return type.cast(getCharFromString(value));\n" +
                "            else if (type.equals(Integer.class)) return type.cast(Integer.valueOf(value));\n" +
                "            else if (type.equals(Float.class)) return type.cast(Float.valueOf(value));\n" +
                "            else if (type.equals(Double.class)) return type.cast(Double.valueOf(value));\n" +
                "            else if (type.equals(Long.class)) return type.cast(Long.valueOf(value));\n" +
                "            else if (type.equals(Boolean.class)) return type.cast(Boolean.valueOf(value));\n" +
                "            else return getDefaultValue(type);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    private static Character getCharFromString(String str) {\n" +
                "        if (str.equals(\"\\\\0\")) {\n" +
                "            return '\\0';\n" +
                "        }\n" +
                "        String[] arr = str.split(\"u\");\n" +
                "        if (arr.length == 2) {\n" +
                "            return (char) Integer.parseInt(arr[1], 16);\n" +
                "        } else {\n" +
                "            return str.charAt(0);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static <T> T getDefaultValue(Class<T> type) {\n" +
                "        if (type.isPrimitive()) return (T)Array.get(Array.newInstance(type, 1), 0);\n" +
                "        else if (type.equals(String.class)) return type.cast(\"\");\n" +
                "        else if (type.equals(Character.class)) return type.cast('\\0');\n" +
                "        else if (type.equals(Integer.class)) return type.cast(0);\n" +
                "        else if (type.equals(Float.class)) return type.cast(0.0f);\n" +
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
        var parser = new JavaParser(parserConfiguration);
        return parser.parse(str).getResult().get();
    }

    private class ConstantVisitor extends ModifierVisitor<Void> {

        private final CompilationUnit decrypterUnit;
        private AtomicInteger constantCount;
        private final ArrayList<Pair<Expression, Expression>> changeList;
        private final StringEncryption stringEncryption;
        private final ClassOrInterfaceDeclaration constantsClass;
        private final MethodDeclaration constantsInitMethod;

        private ConstantVisitor(CompilationUnit decrypterUnit,
                                AtomicInteger constantCount,
                                ArrayList<Pair<Expression, Expression>> changeList,
                                StringEncryption stringEncryption) {
            this.decrypterUnit = decrypterUnit;
            this.constantCount = constantCount;
            this.changeList = changeList;
            this.stringEncryption = stringEncryption;
            this.constantsInitMethod = getConstantsInitMethod();
            this.constantsClass = getConstantsClassDeclaration();
        }

        private MethodDeclaration getConstantsInitMethod() {
            return decrypterUnit.findAll(MethodDeclaration.class)
                    .stream()
                    .filter(methodDeclaration ->
                            methodDeclaration.getNameAsString().equals("addConstants"))
                    .findFirst()
                    .orElseThrow();

        }

        private ClassOrInterfaceDeclaration getConstantsClassDeclaration() {
            return decrypterUnit.findAll(ClassOrInterfaceDeclaration.class)
                    .get(0);
        }

        public CharLiteralExpr visit(CharLiteralExpr cl, Void args) {
            super.visit(cl, args);

            try {
                String value = String.valueOf(cl.getValue());
                if (requiresCompileTimeConstant(cl)) {
                    var varName = stringEncryption.getEncryptedVariableName();
                    replaceWithEncryptedVariable(cl, char.class, varName, value);
                } else {
                    var keyConstantPair = stringEncryption.encrypt(value);
                    replaceWithEncryptedMethod(cl, keyConstantPair, "Character.class");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return cl;
        }

        public StringLiteralExpr visit(StringLiteralExpr sl, Void args) {
            super.visit(sl, args);

            try {
                String value = String.valueOf(sl.getValue());
                if (requiresCompileTimeConstant(sl)) {
                    var varName = stringEncryption.getEncryptedVariableName();
                    replaceWithEncryptedVariable(sl, String.class, varName, value);
                } else {
                    var keyConstantPair = stringEncryption.encrypt(value);
                    replaceWithEncryptedMethod(sl, keyConstantPair, "String.class");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return sl;
        }

        public IntegerLiteralExpr visit(IntegerLiteralExpr il, Void args) {
            super.visit(il, args);

            try {
                if (isIntegerToCharAssignment(il)) return il;
                if (isCharCasted(il)) return il;

                var value = getNumberValue(il);
                if (requiresCompileTimeConstant(il)) {
                    var varName = stringEncryption.getEncryptedVariableName();
                    replaceWithEncryptedVariable(il, int.class, varName, value);
                } else {
                    var keyConstantPair = stringEncryption.encrypt(value);
                    replaceWithEncryptedMethod(il, keyConstantPair, "Integer.class");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return il;
        }

        public DoubleLiteralExpr visit(DoubleLiteralExpr dl, Void args) {
            super.visit(dl, args);

            try {
                var value = getNumberValue(dl);
                var isFloat = (dl.toString().endsWith("f") || dl.toString().endsWith("F"));
                if (requiresCompileTimeConstant(dl)) {
                    var varName = stringEncryption.getEncryptedVariableName();
                    if (isFloat) {
                        replaceWithEncryptedVariable(dl, float.class, varName, value + "f");
                    } else {
                        replaceWithEncryptedVariable(dl, double.class, varName, value);
                    }
                } else {
                    if (isFloat) {
                        var keyConstantPair = stringEncryption.encrypt(value + "f");
                        replaceWithEncryptedMethod(dl, keyConstantPair, "Float.class");
                    } else {
                        var keyConstantPair = stringEncryption.encrypt(value);
                        replaceWithEncryptedMethod(dl, keyConstantPair, "Double.class");
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return dl;
        }

        public LongLiteralExpr visit(LongLiteralExpr ll, Void args) {
            super.visit(ll, args);

            try {
                var value = getNumberValue(ll);
                if (requiresCompileTimeConstant(ll)) {
                    var varName = stringEncryption.getEncryptedVariableName();
                    replaceWithEncryptedVariable(ll, long.class, varName, value);
                } else {
                    var keyConstantPair = stringEncryption.encrypt(value);
                    replaceWithEncryptedMethod(ll, keyConstantPair, "Long.class");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ll;
        }

        public BooleanLiteralExpr visit(BooleanLiteralExpr bl, Void args) {
            super.visit(bl, args);

            try {
                String value = String.valueOf(bl.getValue());
                if (requiresCompileTimeConstant(bl)) {
                    var varName = stringEncryption.getEncryptedVariableName();
                    replaceWithEncryptedVariable(bl, boolean.class, varName, value);
                } else {
                    var keyConstantPair = stringEncryption.encrypt(value);
                    replaceWithEncryptedMethod(bl, keyConstantPair, "Boolean.class");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return bl;
        }

        private String getNumberValue(Expression expr) {
            if (isNegative(expr)) {
                if (expr.getClass().equals(IntegerLiteralExpr.class)) {
                    return String.valueOf(((IntegerLiteralExpr) expr).asNumber().intValue() * -1);
                } else if (expr.getClass().equals(DoubleLiteralExpr.class)) {
                    return String.valueOf(((DoubleLiteralExpr) expr).asDouble() * -1.0);
                } else if (expr.getClass().equals(LongLiteralExpr.class)) {
                    return String.valueOf(((LongLiteralExpr) expr).asNumber().longValue() * -1L);
                }
            }
            if (expr.getClass().equals(IntegerLiteralExpr.class)) {
                return String.valueOf(((IntegerLiteralExpr) expr).asNumber().intValue());
            } else if (expr.getClass().equals(DoubleLiteralExpr.class)) {
                return String.valueOf(((DoubleLiteralExpr) expr).asDouble());
            } else if (expr.getClass().equals(LongLiteralExpr.class)) {
                return String.valueOf(((LongLiteralExpr) expr).asNumber().longValue());
            } else {
                return null;
            }
        }

        private boolean isNegative(Expression expr) {
            if (expr.getParentNode().isPresent()) {
                var parent = expr.getParentNode().get();
                if (parent.getClass().equals(UnaryExpr.class)) {
                    var unary = (UnaryExpr) expr.getParentNode().get();
                    return (unary.getOperator() == UnaryExpr.Operator.MINUS);
                }
            }
            return false;
        }

        private boolean isIntegerToCharAssignment(Expression expr) {
            var parent = expr.getParentNode();
            if (parent.isPresent() && parent.get().getClass().equals(VariableDeclarator.class)) {
                var vd = (VariableDeclarator) parent.get();
                return vd.getTypeAsString().equals("char");
            }
            return false;
        }

        private boolean isCharCasted(Expression expr) {
            var parent = expr.getParentNode();
            if (parent.isPresent() && parent.get().getClass().equals(CastExpr.class)) {
                var cast = (CastExpr) parent.get();
                return cast.getTypeAsString().equals("char");
            }
            return false;
        }

        private String addEscapeChars(String str) {
            final String[] metaCharacters = {"\\"};
            String newStr = str;
            for (String character : metaCharacters) {
                newStr = newStr.replace(character, "\\" + character);
            }
            return newStr;
        }

        private boolean requiresCompileTimeConstant(Expression expr) {
            if (expr.getParentNode().isPresent() &&
                expr.getParentNode().get() instanceof VariableDeclarator &&
                expr.getParentNode().get().getParentNode().isPresent()) {

                if (expr.getParentNode().get().getParentNode().get() instanceof FieldDeclaration) {
                    var fd = (FieldDeclaration)expr.getParentNode().get().getParentNode().get();
                    return fd.isFinal();
                } else if (expr.getParentNode().get().getParentNode().get() instanceof VariableDeclarationExpr) {
                    var vd = (VariableDeclarationExpr)expr.getParentNode().get().getParentNode().get();
                    return vd.isFinal();
                }
            }

            return expr.findAncestor(AnnotationDeclaration.class).isPresent() ||
                   expr.findAncestor(NormalAnnotationExpr.class).isPresent() ||
                   expr.findAncestor(SingleMemberAnnotationExpr.class).isPresent() ||
                   expr.findAncestor(SwitchEntry.class).isPresent();
        }

        private void replaceWithEncryptedVariable(Expression expr, Class type, String varName, String value) {
            Expression initializer = null;
            if (type.equals(char.class)) {
                initializer = new CharLiteralExpr(value);
            } else if (type.equals(String.class)) {
                initializer = new StringLiteralExpr(value);
            } else if (type.equals(int.class)) {
                initializer = new IntegerLiteralExpr(value);
            } else if (type.equals(float.class)) {
                initializer = new DoubleLiteralExpr(value);
            } else if (type.equals(double.class)) {
                initializer = new DoubleLiteralExpr(value);
            } else if (type.equals(long.class)) {
                initializer = new LongLiteralExpr(value);
            } else if (type.equals(boolean.class)) {
                initializer = new BooleanLiteralExpr(Boolean.parseBoolean(value));
            }
            if (initializer != null) {
                constantsClass.addFieldWithInitializer(type, varName, initializer, Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
                var scope = new NameExpr(constantsClass.getNameAsString());
                var fieldAccess = new FieldAccessExpr(scope, varName);
                if (isNegative(expr)) {
                    var parent = (UnaryExpr)expr.getParentNode().get();
                    changeList.add(new Pair<>(parent, fieldAccess));
                } else {
                    changeList.add(new Pair<>(expr, fieldAccess));
                }
            }
        }

        private void replaceWithEncryptedMethod(Expression expr, Pair<String, String> keyConstantPair, String type) {
            var result = addToConstantsMap(keyConstantPair);
            if (result) {
                var scope = new NameExpr(constantsClass.getNameAsString());
                var method = new MethodCallExpr(scope, "getConstant");
                method.addArgument("\"" + keyConstantPair.getKey() + "\"");
                method.addArgument(type);
                if (isNegative(expr)) {
                    var parent = (UnaryExpr)expr.getParentNode().get();
                    changeList.add(new Pair<>(parent, method));
                } else {
                    changeList.add(new Pair<>(expr, method));
                }
                constantCount.incrementAndGet();
            }
        }

        private boolean addToConstantsMap(Pair<String, String> keyConstantPair) {
            var result = new AtomicBoolean(false);

            constantsInitMethod.getBody().ifPresent(blockStmt -> {
                blockStmt.addStatement(
                        "globalConstants.put(" +
                                "\"" + addEscapeChars(keyConstantPair.getKey()) + "\"," +
                                "\"" + addEscapeChars(keyConstantPair.getValue()) + "\"" +
                                ");"
                );
                result.set(true);
            });
            return result.get();
        }
    }
}
