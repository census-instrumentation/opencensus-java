/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.example.gameoflife;

/**
 * This class implements Conway's Game of Life. The Game of Life consists of a matrix of cells that
 * are either alive or dead. The next generation is calculated from the current generation based on
 * the following rules:
 *
 * <ol>
 *   <li>If a cell is alive in the current generation, it will be alive in the next generation iff
 *       it has less than 2 live neighbors or more than 3 live neighbors in the current generation.
 *   <li>If a cell is dead in the current generation, it will be alive in the next generation iff it
 *       has exactly 3 live neighbors in the current generation.
 * </ol>
 *
 * This implementation is restricted to square matricies.
 *
 * <p>
 */
final class GameOfLife {
  // Square matrix representation of the current generation - a live cell is
  // represented by 'true' and a dead cell by 'false'.
  private final boolean[][] generation;

  // Dimension of the square matrix.
  final int dimension;

  /**
   * Creates an instance of the GameOfLife with the current generation initalized to the input
   * layout String. The layout is assumed to be in row-major order with character '0' representing
   * false and any other character (typically '1') representing true.
   *
   * <p>Note that if the length of the input is not a square, the dimensions of the generation is
   * set to 0.
   *
   * @param layout Specification of the initial generation.
   * @return instance with the current generation initialized as specified.
   */
  public GameOfLife(int dimension, String layout) {
    this.dimension = dimension;
    generation = new boolean[dimension][dimension];
    int index = 0;
    for (int row = 0; row < dimension; ++row) {
      for (int col = 0; col < dimension; ++col) {
        generation[row][col] = toBool(layout.charAt(index));
        ++index;
      }
    }
  }

  // Creates an instance of the GameOfLife with the specified layout and dimension.
  // Assumes that 'generation' is a square matrix of dimension 'dimension'.
  private GameOfLife(boolean[][] generation, int dimension) {
    this.dimension = dimension;
    this.generation = generation;
  }

  /**
   * Encodes the current generation as a string in row-major order with character '1' to
   * representing true and '0' to representing false.
   *
   * @return String encoding of the current generation.
   */
  public String encode() {
    StringBuilder result = new StringBuilder(dimension * dimension);
    for (int row = 0; row < dimension; ++row) {
      for (int col = 0; col < dimension; ++col) {
        result.append(generation[row][col] ? "1" : "0");
      }
    }
    return result.toString();
  }

  /**
   * Updates current generation the input number of times.
   *
   * @param gens the number of generations to calculate.
   */
  public GameOfLife calcNextGenerations(int gens) {
    GameOfLife result = this;
    for (int i = 0; i < gens; ++i) {
      result = result.calcNextGeneration();
    }
    return result;
  }

  // Calculates the next generation based on the current generation then updates
  // the current generation.
  private GameOfLife calcNextGeneration() {
    boolean[][] nextGeneration = new boolean[dimension][dimension];
    // Calculates the value of each cell in the next generation based
    // on it's direct neighbors in the current generation.
    for (int row = 0; row < dimension; ++row) {
      for (int col = 0; col < dimension; ++col) {
        nextGeneration[row][col] = calcCell(row, col);
      }
    }
    return new GameOfLife(nextGeneration, dimension);
  }

  // Calculates whether the cell specified by the input row and column is dead
  // or alive in the next generation based on it's neighbors in the current
  // generation. The rules are:
  //
  // 1. If the cell is alive in the current generation, it will be alive in the
  //    next generation iff it has less than 2 live neighbors or more than 3
  //    live neighbors in the current generation.
  //
  // 2. If the cell is dead in the current generation, it will be alive in the
  //    next generation iff it has exactly 3 live neighbors in the current
  //    generation.
  //
  // Note that this calculation uses wrap-around when counting neighbors such
  // that the bottom row uses the top row (and vice-versa) and the left-most
  // column use the right-most column (and vice-versa) as neighbors.
  private boolean calcCell(int row, int col) {
    int liveNeighbors = 0;
    // This loop would range from -1 to 1 except that Java's modulo operator
    // returns a value with the same sign as the numerator, so 'dimension' is
    // added to ensure that the numerator is always positive.
    for (int rowX = dimension - 1; rowX < dimension + 2; ++rowX) {
      for (int colX = dimension - 1; colX < dimension + 2; ++colX) {
        if (generation[(row + rowX) % dimension][(col + colX) % dimension]) {
          liveNeighbors++;
        }
      }
    }
    if (generation[row][col]) {
      liveNeighbors--;
      if (liveNeighbors < 2 || liveNeighbors > 3) {
        return false;
      }
      return true;
    } else {
      if (liveNeighbors == 3) {
        return true;
      }
      return false;
    }
  }

  private static boolean toBool(char c) {
    return c != '0';
  }
}
