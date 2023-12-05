package com.odinbook.accountservice.DTO;

import org.springframework.web.multipart.MultipartFile;

public class ImageDTO {

    private String id;
    private MultipartFile file;

    public ImageDTO(String id, MultipartFile file) {
        this.id = id;
        this.file = file;
    }

    public ImageDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

}
