# ⚡ Inazuma Legacy

> Aplicación web fan-made inspirada en el universo de **Inazuma Eleven**, desarrollada con Spring Boot como proyecto académico del ciclo de DAM en el instituto El Puig.

---

## 📋 Descripción

**Inazuma Legacy** es una plataforma social y de gestión de personajes basada en el anime/videojuego *Inazuma Eleven*. Los usuarios pueden registrarse, consultar fichas de jugadores de las tres temporadas, jugar pachangas entre personajes, seguirse entre sí y chatear en tiempo real, incluyendo un asistente de IA integrado.

---

## ✨ Funcionalidades principales

- **Autenticación**: Registro, login y logout, con contraseñas cifradas.
- **Recuperación de contraseña**: Flujo en 3 pasos con código de verificación enviado por email (válido 15 minutos).
- **Fichas de jugadores**: Catálogo completo de personajes de las temporadas 1, 2 y 3 con estadísticas detalladas.
- **Pachanga (minijuego)**: Enfrenta a tus jugadores contra rivales aleatorios. Cooldown de 60 segundos entre partidas para evitar spam.
- **Ranking**: Clasificación global de jugadores por puntos Inazuma acumulados.
- **Chat en tiempo real**: Chat global y mensajes privados entre usuarios.
- **Asistente IA**: Canal de chat con una IA (integrada vía API de OpenAI / GPT-4o) que responde en español.
- **Perfil personalizable**: Nombre, rango, afiliación y descripción editables.

---

## 🛠️ Tecnologías utilizadas

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 4.0.3 |
| Plantillas | Thymeleaf + Thymeleaf Security Extras |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL (Supabase & pgAdmin4) |
| Seguridad | Spring Security + BCrypt |
| Tiempo real | WebSocket (STOMP) |
| HTTP reactivo | Spring WebFlux (WebClient) |
| Email | Spring Mail (SMTP Gmail) |
| IA | OpenAI API (GPT-4o) |
| Build | Maven |
| Despliegue | Railway |

---

## ⚙️ Configuración y variables de entorno

El proyecto usa variables de entorno para la configuración sensible. Puedes definirlas en el sistema, en Railway o en el propio `application.properties` como fallback.

| Variable | Descripción | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de la base de datos | `jdbc:postgresql://...` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres.xxxx` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `••••••••` |
| `openai.api.key` | API Key de OpenAI para el asistente IA | `sk-proj-...` |
| `spring.mail.username` | Cuenta Gmail para envío de emails | `tu@gmail.com` |
| `spring.mail.password` | Contraseña de aplicación de Gmail | `xxxx xxxx xxxx xxxx` |

---

## 🚀 Instalación y ejecución local

### Requisitos previos

- Java 21 o superior
- Maven 3.x
- PostgreSQL en local **o** acceso a la instancia de Supabase

### Pasos

```bash
# 1. Clona el repositorio
git clone https://github.com/tu-usuario/InazumaLegacy.git
cd InazumaLegacy/InazumaLegacy

# 2. Configura tus variables de entorno (o edita application.properties)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/inazuma
export SPRING_DATASOURCE_USERNAME=tu_usuario
export SPRING_DATASOURCE_PASSWORD=tu_password

# 3. Compila y ejecuta
./mvnw spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`.

> En Windows usa `mvnw.cmd spring-boot:run` en lugar de `./mvnw`.
