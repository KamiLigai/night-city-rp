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
public class CharacterEntity {

    @Id
    @UuidGenerator
    private UUID id;

    private UUID ownerId;

    @Column
    private String name;

    @Column
    private Integer age;
}
