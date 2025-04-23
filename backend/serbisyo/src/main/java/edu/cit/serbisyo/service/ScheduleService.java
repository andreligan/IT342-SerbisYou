package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.ScheduleEntity;
import edu.cit.serbisyo.entity.ServiceProviderEntity;
import edu.cit.serbisyo.repository.ScheduleRepository;
import edu.cit.serbisyo.repository.ServiceProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    // Create a new schedule entry
    public ScheduleEntity createSchedule(Long providerId, ScheduleEntity scheduleDetails) {
        ServiceProviderEntity provider = serviceProviderRepository.findById(providerId)
                .orElseThrow(() -> new NoSuchElementException("Service Provider with ID " + providerId + " not found"));
        
        scheduleDetails.setServiceProvider(provider);
        return scheduleRepository.save(scheduleDetails);
    }
    
    // Get all schedules for a provider
    public List<ScheduleEntity> getSchedulesByProviderId(Long providerId) {
        if (!serviceProviderRepository.existsById(providerId)) {
            throw new NoSuchElementException("Service Provider with ID " + providerId + " not found");
        }
        // Use the updated method name
        return scheduleRepository.findByServiceProviderProviderId(providerId);
    }
    
    // Get available schedules for a specific day
    public List<ScheduleEntity> getAvailableSchedulesForDay(Long providerId, DayOfWeek dayOfWeek) {
        if (!serviceProviderRepository.existsById(providerId)) {
            throw new NoSuchElementException("Service Provider with ID " + providerId + " not found");
        }
        // Use the updated method name
        return scheduleRepository.findByServiceProviderProviderIdAndDayOfWeekAndIsAvailableTrue(providerId, dayOfWeek);
    }
    
    // Update a schedule
    public ScheduleEntity updateSchedule(Long scheduleId, ScheduleEntity updatedSchedule) {
        ScheduleEntity existingSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Schedule with ID " + scheduleId + " not found"));
        
        existingSchedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
        existingSchedule.setStartTime(updatedSchedule.getStartTime());
        existingSchedule.setEndTime(updatedSchedule.getEndTime());
        existingSchedule.setAvailable(updatedSchedule.isAvailable());
        
        return scheduleRepository.save(existingSchedule);
    }
    
    // Delete a schedule
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new NoSuchElementException("Schedule with ID " + scheduleId + " not found");
        }
        scheduleRepository.deleteById(scheduleId);
    }
    
    // Check if a service provider is available at a specific date and time
    public boolean isProviderAvailable(Long providerId, LocalDate date, String time) {
        // Implementation would check if the provider has availability for the given day and time
        // and also check if there are no conflicting bookings
        
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<ScheduleEntity> availableSchedules = scheduleRepository
                .findByServiceProviderProviderIdAndDayOfWeekAndIsAvailableTrue(providerId, dayOfWeek);
        
        // Further implementation to check if the specific time falls within available hours
        // and if there are no existing bookings at that time
        
        return !availableSchedules.isEmpty();
    }
}
