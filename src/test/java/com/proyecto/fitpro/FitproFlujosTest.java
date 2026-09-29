package com.proyecto.fitpro;

import com.proyecto.fitpro.model.*;
import com.proyecto.fitpro.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FitproFlujosTest {

    @Autowired private MockMvc mvc;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private RutinaRepository rutinaRepository;
    @Autowired private AlimentacionRepository alimentacionRepository;
    @Autowired private ClaseRepository claseRepository;
    @Autowired private EntrenadorRepository entrenadorRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private SuscripcionRepository suscripcionRepository;
    @Autowired private RegistroPesoRepository registroPesoRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private EjercicioRepository ejercicioRepository;
    @Autowired private TokenRecuperacionRepository tokenRepository;
    @Autowired private javax.sql.DataSource dataSource;
    @Autowired private com.proyecto.fitpro.service.SuscripcionService suscripcionService;

    /** Sustituye el envío real de correos para poder leer el enlace de recuperación. */
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.proyecto.fitpro.service.NotificacionService notificacionService;

    @Autowired @Qualifier("crearPlanesIniciales") private ApplicationRunner crearPlanesIniciales;

    @BeforeEach
    void limpiar() throws Exception {
        suscripcionRepository.deleteAll();
        registroPesoRepository.deleteAll();
        // Cada prueba empieza con los 3 planes por defecto, sin cambios de pruebas anteriores
        planRepository.deleteAll();
        crearPlanesIniciales.run(null);
        rutinaRepository.deleteAll();
        alimentacionRepository.deleteAll();
        clienteRepository.findAll().forEach(c -> {
            c.setClases(new ArrayList<>());
            clienteRepository.save(c);
        });
        clienteRepository.deleteAll();
        claseRepository.deleteAll();
        entrenadorRepository.deleteAll();
        administradorRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    // ---------- Seguridad ----------

    @Test
    void registroDeAdministradorNoEsPublico() throws Exception {
        mvc.perform(get("/registroAdmin"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));

        mvc.perform(post("/registroAdmin").with(csrf())
                .param("nombre", "Intruso").param("apellido", "X")
                .param("email", "intruso@test.com").param("password", "12345678"))
            .andExpect(status().is3xxRedirection());
        assertTrue(administradorRepository.findByEmail("intruso@test.com").isEmpty());
    }

    @Test
    void unClienteNoPuedeCrearAdministradores() throws Exception {
        mvc.perform(get("/registroAdmin").with(user("1").roles("CLIENTE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void elRegistroNoPuedeSobrescribirOtraCuenta() throws Exception {
        Cliente victima = crearCliente("111", "clave-original");

        mvc.perform(post("/registro").with(csrf())
                .param("idCliente", String.valueOf(victima.getIdCliente()))
                .param("nombre", "Atacante").param("apellido", "Malo")
                .param("documento", "222").param("telefono", "3000000000")
                .param("password", "clave-atacante"))
            .andExpect(redirectedUrl("/login"));

        Cliente despues = clienteRepository.findById(victima.getIdCliente()).orElseThrow();
        assertEquals("Juan", despues.getNombre());
        assertTrue(passwordEncoder.matches("clave-original", despues.getPassword()));
        assertEquals(2, clienteRepository.count());
    }

    @Test
    void registroRechazaContrasenaCortaYDocumentoRepetido() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                .param("nombre", "Ana").param("apellido", "Gomez")
                .param("documento", "333").param("password", "corta"))
            .andExpect(status().isOk())
            .andExpect(view().name("registro"));
        assertEquals(0, clienteRepository.count());

        crearCliente("444", "clave-segura");
        mvc.perform(post("/registro").with(csrf())
                .param("nombre", "Ana").param("apellido", "Gomez")
                .param("documento", "444").param("password", "otra-clave-segura"))
            .andExpect(view().name("registro"))
            .andExpect(model().attribute("error", "Ya existe un cliente con ese documento"));
        assertEquals(1, clienteRepository.count());
    }

    @Test
    void loginFuncionaConTokenCsrfYFallaSinEl() throws Exception {
        crearCliente("555", "clave-segura");

        mvc.perform(post("/login").with(csrf()).param("username", "555").param("password", "clave-segura"))
            .andExpect(redirectedUrl("/cliente/panel"));

        mvc.perform(post("/login").param("username", "555").param("password", "clave-segura"))
            .andExpect(status().isForbidden());
    }

    @Test
    void lasPaginasCarganConSusFormulariosProtegidos() throws Exception {
        Cliente cliente = crearCliente("666", "clave-segura");
        Administrador admin = crearAdmin();

        mvc.perform(get("/login")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"_csrf\"")));
        mvc.perform(get("/registro")).andExpect(status().isOk());
        mvc.perform(get("/cliente/panel").with(user(String.valueOf(cliente.getIdCliente())).roles("CLIENTE")))
            .andExpect(status().isOk());
        mvc.perform(get("/admin/panel").with(user(String.valueOf(admin.getIdAdministrador())).roles("ADMIN")))
            .andExpect(status().isOk());
        mvc.perform(get("/admin/cliente/editar/" + cliente.getIdCliente()).with(user("1").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    // ---------- Panel de administración ----------

    @Test
    void editarClienteConservaContrasenaEInscripciones() throws Exception {
        Cliente cliente = crearCliente("777", "clave-segura");
        Clase clase = crearClase(crearEntrenador(), 10);
        inscribir(cliente, clase);

        mvc.perform(post("/admin/cliente/editar/" + cliente.getIdCliente()).with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Juanito").param("apellido", "Perez")
                .param("documento", "777").param("telefono", "3001112233").param("password", ""))
            .andExpect(flash().attribute("success", "Cliente actualizado exitosamente"));

        Cliente despues = clienteRepository.findByIdWithClases(cliente.getIdCliente()).orElseThrow();
        assertEquals("Juanito", despues.getNombre());
        assertTrue(passwordEncoder.matches("clave-segura", despues.getPassword()));
        assertEquals(1, despues.getClases().size());
        assertNotNull(despues.getFechaRegistro());
    }

    @Test
    void eliminarClienteConRutinasYClases() throws Exception {
        Cliente cliente = crearCliente("888", "clave-segura");
        inscribir(cliente, crearClase(crearEntrenador(), 10));
        Rutina rutina = new Rutina();
        rutina.setCliente(cliente);
        rutina.setNombre("Fuerza");
        rutinaRepository.save(rutina);

        mvc.perform(post("/admin/cliente/eliminar/" + cliente.getIdCliente()).with(csrf()).with(user("1").roles("ADMIN")))
            .andExpect(flash().attribute("success", "Cliente eliminado exitosamente"));

        assertFalse(clienteRepository.existsById(cliente.getIdCliente()));
        assertEquals(0, rutinaRepository.count());
    }

    @Test
    void noSeEliminaEntrenadorConClases() throws Exception {
        Entrenador entrenador = crearEntrenador();
        crearClase(entrenador, 10);

        mvc.perform(post("/admin/entrenador/eliminar/" + entrenador.getIdEntrenador()).with(csrf()).with(user("1").roles("ADMIN")))
            .andExpect(flash().attribute("error", org.hamcrest.Matchers.startsWith("No se puede eliminar")));
        assertTrue(entrenadorRepository.existsById(entrenador.getIdEntrenador()));
    }

    @Test
    void noSeInscribeEnUnaClaseLlena() throws Exception {
        Clase clase = crearClase(crearEntrenador(), 1);
        inscribir(crearCliente("901", "clave-segura"), clase);
        Cliente segundo = crearCliente("902", "clave-segura");

        mvc.perform(post("/admin/clase/inscribir").with(csrf()).with(user("1").roles("ADMIN"))
                .param("idCliente", String.valueOf(segundo.getIdCliente()))
                .param("idClase", String.valueOf(clase.getIdClase())))
            .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("no tiene cupos")));
    }

    @Test
    void adminCreaOtroAdministrador() throws Exception {
        mvc.perform(post("/registroAdmin").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Laura").param("apellido", "Diaz")
                .param("email", "laura@fitpro.com").param("password", "clave-segura"))
            .andExpect(redirectedUrl("/admin/panel"));

        Administrador nuevo = administradorRepository.findByEmail("laura@fitpro.com").orElseThrow();
        assertEquals("Laura", nuevo.getNombre());
        assertTrue(passwordEncoder.matches("clave-segura", nuevo.getPassword()));
    }

    // ---------- Suscripciones ----------

    @Test
    void existenLosPlanesInicialesYSeMuestranEnLaPortada() throws Exception {
        assertEquals(3, planRepository.findByActivoTrueOrderByPrecioMensualAsc().size());
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Elige cómo quieres entrenar")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("$109.900")));
    }

    @Test
    void clienteSeSuscribeCambiaDePlanYCancela() throws Exception {
        Cliente cliente = crearCliente("1100", "clave-segura");
        Plan basico = plan("Básico");
        Plan pro = plan("Pro");
        String id = String.valueOf(cliente.getIdCliente());

        mvc.perform(post("/cliente/suscripcion").with(csrf()).with(user(id).roles("CLIENTE"))
                .param("idPlan", String.valueOf(basico.getIdPlan())))
            .andExpect(flash().attribute("success", org.hamcrest.Matchers.startsWith("¡Listo! Tu plan Básico")));

        // Cambiar de plan cierra la suscripción anterior: sólo queda una activa
        mvc.perform(post("/cliente/suscripcion").with(csrf()).with(user(id).roles("CLIENTE"))
                .param("idPlan", String.valueOf(pro.getIdPlan())));
        List<Suscripcion> activas = suscripcionRepository
            .findByCliente_IdClienteAndEstado(cliente.getIdCliente(), Suscripcion.Estado.ACTIVA);
        assertEquals(1, activas.size());
        assertEquals(pro.getPrecioMensual(), activas.get(0).getPrecioPagado());

        mvc.perform(post("/cliente/suscripcion/cancelar").with(csrf()).with(user(id).roles("CLIENTE")))
            .andExpect(flash().attribute("success", "Tu suscripción fue cancelada"));
        assertTrue(suscripcionRepository
            .findByCliente_IdClienteAndEstado(cliente.getIdCliente(), Suscripcion.Estado.ACTIVA).isEmpty());
    }

    @Test
    void noSePuedeSuscribirAUnPlanInactivo() throws Exception {
        Cliente cliente = crearCliente("1101", "clave-segura");
        Plan elite = plan("Élite");
        elite.setActivo(false);
        planRepository.save(elite);

        mvc.perform(post("/cliente/suscripcion").with(csrf()).with(user(String.valueOf(cliente.getIdCliente())).roles("CLIENTE"))
                .param("idPlan", String.valueOf(elite.getIdPlan())))
            .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("ya no está disponible")));
    }

    @Test
    void autoinscripcionAClasesExigePlanConClases() throws Exception {
        Cliente cliente = crearCliente("1102", "clave-segura");
        Clase clase = crearClase(crearEntrenador(), 10);
        String id = String.valueOf(cliente.getIdCliente());

        // Sin plan: rechazado
        mvc.perform(post("/cliente/clase/" + clase.getIdClase() + "/inscribir").with(csrf()).with(user(id).roles("CLIENTE")))
            .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("necesitas un plan")));

        // Con plan Básico (no incluye clases): rechazado
        suscribir(cliente, plan("Básico"));
        mvc.perform(post("/cliente/clase/" + clase.getIdClase() + "/inscribir").with(csrf()).with(user(id).roles("CLIENTE")))
            .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("necesitas un plan")));

        // Con plan Pro: aceptado, y luego puede cancelar
        suscribir(cliente, plan("Pro"));
        mvc.perform(post("/cliente/clase/" + clase.getIdClase() + "/inscribir").with(csrf()).with(user(id).roles("CLIENTE")))
            .andExpect(flash().attribute("success", "Te inscribiste en la clase"));
        assertEquals(1, clienteRepository.findByIdWithClases(cliente.getIdCliente()).orElseThrow().getClases().size());

        mvc.perform(post("/cliente/clase/" + clase.getIdClase() + "/cancelar").with(csrf()).with(user(id).roles("CLIENTE")))
            .andExpect(flash().attribute("success", "Cancelaste tu inscripción"));
        assertTrue(clienteRepository.findByIdWithClases(cliente.getIdCliente()).orElseThrow().getClases().isEmpty());
    }

    @Test
    void adminGestionaPlanesYNoBorraUnoConSuscripciones() throws Exception {
        mvc.perform(post("/admin/plan/crear").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Estudiante").param("precioMensual", "49900").param("duracionDias", "30")
                .param("beneficios", "Horario valle\nPesas y cardio"))
            .andExpect(flash().attribute("success", "Plan creado exitosamente"));
        Plan estudiante = planRepository.findAll().stream()
            .filter(p -> p.getNombre().equals("Estudiante")).findFirst().orElseThrow();
        assertEquals(List.of("Horario valle", "Pesas y cardio"), estudiante.getListaBeneficios());

        mvc.perform(post("/admin/plan/crear").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Gratis").param("precioMensual", "-5").param("duracionDias", "30"))
            .andExpect(flash().attribute("error", "El precio no puede ser negativo"));

        suscribir(crearCliente("1103", "clave-segura"), estudiante);
        mvc.perform(post("/admin/plan/eliminar/" + estudiante.getIdPlan()).with(csrf()).with(user("1").roles("ADMIN")))
            .andExpect(flash().attribute("error", org.hamcrest.Matchers.containsString("desactívalo")));

        mvc.perform(post("/admin/plan/estado/" + estudiante.getIdPlan()).with(csrf()).with(user("1").roles("ADMIN")));
        assertFalse(planRepository.findById(estudiante.getIdPlan()).orElseThrow().isActivo());
    }

    @Test
    void adminAsignaYCancelaSuscripcion() throws Exception {
        Cliente cliente = crearCliente("1104", "clave-segura");
        Plan pro = plan("Pro");
        mvc.perform(post("/admin/suscripcion/asignar").with(csrf()).with(user("1").roles("ADMIN"))
                .param("idCliente", String.valueOf(cliente.getIdCliente()))
                .param("idPlan", String.valueOf(pro.getIdPlan())))
            .andExpect(flash().attribute("success", "Plan asignado al cliente"));
        Suscripcion s = suscripcionRepository
            .findByCliente_IdClienteAndEstado(cliente.getIdCliente(), Suscripcion.Estado.ACTIVA).get(0);
        assertEquals(java.time.LocalDate.now().plusDays(30), s.getFechaFin());

        mvc.perform(post("/admin/suscripcion/cancelar/" + s.getIdSuscripcion()).with(csrf()).with(user("1").roles("ADMIN")))
            .andExpect(flash().attribute("success", "Suscripción cancelada"));
        assertEquals(Suscripcion.Estado.CANCELADA, suscripcionRepository.findById(s.getIdSuscripcion()).orElseThrow().getEstado());
    }

    // ---------- Ejercicios y progreso ----------

    @Test
    void adminAgregaYEliminaEjerciciosDeUnaRutina() throws Exception {
        Cliente cliente = crearCliente("1200", "clave-segura");
        Rutina rutina = new Rutina();
        rutina.setCliente(cliente);
        rutina.setNombre("Pierna");
        rutina = rutinaRepository.save(rutina);

        mvc.perform(post("/admin/rutina/" + rutina.getIdRutina() + "/ejercicio").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Sentadilla").param("series", "4").param("repeticiones", "10").param("descansoSegundos", "90"))
            .andExpect(redirectedUrl("/admin/rutina/editar/" + rutina.getIdRutina() + "#ejercicios"))
            .andExpect(flash().attribute("success", "Ejercicio agregado"));

        mvc.perform(post("/admin/rutina/" + rutina.getIdRutina() + "/ejercicio").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Peso muerto").param("series", "0").param("repeticiones", "8"))
            .andExpect(flash().attribute("error", "Debe haber al menos 1 serie"));

        List<Ejercicio> ejercicios = ejercicioRepository.findByRutina_IdRutinaOrderByIdEjercicioAsc(rutina.getIdRutina());
        assertEquals(1, ejercicios.size());
        assertEquals(90, ejercicios.get(0).getDescansoSegundos());

        mvc.perform(get("/admin/rutina/editar/" + rutina.getIdRutina()).with(user("1").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Sentadilla")));

        mvc.perform(post("/admin/ejercicio/eliminar/" + ejercicios.get(0).getIdEjercicio()).with(csrf()).with(user("1").roles("ADMIN")))
            .andExpect(flash().attribute("success", "Ejercicio eliminado"));
        assertEquals(0, ejercicioRepository.count());
    }

    @Test
    void actualizarDatosFisicosGuardaElProgresoUnaVezPorDia() throws Exception {
        Cliente cliente = crearCliente("1300", "clave-segura");
        String id = String.valueOf(cliente.getIdCliente());

        for (String peso : List.of("80.0", "79.5")) {
            mvc.perform(post("/cliente/actualizar-datos-fisicos").with(csrf()).with(user(id).roles("CLIENTE"))
                    .param("peso", peso).param("altura", "1.75").param("edad", "30").param("genero", "Masculino"))
                .andExpect(flash().attribute("success", org.hamcrest.Matchers.startsWith("Datos físicos actualizados")));
        }
        List<RegistroPeso> historial = registroPesoRepository.findByCliente_IdClienteOrderByFechaAsc(cliente.getIdCliente());
        assertEquals(1, historial.size());
        assertEquals(79.5, historial.get(0).getPeso());

        // Una medición de un día anterior: el panel ya puede dibujar la línea de evolución
        RegistroPeso ayer = new RegistroPeso();
        ayer.setCliente(cliente);
        ayer.setFecha(java.time.LocalDate.now().minusDays(7));
        ayer.setPeso(82.0);
        ayer.setAltura(1.75);
        registroPesoRepository.save(ayer);

        mvc.perform(get("/cliente/panel").with(user(id).roles("CLIENTE")))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("grafico"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("-2.5 kg")));
    }

    @Test
    void listaDeClientesPaginadaYConBusqueda() throws Exception {
        for (int i = 0; i < 25; i++) {
            crearCliente("D" + i, "clave-segura");
        }
        Cliente ana = crearCliente("777001", "clave-segura");
        ana.setNombre("Anabel");
        clienteRepository.save(ana);

        mvc.perform(get("/admin/clientes").with(user("1").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Página 1 de 2")));

        mvc.perform(get("/admin/clientes").param("q", "anabel").with(user("1").roles("ADMIN")))
            .andExpect(model().attribute("paginaClientes",
                org.hamcrest.Matchers.hasProperty("totalElements", org.hamcrest.Matchers.is(1L))));
    }

    @Test
    void portadaMuestraCifrasReales() throws Exception {
        crearCliente("1400", "clave-segura");
        crearCliente("1401", "clave-segura");
        mvc.perform(get("/"))
            .andExpect(model().attribute("totalMiembros", 2L));
    }

    // ---------- Recuperación de contraseña ----------

    @Test
    void recuperarContrasenaConEnlaceDeUnSoloUso() throws Exception {
        Cliente cliente = crearCliente("2000", "clave-olvidada");
        cliente.setEmail("ana@correo.com");
        clienteRepository.save(cliente);

        mvc.perform(post("/recuperar").with(csrf()).param("email", "ana@correo.com"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("enviado", true));

        org.mockito.ArgumentCaptor<String> cuerpo = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(notificacionService)
            .enviar(org.mockito.ArgumentMatchers.eq("ana@correo.com"), org.mockito.ArgumentMatchers.anyString(), cuerpo.capture());
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("/recuperar/([A-Za-z0-9_-]+)").matcher(cuerpo.getValue());
        assertTrue(m.find());
        String token = m.group(1);

        // En la base sólo queda el hash, nunca el token en claro
        assertTrue(tokenRepository.findAll().stream().noneMatch(t -> t.getTokenHash().equals(token)));

        mvc.perform(get("/recuperar/" + token)).andExpect(model().attribute("valido", true));
        mvc.perform(post("/recuperar/" + token).with(csrf()).param("nueva", "clave-nueva-1").param("confirmacion", "otra"))
            .andExpect(model().attribute("error", "Las contraseñas no coinciden"));
        mvc.perform(post("/recuperar/" + token).with(csrf()).param("nueva", "clave-nueva-1").param("confirmacion", "clave-nueva-1"))
            .andExpect(redirectedUrl("/login"));

        assertTrue(passwordEncoder.matches("clave-nueva-1", clienteRepository.findById(cliente.getIdCliente()).orElseThrow().getPassword()));
        // El enlace ya no sirve una segunda vez
        mvc.perform(get("/recuperar/" + token)).andExpect(model().attribute("valido", false));
    }

    @Test
    void recuperarConEmailInexistenteNoRevelaNada() throws Exception {
        mvc.perform(post("/recuperar").with(csrf()).param("email", "nadie@correo.com"))
            .andExpect(model().attribute("enviado", true));
        org.mockito.Mockito.verifyNoInteractions(notificacionService);
        assertEquals(0, tokenRepository.count());
    }

    // ---------- Perfil del cliente ----------

    @Test
    void clienteEditaSuContactoYCambiaContrasena() throws Exception {
        Cliente cliente = crearCliente("2100", "clave-actual");
        String id = String.valueOf(cliente.getIdCliente());

        mvc.perform(get("/cliente/perfil").with(user(id).roles("CLIENTE"))).andExpect(status().isOk());

        mvc.perform(post("/cliente/perfil").with(csrf()).with(user(id).roles("CLIENTE"))
                .param("email", "nuevo@correo.com").param("telefono", "3111111111").param("direccion", "Medellín"))
            .andExpect(flash().attribute("success", "Tus datos de contacto se actualizaron"));
        assertEquals("nuevo@correo.com", clienteRepository.findById(cliente.getIdCliente()).orElseThrow().getEmail());

        mvc.perform(post("/cliente/perfil/password").with(csrf()).with(user(id).roles("CLIENTE"))
                .param("actual", "equivocada").param("nueva", "clave-nueva-2").param("confirmacion", "clave-nueva-2"))
            .andExpect(flash().attribute("error", "La contraseña actual no es correcta"));

        mvc.perform(post("/cliente/perfil/password").with(csrf()).with(user(id).roles("CLIENTE"))
                .param("actual", "clave-actual").param("nueva", "clave-nueva-2").param("confirmacion", "clave-nueva-2"))
            .andExpect(flash().attribute("success", "Tu contraseña se cambió correctamente"));
        assertTrue(passwordEncoder.matches("clave-nueva-2", clienteRepository.findById(cliente.getIdCliente()).orElseThrow().getPassword()));
    }

    // ---------- Horario de clases ----------

    @Test
    void adminProgramaClasesConDiaYHora() throws Exception {
        Entrenador entrenador = crearEntrenador();
        mvc.perform(post("/admin/clase/crear").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Yoga").param("descripcion", "Flexibilidad").param("capacidad", "10")
                .param("idEntrenador", String.valueOf(entrenador.getIdEntrenador()))
                .param("diaSemana", "WEDNESDAY").param("hora", "18:30").param("duracionMinutos", "60"))
            .andExpect(flash().attribute("success", "Clase creada exitosamente"));
        Clase yoga = claseRepository.findAll().get(0);
        assertEquals("Miércoles 18:30 – 19:30", yoga.getHorarioTexto());

        mvc.perform(post("/admin/clase/crear").with(csrf()).with(user("1").roles("ADMIN"))
                .param("nombre", "Pilates").param("descripcion", "Core").param("capacidad", "10")
                .param("idEntrenador", String.valueOf(entrenador.getIdEntrenador())).param("diaSemana", "MONDAY"))
            .andExpect(flash().attribute("error", "Para programar la clase indica el día y la hora"));

        mvc.perform(get("/admin/clases").with(user("1").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("18:30 – 19:30")));
    }

    // ---------- Vencimiento de planes ----------

    @Test
    void avisaUnaSolaVezALosPlanesPorVencer() {
        Cliente cliente = crearCliente("2200", "clave-segura");
        cliente.setEmail("vence@correo.com");
        clienteRepository.save(cliente);
        Suscripcion s = new Suscripcion();
        s.setCliente(cliente);
        s.setPlan(plan("Pro"));
        s.setFechaInicio(java.time.LocalDate.now().minusDays(27));
        s.setFechaFin(java.time.LocalDate.now().plusDays(3));
        s.setPrecioPagado(109_900);
        s.setEstado(Suscripcion.Estado.ACTIVA);
        suscripcionRepository.save(s);

        assertEquals(1, suscripcionService.enviarAvisosVencimiento());
        assertEquals(0, suscripcionService.enviarAvisosVencimiento());
        org.mockito.Mockito.verify(notificacionService, org.mockito.Mockito.times(1)).enviar(
            org.mockito.ArgumentMatchers.eq("vence@correo.com"),
            org.mockito.ArgumentMatchers.contains("vence en 3 día(s)"), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void panelDelClienteAvisaSiElPlanVencio() throws Exception {
        Cliente cliente = crearCliente("2300", "clave-segura");
        Suscripcion s = new Suscripcion();
        s.setCliente(cliente);
        s.setPlan(plan("Básico"));
        s.setFechaInicio(java.time.LocalDate.now().minusDays(40));
        s.setFechaFin(java.time.LocalDate.now().minusDays(10));
        s.setPrecioPagado(69_900);
        s.setEstado(Suscripcion.Estado.ACTIVA);
        suscripcionRepository.save(s);

        mvc.perform(get("/cliente/panel").with(user(String.valueOf(cliente.getIdCliente())).roles("CLIENTE")))
            .andExpect(model().attributeExists("planVencido"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("venció el")));
    }

    @Test
    void suscripcionesAntiguasSinAvisoNoRompenElPanel() throws Exception {
        // Así quedan en MySQL las suscripciones creadas antes de existir la columna del aviso
        Cliente cliente = crearCliente("2350", "clave-segura");
        suscribir(cliente, plan("Pro"));
        org.springframework.jdbc.core.JdbcTemplate jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
        jdbc.update("UPDATE suscripcion SET aviso_vencimiento_enviado = NULL");

        mvc.perform(get("/cliente/panel").with(user(String.valueOf(cliente.getIdCliente())).roles("CLIENTE")))
            .andExpect(status().isOk());
        assertEquals(0, suscripcionService.enviarAvisosVencimiento());
    }

    @Test
    void laPaginaDeErrorSeMuestraBien() throws Exception {
        mvc.perform(get("/no-existe").with(user("1").roles("ADMIN")))
            .andExpect(status().isNotFound())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Algo salió mal")));
    }

    // ---------- Rol entrenador ----------

    @Test
    void entrenadorConAccesoEntraASuPanelYCreaRutinasSoloParaSusAlumnos() throws Exception {
        Entrenador entrenador = crearEntrenador();
        entrenador.setEmail("coach@fitpro.com");
        entrenadorRepository.save(entrenador);
        Clase clase = crearClase(entrenador, 10);
        Cliente alumno = crearCliente("2400", "clave-segura");
        inscribir(alumno, clase);
        Cliente ajeno = crearCliente("2401", "clave-segura");

        mvc.perform(post("/admin/entrenador/" + entrenador.getIdEntrenador() + "/acceso").with(csrf()).with(user("1").roles("ADMIN"))
                .param("password", "coach-clave-1"))
            .andExpect(flash().attribute("success", org.hamcrest.Matchers.startsWith("Acceso guardado")));

        mvc.perform(post("/login").with(csrf()).param("username", "coach@fitpro.com").param("password", "coach-clave-1"))
            .andExpect(redirectedUrl("/entrenador/panel"));

        String id = String.valueOf(entrenador.getIdEntrenador());
        mvc.perform(get("/entrenador/panel").with(user(id).roles("ENTRENADOR")))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString(alumno.getNombre())));

        mvc.perform(post("/entrenador/rutina/crear").with(csrf()).with(user(id).roles("ENTRENADOR"))
                .param("idCliente", String.valueOf(ajeno.getIdCliente()))
                .param("nombre", "X").param("objetivo", "Y").param("nivel", "Básico"))
            .andExpect(flash().attribute("error", "Sólo puedes crear rutinas para clientes inscritos en tus clases"));

        mvc.perform(post("/entrenador/rutina/crear").with(csrf()).with(user(id).roles("ENTRENADOR"))
                .param("idCliente", String.valueOf(alumno.getIdCliente()))
                .param("nombre", "Full body").param("objetivo", "Resistencia").param("nivel", "Básico"))
            .andExpect(redirectedUrlPattern("/entrenador/rutina/*"));
        Rutina rutina = rutinaRepository.findByEntrenador_IdEntrenadorOrderByIdRutinaDesc(entrenador.getIdEntrenador()).get(0);

        mvc.perform(post("/entrenador/rutina/" + rutina.getIdRutina() + "/ejercicio").with(csrf()).with(user(id).roles("ENTRENADOR"))
                .param("nombre", "Burpees").param("series", "3").param("repeticiones", "15"))
            .andExpect(flash().attribute("success", "Ejercicio agregado"));

        // Otro entrenador no puede ver ni tocar esa rutina
        Entrenador otro = crearEntrenador();
        mvc.perform(get("/entrenador/rutina/" + rutina.getIdRutina()).with(user(String.valueOf(otro.getIdEntrenador())).roles("ENTRENADOR")))
            .andExpect(redirectedUrl("/entrenador/panel"))
            .andExpect(flash().attribute("error", "Esta rutina no es tuya"));

        // Y un cliente no entra al panel de entrenador
        mvc.perform(get("/entrenador/panel").with(user("1").roles("CLIENTE"))).andExpect(status().isForbidden());
    }

    @Test
    void todasLasSeccionesDelAdminCargan() throws Exception {
        Cliente cliente = crearCliente("2500", "clave-segura");
        Entrenador entrenador = crearEntrenador();
        Clase clase = crearClase(entrenador, 5);
        clase.setDiaSemana(java.time.LocalDate.now().getDayOfWeek());
        clase.setHora(java.time.LocalTime.of(7, 0));
        claseRepository.save(clase);
        inscribir(cliente, clase);
        suscribir(cliente, plan("Pro"));

        for (String seccion : List.of("panel", "clientes", "suscripciones", "rutinas", "alimentacion",
                "entrenadores", "clases", "reportes", "entrenador/editar/" + entrenador.getIdEntrenador(),
                "clase/editar/" + clase.getIdClase())) {
            mvc.perform(get("/admin/" + seccion).with(user("1").roles("ADMIN")))
                .andExpect(status().isOk());
        }
        mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Clases grupales de la semana")));
    }

    // ---------- Datos de prueba ----------

    private Plan plan(String nombre) {
        return planRepository.findAll().stream().filter(p -> p.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private void suscribir(Cliente cliente, Plan plan) {
        suscripcionRepository.findByCliente_IdClienteAndEstado(cliente.getIdCliente(), Suscripcion.Estado.ACTIVA)
            .forEach(s -> {
                s.setEstado(Suscripcion.Estado.CANCELADA);
                suscripcionRepository.save(s);
            });
        Suscripcion s = new Suscripcion();
        s.setCliente(cliente);
        s.setPlan(plan);
        s.setFechaInicio(java.time.LocalDate.now());
        s.setFechaFin(java.time.LocalDate.now().plusDays(30));
        s.setPrecioPagado(plan.getPrecioMensual());
        s.setEstado(Suscripcion.Estado.ACTIVA);
        suscripcionRepository.save(s);
    }

    private Cliente crearCliente(String documento, String password) {
        Cliente c = new Cliente();
        c.setNombre("Juan");
        c.setApellido("Perez");
        c.setDocumento(documento);
        c.setTelefono("3000000000");
        c.setPassword(passwordEncoder.encode(password));
        return clienteRepository.save(c);
    }

    private Administrador crearAdmin() {
        Administrador a = new Administrador();
        a.setNombre("Admin");
        a.setApellido("Principal");
        a.setEmail("admin@fitpro.com");
        a.setPassword(passwordEncoder.encode("clave-segura"));
        return administradorRepository.save(a);
    }

    private Entrenador crearEntrenador() {
        Entrenador e = new Entrenador();
        e.setNombre("Carlos");
        e.setEspecialidad("Fuerza");
        return entrenadorRepository.save(e);
    }

    private Clase crearClase(Entrenador entrenador, int capacidad) {
        Clase clase = new Clase();
        clase.setNombre("Spinning");
        clase.setCapacidad(capacidad);
        clase.setEntrenador(entrenador);
        return claseRepository.save(clase);
    }

    private void inscribir(Cliente cliente, Clase clase) {
        Cliente c = clienteRepository.findByIdWithClases(cliente.getIdCliente()).orElseThrow();
        List<Clase> clases = new ArrayList<>(c.getClases());
        clases.add(clase);
        c.setClases(clases);
        clienteRepository.save(c);
    }
}
