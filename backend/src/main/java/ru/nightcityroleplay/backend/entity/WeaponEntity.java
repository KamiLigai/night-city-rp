package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Entity
@Table(name = "weapons")
@Setter
@Getter

public class WeaponEntity {
    private int id;
    private Boolean is_melee;
    private String name;
    private String weapon_type;
    private int penetration;
    private int reputation_requirement;

}
