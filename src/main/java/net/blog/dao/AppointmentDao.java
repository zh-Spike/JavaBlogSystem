package net.blog.dao;

import net.blog.pojo.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppointmentDao extends JpaRepository<Appointment, String>, JpaSpecificationExecutor<Appointment> {
    Appointment findOneById(String id);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_appointment SET state = 0 WHERE id = ? ")
    int deleteAppointmentByUpdateState(String AppointmentId);

    @Query(nativeQuery = true, value = "SELECT * FROM tb_appointment WHERE state = ? ORDER BY create_time DESC")
    List<Appointment> listAppointmentByState(String state);
}
