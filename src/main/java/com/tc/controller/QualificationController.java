package com.tc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tc.model.Qualification;
import com.tc.repository.DriverRepository;
import com.tc.repository.QualificationRepository;
import com.tc.request.CreateQualificationRequest;
import com.tc.response.QualificationResponse;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Qualification")
@RestController
@RequestMapping("/api")
public class QualificationController {
    private final QualificationRepository qualificationRepository;
    private final DriverRepository driverRepository;

    public QualificationController(DriverRepository driverRepository, QualificationRepository qualificationRepository) {
        this.driverRepository = driverRepository;
        this.qualificationRepository = qualificationRepository;
    }

    @GetMapping("/qualifications")
    public ResponseEntity<List<QualificationResponse>> getAllQualifications() {
        var qualifications = qualificationRepository.findAll();
        var qualificationsResponse = qualifications.stream().map(qualification -> {
            return new QualificationResponse(qualification.getId(), qualification.getType());
        }).toList();
        return new ResponseEntity<>(qualificationsResponse, HttpStatus.OK);
    }

    @GetMapping("/qualifications/{id}")
    public ResponseEntity<QualificationResponse> getQualificationById(@PathVariable("id") Long id) {
        try {
            var qualificationOpt = qualificationRepository.findById(id);

            if (!qualificationOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var qualification = qualificationOpt.get();
            var response = new QualificationResponse(qualification.getId(), qualification.getType());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/drivers/{driverId}/qualifications")
    public ResponseEntity<List<QualificationResponse>> getQualificationsByDriverId(
            @PathVariable("driverId") Long driverId) {
        var driver = driverRepository.findById(driverId);
        if (!driver.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var qualifications = qualificationRepository.findQualificationsByDriversId(driverId);
        var qualificationsResponse = qualifications.stream().map(qualification -> {
            return new QualificationResponse(qualification.getId(), qualification.getType());
        }).toList();
        return new ResponseEntity<>(qualificationsResponse, HttpStatus.OK);
    }

    @PostMapping("/drivers/{driverId}/qualifications")
    public ResponseEntity<QualificationResponse> createQualification(@PathVariable("driverId") Long driverId,
            @RequestBody CreateQualificationRequest request) {
        try {
            var driverOpt = driverRepository.findById(driverId);
            if (!driverOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            var driver = driverOpt.get();
            var qualificationId = request.id();
            if (qualificationId != null && qualificationId != 0) {
                var qualificationOpt = qualificationRepository.findById(qualificationId);
                if (!qualificationOpt.isPresent()) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                var qualification = qualificationOpt.get();
                driver.addQualification(qualification);
                driverRepository.save(driver);
                var response = new QualificationResponse(qualification.getId(), qualification.getType());
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            }

            var qualification = new Qualification(request.type());
            driver.addQualification(qualification);
            qualificationRepository.save(qualification);
            var response = new QualificationResponse(qualification.getId(), qualification.getType());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/drivers/{driverId}/qualifications/{qualificationId}")
    public ResponseEntity<HttpStatus> deleteQualification(@PathVariable("driverId") Long driverId,
            @PathVariable("qualificationId") Long qualificationId) {
        try {
            var driverOpt = driverRepository.findById(driverId);
            if (!driverOpt.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            var driver = driverOpt.get();
            driver.removeQualification(qualificationId);
            driverRepository.save(driver);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
