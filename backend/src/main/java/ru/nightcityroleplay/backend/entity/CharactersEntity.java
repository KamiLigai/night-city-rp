package ru.nightcityroleplay.backend.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;


@Entity
@Table(name = "characters")
@Setter
@Getter


public class CharactersEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Id
    @UuidGenerator
    private UUID owner_id;

    @Column
    private String name;

    @Column
    private Integer age;
}
