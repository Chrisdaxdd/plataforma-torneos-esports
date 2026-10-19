# 🏆 Plataforma de Torneos y Emparejamiento Competitivo para Esports

Proyecto Integrador de Aula desarrollado bajo metodología **Scrum** con arquitectura distribuida de microservicios en **Java 21**, **Spring Boot 3**, **MongoDB Atlas (NoSQL)**, **Thymeleaf** y **Bootstrap 5 (Dark Gaming UI)**.

---

## 👥 Equipo y Roles Scrum

* **Product Owner:** Julian Barney (Docente UTS)
* **Scrum Master & Developer:** Cesar Johan Casado Calderon
* **Developer:** Brayan Esteban Cruz Arango
* **Developer (Integración y Despliegue):** Cristian Danilo Pineda

---

## 📂 Estructura del Proyecto

```text
├── plataforma-esports-parent/   # Proyecto agregador POM Maven
├── torneos-service/             # Microservicio de Torneos, Inscripciones y Usuarios (Puerto 8081)
├── matchmaking-service/         # Microservicio de Llaves, Enfrentamientos y Arbitraje (Puerto 8083)
├── frontend-web/                # Aplicación Web Thymeleaf + Bootstrap 5 (Puerto 8082)
├── docker-compose.yml           # Orquestación de contenedores Docker
├── iniciar_proyecto.bat         # Script de ejecución con un clic para Windows
├── iniciar_proyecto.sh          # Script de ejecución para Linux/macOS
└── README.md                    # Documentación del proyecto
```

---

## ⚙️ Requisitos Previos

Para ejecutar el proyecto, el equipo receptor solo necesita tener instalado:
1. **Java JDK 17 o 21** instalado y configurado en el `PATH`.
2. Conexión a Internet (para conectarse al clúster en la nube de **MongoDB Atlas** y para que el Maven Wrapper descargue las dependencias si no están en caché).
3. *(Opcional)* **Docker Desktop** si se prefiere ejecutar mediante contenedores.
4. *(Opcional)* **Spring Tools Suite (STS)**, **Eclipse** o **IntelliJ IDEA**.

> [!NOTE]
> **No es necesario instalar MongoDB localmente:** El proyecto ya viene configurado para conectarse directamente al clúster de MongoDB Atlas (`db_torneos` y `db_matchmaking`).

---

## 🚀 Formas de Ejecutar el Proyecto

Puedes elegir cualquiera de las siguientes alternativas para correr el proyecto:

### Alternativa 1: Ejecución Rápida con 1 Clic (Recomendada en Windows)
1. Haz doble clic en el archivo **`iniciar_proyecto.bat`**.
2. Se abrirán 3 ventanas de terminal ejecutando automáticamente cada servicio:
   * Ventana 1: `torneos-service` (Puerto 8081)
   * Ventana 2: `matchmaking-service` (Puerto 8083)
   * Ventana 3: `frontend-web` (Puerto 8082)
3. Abre tu navegador web en: [http://localhost:8082/torneos](http://localhost:8082/torneos).

---

### Alternativa 2: Importar en Spring Tools Suite (STS) o Eclipse
1. Abre **Spring Tool Suite (STS)** o **Eclipse**.
2. Ve al menú: **File** ➡️ **Import...**
3. Selecciona **Maven** ➡️ **Existing Maven Projects** y haz clic en **Next**.
4. En **Root Directory**, selecciona la carpeta que contiene los proyectos:
   * Selecciona las carpetas: `plataforma-esports-parent`, `torneos-service`, `matchmaking-service` y `frontend-web`.
5. Haz clic en **Finish**.
6. En la pestaña **Boot Dashboard** o en el explorador de paquetes:
   * Clic derecho en `TorneosServiceApplication.java` ➡️ **Run As** ➡️ **Spring Boot App**.
   * Clic derecho en `MatchmakingServiceApplication.java` ➡️ **Run As** ➡️ **Spring Boot App**.
   * Clic derecho en `FrontendWebApplication.java` ➡️ **Run As** ➡️ **Spring Boot App**.

---

### Alternativa 3: Ejecución con Docker Compose
Si tienes Docker instalado:
```bash
docker compose up --build
```
Esto compilará las imágenes multi-stage y levantará los 3 contenedores coordinados automáticamente.

---

### Alternativa 4: Ejecución Manual por Terminal (Maven Wrapper)
Abre 3 terminales distintas en la raíz del proyecto y ejecuta:

* **Terminal 1:**
  ```bash
  cd torneos-service
  ./mvnw spring-boot:run
  ```
* **Terminal 2:**
  ```bash
  cd matchmaking-service
  ./mvnw spring-boot:run
  ```
* **Terminal 3:**
  ```bash
  cd frontend-web
  ./mvnw spring-boot:run
  ```

---

## 🌐 Enlaces del Sistema en Ejecución

Una vez que los servicios estén iniciados:

* **Plataforma Web (Thymeleaf UI):**
  👉 [http://localhost:8082/torneos](http://localhost:8082/torneos)
* **Documentación OpenAPI / Swagger de Torneos:**
  👉 [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
* **Documentación OpenAPI / Swagger de Matchmaking:**
  👉 [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

---

## 🎮 Datos y Torneos Precargados

La base de datos en la nube ya cuenta con datos de prueba reales para evaluar el flujo completo:
* **Torneos Reales:** *VCT Masters Bangkok 2026*, *VCT Champions París 2026*, *Esports World Cup (EWC) 2026*.
* **Equipos Precargados:** *Sentinels*, *Fnatic*, *KRÜ Esports*, *Paper Rex*, *Leviatán*, *Team Heretics*, *Edward Gaming*, *Gen.G*.
* **Plantillas con Buscador Predictivo:** Al registrar inscripciones o asignar árbitros en las llaves, puedes empezar a escribir el nombre del jugador o árbitro y el autocompletado interactivo desplegará las opciones oficiales.
