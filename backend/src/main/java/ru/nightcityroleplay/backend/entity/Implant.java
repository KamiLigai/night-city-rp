package ru.nightcityroleplay.backend.entity;

import jakarta.persistence.*;
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

