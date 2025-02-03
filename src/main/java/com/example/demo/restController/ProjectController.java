package com.example.demo.restController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.example.demo.fileService.FileService;
import com.example.demo.jdkService.JdkService;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final FileService fileService;
    private final JdkService jdkService;

    @GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No main class found in the project.");
            }

            System.out.println("Main class: " + mainClassName);
            System.out.println("Main first line: " + jdkService.getMainFirstLine(projectDir, mainClassName));


            // Compile the Java project
            boolean compileSuccess = jdkService.compileJavaProject(projectDir);
            if (!compileSuccess) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Compilation failed.");
            }


            // Run the Java project
            String output = jdkService.runJavaProject(projectDir, mainClassName);
            System.out.println("Program output: " + output);

            // Clean up
            Files.delete(tempZipFile);
            fileService.deleteDirectory(projectDir.getParent().toFile());

            return ResponseEntity.ok("Java project executed successfully!\n"+file.getOriginalFilename()+"\nOutput:\n" + output);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the file: " + e.getMessage());
        }
    }


}
