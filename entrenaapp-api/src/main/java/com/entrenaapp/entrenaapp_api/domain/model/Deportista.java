package com.entrenaapp.entrenaapp_api.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "deportistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deportista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // UNIQUE real: el documento identifica siempre a la misma fila, incluso si
    // esta desactivada. Si se "recrea" un deportista con un documento que
    // pertenece a uno inactivo, el servicio reactiva esa fila en vez de
    // insertar una nueva (ver DeportistaService.crear).
    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false)
    private Integer edad;

    @Column(nullable = false, length = 50)
    private String disciplina;

    @Column(name = "foto_path")
    private String fotoPath;

    // Soft-delete por auditoria: "eliminar" un deportista desde la app solo lo
    // desactiva, no se borra la fila. Un borrado fisico definitivo (ej. tras un
    // año) queda fuera del alcance del MVP.
    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
