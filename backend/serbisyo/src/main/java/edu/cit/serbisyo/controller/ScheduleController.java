package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.ScheduleEntity;
import edu.cit.serbisyo.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping("/provider/{providerId}")
    public ResponseEntity<?> createSchedule(@PathVariable Long providerId, @RequestBody ScheduleEntity schedule) {
        try {
            ScheduleEntity createdSchedule = scheduleService.createSchedule(providerId, schedule);
            return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getProviderSchedules(@PathVariable Long providerId) {
        try {
            List<ScheduleEntity> schedules = scheduleService.getSchedulesByProviderId(providerId);
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/provider/{providerId}/day/{dayOfWeek}")
    public ResponseEntity<?> getProviderSchedulesByDay(
            @PathVariable Long providerId,
            @PathVariable DayOfWeek dayOfWeek) {
        try {
            List<ScheduleEntity> schedules = scheduleService.getAvailableSchedulesForDay(providerId, dayOfWeek);
            return new ResponseEntity<>(schedules, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleEntity schedule) {
        try {
            ScheduleEntity updatedSchedule = scheduleService.updateSchedule(scheduleId, schedule);
            return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            return new ResponseEntity<>("Schedule deleted successfully", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String time) {
        try {
            boolean isAvailable = scheduleService.isProviderAvailable(providerId, date, time);
            return new ResponseEntity<>(isAvailable, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
