package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "characters")
@Setter
@Getter
@Accessors(chain = true)
public class CharacterEntity {
    @Id
    @UuidGenerator
    private UUID id;
    private UUID ownerId;
    private String name;
    private Integer age;
    private Integer reputation;
    private Integer implantPoints;
    private Integer specialImplantPoints;
    private Integer battlePoints;
    private Integer civilPoints;

    @ManyToMany
    @JoinTable(name = "characters_weapons",
        joinColumns = @JoinColumn(name = "char_id"),
        inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<Weapon> weapons = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "characters_skills",
        joinColumns = @JoinColumn(name = "char_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "characters_implants",
        joinColumns = @JoinColumn(name = "char_id"),
        inverseJoinColumns = @JoinColumn(name = "implant_id"))
    private List<Implant> implants = new ArrayList<>();
}
