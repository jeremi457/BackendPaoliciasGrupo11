package cl.paolicias.reservas.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clienteEmail;

    @Column(nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private String productoNombre;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String horario;

    @Column(nullable = false)
    private Integer asistentes;

    @Column(nullable = false)
    private String telefono;

    @Column(length = 200)
    private String observaciones;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = false)
    private Integer abono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;

    @Column(nullable = false)
    private LocalDateTime creadaEn;
}