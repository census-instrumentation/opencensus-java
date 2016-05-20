/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census.examples.gol;

/**
 * This class implements Conway's Game of Life. The Game of Life consists of
 * a matrix of cells that are either alive or dead. The next generation is
 * calculated from the current generation based on the following rules:
 * <ol>
 * <li> If a cell is alive in the current generation, it will be alive in the
 *      next generation iff it has less than 2 live neighbors or more than 3
 *      live neighbors in the current generation.
 * <li> If a cell is dead in the current generation, it will be alive in the
 *     next generation iff it has exactly 3 live neighbors in the current
 *      generation.
 * </ol>
 * This implementation is restricted to square matricies.
 * <p>
 * @author dpo@google.com (Dino Oliva)
 */
public class GameOfLife {
  // Square matrix representation of the current generation - a live cell is
  // represented by 'true' and a dead cell by 'false'.
  private final boolean[][] generation;

  // Dimension of the square matrix.
  final int dimension;

  /**
   * Creates an instance of the GameOfLife with the current generation
   * initalized to the input layout String. The layout is assumed to be in
   * row-major order with character '0' representing false and any other
   * character (typically '1') representing true.
   * <p>
   * Note that if the length of the input is not a square, the dimensions
   * of the generation is set to 0.
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
   * Encodes the current generation as a string in row-major order with
   * character '1' to representing true and '0' to representing false.
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
