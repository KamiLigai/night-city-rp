package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "weapons")
@Setter
@Getter
@Accessors(chain = true)
public class Weapon {
    @Id
    @UuidGenerator
    private UUID id;
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private int penetration;
    private int reputationRequirement;
    @ManyToMany(mappedBy = "weapons")
    private List<CharacterEntity> characters;
}
