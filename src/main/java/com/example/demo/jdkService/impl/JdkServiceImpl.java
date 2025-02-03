package com.example.demo.jdkService.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.NoArgsConstructor;
import com.example.demo.jdkService.JdkService;


@NoArgsConstructor
@Component
public class JdkServiceImpl implements JdkService {
    
    public String findMainClass(Path projectDir) throws IOException {
		System.out.println("find main :");
        return Files.walk(projectDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .filter(path -> {
                    try {
                        return Files.readAllLines(path).stream()
                                .anyMatch(line -> line.contains("main(String[] args)"));
                    } catch (IOException e) {
                        System.out.println("error : " + e.getMessage());
                        // e.printStackTrace();/
                        return false;
                    }
                })
                .map(path -> projectDir.relativize(path).toString()
                        .replace(".java", "")
				)
                .findFirst()
                .orElse(null);
    }

    public boolean compileJavaProject(Path projectDir) throws IOException {
        System.out.println("Compiling Java project...");

        // Find all .java files in the project
        List<String> javaFiles = new ArrayList<>();
        Files.walk(projectDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> javaFiles.add(path.toString()));

        // Compile all .java files
        ProcessBuilder processBuilder = new ProcessBuilder("javac", "-cp", projectDir.toString());
        processBuilder.command().addAll(javaFiles);
        processBuilder.directory(projectDir.toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Compilation successful!");
                return true;
            } else {
                System.err.println("Compilation failed!");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String runJavaProject(Path projectDir, String mainClassName) throws IOException {
        System.out.println("Running Java project: " + mainClassName);

        // add project directory name to classpath


        mainClassName = mainClassName.replace(File.separator, ".");
        // extract className after src
        mainClassName = mainClassName.substring(mainClassName.indexOf("src") + 4);

        System.out.println("Main class name: " + mainClassName);
        // Include the project directory and any JAR files in the classpath
        String classpath = projectDir.toString() + "/src" +File.pathSeparator + "bin";

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", classpath, mainClassName);
        processBuilder.directory(projectDir.toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            output.append("Program exited with code: ").append(exitCode);
            return output.toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Error running the project: " + e.getMessage();
        }
    }

    public String getMainFirstLine(Path projectDir, String mainClassName) throws IOException {
        System.out.println("Getting first line of main method...");

        // Find the .java file containing the main class
        Path mainClassPath = Files.walk(projectDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(mainClassName + ".java"))
                .findFirst()
                .orElseThrow(() -> new IOException("Main class file not found"));

        // Read the first line of the main method
        return Files.readAllLines(mainClassPath).stream()
                .findFirst()
                .orElseThrow(() -> new IOException("Main method not found"));
    }
    
}
