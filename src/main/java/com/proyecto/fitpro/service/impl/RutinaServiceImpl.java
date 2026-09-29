package com.proyecto.fitpro.service.impl;

import com.proyecto.fitpro.dto.EjercicioDTO;
import com.proyecto.fitpro.exception.NegocioException;
import com.proyecto.fitpro.model.Ejercicio;
import com.proyecto.fitpro.model.Rutina;
import com.proyecto.fitpro.repository.EjercicioRepository;
import com.proyecto.fitpro.repository.RutinaRepository;
import com.proyecto.fitpro.service.RutinaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RutinaServiceImpl implements RutinaService {

    private final RutinaRepository rutinaRepository;
    private final EjercicioRepository ejercicioRepository;

    public RutinaServiceImpl(RutinaRepository rutinaRepository, EjercicioRepository ejercicioRepository) {
        this.rutinaRepository = rutinaRepository;
        this.ejercicioRepository = ejercicioRepository;
    }

    @Override
    public Rutina crear(Rutina rutina) {
        return rutinaRepository.save(rutina);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rutina> obtenerTodas() {
        return rutinaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rutina> obtenerPorCliente(Integer idCliente) {
        return rutinaRepository.findByCliente_IdCliente(idCliente);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rutina> obtenerPorId(Integer id) {
        return rutinaRepository.findById(id);
    }

    @Override
    public Rutina actualizar(Rutina rutina) {
        return rutinaRepository.save(rutina);
    }

    @Override
    public void eliminar(Integer id) {
        // Carga la entidad para que la cascada borre también sus ejercicios
        rutinaRepository.findById(id).ifPresent(rutinaRepository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ejercicio> obtenerEjercicios(Integer idRutina) {
        return ejercicioRepository.findByRutina_IdRutinaOrderByIdEjercicioAsc(idRutina);
    }

    @Override
    public Ejercicio agregarEjercicio(Integer idRutina, EjercicioDTO datos) {
        Rutina rutina = rutinaRepository.findById(idRutina)
            .orElseThrow(() -> new NegocioException("La rutina no existe"));
        Ejercicio ejercicio = new Ejercicio();
        ejercicio.setRutina(rutina);
        ejercicio.setNombre(datos.getNombre());
        ejercicio.setSeries(datos.getSeries());
        ejercicio.setRepeticiones(datos.getRepeticiones());
        ejercicio.setDescansoSegundos(datos.getDescansoSegundos());
        ejercicio.setNotas(datos.getNotas());
        return ejercicioRepository.save(ejercicio);
    }

    @Override
    public Integer eliminarEjercicio(Integer idEjercicio) {
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
            .orElseThrow(() -> new NegocioException("El ejercicio no existe"));
        Integer idRutina = ejercicio.getRutina().getIdRutina();
        // Se quita de la colección de la rutina: con orphanRemoval, así se borra sin conflicto con la cascada
        ejercicio.getRutina().getEjercicios().remove(ejercicio);
        ejercicioRepository.delete(ejercicio);
        return idRutina;
    }
}
