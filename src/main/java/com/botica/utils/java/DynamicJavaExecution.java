package com.botica.utils.java;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;

public class DynamicJavaExecution {

    private DynamicJavaExecution() {
    }

    public static String generateJavaCode(String botId, String botCode) {
        
        return "package com.botica.temp;\n\n"
            + "import org.json.JSONObject;\n"
            + "import org.json.JSONArray;\n\n"
            + "import com.botica.runners.RunnerBase;\n\n"
            + "public class BotLauncher_" + botId + " {\n"
            + "\tpublic static void main(String[] args) {\n"
            + botCode
            + "\t}\n"
            +"}";
    }

    public static void writeToFile(String fileName, String code) throws IOException {
        Path path = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(code);
        }
    }

    public static void compileJavaFile(String fileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(fileName);

        CompilationTask task = compiler.getTask(null, fileManager, null, null, null, compilationUnits);
        task.call();

        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

