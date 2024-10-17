package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
    private Integer age;
    private Integer reputation;
    private Integer implant_points;
    private Integer special_implant_points;
    private Integer battle_points;
    private Integer civil_points;
    @ManyToMany
    @JoinTable(name = "characters_skills",
            joinColumns = @JoinColumn(name = "char_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private List<Skill> skills;
}
