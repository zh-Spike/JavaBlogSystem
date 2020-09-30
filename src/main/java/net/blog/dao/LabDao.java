package net.blog.dao;

import net.blog.pojo.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabDao extends JpaRepository<Lab, String>, JpaSpecificationExecutor<Lab> {
    Lab findOneById(String id);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE tb_lab SET state = 0 WHERE id = ? ")
    int deleteLabByUpdateState(String labId);

    @Query(nativeQuery = true, value = "SELECT * FROM tb_lab WHERE state = ? ORDER BY create_time DESC")
    List<Lab> listLabByState(String state);
}
