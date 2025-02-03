package com.example.demo.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.fileService.FileService;
import com.example.demo.jdkService.JdkService;
import com.example.demo.service.AppService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppServiceimpl implements AppService {

    private final FileService fileService;
    private final JdkService jdkService;

    public String runJavaFile(String code, String className ) {
        try {
            Path tempDir = Files.createTempDirectory("java-file");
            // create src directory
            Path srcDir = Files.createDirectory(tempDir.resolve("src"));
            Path tempFile = Files.createTempFile(srcDir, "Main", ".java");
            tempFile = Files.move(tempFile, srcDir.resolve(className + ".java"));
            Files.write(tempFile, code.getBytes());

            // Compile the Java file
            boolean compileSuccess = jdkService.compileJavaProject(tempDir);
            if (!compileSuccess) {
                return "Compilation failed.";
            }

            // Find the main class
            String mainClassName = jdkService.findMainClass(tempDir);

            System.out.println("Main class: " + mainClassName);

            // Run the Java file
            String output = jdkService.runJavaProject(tempDir, mainClassName);
            System.out.println("Program output: " + output);

            // Clean up
            fileService.deleteDirectory(tempDir.toFile());

            return "Java file executed successfully!\nOutput:\n" + output;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing the code: " + e.getMessage();
        }
    }

    public String runJavaProject(MultipartFile file) {
        try {
            // Save the uploaded ZIP file to a temporary location
            Path tempZipFile = Files.createTempFile("project", ".zip");
            file.transferTo(tempZipFile.toFile());
            System.out.println("ZIP file saved to: " + tempZipFile);
            System.out.println("ZIP file name: " + file.getOriginalFilename());
            // Decompress the ZIP file
            Path projectDir = Files.createTempDirectory("java-project");
            fileService.unzip(tempZipFile, projectDir);
            projectDir = projectDir.toFile().listFiles()[0].toPath();
            System.out.println("Project extracted to: " + projectDir);

            // Find the main class
            String mainClassName = jdkService.findMainClass(projectDir);
            if (mainClassName == null) {
                return "No main class found in the project.";
            }

            System.out.println("Main class: " + mainClassName);
            System.out.println("Main first line: " + jdkService.getMainFirstLine(projectDir, mainClassName));

            // Compile the Java project
            boolean compileSuccess = jdkService.compileJavaProject(projectDir);
            if (!compileSuccess) {
                return "Compilation failed.";
            }

            // Run the Java project
            String output = jdkService.runJavaProject(projectDir, mainClassName);
            System.out.println("Program output: " + output);

            // Clean up
            Files.delete(tempZipFile);
            fileService.deleteDirectory(projectDir.getParent().toFile());

            return "Java project executed successfully!\n" + file.getOriginalFilename() + "\nOutput:\n" + output;

        } catch (IOException e) {
            e.printStackTrace();
            return "Error processing the file: " + e.getMessage();
        }
    }
}
