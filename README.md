# EntrenaApp — MVP

Aplicación para entrenadores deportivos: registro de deportistas, entrenamientos (con GPS y nota de voz), asistencia y reportes de carga. Demostración **100% autónoma en un solo dispositivo móvil** (Android), con sincronización bidireccional a un backend central cuando hay conectividad.

**Actividad:** Actividad III — Arquitectura y desarrollo móvil
**Stack:** Android Studio (Java, SQLite nativo) · Spring Boot 4.1.1 (arquitectura hexagonal, Java 21) + Spring Security/JWT · PostgreSQL 17 · Docker

## Módulos del repositorio

| Carpeta | Contenido | Estado |
|---|---|---|
| [`entrenaapp-api/`](entrenaapp-api) | Backend REST (Spring Boot) | **Completo.** CRUD, auth JWT, reportes/dashboard. |
| [`entrenaapp-db/`](entrenaapp-db) | PostgreSQL vía Docker Compose + `init.sql` | Completo. |
| [`entrenaapp-mobile/`](entrenaapp-mobile) | App Android (Java) | **Completo (MVP).** Login, CRUD offline-first, GPS+audio, sincronización bidireccional, dashboard. Ver [`ARCHITECTURE.md`](entrenaapp-mobile/ARCHITECTURE.md) para el detalle de paquetes. |

## Decisiones de alcance del MVP

Estas decisiones fijan el alcance real frente al diagrama de arquitectura ideal:

1. **Autenticación:** JWT real en la API (`AuthController` + Spring Security). La app mobile hace `POST /api/v1/auth/login`, guarda el token cifrado (`EncryptedSharedPreferences`) y lo reutiliza como sesión offline — no se guarda copia de la contraseña en el dispositivo.
2. **Multimedia (foto de perfil, nota de voz):** se guardan **solo localmente** en el dispositivo; en SQLite y en PostgreSQL solo se persiste la *ruta* (`foto_path`, `observacion_audio_path`), no el binario. No hay subida de archivos a la API — coherente con "demo autónoma en 1 dispositivo".
3. **IDs de sincronización:** el móvil genera el UUID de cada registro (deportista/entrenamiento/asistencia) y la API lo acepta y reutiliza (`POST` hace upsert por `id`). No existe tabla de mapeo entre IDs locales y remotos.
4. **Eliminación de Deportista:** soft-delete (`activo`), no borrado físico, para no perder historial de asistencia. Si se vuelve a crear un deportista con el mismo documento de uno inactivo, se reactiva en vez de duplicar. `Entrenamiento` no tiene soft-delete (borrado físico simple).
5. **Sincronización:** bidireccional. *Push* automático (WorkManager) de cada escritura local pendiente hacia la API. *Pull* automático que reconcilia deportistas/entrenamientos/asistencias contra lo que hay en el servidor (inserta lo nuevo, actualiza lo cambiado, refleja localmente lo borrado/desactivado remoto) — necesario porque el servidor es la fuente de verdad y puede cambiar fuera de la app (ej. un delete directo en la BD o desde otro dispositivo).
6. **Reportes/Dashboard:** existen ambas variantes. La API expone `GET /api/v1/dashboard/stats` y `GET /api/v1/reportes/carga` como agregaciones simples. El Home de la app mobile, sin embargo, calcula sus propias estadísticas **desde SQLite local** (no llama a estos endpoints), para mantener el dashboard funcionando sin conexión igual que el resto de la app.
7. **QR:** reemplazado por foto de perfil vía cámara en el MVP. Lectura de QR queda registrada como Fase II (post-MVP).

## Arquitectura

### Casos de uso

