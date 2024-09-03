package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

// todo: add Many-to-Many

@Entity
@Setter
@Getter
@Table (name = "skills")
public class Skill {

    @Id
    @UuidGenerator
    private UUID id;
    private String name;
    private String description;
    private int level;
    private String type;
    private int cost;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "characters_skills",
            joinColumns = @JoinColumn(name = "char_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<CharacterEntity> charsId;


}
