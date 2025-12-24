package com.odk.Service.Interface.Service;

import com.odk.Entity.Activite;
import com.odk.Repository.ActiviteRepository;
import com.odk.dto.ReportingHebdoActiviteDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportingHebdoService {

    private final ActiviteRepository activiteRepository;

    public List<ReportingHebdoActiviteDTO> getActivitesHebdo(LocalDate referenceDate) {

        // Calcul du lundi et dimanche de la semaine
        LocalDate startWeek = referenceDate.with(DayOfWeek.MONDAY);
        LocalDate endWeek = referenceDate.with(DayOfWeek.SUNDAY);

        return activiteRepository.findAll().stream()
                .filter(a ->
                        a.getDateDebut() != null &&
                                isInWeek(a.getDateDebut(), startWeek, endWeek)
                )
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    public List<ReportingHebdoActiviteDTO> getAllActivites() {
        return activiteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    private boolean isInWeek(Date date, LocalDate start, LocalDate end) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return !localDate.isBefore(start) && !localDate.isAfter(end);
    }

    private ReportingHebdoActiviteDTO mapToDTO(Activite activite) {
        return new ReportingHebdoActiviteDTO(
                activite.getId(),
                activite.getNom(),
                activite.getTitre(),
                activite.getDateDebut(),
                activite.getDateFin(),
                activite.getStatut(),
                activite.getLieu(),
                activite.getDescription(),
                activite.getObjectifParticipation(),
                activite.getEntite(),
                null, // salleId supprimé ou remplacé par null
                activite.getCreatedBy() != null
                        ? activite.getCreatedBy().getNom() + " " + activite.getCreatedBy().getPrenom()
                        : null,
                activite.getTypeActivite()
        );
    }
}
