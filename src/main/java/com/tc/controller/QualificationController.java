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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Retrieve all qualifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The qualifications were retrieved") })
    @GetMapping("/qualifications")
    public ResponseEntity<List<QualificationResponse>> getAllQualifications() {
        var qualifications = qualificationRepository.findAll();
        var qualificationsResponse = qualifications.stream().map(qualification -> {
            return new QualificationResponse(qualification.getId(), qualification.getType());
        }).toList();
        return new ResponseEntity<>(qualificationsResponse, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve a qualification by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The qualification was retrieved"),
            @ApiResponse(responseCode = "404", description = "The qualification was not found") })
    @GetMapping("/qualifications/{id}")
    public ResponseEntity<QualificationResponse> getQualificationById(
            @Parameter(description = "the id of the qualification") @PathVariable("id") Long id) {
        var qualification = qualificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("qualification not found"));
        var response = new QualificationResponse(qualification.getId(), qualification.getType());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve qualifications of a driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The qualifications were retrieved"),
            @ApiResponse(responseCode = "404", description = "The driver was not found") })
    @GetMapping("/drivers/{driverId}/qualifications")
    public ResponseEntity<List<QualificationResponse>> getQualificationsByDriverId(
            @Parameter(description = "the id of the driver") @PathVariable("driverId") Long driverId) {
        var driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException("driver not found"));
        var qualifications = qualificationRepository.findQualificationsByDriversId(driver.getId());
        var qualificationsResponse = qualifications.stream().map(qualification -> {
            return new QualificationResponse(qualification.getId(), qualification.getType());
        }).toList();
        return new ResponseEntity<>(qualificationsResponse, HttpStatus.OK);
    }

    @Operation(summary = "Create a qualification for a driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The qualification was created"),
            @ApiResponse(responseCode = "400", description = "The request parameters were not valid"),
            @ApiResponse(responseCode = "404", description = "The driver was not found") })
    @PostMapping("/drivers/{driverId}/qualifications")
    public ResponseEntity<QualificationResponse> createQualification(
            @Parameter(description = "the id of the driver") @PathVariable("driverId") Long driverId,
            @Parameter(description = "the create parameters") @RequestBody @Valid CreateQualificationRequest request) {
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

    @Operation(summary = "Delete a qualification of a driver")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The qualification was removed from the driver's qualifications"),
            @ApiResponse(responseCode = "404", description = "The driver was not found") })
    @DeleteMapping("/drivers/{driverId}/qualifications/{qualificationId}")
    public ResponseEntity<HttpStatus> deleteQualification(
            @Parameter(description = "the id of the driver") @PathVariable("driverId") Long driverId,
            @Parameter(description = "the id of the qualification") @PathVariable("qualificationId") Long qualificationId) {
        var driver = driverRepository.findById(driverId).orElseThrow(() -> new NotFoundException("driver not found"));
        driver.removeQualification(qualificationId);
        driverRepository.save(driver);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
