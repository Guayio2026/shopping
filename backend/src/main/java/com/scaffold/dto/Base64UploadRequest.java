package com.scaffold.dto;

import lombok.Data;

@Data
public class Base64UploadRequest {

    private String data;
    private String extension = "jpg";
}
