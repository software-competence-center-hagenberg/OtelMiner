package at.scch.freiseisen.ma.data_layer.dto;

import java.util.List;

public record ProbDeclareModel(
        String id,
        List<ProbDeclareConstraint> constraints,
        boolean generating,
        boolean paused
) {
}
