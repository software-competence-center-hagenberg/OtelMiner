package at.scch.freiseisen.ma.data_layer.repository.pre_processing;

import at.scch.freiseisen.ma.data_layer.entity.pre_processing.CanonizedSpanTree;
import at.scch.freiseisen.ma.data_layer.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CanonizedSpanTreeRepository extends BaseRepository<CanonizedSpanTree, String> {
}
