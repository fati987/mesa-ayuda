package com.mesaayuda.area;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mesaayuda.area.dto.AreaActualizarRequest;
import com.mesaayuda.area.dto.AreaCrearRequest;
import com.mesaayuda.area.dto.AreaDto;
import com.mesaayuda.area.mapper.AreaMapper;
import com.mesaayuda.shared.paginacion.PaginaResponse;

@Service
@Transactional(readOnly = true)
public class AreaService {

    private final AreaRepository areaRepository;

    public AreaService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    public PaginaResponse<AreaDto> listar(Pageable pageable) {
        Page<Area> pagina = areaRepository.findAll(pageable);
        return PaginaResponse.de(pagina, AreaMapper::aDto);
    }

    public AreaDto obtener(Long id) {
        return AreaMapper.aDto(buscarPorId(id));
    }

    @Transactional
    public AreaDto crear(AreaCrearRequest request) {
        if (areaRepository.existsByNombre(request.nombre())) {
            throw new AreaNombreDuplicadoException(request.nombre());
        }
        Area area = new Area();
        area.setNombre(request.nombre());
        area.setRecibeLlamadas(request.recibeLlamadas());
        area.setLimiteWipAgente(request.limiteWipAgente());
        area.setActivo(true);
        return AreaMapper.aDto(areaRepository.save(area));
    }

    @Transactional
    public AreaDto actualizar(Long id, AreaActualizarRequest request) {
        Area area = buscarPorId(id);
        area.setNombre(request.nombre());
        area.setRecibeLlamadas(request.recibeLlamadas());
        area.setLimiteWipAgente(request.limiteWipAgente());
        area.setActivo(request.activo());
        return AreaMapper.aDto(area);
    }

    @Transactional
    public void desactivar(Long id) {
        Area area = buscarPorId(id);
        area.setActivo(false);
    }

    private Area buscarPorId(Long id) {
        return areaRepository.findById(id).orElseThrow(() -> new AreaNoEncontradaException(id));
    }
}
