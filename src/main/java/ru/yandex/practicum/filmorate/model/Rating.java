package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rating {
    private Integer id;
    private String name;

    public Rating() {
    }

    public Rating(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}