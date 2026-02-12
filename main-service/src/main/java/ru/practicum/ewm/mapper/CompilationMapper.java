package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mappings({
            @Mapping(target = "id", source = "newCompilationDto.id"),
            @Mapping(target = "events", source = "events")
    })
    Compilation toCompilation(NewCompilationDto newCompilationDto, Set<Event> events);

    CompilationDto toCompilationDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCompilation(NewCompilationDto newCompilationDto, @MappingTarget Compilation compilation);

}
