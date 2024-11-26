package at.scch.freiseisen.ma.data_layer.repository.process_mining;

import at.scch.freiseisen.ma.data_layer.entity.process_mining.Declare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeclareRepository extends BaseRepository<Declare, String> {

    List<Declare> findAllByConstraintTemplateInAndProbDeclare(List<String> constraintTemplates, ProbDeclare probDeclare);
}
