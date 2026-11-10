
package com.logitrack.shipmentservice.dominio.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearMiEnvioRequestDto(

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor que cero")
        Double peso,

        @NotNull(message = "El ancho es obligatorio")
        @Positive(message = "El ancho debe ser mayor que cero")
        Double ancho,

        @NotNull(message = "El alto es obligatorio")
        @Positive(message = "El alto debe ser mayor que cero")
        Double alto,

        @NotNull(message = "El largo es obligatorio")
        @Positive(message = "El largo debe ser mayor que cero")
        Double largo,

        @Valid
        @NotNull(message = "La dirección de origen es obligatoria")
        DireccionEntrada origen,

        @Valid
        @NotNull(message = "La dirección de destino es obligatoria")
        DireccionEntrada destino
) {

    public record DireccionEntrada(

            @NotBlank(message = "La calle es obligatoria")
            @Size(max = 100)
            String calle,

            @NotBlank(message = "El número es obligatorio")
            @Size(max = 20)
            String numero,

            @NotBlank(message = "La ciudad es obligatoria")
            @Size(max = 100)
            String ciudad,

            @NotBlank(message = "El código postal es obligatorio")
            @Size(max = 20)
            String codigoPostal,

            @NotBlank(message = "La provincia es obligatoria")
            @Size(max = 100)
            String provincia,

            @Size(max = 100)
            String localidad,

            @Size(max = 255)
            String descripcion
    ) {
        public DireccionDto toDto() {
            return new DireccionDto(
                    calle.trim(),
                    numero.trim(),
                    ciudad.trim(),
                    codigoPostal.trim(),
                    provincia.trim(),
                    localidad == null ? "" : localidad.trim(),
                    descripcion == null ? "" : descripcion.trim()
            );
        }
    }
}
