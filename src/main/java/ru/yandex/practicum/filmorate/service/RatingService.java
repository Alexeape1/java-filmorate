package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RatingService {

    private final RatingStorage ratingStorage;

    @Autowired
    public RatingService(@Qualifier("ratingDbStorage") RatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public List<RatingDto> getAllMpa() {
        log.info("Получение всех рейтингов");
        return ratingStorage.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public RatingDto getMpaById(Integer id) {
        log.info("Получение рейтинга по id: {}", id);
        Rating rating = ratingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id=" + id + " не найден"));
        return toDto(rating);
    }

    private RatingDto toDto(Rating rating) {
        return new RatingDto(rating.getId(), rating.getName());
    }
}