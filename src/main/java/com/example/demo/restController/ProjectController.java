package com.example.demo.restController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.example.demo.service.AppService;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final AppService appService;

    @GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(appService.runJavaProject(file));
    }

    @PostMapping("/run")
    public ResponseEntity<String> runJavaCode(@RequestParam("code") String code) {
        return ResponseEntity.ok(appService.runJavaFile(code));
    }


}
