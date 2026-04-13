package com.financetracker.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class CategoryConfig {

    public static final List<String> CATEGORIES = List.of(
        "Food", "Milk", "Games", "Movies", "Groceries", "WiFi", "Rent", "Miscellaneous"
    );

    @ModelAttribute("categories")
    public List<String> categories() {
        return CATEGORIES;
    }
}
