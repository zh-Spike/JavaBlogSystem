package net.blog.dao;

import net.blog.pojo.Looper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoopDao extends JpaRepository<Looper, String>, JpaSpecificationExecutor<Looper> {
    Looper findOneById(String loopId);

    @Query(nativeQuery = true,value = "SELECT * FROM tb_looper WHERE state = ? ORDER BY create_time DESC")
    List<Looper> listLoopByState(String s);
}
