package at.scch.freiseisen.ma.data_layer.repository.process_mining;

import at.scch.freiseisen.ma.data_layer.dto.ProbDeclareInfo;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclare;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTrace;
import at.scch.freiseisen.ma.data_layer.entity.process_mining.ProbDeclareToTraceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProbDeclareToTraceRepository extends JpaRepository<ProbDeclareToTrace, ProbDeclareToTraceId> {


    @Query("SELECT DISTINCT pdt.probDeclare FROM ProbDeclareToTrace pdt WHERE pdt.trace.sourceFile = :sourceFile")
    List<ProbDeclare> findDistinctProbDeclareByTraceSourceFile(@Param("sourceFile") String sourceFile);
//    @Query("SELECT DISTINCT new at.scch.freiseisen.ma.data_layer.dto.ProbDeclareInfo(pdt.probDeclare.id, pdt.probDeclare.insertDate, pdt.probDeclare.updateDate, pdt.probDeclare.generating) " +
//           "FROM ProbDeclareToTrace pdt WHERE pdt.trace.sourceFile = :sourceFile")
//    List<ProbDeclareInfo> findDistinctProbDeclareInfoByTraceSourceFile(@Param("sourceFile") String sourceFile);
}
