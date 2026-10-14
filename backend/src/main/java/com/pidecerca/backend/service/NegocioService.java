package com.pidecerca.backend.service;

import com.pidecerca.backend.entity.Negocio;
import com.pidecerca.backend.repository.NegocioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NegocioService {

    private final NegocioRepository negocioRepository;

    public NegocioService(NegocioRepository negocioRepository) {
        this.negocioRepository = negocioRepository;
    }

    public List<Negocio> listar() {
        return negocioRepository.findAll();
    }

    public Negocio obtenerPorId(Long id) {
        return negocioRepository.findById(id).orElse(null);
    }

    public Negocio guardar(Negocio negocio) {
        return negocioRepository.save(negocio);
    }

    public void eliminar(Long id) {
        negocioRepository.deleteById(id);
    }
}