```mermaid
graph TD
    E[Entrenador / Usuario]

    subgraph MVP["EntrenaApp MVP Móvil"]
        UC1[Autenticarse JWT]
        UC2[CRUD Deportistas + soft-delete]
        UC3[Tomar foto de perfil]
        UC4[Crear / listar / eliminar Entrenamientos]
        UC5[Capturar ubicación GPS]
        UC6[Grabar nota de voz]
        UC7[Registrar asistencia manual]
        UC8[Ver dashboard de carga]
        UC9[Sincronizar datos offline/online]
    end

    subgraph HW["Hardware del dispositivo"]
        HW1[Cámara]
        HW2[Sensor GPS]
        HW3[Micrófono]
    end

    subgraph BE["Backend & DB"]
        API[Spring Boot API REST]
        PG[(PostgreSQL)]
    end

    E --> UC1
    E --> UC2
    E --> UC4
    E --> UC7
    E --> UC8

    UC2 --> UC3
    UC4 --> UC5
    UC4 --> UC6

    UC3 --> HW1
    UC5 --> HW2
    UC6 --> HW3

    UC1 --> API
    UC9 --> API
    API --> PG
```

### Componentes

```mermaid
graph TB
    subgraph Mobile["Dispositivo Android (Java)"]
        UI[Activities / Fragments]
        REPO_L[Repository local]
        SQLITE[(SQLite - SQLiteOpenHelper)]
        REPO_R[Repository remoto - Sync]
        WM[WorkManager - sync periodica + on-demand]
        CAM[Intent camara / FileProvider]
        GPS[FusedLocationClient]
        MIC[MediaRecorder / MediaPlayer]
    end

    subgraph Backend["Spring Boot - Hexagonal"]
        SEC[Spring Security + JWT Filter]
        REST[REST Controllers]
        SVC[Servicios / Casos de uso]
        MODEL[Entidades de dominio]
        JPA[JPA / Hibernate]
    end

    PG[(PostgreSQL)]

    UI --> REPO_L --> SQLITE
    UI --> REPO_L --> CAM
    UI --> REPO_L --> GPS
    UI --> REPO_L --> MIC
    REPO_L --> WM
    WM --> REPO_R

    REPO_R -- HTTP/JSON + JWT --> SEC --> REST --> SVC --> MODEL
    SVC --> JPA --> PG
```

### Flujo: registrar un entrenamiento (offline-first, con pull-sync)

```mermaid
sequenceDiagram
    autonumber
    actor U as Entrenador
    participant UI as EntrenamientoFormActivity
    participant HW as GPS / Mic
    participant DB as SQLite local
    participant WM as WorkManager
    participant API as Spring Boot API
    participant PG as PostgreSQL

    U->>UI: Completa tipo, duración, intensidad
    UI->>HW: Solicita ubicación y graba audio
    HW-->>UI: lat/lng + ruta de audio local
    U->>UI: Guardar entrenamiento
    UI->>DB: INSERT (id generado en el movil, sync_status = PENDING)
    DB-->>UI: OK
    UI-->>U: "Guardado localmente"

    Note over DB,WM: Sincronizacion en segundo plano (push)
    WM->>DB: SELECT WHERE sync_status = PENDING
    WM->>API: POST /api/v1/entrenamientos (JWT, upsert por id)
    API->>PG: INSERT/UPDATE
    API-->>WM: 200/201
    WM->>DB: UPDATE sync_status = SYNCED

    Note over DB,WM: Sincronizacion en segundo plano (pull)
    WM->>API: GET /api/v1/entrenamientos
    API-->>WM: lista actual del servidor
    WM->>DB: reconcilia inserts/updates/deletes contra filas SYNCED
```

### Modelo de datos

```mermaid
erDiagram
    USUARIOS {
        string id PK
        string nombre
        string email UK
        string password_hash
        string rol
        timestamp created_at
    }
    DEPORTISTAS {
        string id PK "generado en el movil"
        string nombre
        string documento UK
        int edad
        string disciplina
        string foto_path
        boolean activo "soft-delete + reactivacion"
        string sync_status
    }
    ENTRENAMIENTOS {
        string id PK "generado en el movil"
        date fecha "no puede ser anterior a hoy"
        string tipo
        int duracion_min
        string intensidad
        double latitud
        double longitud
        string observacion_audio_path
        string sync_status
    }
    ASISTENCIAS {
        string id PK "generado en el movil"
        string entrenamiento_id FK
        string deportista_id FK
        boolean asistio
        string observacion
        string sync_status
    }
    ENTRENAMIENTOS ||--o{ ASISTENCIAS : contiene
    DEPORTISTAS ||--o{ ASISTENCIAS : registra
```

