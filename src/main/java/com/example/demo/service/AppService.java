package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface AppService {
    
    public String runJavaFile(String code);

    public String runJavaProject(MultipartFile file);
}
