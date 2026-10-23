package com.team.task_manager.service.algorithm.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacencyMatrixGraph {
    private final Map<Long, Integer> vertexIndexes = new HashMap<>();
    private final List<List<Boolean>> adjacencyMatrix = new ArrayList<>();

    public AdjacencyMatrixGraph(List<List<Long>> edges) {
        for (List<Long> edge : edges) {
            if (edge == null || edge.size() != 2 || edge.get(0) == null || edge.get(1) == null) {
                throw new IllegalArgumentException("Edge must contain exactly two vertex identifiers");
            }
            addEdge(edge.get(0), edge.get(1));
        }
    }

    private void addEdge(Long fromVertexId, Long toVertexId) {
        int fromVertexIndex = addVertexIfMissing(fromVertexId);
        int toVertexIndex = addVertexIfMissing(toVertexId);
        adjacencyMatrix.get(fromVertexIndex).set(toVertexIndex, true);
    }

    public boolean hasPath(Long startVertexId, Long targetVertexId) {
        Integer startVertexIndex = vertexIndexes.get(startVertexId);
        Integer targetVertexIndex = vertexIndexes.get(targetVertexId);
        if (startVertexIndex == null || targetVertexIndex == null) {
            return false;
        }

        return dfs(startVertexIndex, targetVertexIndex, new boolean[adjacencyMatrix.size()]);
    }

    private boolean dfs(int currentVertexIndex, int targetVertexIndex, boolean[] visitedVertices) {
        if (currentVertexIndex == targetVertexIndex) {
            return true;
        }
        if (visitedVertices[currentVertexIndex]) {
            return false;
        }

        visitedVertices[currentVertexIndex] = true;
        for (int adjacentVertexIndex = 0; adjacentVertexIndex < adjacencyMatrix.size(); adjacentVertexIndex++) {
            if (adjacencyMatrix.get(currentVertexIndex).get(adjacentVertexIndex)
                    && dfs(adjacentVertexIndex, targetVertexIndex, visitedVertices)) {
                return true;
            }
        }
        return false;
    }

    private int addVertexIfMissing(Long vertexId) {
        Integer existingIndex = vertexIndexes.get(vertexId);
        if (existingIndex != null) {
            return existingIndex;
        }

        int newVertexIndex = vertexIndexes.size();
        vertexIndexes.put(vertexId, newVertexIndex);

        for (List<Boolean> row : adjacencyMatrix) {
            row.add(false);
        }

        List<Boolean> newRow = new ArrayList<>();
        for (int column = 0; column <= newVertexIndex; column++) {
            newRow.add(false);
        }
        adjacencyMatrix.add(newRow);

        return newVertexIndex;
    }
}
