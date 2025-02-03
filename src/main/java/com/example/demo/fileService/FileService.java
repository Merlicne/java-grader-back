package com.example.demo.fileService;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface FileService {

    public void unzip(Path zipFile, Path destDir) throws IOException;

    public void deleteDirectory(File directory);
} 
