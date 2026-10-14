package com.pidecerca.backend.controller;

import com.pidecerca.backend.service.ResumenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ResumenService resumenService;

    public AdminController(ResumenService resumenService) {
        this.resumenService = resumenService;
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return resumenService.obtener();
    }
}
