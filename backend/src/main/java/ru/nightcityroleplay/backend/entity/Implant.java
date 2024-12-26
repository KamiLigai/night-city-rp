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
@Table(name = "implants")
public class Implant {
    @Id
    @UuidGenerator
    private UUID id;
    private String name;
    private String implantType;
    private String description;
    private int reputationRequirement;
    private int implantPointsCost;
    private int specialImplantPointsCost;
    @ManyToMany(mappedBy = "implants")
    private List<CharacterEntity> charsId;
}

