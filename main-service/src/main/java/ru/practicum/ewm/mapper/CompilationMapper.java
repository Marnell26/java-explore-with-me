package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.Set;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "events", source = "events")
    })
    Compilation toCompilation(NewCompilationDto newCompilationDto, Set<Event> events);

    CompilationDto toCompilationDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "events", source = "events")
    })
    void updateCompilation(UpdateCompilationDto updateCompilationDto, Set<Event> events,
                           @MappingTarget Compilation compilation);

}
