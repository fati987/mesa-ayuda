package com.mesaayuda.area.mapper;

import com.mesaayuda.area.Area;
import com.mesaayuda.area.dto.AreaDto;

public final class AreaMapper {

    private AreaMapper() {
    }

    public static AreaDto aDto(Area area) {
        return new AreaDto(area.getId(), area.getNombre(), area.isRecibeLlamadas(), area.getLimiteWipAgente(), area.isActivo());
    }
}
