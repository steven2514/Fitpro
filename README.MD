# FitPro

Muchas personas quieren comenzar a ejercitarse, pero no cuentan con orientación confiable, lo que genera desmotivación. Ante la falta de recursos claros y accesibles, surge FIT PRO, una plataforma digital que ofrece guías básicas, rutinas, horarios y hábitos saludables para apoyar a los principiantes y promover el ejercicio seguro y responsable.

Sistema de gestión para gimnasios, con un panel para administradores y otro para clientes.

**Tecnologías:** Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA · Thymeleaf · MySQL 8

## Funciones

**Portada pública**
- Cómo funciona, horario semanal de clases, entrenadores, planes con precios, calculadora de IMC,
  preguntas frecuentes y contacto. Las cifras y el horario salen de la base de datos.

**Clientes**
- Registro e inicio de sesión con documento o email, y **recuperación de contraseña** por correo
  (enlace de un solo uso que caduca a los 30 minutos).
- **Mi perfil**: editar email, teléfono y dirección, cambiar la contraseña e historial de planes.
- **Planes de suscripción** (Básico, Pro, Élite): elegir, cambiar o cancelar el plan, con **avisos de
  vencimiento** en el panel y por correo.
- Datos físicos con cálculo de IMC, categoría y peso ideal, e **historial de progreso** con gráfico.
- Rutinas con sus **ejercicios** (series, repeticiones, descanso y notas).
- Plan de alimentación con total de calorías.
- **Horario semanal** de sus clases y **autoinscripción** con control de cupos (requiere un plan con clases).

**Entrenadores** (con acceso dado por un administrador)
- Su horario, sus clases con los inscritos y la lista de sus alumnos.
- Crear rutinas y ejercicios para sus alumnos (sólo los inscritos en sus clases).

**Administradores** — panel dividido en secciones con menú lateral
- **Resumen**: cifras clave, buscador por documento, clases de hoy, planes por vencer y últimos clientes.
- **Clientes** (paginados con búsqueda), **planes y suscripciones**, **clases y horario**, **entrenadores**
  (con su acceso), **rutinas**, **alimentación**.
- **Reportes**: ventas por mes, clientes nuevos por mes, suscripciones por plan y ocupación de clases.
- Creación de otros administradores (el registro de administradores no es público).

**Seguridad**
- Contraseñas cifradas con BCrypt, protección CSRF en todos los formularios y Content-Security-Policy
  estricta (sin scripts ni estilos inline).
- Los formularios usan DTO validados: no se puede sobrescribir otra cuenta manipulando un formulario.
- **Bloqueo por intentos fallidos**: tras 5 contraseñas incorrectas seguidas desde la misma IP, el
  login se bloquea 15 minutos (configurable con `LOGIN_MAX_INTENTOS` y `LOGIN_MINUTOS_BLOQUEO`).
- El correo de recuperación se envía en segundo plano: el formulario responde igual de rápido
  exista o no la cuenta, así que no sirve para averiguar qué emails están registrados.
- Las credenciales se leen de `.env`, que no se sube al repositorio.

## Cómo ejecutarlo

1. Crea la base de datos en MySQL:
   ```sql
   CREATE DATABASE fitpro_db;
   ```
   Las tablas se crean solas al arrancar (`ddl-auto=update`), y también los 3 planes de suscripción
   por defecto. Si prefieres crearlas a mano, usa [`database/fitpro_db.sql`](database/fitpro_db.sql).

2. Copia `.env.example` como `.env` y rellena la contraseña de MySQL. Si la base está vacía,
   define también `ADMIN_EMAIL` y `ADMIN_PASSWORD` para crear el primer administrador.

3. Arranca la aplicación:
   ```bash
   ./mvnw spring-boot:run
   ```
   Con el perfil de desarrollo (muestra el SQL, recarga las plantillas sin reiniciar y, si no hay
   correo configurado, escribe en la consola el enlace de recuperación de contraseña):
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. Abre <http://localhost:8082>.

### En un servidor

Usa el perfil `prod`: no modifica las tablas (`ddl-auto=validate`, así que deben existir ya) y la
cookie de sesión sólo viaja por HTTPS.

```bash
./mvnw clean package
SPRING_PROFILES_ACTIVE=prod java -jar target/fitpro-1.0.0.jar
```

Si todavía no hay HTTPS, añade `COOKIE_SEGURA=false` o el inicio de sesión no funcionará.

## Reglas de negocio

| Regla | Dónde |
|-------|-------|
| Un cliente tiene como máximo una suscripción activa; cambiar de plan cierra la anterior. | `SuscripcionServiceImpl` |
| Una suscripción vale mientras esté ACTIVA y su fecha de fin no haya pasado. | `Suscripcion.isVigente()` |
| Un plan con suscripciones no se elimina: se desactiva (deja de ofrecerse, pero las existentes siguen). | `PlanServiceImpl` |
| El cliente sólo se inscribe solo en clases si su plan las incluye; el admin puede inscribir a cualquiera. | `ClienteServiceImpl` |
| Nadie se inscribe en una clase llena, y su capacidad no puede bajar de los inscritos actuales. | `ClienteServiceImpl`, `AdminController` |
| Se guarda una medición de peso por día (la última del día manda). | `ProgresoServiceImpl` |

## Pruebas

```bash
./mvnw test
```

Las pruebas usan una base H2 en memoria, así que no necesitan MySQL. Cubren la seguridad
(CSRF, roles, sobrescritura de cuentas, bloqueo del login) y los flujos principales: suscripciones, clases, ejercicios,
progreso y paginación.

## Estructura

```
src/main/java/com/proyecto/fitpro/
├── config/       Seguridad, administrador inicial y planes por defecto
├── controller/   Rutas web (inicio, auth, panel admin, panel cliente)
├── dto/          Datos de formularios con sus validaciones, y el gráfico de progreso
├── exception/    Errores de negocio y manejo global de errores
├── model/        Entidades JPA
├── repository/   Acceso a datos
└── service/      Lógica de negocio
```
