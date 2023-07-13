package com.gestion.stock.controller;


import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.MediaType;

import com.gestion.stock.Repository.ImageRepository;
import com.gestion.stock.Repository.StockRepository;
import com.gestion.stock.entity.Image;
import com.gestion.stock.entity.ItemStock;
import com.gestion.stock.util.ImageUtils;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@CrossOrigin(origins = "https://localhost:8081")
@RestController
@RequestMapping("/product")
public class StockController {

    @Autowired
    private final StockRepository repository;

    @Autowired 
    private final ImageRepository imageRepository;

    @Autowired
    private ImageUtils converter;


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Image.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                MultipartFile file = ((MultipartFile) getValue());
                if (file != null && !file.isEmpty()) {
                    setValue(converter.convert(file));
                }
            }
        });
    }


    @PostMapping("/upload")
    public ModelAndView uploadImage(@ModelAttribute ItemStock itemStock,
                                    @RequestParam("itemName") String itemName,
                                    @RequestParam("itemQuantity") int itemQuantity,
                                    @RequestParam("itemDescription") String itemDescription,
                                    @RequestParam("itemPrice") int itemPrice,
                                    @RequestParam("itemImage") MultipartFile itemImage,
                                    Model model) {

        String fileName = StringUtils.cleanPath(itemImage.getOriginalFilename());

        try {
            // Obtient le chemin absolu du répertoire "static/images"
            String uploadDir = ResourceUtils.getFile("classpath:static/images/").getAbsolutePath();
            String filePath = uploadDir + "/" + fileName;

            // Enregistre le fichier sur le système de fichiers
            Files.write(Paths.get(filePath), itemImage.getBytes());

            // Enregistre les autres détails du produit dans la base de données
            ItemStock newItem = new ItemStock();
            newItem.setItemName(itemName);
            newItem.setItemQuantity(itemQuantity);
            newItem.setItemDescription(itemDescription);
            newItem.setItemPrice(itemPrice);

            // Conversion du MultipartFile en Image
            Image image = converter.convert(itemImage);
            newItem.setItemImage(image);

            // Sauvegarde l'objet ItemStock dans la base de données
            repository.save(newItem);

            model.addAttribute("message", "Le produit a été ajouté avec succès.");
            return getItems(model);
        } catch (IOException e) {
            e.printStackTrace();
            ModelAndView modelAndView = new ModelAndView("add-product");
            modelAndView.addObject("message", "Une erreur s'est produite lors du téléchargement de l'image.");
            return modelAndView;
        }
    }


    @GetMapping("/add-product")
    public ModelAndView getAddProductPage() {
        return new ModelAndView("add-product");
    }


    @GetMapping("/items/{id}/toggleDelete")
    public ModelAndView toggleProductDelete(@PathVariable("id") int id, Model model) {
        ItemStock product = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
        repository.delete(product);
        return getItems(model);
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        Optional<Image> imageOptional = imageRepository.findById(id);

        if (imageOptional.isPresent()) {
            Image image = imageOptional.get();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(image.getImageData());
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/items")
    public ModelAndView getItems(Model model) {
        List<ItemStock> items = repository.findAll();

        for (ItemStock item : items) {
            Image image = item.getItemImage();
            if (image != null) {
                String imageUrl = "/product/image/" + image.getId();
                item.setImageUrl(imageUrl);
            }
        }

        model.addAttribute("items", items);

        return new ModelAndView("items-list");
    }

    @GetMapping("/download/{imageId}")
    public ResponseEntity<ByteArrayResource> downloadImage(@PathVariable int imageId) {
        // Recherchez l'image dans la base de données en utilisant l'ID
        try {
            ItemStock image = repository.findById(imageId)
                    .orElseThrow(() -> new NotFoundException());
    
            // Créez un ByteArrayResource à partir des données de l'image
            ByteArrayResource resource = new ByteArrayResource(image.getItemImage().getImageData());
    
            // Renvoie une réponse avec le corps de la ressource et les en-têtes appropriés
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getItemImage().getId() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (Exception e) {
            return null;
        }
    }
}
