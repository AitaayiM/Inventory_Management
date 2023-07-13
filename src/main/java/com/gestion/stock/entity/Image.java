package com.gestion.stock.entity;

import lombok.Builder;

import java.io.IOException;
import java.util.Base64;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] imageData;

    public void setImageData(MultipartFile file) {
        try {
            this.imageData = file.getBytes();
        } catch (IOException e) {
            // Gérer l'exception selon vos besoins
        }
    }

    public String getImageDataAsBase64() {
        if (imageData != null) {
            return Base64.getEncoder().encodeToString(getImageData());
        }
        return null;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
