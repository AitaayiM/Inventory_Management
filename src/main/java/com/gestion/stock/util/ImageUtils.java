package com.gestion.stock.util;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.convert.converter.Converter;


import com.gestion.stock.entity.Image;

@Component
public class ImageUtils implements Converter<MultipartFile, Image>{

    public static final int BITE_SIZE = 4 * 1024;

    @Override
    public Image convert(MultipartFile file) {
        Image image = new Image();
        try {
            image.setImageData(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

}
