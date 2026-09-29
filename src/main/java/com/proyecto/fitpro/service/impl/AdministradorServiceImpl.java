package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.dto.AdministradorDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Administrador;
import com.proyecto.fitpro.repository.AdministradorRepository;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.EntrenadorRepository;
import com.proyecto.fitpro.service.AdministradorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final ClienteRepository clienteRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorServiceImpl(AdministradorRepository administradorRepository,
            ClienteRepository clienteRepository, EntrenadorRepository entrenadorRepository,
            PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.clienteRepository = clienteRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Administrador registrar(AdministradorDTO datos) {
        // El login busca primero clientes por email: un email repetido dejaría al admin sin acceso
        if (administradorRepository.findByEmail(datos.getEmail()).isPresent()
                || clienteRepository.findByEmail(datos.getEmail()).isPresent()
                || entrenadorRepository.findFirstByEmailAndPasswordIsNotNull(datos.getEmail()).isPresent()) {
            throw new NegocioException("Ya existe una cuenta con ese email");
        }
        Administrador admin = new Administrador();
        admin.setNombre(datos.getNombre());
        admin.setApellido(datos.getApellido());
        admin.setEmail(datos.getEmail());
        admin.setTelefono(datos.getTelefono());
        admin.setPassword(passwordEncoder.encode(datos.getPassword()));
        return administradorRepository.save(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Administrador> obtenerTodos() {
        return administradorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Administrador> obtenerPorId(Integer id) {
        return administradorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Administrador> obtenerPorEmail(String email) {
        return administradorRepository.findByEmail(email);
    }

    @Override
    public void eliminar(Integer id) {
        administradorRepository.deleteById(id);
    }
}
