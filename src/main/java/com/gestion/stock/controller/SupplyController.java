package com.gestion.stock.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.gestion.stock.Repository.ImageRepository;
import com.gestion.stock.Repository.StockRepository;
import com.gestion.stock.Repository.SupplyRepository;
import com.gestion.stock.entity.Image;
import com.gestion.stock.entity.ItemStock;
import com.gestion.stock.entity.SupplyItem;
import com.gestion.stock.util.DataPoint;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@CrossOrigin(origins = "https://localhost:8081")
@RestController
@RequestMapping("/supply")
public class SupplyController {
    
    @Autowired
    private final SupplyRepository repository;

    @Autowired
    private final StockRepository stockRepository;

    @Autowired 
    private final ImageRepository imageRepository;

    @GetMapping("/upload")
    public ModelAndView showUploadForm(Model model) {
        List<ItemStock> products = stockRepository.findAll();
        model.addAttribute("products", products);
        return new ModelAndView("add-supply");
    }

    @PostMapping("/upload")
    public ModelAndView addSupply(@RequestParam("productId") int productId,
                                @RequestParam("quantityOrdered") int quantityOrdered,
                                @RequestParam("dateExpectedDelivery") @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate dateExpectedDelivery,
                                Model model) {
        // Vérifier si un approvisionnement existe déjà pour le produit et la date donnés
        Optional<SupplyItem> existingSupplyItem = repository.findByItemCodeAndDateExpectedDelivery(productId, dateExpectedDelivery);
        // Récupérer l'ItemStock à partir de la base de données
        Optional<ItemStock> itemStockData = stockRepository.findById(productId);
        if (itemStockData.isPresent()) {
            ItemStock itemStock = itemStockData.get();
            if (existingSupplyItem.isPresent()) {
                // Mettre à jour la quantité commandée de l'approvisionnement existant
                SupplyItem supplyItem = existingSupplyItem.get();
                supplyItem.setQuantityOrdered(quantityOrdered+supplyItem.getQuantityOrdered());
                itemStock.setItemQuantity(itemStock.getItemQuantity()-quantityOrdered);
                repository.save(supplyItem);
                stockRepository.save(itemStock);
                return showUploadResult("Approvisionnement mis à jour avec succès", model);
            } else {
                    // Créer un nouvel objet SupplyItem
                    SupplyItem supplyItem = new SupplyItem();
                    supplyItem.setQuantityOrdered(quantityOrdered);
                    supplyItem.setDateExpectedDelivery(dateExpectedDelivery);
                    supplyItem.setItemImage(itemStock.getItemImage());
                    supplyItem.setImageUrl(itemStock.getImageUrl()); // Définir l'URL de l'image
                    itemStock.setItemQuantity(itemStock.getItemQuantity()-quantityOrdered);
                    // Enregistrer l'objet SupplyItem dans la base de données
                    repository.save(supplyItem);
                    stockRepository.save(itemStock);
                    return showUploadResult("Approvisionnement ajouté avec succès", model);
            }
        }else {
            return showUploadResult("Produit introuvable", model);
        }
    }


    @GetMapping("/uploadResult")
    public ModelAndView showUploadResult(@RequestParam("message") String message, Model model) {
        List<ItemStock> products = stockRepository.findAll();
        model.addAttribute("products",products);
        model.addAttribute("message", message);
        return new ModelAndView("add-supply");
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

    @GetMapping("/supply-list")
    public ModelAndView getSupplyList(Model model) {
        List<SupplyItem> supplyItems = repository.findAll();
        for (SupplyItem item : supplyItems) {
            Image image = item.getItemImage();
            if (image != null) {
                String imageUrl = "/supply/image/" + image.getId();
                item.setImageUrl(imageUrl);
            }
        }
        model.addAttribute("supplyItems", supplyItems);
        return new ModelAndView("supply-list");
    }

    @GetMapping("/supply-list/{id}/toggleDelete")
    public ModelAndView toggleProductDelete(@PathVariable("id") int id, Model model) {
        SupplyItem supply = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
        supply.setItemImage(null); // Dissocier l'image de l'élément de fourniture
        repository.delete(supply);
        return getSupplyList(model);
    }

    @GetMapping("/analyse")
    public ModelAndView showAnalyse() {
        ModelAndView modelAndView = new ModelAndView("Analyses");

        // Exemple de code pour initialiser les variables
        List<DataPoint> dataPoints = getSupplyPercentage(); // Appel à la méthode getSupplyPercentage pour obtenir les données
        String chartTitle = "Supply Percentage";
        String chartSubtitle = "all supplies";

        // Ajout des variables au modèle
        modelAndView.addObject("dataPoints", dataPoints);
        modelAndView.addObject("chartTitle", chartTitle);
        modelAndView.addObject("chartSubtitle", chartSubtitle);

        return modelAndView;
    }

    @GetMapping("/percentage")
    public List<DataPoint> getSupplyPercentage() {
        List<DataPoint> dataPoints = new ArrayList<>();

        // Logique pour calculer les pourcentages d'approvisionnement
        // Utilisez les données appropriées de la base de données
        List<SupplyItem> supplyItems = repository.findAll();
        int totalQuantity = 0;
        for (SupplyItem item : supplyItems) {
            totalQuantity += item.getQuantityOrdered();
        }
        for (SupplyItem item : supplyItems) {
            double percentage = (item.getQuantityOrdered() / (double) totalQuantity) * 100;
            dataPoints.add(new DataPoint(item.getItemCode(), percentage));
        }

        return dataPoints;
    }

}