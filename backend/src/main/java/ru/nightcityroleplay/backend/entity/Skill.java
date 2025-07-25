package ru.nightcityroleplay.backend.entity;

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
@Setter
@Getter
@Table(name = "skills")
public class Skill {

    @Id
    @UuidGenerator
    private UUID id;
    private String skillFamily;
    private UUID skillFamilyId;
    private String name;
    private String description;
    private String skillClass;
    private Boolean typeIsBattle;
    private int level;
    private int battleCost;
    private int civilCost;
    private int reputationRequirement;
    @ManyToMany(mappedBy = "skills")
    private List<CharacterEntity> characters;
}
