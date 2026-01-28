package me.stky.relaytd.api.repository;

import me.stky.relaytd.api.model.Astre;
import me.stky.relaytd.api.model.AstreID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AstreRepository extends JpaRepository<Astre, AstreID> {


    //

    @Query("SELECT a FROM Astre a WHERE a.astreID.type = :type")
    List<Astre> findByType(@Param("type") String type);

    @Query("SELECT a FROM Astre a WHERE a.astreID.name = :name")
    List<Astre> findByName(@Param("name") String name);
}

