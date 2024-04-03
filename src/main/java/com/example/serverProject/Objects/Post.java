package com.example.serverProject.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@AllArgsConstructor
@Setter
@Getter
public class Post {
    private String text;
    private Timestamp publishDate;
    private String name;
}
