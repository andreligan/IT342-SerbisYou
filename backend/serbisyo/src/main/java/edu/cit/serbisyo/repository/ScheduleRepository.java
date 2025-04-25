package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    // Update method to use providerId instead of id
    List<ScheduleEntity> findByServiceProviderProviderId(Long providerId);
    
    // Update method to use providerId instead of id
    List<ScheduleEntity> findByServiceProviderProviderIdAndDayOfWeekAndIsAvailableTrue(Long providerId, DayOfWeek dayOfWeek);
    
    // Add new method to find schedule by provider, day of week and time range
    List<ScheduleEntity> findByServiceProviderProviderIdAndDayOfWeekAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        Long providerId, DayOfWeek dayOfWeek, LocalTime time, LocalTime time2);
}
