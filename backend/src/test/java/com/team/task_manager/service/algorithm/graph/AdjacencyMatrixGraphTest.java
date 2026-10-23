package com.team.task_manager.service.algorithm.graph;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdjacencyMatrixGraphTest {

    @Test
    void findsPathBetweenVertices() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(List.of(
                List.of(1L, 2L),
                List.of(2L, 3L)
        ));

        assertTrue(graph.hasPath(1L, 3L));
    }

    @Test
    void returnsFalseWhenVerticesAreNotConnected() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(List.of(
                List.of(1L, 2L),
                List.of(3L, 4L)
        ));

        assertFalse(graph.hasPath(1L, 4L));
    }

    @Test
    void handlesExistingCycleWithoutInfiniteTraversal() {
        AdjacencyMatrixGraph graph = new AdjacencyMatrixGraph(List.of(
                List.of(1L, 2L),
                List.of(2L, 1L)
        ));

        assertFalse(graph.hasPath(1L, 3L));
    }

    @Test
    void rejectsEdgeWithoutTwoVertexIdentifiers() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AdjacencyMatrixGraph(List.of(List.of(1L)))
        );
    }
}
