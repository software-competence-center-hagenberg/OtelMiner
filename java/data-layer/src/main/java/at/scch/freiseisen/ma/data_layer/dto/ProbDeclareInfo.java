package at.scch.freiseisen.ma.data_layer.dto;

import java.time.LocalDateTime;

public record ProbDeclareInfo(String id, LocalDateTime insertDate, LocalDateTime updateDate, boolean generating) {
}
