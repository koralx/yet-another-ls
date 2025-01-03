package com.koral;

import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

@Getter
public class OutputResult {

    private String name;
    private Path path;

    private Long size;

    private int permissions;

    private String owner;

    private FileTime lastModified;

    public OutputResult(String name,
                        Path path,
                        Long size,
                        int permissions,
                        String owner,
                        FileTime lastModified) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.permissions = permissions;
        this.owner = owner;
        this.lastModified = lastModified;
    }
}
