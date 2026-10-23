package com.team.task_manager.service.algorithm.graph;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacencyMatrixGraph {
    private final Map<Long, Integer> vertexIndexes = new HashMap<>();
    private boolean[][] adjacencyMatrix = new boolean[0][0];

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
        adjacencyMatrix[fromVertexIndex][toVertexIndex] = true;
    }

    public boolean hasPath(Long startVertexId, Long targetVertexId) {
        Integer startVertexIndex = vertexIndexes.get(startVertexId);
        Integer targetVertexIndex = vertexIndexes.get(targetVertexId);
        if (startVertexIndex == null || targetVertexIndex == null) {
            return false;
        }

        ArrayDeque<Integer> verticesToVisit = new ArrayDeque<>();
        boolean[] visitedVertices = new boolean[adjacencyMatrix.length];
        verticesToVisit.push(startVertexIndex);

        while (!verticesToVisit.isEmpty()) {
            int currentVertexIndex = verticesToVisit.pop();
            if (currentVertexIndex == targetVertexIndex) {
                return true;
            }

            if (!visitedVertices[currentVertexIndex]) {
                visitedVertices[currentVertexIndex] = true;
                for (int adjacentVertexIndex = 0;
                     adjacentVertexIndex < adjacencyMatrix.length;
                     adjacentVertexIndex++) {
                    if (adjacencyMatrix[currentVertexIndex][adjacentVertexIndex]) {
                        verticesToVisit.push(adjacentVertexIndex);
                    }
                }
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

        boolean[][] expandedMatrix = new boolean[newVertexIndex + 1][newVertexIndex + 1];
        for (int row = 0; row < adjacencyMatrix.length; row++) {
            System.arraycopy(adjacencyMatrix[row], 0, expandedMatrix[row], 0, adjacencyMatrix.length);
        }
        adjacencyMatrix = expandedMatrix;

        return newVertexIndex;
    }
}
