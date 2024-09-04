package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "weapons")
@Setter
@Getter

public class WeaponEntity {
    @Id
    @UuidGenerator
    private UUID id;
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private int penetration;
    private int reputationRequirement;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "characters_weapons",
        joinColumns = @JoinColumn(name = "char_id"),
        inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<CharacterEntity> charsId;
}
