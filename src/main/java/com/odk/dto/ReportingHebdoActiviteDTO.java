package com.odk.dto;

import com.odk.Entity.Entite;
import com.odk.Entity.TypeActivite;
import com.odk.Enum.Statut;

import java.util.Date;

public class ReportingHebdoActiviteDTO {

    private Long id;
    private String nom;
    private String titre;
    private Date dateDebut;
    private Date dateFin;
    private Statut statut; // enum
    private String lieu;
    private String description;
    private Integer objectifParticipation;
    private Entite entite;
    private Long salleId; // uniquement l'id
    private String createdBy; // nom complet de l'utilisateur
    private TypeActivite typeActivite;

    public ReportingHebdoActiviteDTO(
            Long id,
            String nom,
            String titre,
            Date dateDebut,
            Date dateFin,
            Statut statut,
            String lieu,
            String description,
            Integer objectifParticipation,
            Entite entite,
            Long salleId,
            String createdBy,
            TypeActivite typeActivite
    ) {
        this.id = id;
        this.nom = nom;
        this.titre = titre;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.lieu = lieu;
        this.description = description;
        this.objectifParticipation = objectifParticipation;
        this.entite = entite;
        this.salleId = salleId;
        this.createdBy = createdBy;
        this.typeActivite = typeActivite;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public Date getDateDebut() { return dateDebut; }
    public void setDateDebut(Date dateDebut) { this.dateDebut = dateDebut; }

    public Date getDateFin() { return dateFin; }
    public void setDateFin(Date dateFin) { this.dateFin = dateFin; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getObjectifParticipation() { return objectifParticipation; }
    public void setObjectifParticipation(Integer objectifParticipation) { this.objectifParticipation = objectifParticipation; }

    public Entite getEntite() { return entite; }
    public void setEntite(Entite entite) { this.entite = entite; }

    public Long getSalleId() { return salleId; }
    public void setSalleId(Long salleId) { this.salleId = salleId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public TypeActivite getTypeActivite() { return typeActivite; }
    public void setTypeActivite(TypeActivite typeActivite) { this.typeActivite = typeActivite; }
}
