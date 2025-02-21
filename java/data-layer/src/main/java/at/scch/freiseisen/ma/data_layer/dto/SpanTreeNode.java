package at.scch.freiseisen.ma.data_layer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SpanTreeNode {
    // type span_tree_node = { span : Trace.span; children : span_tree_node list }

    private MinimalSpan span;

    private List<SpanTreeNode> children;
}
