package com.odk.Entity;

import java.util.Date;

import com.odk.Enum.StatutCourrier;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoriqueCourrier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //Identifiant de la table historique: Auto-Incrémente...

    @ManyToOne
    @JoinColumn(name="courrier_id")
    private Courrier courrier; //Association avec la table Courrier: courrier concerné...

    @ManyToOne
    @JoinColumn(name = "entite_id")
    private Entite entite; //Entité qui recoit ou traite le courrier ...

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur; //Directeur ou responsable qui fait l'action ...

    @Enumerated(EnumType.STRING)
    private StatutCourrier statut;   //Statut du courrier au moment de l'action ... 

    @Temporal(TemporalType.DATE)
    private Date dateAction;

    private String commentaire;
}
