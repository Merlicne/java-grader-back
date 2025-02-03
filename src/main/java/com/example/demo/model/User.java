package com.example.demo.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class User {
    
    private Integer id;
    private String username;
    private String password;

    public boolean IsValid() {
        return this.username != null && this.password != null;
    }

}
