package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.ParticipationRequest;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

}
