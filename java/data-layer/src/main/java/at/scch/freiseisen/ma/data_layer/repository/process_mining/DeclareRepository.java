package at.scch.freiseisen.ma.data_layer.repository.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.DeclareId;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeclareRepository extends JpaRepository<Declare, DeclareId> {

    List<Declare> findAllByProbDeclare(ProbDeclare probDeclare);
}
