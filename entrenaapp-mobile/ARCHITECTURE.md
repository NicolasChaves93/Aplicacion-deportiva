# Organización de paquetes — EntrenaApp Mobile

Paquete raíz: `com.entrenaapp.mobile`

```
com.entrenaapp.mobile
├── ui/                             Pantallas (Activities/Fragments). Hablan solo con los Repository.
│   └── LoginActivity.java
├── MainActivity.java               Pantalla post-login (placeholder de la Fase 3).
│
└── data/                           Capa de datos: todo lo que trae o guarda información.
    │
    ├── remote/                     Todo lo relacionado con la API (Spring Boot).
    │   ├── model/                  DTOs: la "forma" del JSON que la API envía/recibe.
    │   │   ├── LoginRequest.java
    │   │   └── AuthResponse.java
    │   ├── AuthApiService.java     Contrato Retrofit: qué endpoints existen (@POST, @GET...).
    │   ├── RetrofitClient.java     Construye el cliente HTTP una sola vez (singleton).
    │   └── repository/
    │       └── AuthRepository.java Única puerta que la UI usa para pedir datos de red.
    │
    ├── local/                      Todo lo relacionado con SQLite (misma forma que remote/).
    │   ├── entities/               POJOs: la "forma" de una fila guardada localmente.
    │   │   ├── Deportista.java
    │   │   ├── Entrenamiento.java
    │   │   └── Asistencia.java
    │   ├── managerdb/              Contrato SQLite: tablas, columnas, versión de la BD.
    │   │   ├── ManagerDataBase.java        SQLiteOpenHelper: crea/versiona las tablas.
    │   │   ├── DeportistaContract.java     Nombres de tabla/columna + CREATE TABLE.
    │   │   ├── EntrenamientoContract.java
    │   │   ├── AsistenciaContract.java
    │   │   └── SyncStatus.java             Constantes PENDING/SYNCED (offline-first).
    │   └── repository/
    │       ├── DeportistaRepository.java   Única puerta que la UI usa para leer/escribir
    │       ├── EntrenamientoRepository.java  en SQLite (insert, obtenerTodos, actualizar...).
    │       └── AsistenciaRepository.java
    │
    └── session/                    Estado de sesión del usuario (no es un origen de datos).
        └── SessionManager.java     Guarda el JWT cifrado (EncryptedSharedPreferences) y
                                     decide si hay sesión activa leyendo el "exp" del token.
```

## La idea central: `remote/` y `local/` son simétricos a propósito

Ambos resuelven la misma pregunta ("¿cómo obtengo/guardo datos?") para un origen distinto, y por
eso tienen la misma forma interna:

| Pregunta | Lado API (`remote/`) | Lado SQLite (`local/`) |
|---|---|---|
| ¿Qué forma tienen los datos? | `model/` (DTOs) | `entities/` (POJOs) |
| ¿Cómo se accede al origen? | `AuthApiService` + `RetrofitClient` | `managerdb/` (Contracts + `ManagerDataBase`) |
| ¿Quién le habla a la UI? | `repository/` | `repository/` |

`session/` queda aparte de ambos porque no es un origen de datos: es el estado de "quién está
logueado ahora mismo" en el dispositivo, y lo usan tanto `LoginActivity` como `MainActivity`.

## Regla de oro: la UI nunca habla directo con Retrofit o SQLite

Una `Activity`/`Fragment` solo llama a un `Repository` (`authRepository.login(...)`,
`deportistaRepository.obtenerTodos()`). Nunca ve un `Call<T>` de Retrofit por dentro, ni un
`Cursor` ni `ContentValues` de SQLite. Dos beneficios concretos para este proyecto:

1. La UI se puede probar/cambiar sin tocar cómo se obtienen los datos.
2. Cuando construyamos la sincronización (Fase 4 del plan en el `README.md` raíz), el código que
   decide "¿leo de SQLite o llamo a la API?" para cada entidad vive en un solo lugar (su
   `Repository`), no regado por toda la app.

## Qué falta para que `remote/` y `local/` reflejen las mismas 4 entidades

Hoy `remote/` solo tiene el DTO de `Auth` (login). Cuando construyamos las pantallas de
Deportistas/Entrenamientos/Asistencias (Fase 3) y su sincronización (Fase 4), cada entidad va a
necesitar:

- Un DTO en `data/remote/model/` (ej. `DeportistaResponse`) que refleje el JSON de la API.
- Un método en un `ApiService` para consumir/enviar esos datos.
- Un mapeo `entity ↔ DTO` dentro del `Repository` local correspondiente, que es quien decide
  cuándo pedirle algo a la API y cuándo resolverlo con lo que ya hay en SQLite.
