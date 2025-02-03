package com.example.demo.jdkService;

import java.io.IOException;
import java.nio.file.Path;

public interface JdkService {
    
    
    public String findMainClass(Path projectDir) throws IOException;

    public boolean compileJavaProject(Path projectDir) throws IOException;

    public String runJavaProject(Path projectDir, String mainClassName) throws IOException;

    // get main first line
    public String getMainFirstLine(Path projectDir, String mainClassName) throws IOException;

}