`sync_status` (`PENDING` / `SYNCED`) solo existe en SQLite (mobile); en PostgreSQL cada fila sincronizada ya está confirmada, así que la API no necesita esa columna. `activo` sí existe en ambos lados (Deportista), porque el soft-delete es una regla de negocio, no un detalle de sincronización.

### Mapeo Pantalla → Android → Sensor → Endpoint → Datos

| Pantalla | Componente Android | Sensor | Endpoint API | Datos |
|---|---|---|---|---|
| Login | `LoginActivity` | — | `POST /api/v1/auth/login` | JWT en `EncryptedSharedPreferences` (no se guarda password) |
| Home / Dashboard | `HomeFragment` | — | *(ninguno — calculado local)* | Agregado sobre SQLite (deportistas/entrenamientos/asistencias) |
| Deportistas | `DeportistasFragment` / `DeportistaFormActivity` / `DeportistaDetailActivity` | Cámara | `GET/POST/PUT/DELETE /api/v1/deportistas` | SQLite + PostgreSQL (solo `foto_path`, soft-delete) |
| Entrenamientos | `EntrenamientosFragment` / `EntrenamientoFormActivity` / `EntrenamientoDetailActivity` | GPS + Mic | `GET/POST /api/v1/entrenamientos` | SQLite + PostgreSQL |
| Asistencia | `AsistenciaFragment` | — | `POST /api/v1/asistencias`, `GET /api/v1/asistencias/entrenamiento/{id}` | SQLite + PostgreSQL |
| Ajustes | `SettingsFragment` | — | — | Cerrar sesión (borra el JWT local) |
| *(disponible, no consumido por mobile)* | — | — | `GET /api/v1/dashboard/stats`, `GET /api/v1/reportes/carga` | Calculado en la API sobre PostgreSQL |

## Cómo levantar el entorno (dev)

### Opción A — todo en Docker (recomendado, no depende del IDE)

Desde la raíz del repo:

```bash
docker compose up -d --build
```

Levanta Postgres (`entrenaapp_db`) y la API (`entrenaapp_api`, puerto 8080) en contenedores, esperando a que Postgres esté healthy antes de arrancar la API. Para reconstruir tras cambios en `entrenaapp-api/`: `docker compose up -d --build api`. Logs: `docker compose logs -f api`. Apagar: `docker compose down` (agrega `-v` solo si quieres borrar también los datos de Postgres).

### Opción B — API desde el IDE (para debug)

```bash
# 1. Solo la base de datos
cd entrenaapp-db
docker compose up -d

# 2. API (desde entrenaapp-api/, Windows) — requiere JDK 21
mvnw.cmd spring-boot:run
```

### Mobile

Abrir `entrenaapp-mobile/` en Android Studio y ejecutar en emulador/dispositivo, o por línea de comandos:

```bash
cd entrenaapp-mobile
gradlew.bat :app:installDebug
```

La URL base de la API se lee de `entrenaapp-mobile/local.properties` (gitignored, no se commitea) como `API_BASE_URL=...`:

- **Emulador:** no hace falta configurarlo, cae por defecto a `http://10.0.2.2:8080/` (la API corriendo en `localhost:8080` del host).
- **Dispositivo físico:** necesita una URL alcanzable desde el teléfono. Para pruebas rápidas sin desplegar, se puede exponer la API local con [ngrok](https://ngrok.com/) (`ngrok http 8080`) y usar esa URL HTTPS en `local.properties`.

## Fase II (post-MVP, fuera de alcance actual)

- Registro/lectura por código QR en vez de/junto a la foto de perfil.
- Subida de foto/audio como binario al backend (multipart) en lugar de solo la ruta local.
- Roles y permisos por `Usuario.rol` (hoy el campo existe pero no se usa para autorización).
- Consumo real de `GET /api/v1/dashboard/stats` / `GET /api/v1/reportes/carga` desde mobile (ej. para un modo "resumen del equipo" agregando varios dispositivos), o su eliminación si nunca llegan a usarse.
- Pantalla de perfil individual del deportista con métricas de rendimiento (1RM, RPE, volumen) — evaluada durante Reportes y descartada para este MVP por no tener esas métricas modeladas todavía.
