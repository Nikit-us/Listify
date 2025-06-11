package com.tech.listify.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// ИЗМЕНЕНО: Добавляем 'district' в исключения ToString
@ToString(exclude = {"district", "users", "advertisements"})
@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    // ИЗМЕНЕНО: Убираем UNIQUE, т.к. уникальность теперь составная (имя + район)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // ДОБАВЛЕНО: Связь "многие-к-одному" с районом
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    // Эти связи остаются без изменений
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private Set<Advertisement> advertisements = new HashSet<>();
}