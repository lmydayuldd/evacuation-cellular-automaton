package org.zetool.simulation.cellularautomaton;

import java.util.ArrayList;
import java.util.List;
import org.zetool.common.function.IntBiFunction;

/**
 * A cell matrix representing a finite rectangular area. The matrix allows fast index based access.
 *
 * @author Jan-Philipp Kappmeier
 * @param <E>
 * @param <S>
 */
public class FiniteCellMatrix<E extends Cell<E, S>, S> implements CellMatrix<E, S> {

    /** Manages the Cells into which the room is divided. */
    private final ArrayList2D<E> cells;

    protected FiniteCellMatrix(int width, int height) {
        cells = new ArrayList2D(width, height);
    }

    /**
     * Initializes the cell matrix and fills the entries with cells generated by a generator.
     *
     * @param width the width of the matrix
     * @param height the height of the matrix
     * @param cellGenerator a function that returns a cell for each coordinate
     */
    protected FiniteCellMatrix(int width, int height, IntBiFunction<E> cellGenerator) {
        this(width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                setCellInt(i, j, cellGenerator.apply(i, j));
            }
        }
    }

    public void populate(IntBiFunction<E> cellGenerator) {
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                setCellInt(i, j, cellGenerator.apply(i, j));
            }
        }
    }

    public void clear() {
        for (int i = 0; i < cells.getWidth(); i++) {
            for (int j = 0; j < cells.getHeight(); j++) {
                cells.set(i, j, null);
            }
        }
    }

    /**
     * Returns the number of cells on the x-axis of the room.
     *
     * @return The number of cells on the x-axis of the room.
     */
    @Override
    public int getWidth() {
        return cells.getWidth();
    }

    /**
     * Returns the number of cells on the y-axis of the room.
     *
     * @return The number of cells on the y-axis of the room.
     */
    @Override
    public int getHeight() {
        return cells.getHeight();
    }

    /**
     * Returns a list of all cells in the cell matrix.
     *
     * @return a list of all cells
     */
    @Override
    public List<E> getAllCells() {
        List<E> collectedCells = new ArrayList<>();
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                E cell = getCell(i, j);
                if( cell != null) {
                    collectedCells.add(cell);                
                }
            }
        }
        return collectedCells;
    }

    /**
     * Returns the cell referenced at position (x,y)
     *
     * @param x {@code x}-coordinate of the cell. 0 &lt;= x &lt;= width-1
     * @param y {@code y}-coordinate of the cell. 0 &lt;= y &lt;= height-1
     * @return The cell referenced at position {@code (x,y)}. If position {@code (x,y)} is empty
     * (in other words: does not reference any cell) {@code null} is returned.
     * @throws IllegalArgumentException if the {@code x}- or the {@code y}-parameter is out of bounds.
     */
    @Override
    public E getCell(int x, int y) {
        checkCoordinates(x, y);
        return cells.get(x, y);
    }

    public void setCell(int x, int y, E value) {
        checkCoordinates(x, y);
        setCellInt(x, y, value);
    }

    private void setCellInt(int x, int y, E value) {
        cells.set(x, y, value);
    }
    
    private void checkCoordinates(int x, int y) {
        if ((x < 0) || (x > cells.getWidth() - 1)) {
            throw new IllegalArgumentException("Invalid x-value: " + x);
        }
        if ((y < 0) || (y > cells.getHeight() - 1)) {
            throw new IllegalArgumentException("Invalid y-value: " + y);
        }
    }

    /**
     * Checks whether the cell at position (x,y) of the matrix exists or not. A cell is considered to exist if it is
     * inside the bounds of the (rectangular) matrix and the cell at the position is not {@code null}.
     *
     * @param x {@code x}-coordinate of the cell to be checked
     * @param y {@code y}-coordinate of the cell to be checked
     * @return {@code true}, if the cell at position {@code (x,y)} exists, {@code false}, if not
     */
    @Override
    public boolean existsCellAt(int x, int y) {
        if ((x < 0) || (x > cells.getWidth() - 1)) {
            return false;
        } else if ((y < 0) || (y > cells.getHeight() - 1)) {
            return false;
        } else {
            return this.getCell(x, y) != null;
        }
    }

    /**
     * Returns the number of cells contained in this room. The parameter {@code allCells} indicates wheather the number
     * of all cells is returned or the number of all cells that are not {@code null}. These cells can occur if there are
     * "holes" in the room.
     *
     * @param allCells indicates wheather all cells are counted, or not
     * @return the number of cells
     */
    public int getCellCount(boolean allCells) {
        int count = 0;
        if (allCells) {
            count = cells.getHeight() * cells.getWidth();
        } else {
            for (int i = 0; i < cells.getWidth(); i++) {
                for (int j = 0; j < cells.getHeight(); j++) {
                    if (cells.get(i, j) != null) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns the width and height
     */
    @Override
    public String toString() {
        return "width=" + getWidth() + ";height=" + getHeight();
    }
}
