package com.tc.controller;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tc.exception.NotFoundException;
import com.tc.model.Qualification;
import com.tc.repository.DriverRepository;
import com.tc.repository.QualificationRepository;
import com.tc.request.CreateQualificationRequest;
import com.tc.response.QualificationResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
        var qualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("qualification not found"));
        var response = new QualificationResponse(qualification.getId(), qualification.getType());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/drivers/{driverId}/qualifications")
    public ResponseEntity<List<QualificationResponse>> getQualificationsByDriverId(
            @PathVariable("driverId") Long driverId) {
        var driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException("driver not found"));
        var qualifications = qualificationRepository.findQualificationsByDriversId(driver.getId());
        var qualificationsResponse = qualifications.stream().map(qualification -> {
            return new QualificationResponse(qualification.getId(), qualification.getType());
        }).toList();
        return new ResponseEntity<>(qualificationsResponse, HttpStatus.OK);
    }

    @PostMapping("/drivers/{driverId}/qualifications")
    public ResponseEntity<QualificationResponse> createQualification(@PathVariable("driverId") Long driverId,
            @RequestBody @Valid CreateQualificationRequest request) {
        var driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("driver not found"));
        var qualificationType = request.type;
        Qualification exQualification = new Qualification(qualificationType);
        Example<Qualification> example = Example.of(exQualification);

        var qualificationOpt = qualificationRepository.findOne(example);
        if (qualificationOpt.isPresent()) {
            var qualification = qualificationOpt.get();
            driver.addQualification(qualification);
            driverRepository.save(driver);
            var response = new QualificationResponse(qualification.getId(), qualification.getType());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        var qualification = new Qualification(qualificationType);
        driver.addQualification(qualification);
        qualificationRepository.save(qualification);
        var response = new QualificationResponse(qualification.getId(), qualification.getType());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/drivers/{driverId}/qualifications/{qualificationId}")
    public ResponseEntity<HttpStatus> deleteQualification(@PathVariable("driverId") Long driverId,
            @PathVariable("qualificationId") Long qualificationId) {
        var driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException("driver not found"));
        driver.removeQualification(qualificationId);
        driverRepository.save(driver);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
