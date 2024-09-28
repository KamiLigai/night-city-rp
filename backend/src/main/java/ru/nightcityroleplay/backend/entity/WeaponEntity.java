package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.*;

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
public class WeaponEntity {
    @Id
    @UuidGenerator
    private UUID id;
    private Boolean isMelee;
    private String name;
    private String weaponType;
    private int penetration;
    private int reputationRequirement;
    @ManyToMany()
    @JoinTable(name = "characters_weapons",
        joinColumns = @JoinColumn(name = "chars"),
        inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<CharacterEntity> charsId;
}
