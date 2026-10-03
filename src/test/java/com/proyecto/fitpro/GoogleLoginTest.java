package com.proyecto.fitpro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Con credenciales de Google configuradas aparece el botón y lleva a la pantalla de Google. */
@SpringBootTest(properties = {
    "spring.security.oauth2.client.registration.google.client-id=id-de-prueba",
    "spring.security.oauth2.client.registration.google.client-secret=secreto-de-prueba"
})
@AutoConfigureMockMvc
class GoogleLoginTest {

    @Autowired private MockMvc mvc;

    @Test
    void elBotonDeGoogleLlevaAGoogle() throws Exception {
        mvc.perform(get("/login"))
            .andExpect(content().string(containsString("Continuar con Google")));
        mvc.perform(get("/registro"))
            .andExpect(content().string(containsString("Registrarse con Google")));

        mvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", startsWith("https://accounts.google.com/o/oauth2/v2/auth")))
            .andExpect(header().string("Location", containsString("client_id=id-de-prueba")));
    }
}
