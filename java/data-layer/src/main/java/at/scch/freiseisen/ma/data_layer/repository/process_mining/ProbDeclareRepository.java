package at.scch.freiseisen.ma.data_layer.repository.process_mining;

import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareInfo;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProbDeclareRepository extends BaseRepository<ProbDeclare, String> {
}
