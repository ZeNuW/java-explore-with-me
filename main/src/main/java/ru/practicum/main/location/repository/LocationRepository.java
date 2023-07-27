package ru.practicum.main.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.location.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    /*
    по описанию в практикуме - да)
    А вот в swagger описание event содержит поле location, а так же локация есть в тестах постман, они заполняются
    и требуются по тесту в возвращаемом dto
     */
}