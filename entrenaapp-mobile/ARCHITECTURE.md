# Organización de paquetes — EntrenaApp Mobile

Paquete raíz: `com.entrenaapp.mobile`

```
com.entrenaapp.mobile
├── MainActivity.java                Host del BottomNavigationView (Home/Atletas/Entrenar/Asistencia/Ajustes)
│                                     + indicador discreto de conexión en línea/sin conexión.
├── EntrenaAppApplication.java       Arranca la sincronización periódica (WorkManager) al iniciar la app.
│
├── ui/                              Pantallas (Activities/Fragments). Hablan solo con los Repository.
│   ├── LoginActivity.java
│   ├── HomeFragment.java            Dashboard: stats + carga semanal, calculados 100% desde SQLite local.
│   ├── SettingsFragment.java        Tab "Ajustes": cerrar sesión.
│   ├── WeeklyBarChartView.java      Grafico de barras dibujado a mano (Canvas), sin librerias externas.
│   ├── deportista/                  Listado, formulario (crear/editar/eliminar), detalle.
│   ├── entrenamiento/               Listado, formulario (GPS + audio), detalle.
│   └── asistencia/                  Selector de sesión + marcado manual por deportista.
│
├── util/                            Helpers reutilizables, sin estado de UI propio.
│   ├── PermissionManager.java       Wrapper de permisos en runtime (cámara, ubicación, micrófono).
│   ├── ImageUtils.java              Redimensionado/orientación de fotos capturadas.
│   ├── GeocodingUtils.java          Reverse geocoding (lat/lng → dirección legible), best-effort.
│   ├── ConnectivityObserver.java    Detección de conectividad (registerDefaultNetworkCallback).
│   └── SyncStatusView.java          Pinta el badge de sincronizado/pendiente en las tarjetas de listado.
│
└── data/                            Capa de datos: todo lo que trae o guarda información.
    │
    ├── remote/                      Todo lo relacionado con la API (Spring Boot).
    │   ├── model/                   DTOs: la "forma" del JSON que la API envía/recibe.
    │   │   ├── LoginRequest.java / AuthResponse.java
    │   │   └── {Deportista,Entrenamiento,Asistencia}Sync{Request,Response}.java
    │   ├── AuthApiService.java      Contrato Retrofit del login.
    │   ├── SyncApiService.java      Contrato Retrofit de deportistas/entrenamientos/asistencias.
    │   ├── RetrofitClient.java      Cliente HTTP singleton; adjunta el JWT via Interceptor.
    │   └── repository/
    │       ├── AuthRepository.java  Login.
    │       └── SyncRepository.java  Orquesta sincronizarTodo(): descarga + sube cada entidad.
    │
    ├── local/                       Todo lo relacionado con SQLite (misma forma que remote/).
    │   ├── entities/                POJOs: la "forma" de una fila guardada localmente.
    │   │   ├── Deportista.java (incluye `activo` para soft-delete)
    │   │   ├── Entrenamiento.java
    │   │   └── Asistencia.java
    │   ├── managerdb/                Contrato SQLite: tablas, columnas, versión de la BD.
    │   │   ├── ManagerDataBase.java         SQLiteOpenHelper: crea/versiona las tablas.
    │   │   ├── DeportistaContract.java / EntrenamientoContract.java / AsistenciaContract.java
    │   │   └── SyncStatus.java              Constantes PENDING/SYNCED (offline-first).
    │   └── repository/
    │       ├── DeportistaRepository.java    CRUD + soft-delete/reactivación + métodos "DesdeServidor"
    │       ├── EntrenamientoRepository.java   para escribir lo que llega del pull-sync sin
    │       └── AsistenciaRepository.java      disparar una nueva subida.
    │
    ├── session/                     Estado de sesión del usuario (no es un origen de datos).
    │   └── SessionManager.java      Guarda el JWT cifrado (EncryptedSharedPreferences) y
    │                                 decide si hay sesión activa leyendo el "exp" del token.
    │
    └── sync/                        Orquestación de sincronización en segundo plano.
        ├── SyncWorker.java          Worker que llama a SyncRepository.sincronizarTodo().
        └── SyncScheduler.java       Programa la sincronización periódica (15 min) y la inmediata
                                       (on-demand tras cada escritura local o al abrir un tab).
```

## La idea central: `remote/` y `local/` son simétricos a propósito

Ambos resuelven la misma pregunta ("¿cómo obtengo/guardo datos?") para un origen distinto, y por
eso tienen la misma forma interna:

| Pregunta | Lado API (`remote/`) | Lado SQLite (`local/`) |
|---|---|---|
| ¿Qué forma tienen los datos? | `model/` (DTOs) | `entities/` (POJOs) |
| ¿Cómo se accede al origen? | `*ApiService` + `RetrofitClient` | `managerdb/` (Contracts + `ManagerDataBase`) |
| ¿Quién le habla a la UI? | `repository/` | `repository/` |

`session/` y `sync/` quedan aparte de ambos: `session/` es el estado de "quién está logueado
ahora mismo", y `sync/` es la orquestación en segundo plano que conecta ambos lados (lee
pendientes de `local/`, los sube por `remote/`, y baja lo nuevo de `remote/` hacia `local/`).

## Regla de oro: la UI nunca habla directo con Retrofit o SQLite

Una `Activity`/`Fragment` solo llama a un `Repository` (`authRepository.login(...)`,
`deportistaRepository.obtenerTodos()`). Nunca ve un `Call<T>` de Retrofit por dentro, ni un
`Cursor` ni `ContentValues` de SQLite. `HomeFragment` sigue esta misma regla: calcula sus
estadísticas leyendo directamente de los `Repository` locales (nunca llama a la API), para que el
dashboard funcione igual con o sin conexión, igual que el resto de la app.

## Sincronización bidireccional (offline-first)

Cada entidad (Deportista, Entrenamiento, Asistencia) usa **IDs generados en el móvil** (UUID) que
la API acepta y reutiliza — no hay tabla de mapeo entre IDs locales y remotos.

- **Push:** cada escritura local (insert/actualizar/desactivar) marca la fila como `sync_status =
  PENDING` y dispara una sincronización inmediata vía `SyncScheduler.solicitarSincronizacion()`.
  `SyncRepository` sube esas filas con `POST` (la API hace upsert por `id`) y las marca `SYNCED`.
- **Pull:** `SyncRepository` descarga las listas actuales de la API y reconcilia contra lo que ya
  hay en SQLite: inserta lo que falta, actualiza filas `SYNCED` (nunca sobrescribe una fila
  `PENDING` local sin subir todavía), y detecta borrados/desactivaciones remotas para reflejarlos
  localmente (`GET /{id}` puntual: 404 → borrado físico local, `activo:false` → soft-delete local).
- Disparadores: periódico cada 15 min, inmediato tras cada escritura local, e inmediato al abrir
  cada tab (Deportistas/Entrenamientos/Asistencia) en `onResume()`.
