package com.odk.Controller;

import com.odk.Service.Interface.Service.ReportingHebdoService;
import com.odk.dto.ReportingHebdoActiviteDTO;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reporting-hebdo")
@AllArgsConstructor
public class ReportingHebdoController {

    private final ReportingHebdoService reportingHebdoService;

    @GetMapping("/activites")
    public ResponseEntity<List<ReportingHebdoActiviteDTO>> getActivitesHebdo(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                reportingHebdoService.getActivitesHebdo(date)
        );
    }

    // 🔹 API de test : renvoie toutes les activités sans filtre de date
    @GetMapping("/activites/test")
    public ResponseEntity<List<ReportingHebdoActiviteDTO>> getAllActivitesTest() {
        List<ReportingHebdoActiviteDTO> allActivites = reportingHebdoService.getAllActivites();
        return ResponseEntity.ok(allActivites);
    }
}
