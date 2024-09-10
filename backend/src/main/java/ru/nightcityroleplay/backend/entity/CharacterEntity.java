package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
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
    private String name;
    private int age;
    @ManyToMany
    @JoinTable(name = "characters_skills",
            joinColumns = @JoinColumn(name = "char_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;
}
