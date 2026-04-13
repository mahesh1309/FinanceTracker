package com.financetracker.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class SourceOfPurchaseConfig {

    public static final List<String> SOURCES_OF_PURCHASE = List.of(
        "Dmart", "Zepto", "Blinkit", "BigBasket", "Store",
        "Flipkart", "Amazon", "Instamart", "Minutes", "Others"
    );

    @ModelAttribute("sourcesOfPurchase")
    public List<String> sourcesOfPurchase() {
        return SOURCES_OF_PURCHASE;
    }
}
