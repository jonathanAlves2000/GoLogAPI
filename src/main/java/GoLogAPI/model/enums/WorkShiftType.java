package GoLogAPI.model.enums;

public enum WorkShiftType {
    COMERCIAL_PADRAO,     // 08:00 às 18:00 (1h20 almoço, 44h semanais)
    ESCALA_12X36_DIURNO,  // 07:00 às 19:00 (1h almoço)
    ESCALA_12X36_NOTURNO, // 19:00 às 07:00 (1h intervalo)
    TURNO_NOTURNO,        // 22:00 às 06:00
    DIARISTA_FLEXIVEL,    // Conforme demanda
    PERSONALIZADO         // Totalmente customizado por motorista
}
