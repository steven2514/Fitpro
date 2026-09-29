package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.model.RegistroPeso;
import com.proyecto.fitpro.repository.ClienteRepository;
import com.proyecto.fitpro.repository.RegistroPesoRepository;
import com.proyecto.fitpro.service.ProgresoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProgresoServiceImpl implements ProgresoService {

    private final RegistroPesoRepository registroPesoRepository;
    private final ClienteRepository clienteRepository;

    public ProgresoServiceImpl(RegistroPesoRepository registroPesoRepository, ClienteRepository clienteRepository) {
        this.registroPesoRepository = registroPesoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void registrar(Integer idCliente, Double peso, Double altura) {
        if (peso == null || altura == null || altura <= 0) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        // Una medición por día: si el cliente corrige su peso el mismo día, se actualiza la del día
        RegistroPeso registro = registroPesoRepository.findByCliente_IdClienteAndFecha(idCliente, hoy)
            .orElseGet(() -> {
                RegistroPeso nuevo = new RegistroPeso();
                nuevo.setCliente(clienteRepository.getReferenceById(idCliente));
                nuevo.setFecha(hoy);
                return nuevo;
            });
        registro.setPeso(peso);
        registro.setAltura(altura);
        registroPesoRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroPeso> obtenerHistorial(Integer idCliente) {
        return registroPesoRepository.findByCliente_IdClienteOrderByFechaAsc(idCliente);
    }
}
