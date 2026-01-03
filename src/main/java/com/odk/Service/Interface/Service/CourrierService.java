package com.odk.Service.Interface.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.odk.Entity.Courrier;
import com.odk.Entity.Entite;
import com.odk.Entity.HistoriqueCourrier;
import com.odk.Entity.Utilisateur;
import com.odk.Enum.StatutCourrier;
import com.odk.Repository.CourrierRepository;
import com.odk.Repository.EntiteOdcRepository;
import com.odk.Repository.HistoriqueCourrierRepository;
import com.odk.Repository.UtilisateurRepository;
import com.odk.dto.CourrierDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourrierService {

    private final CourrierRepository courrierRepository;
    private final EntiteOdcRepository entiteRepository;
    private final HistoriqueCourrierRepository historiqueRepository;
    private final EmailService emailService;
    private final UtilisateurRepository utilisateurRepository;
    private final String uploadDir = "uploads/courriers";

    /* ======================================================
     *  PARTIE 1 : RÉCEPTION / ENREGISTREMENT DU COURRIER
     * ====================================================== */
    public Courrier enregistrerCourrier(CourrierDTO dto) throws IOException {

        Entite direction = entiteRepository.findById(dto.getDirectionId())
                .orElseThrow(() -> new RuntimeException("Direction non trouvée"));

        // Gestion du fichier
        String cheminFichier = sauvegarderFichier(dto.getFichier());

        Courrier courrier = new Courrier();
        courrier.setNumero(dto.getNumero());
        courrier.setObjet(dto.getObjet());
        courrier.setExpediteur(dto.getExpediteur());
        courrier.setEntite(direction);
        courrier.setFichier(cheminFichier);
        courrier.setStatut(StatutCourrier.RECU);
        courrier.setDateReception(new Date());
        courrier.setDateLimite(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));

        courrierRepository.save(courrier);

       // 5️⃣ Enregistrer l’historique avec l’utilisateur
    enregistrerHistorique(courrier, courrier.getEntite().getResponsable(), courrier.getEntite(),  StatutCourrier.RECU,
            "Réception du courrier à la direction");

    // 5️⃣ Préparer le mail HTML
    if (direction.getResponsable() != null && direction.getResponsable().getEmail() != null) {
        String emailBody = "<!DOCTYPE html><html><body>"
                + "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>Nouveau courrier reçu</h2>"
                + "<p>Bonjour,</p>"
                + "<p>Un nouveau courrier a été enregistré à votre attention.</p>"
                + "<p><b>Numéro :</b> " + courrier.getNumero() + "</p>"
                + "<p><b>Objet :</b> " + courrier.getObjet() + "</p>"
                + "<p><b>Expéditeur :</b> " + courrier.getExpediteur() + "</p>"
                + "<p>Merci de consulter le courrier pour plus de détails.</p>"
                + "<hr>"
                + "<p style='font-size: 0.9em;'>Ceci est un email automatisé. Merci de ne pas y répondre.</p>"
                + "</div></body></html>";

        String sujet = "Nouveau courrier reçu : " + courrier.getNumero();

        // 6️⃣ Envoyer le mail
        System.out.println("Envoi mail au directeur: " + direction.getResponsable().getEmail());
        emailService.sendSimpleEmail(direction.getResponsable().getEmail(), sujet, emailBody);
    }

    return courrier;
}

        
    

    /* ======================================================
     *  PARTIE 2 : IMPUTATION (DIRECTEUR)
     * ====================================================== */

    public Courrier imputerCourrier(Long courrierId, Long entiteCibleId, Utilisateur utilisateurCible) {

    Courrier courrier = getCourrier(courrierId);
   
    // Vérifier si l'entité cible existe
    Entite entiteCible = entiteRepository.findById(entiteCibleId)
            .orElseThrow(() -> new RuntimeException("Entité cible non trouvée"));

    // Récupérer l'utilisateur existant si un id est fourni
    Utilisateur utilisateur = null;
    if (utilisateurCible != null && utilisateurCible.getId() != null) {
        utilisateur = utilisateurRepository.findById(utilisateurCible.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur cible non trouvé"));
    }

    courrier.setEntite(entiteCible);
    courrier.setStatut(StatutCourrier.IMPUTER);

    // Affectation de l'utilisateur
    courrier.setUtilisateurAffecte(utilisateurCible);

    // ⚡ Initialisation automatique des dates et relances si null
    Date now = new Date();
    if (courrier.getDateReception() == null) {
        courrier.setDateReception(now);
    }
    if (courrier.getDateLimite() == null) {
        // dateLimite = dateReception + 7 jours
        courrier.setDateLimite(new Date(courrier.getDateReception().getTime() + 7L * 24 * 60 * 60 * 1000));
    }
    if (courrier.getDateRelance() == null) {
        // dateRelance = dateReception + 2 jours
        courrier.setDateRelance(new Date(courrier.getDateReception().getTime() + 2L * 24 * 60 * 60 * 1000));
    }
    // initialiser les flags
    courrier.setRappelEnvoye(false);
    courrier.setAlerteEnvoyee(false);

    courrierRepository.save(courrier);

    String commentaire = utilisateur != null
            ? "Courrier affecté à : " + utilisateur.getNom()
            : "Courrier imputé à " + entiteCible.getNom();

    // Enregistrer l'historique
    enregistrerHistorique(
            courrier,
            utilisateur,
            entiteCible,
            StatutCourrier.IMPUTER,
            commentaire
    );

    // Envoi email
    if (utilisateur != null && utilisateur.getEmail() != null) {
        emailService.sendSimpleEmail(
            utilisateur.getEmail(),
            "Nouveau courrier à traiter",
            "<p>Un courrier vous a été affecté : " + courrier.getObjet() + "</p>"
        );
    } else if (entiteCible.getResponsable() != null && entiteCible.getResponsable().getEmail() != null) {
        emailService.sendSimpleEmail(
            entiteCible.getResponsable().getEmail(),
            "Courrier imputé à votre département",
            "<p>Un courrier a été imputé à votre département : " + courrier.getObjet() + "</p>"
        );
    }

    return courrier;
}

    /* ======================================================
     *  PARTIE 3 : OUVERTURE / DÉBUT DE TRAITEMENT
     * ====================================================== */
    public ResponseEntity<InputStreamResource> ouvrirCourrier(Long courrierId, Utilisateur utilisateur)
        throws IOException {

    Courrier courrier = getCourrier(courrierId);

    File fichier = new File(courrier.getFichier());
    if (!fichier.exists()) {
        throw new RuntimeException("Fichier non trouvé");
    }


            enregistrerHistorique(
                    courrier,
                    utilisateur,
                    courrier.getEntite(),
                    StatutCourrier.EN_COURS,
                    "Courrier ouvert et en cours de traitement"
            );

    if (courrier.getStatut() == StatutCourrier.IMPUTER) {
        courrier.setStatut(StatutCourrier.EN_COURS);
        courrierRepository.save(courrier);

        enregistrerHistorique(
                courrier,
                utilisateur,
                courrier.getEntite(),
                StatutCourrier.EN_COURS,
                "Courrier téléchargé et en cours de traitement"
        );

        if (utilisateur.getEmail() != null) {
            emailService.sendSimpleEmail(
                    utilisateur.getEmail(),
                    "Courrier en cours de traitement",
                    "<p>Vous avez ouvert le courrier :</p>"
                            + "<p><b>Objet :</b> " + courrier.getObjet() + "</p>"

            );
        }
    }

    InputStreamResource resource =
            new InputStreamResource(new FileInputStream(fichier));

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fichier.getName() + "\"")
            .contentLength(fichier.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
}
    /* ======================================================
     *  PARTIE 4 : ARCHIVAGE
     * ====================================================== */
    public void archiverCourrier(Long courrierId, Utilisateur utilisateur) {

        Courrier courrier = getCourrier(courrierId);

        courrier.setStatut(StatutCourrier.ARCHIVER);
        courrier.setDateArchivage(new Date());
        courrierRepository.save(courrier);

        enregistrerHistorique(
                courrier,
                utilisateur,
                courrier.getEntite(),
                StatutCourrier.ARCHIVER,
                "Courrier archivé"
        );
      if (utilisateur.getEmail() != null) {
            emailService.sendSimpleEmail(
                    utilisateur.getEmail(),
                    "Courrier archivé",
                    "<p>Le courrier suivant a été archivé :</p>"
                    + "<p><b>Objet :</b> " + courrier.getObjet() + "</p>"
            );
        }
    }

    /* ======================================================
     *  PARTIE 5 : CONSULTATION
     * ====================================================== */
    //Afficher la liste des courrier Actifs

    public List<Courrier> courriersActifs(Long entiteId) {
        return courrierRepository.findByEntiteIdAndStatutNot(entiteId, StatutCourrier.ARCHIVER);
    }

    //Afficher la liste des courriers Archives

    public List<Courrier> courriersArchives(Long entiteId) {
        return courrierRepository.findByEntiteIdAndStatut(entiteId, StatutCourrier.ARCHIVER);
    }

    /* ======================================================
     *  PARTIE 6 : HISTORIQUE (CENTRALISÉ)
     * ====================================================== */
    private void enregistrerHistorique(
            Courrier courrier,
            Utilisateur utilisateurCible,
            Entite entiteCible,
            StatutCourrier statut,
            String commentaire
    ) {
        HistoriqueCourrier historique = new HistoriqueCourrier();
        historique.setCourrier(courrier);
        historique.setEntite(entiteCible);
        historique.setUtilisateur(utilisateurCible);
        historique.setStatut(statut);
        historique.setCommentaire(commentaire);
        historique.setDateAction(new Date());

        historiqueRepository.save(historique);
    }

    // =========================================
    // 7️⃣ Scheduler : rappels et alertes
    // =========================================

@Scheduled(cron = "0 0 9 * * ?") // tous les jours à 9h
public void verifierDelaisCourrier() {
    LocalDate today = LocalDate.now();

    List<Courrier> courriersActifs = courrierRepository.findByStatutNot(StatutCourrier.ARCHIVER);

    for (Courrier c : courriersActifs) {
        if (c.getEntite() == null || c.getEntite().getResponsable() == null) continue;

        String email = c.getEntite().getResponsable().getEmail();
        if (email == null) continue;

        LocalDate dateReception = c.getDateReception().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate dateLimite = c.getDateLimite() != null
                ? c.getDateLimite().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;

        long diffJours = ChronoUnit.DAYS.between(dateReception, today);

        // Rappel après 2 jours
        if (diffJours == 2 && !c.isRappelEnvoye()) {
            emailService.sendSimpleEmail(
                    email,
                    "Rappel : Courrier à traiter",
                    "<p>Le courrier " + c.getNumero() + " doit être traité.</p>"
            );
            c.setRappelEnvoye(true); // marque que le rappel a été envoyé
            courrierRepository.save(c);
            System.out.println("Rappel envoyé pour courrier " + c.getNumero());
        }

        // Alerte si date limite dépassée
        if (dateLimite != null && today.isAfter(dateLimite) && !c.isAlerteEnvoyee()) {
            emailService.sendSimpleEmail(
                    email,
                    "Alerte : Courrier en retard",
                    "<p>Le courrier " + c.getNumero() + " n'a pas été traité avant la date limite.</p>"
            );
            c.setAlerteEnvoyee(true); // marque que l'alerte a été envoyée
            courrierRepository.save(c);
            System.out.println("Alerte envoyée pour courrier " + c.getNumero());
        }
    }
}

    /* ======================================================
     *  MÉTHODES UTILITAIRES
     * ====================================================== */
    private Courrier getCourrier(Long id) {
        return courrierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Courrier non trouvé"));
    }

    private String sauvegarderFichier(MultipartFile fichier) throws IOException {

        if (fichier == null || fichier.isEmpty()) {
            return null;
        }
    
        // ✅ dossier racine externe (ex: /var/www/odc/uploads)
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    
        // ✅ création du dossier si inexistant
        Files.createDirectories(uploadPath);
    
        // ✅ sécuriser le nom du fichier
        String originalName = Paths.get(fichier.getOriginalFilename()).getFileName().toString();
        String nomFichier = System.currentTimeMillis() + "_" + originalName;
    
        Path destination = uploadPath.resolve(nomFichier);
    
        // ✅ écriture
        fichier.transferTo(destination.toFile());
    
        return destination.toString();
    }

 

 
}
