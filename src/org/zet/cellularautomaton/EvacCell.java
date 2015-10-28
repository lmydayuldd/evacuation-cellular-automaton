/* zet evacuation tool copyright (c) 2007-15 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zet.cellularautomaton;

import org.zetool.common.util.Direction8;
import org.zetool.common.util.Level;
import org.zetool.simulation.cellularautomaton.SquareCell;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * The Cellular Automaton devides a room into quadratic cells.
 * This abstract class "EvacCell" describes such a cell, which is a part of the room.
 * It is kept abstract, because there are several special kinds of cells, such as
 * door cells, stair cells and exit cells.
 * Generally each cell can be occupied by an individual and can be crossed with a
 * certain speed.
 * @author Marcel Preuß
 * @author Jan-Philipp Kappmeier
 *
 */
public abstract class EvacCell extends SquareCell<EvacCell,EvacuationCellState> implements Comparable<EvacCell> {
    /** This character is used for graphic-like ASCII-output.  */
    protected char graphicalRepresentation = ' ';
    /** Defines the Speed-Factor of the EvacCell. I.e. a value, how fast this cell can be crossed. */
    protected double speedFactor;
    /** Manages the individual that occupies the cell. */
    //protected Individual individual;
    /** The room to which the cell belongs. */
    protected Room room;
    /** The bounds of the  cell. */
    protected EnumSet<Direction8> bounds;
    /** Tells whether the surrounding squares are higher, equal or lower. */
    protected EnumMap<Direction8, Level> levels;
    /** Stores the hashCode of this cell.  */
    protected int hash;
    /** The time up to which the cell is blocked by an individuum (even if it is no longer set to the cell). */
    protected double occupiedUntil = 0;

    /**
     * Constructor defining the values of individual and speedFactor.
     *
     * @param state the state of the ca.
     * @param speedFactor Defines how fast the cell can be crossed. The value should be a rational number greater than
     * or equal to 0 and smaller or equal to 1. Otherwise the standard value of the specific class which inherits from
     * EvacCell is set.
     * @param x x-coordinate of the cell in the room
     * @param y y-coordinate of the cell in the room
     */
    public EvacCell(EvacuationCellState state, double speedFactor, int x, int y) {
        this(state, speedFactor, x, y, null);
    }

    public EvacCell(EvacuationCellState state, double speedFactor, int x, int y, Room room) {
        super(state, x, y, room);
        //this.individual = individual;
        this.setSpeedFactor(speedFactor); // TODO

        // Must be in this order
        setRoom(room);

        this.bounds = EnumSet.noneOf(Direction8.class);
        this.levels = new EnumMap<>(Direction8.class);
    }

    /**
     * Returns the individual that currently occupies the cell.
     *
     * @return The individual that occupies the cell.
     */
    public Individual getIndividual() {
        return getState().getIndividual();
    }

    /**
     * Sets an individual on the cell. It is <b>not</b> automatically removed from its source cell nor are rooms or any
     * lists updated.
     *
     * @param individual The individual occupying the cell from now on. Set this value to "null" in order to mark this
     * cell as not occupied.
     */
    void setIndividual(Individual i) {
        if (getIndividual() != null && !i.equals(getIndividual())) {
            throw new java.lang.IllegalStateException("Individual was already set!");
        }
        if (i == null) {
            throw new java.lang.NullPointerException("Individual is null.");
        }
        getState().setIndividual(i);
    }

    /**
     * Swaps individuals and ignores the already occupied-check.
     *
     * @param ce
     */
    void swapIndividuals(EvacCell ce) {
        EvacCell c1 = this;
        EvacCell c2 = ce;

        Individual c1i = c1.getIndividual();
        Individual c2i = c2.getIndividual();
        if (c1i == null) {
            throw new java.lang.NullPointerException("Individual on cell " + c1 + " is null.");
        }
        if (c2i == null) {
            throw new java.lang.NullPointerException("Individual on cell " + c2 + " is null.");
        }
        c1.getState().setIndividual(c2i);
        c2.getState().setIndividual(c1i);
        c1i.setCell(c2);
        c2i.setCell(c1);
    }

    void removeIndividual() {
        getState().setIndividual(null);
    }

    /**
     * Returns the Speed-Factor of the cell.
     *
     * @return The Speed-Factor of the cell.
     */
    public double getSpeedFactor() {
        return speedFactor;
    }

    /**
     * Changes the Speed-Factor of the EvacCell to the specified value. This method is kept abstract because the
     * standard values may differ in the specific classes which inherit from EvacCell.
     *
     * @param speedFactor Defines how fast the cell can be crossed. The value should be a rational number greater than
     * or equal to 0 and smaller or equal to 1. Otherwise the standard value of the specific class which inherits from
     * EvacCell is set.
     */
    public abstract void setSpeedFactor(double speedFactor);

    /**
     * Returns all existing direct-neighbour-cells that are reachable of this cell
     *
     * @return ArrayList of direct-neighbour-cells of "cell"
     */
    public ArrayList<EvacCell> getNeighbours() {
        return getNeighbours(true, false);
    }

    /**
     * Returns all existing direct-neighbour-cells cell (even those that are not reachable).
     *
     * @return ArrayList of direct-neighbour-cells of "cell"
     */
    @Override
    public Collection<EvacCell> getDirectNeighbors() {
        return getNeighbours(false, false);
    }

    /**
     * Returns a list of all free neighbour cells.
     *
     * @return a list of all free neighbour cells
     */
    public ArrayList<EvacCell> getFreeNeighbours() {
        return getNeighbours(true, true);
    }

    /**
     * Returns the x-coordinate of the cell.
     *
     * @return The x-coordinate of the cell.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Returns the y-coordinate of the cell.
     *
     * @return The y-coordinate of the cell.
     */
    public int getY() {
        return this.y;
    }

    public int getAbsoluteX() {
        return this.x + room.getXOffset();
    }

    public int getAbsoluteY() {
        return this.y + room.getYOffset();
    }

    /**
     * Returns the room to which the cell belongs
     *
     * @return The room to which the cell belongs
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Manages the room to which the cell belongs. This method can only be called by classes belonging to the same
     * package in order to prevent misuse. It should only be called by Room.add(cell) in order so set the room
     * corresponding to the cell automatically.
     *
     * @param room The room to which the cell belongs
     */
    final void setRoom(Room room) {
        this.room = room;

        String s = ((room != null) ? room.getID() : "") + "-" + y + "-" + x;
        hash = s.hashCode();
    }

    /**
     * Specifies the level difference between this cell and the cell at the relative position {@code relPosition}.
     *
     * @param relPosition The relative position of the wished neighbour cell.
     * @param level The level of the other cell according to this cell, can be higher, equal or lower.
     */
    public void setLevel(Direction8 relPosition, Level level) {
        if (this.room.existsCellAt(x + relPosition.xOffset(), y + relPosition.yOffset())) {
            this.room.getCell(x + relPosition.xOffset(),
                    y + relPosition.yOffset()).internalSetLevel(relPosition.invert(), level.getInverse());
        }

        internalSetLevel(relPosition, level);

    }

    public void internalSetLevel(Direction8 relPosition, Level level) {
        levels.put(relPosition, level);
    }

    /**
     * Specifies that this cell is separated from one of its neighbour cells by an unpenetrable bound and that thus this
     * neighbour cell cannot be directly reached from this cell.
     *
     * @param relPosition The relative position of the unreachable neighbour.
     */
    public void setUnPassable(Direction8 relPosition) {
        if (this.room.existsCellAt(x + relPosition.xOffset(), y + relPosition.yOffset())) {
            this.room.getCell(x + relPosition.xOffset(),
                    y + relPosition.yOffset()).internalSetUnPassable(relPosition.invert());
        }

        internalSetUnPassable(relPosition);
    }

    private void internalSetUnPassable(Direction8 relPosition) {
        bounds.add(relPosition);
    }

    /**
     * Specifies that the way from this cell to one of its neighbour cells is clear.
     *
     * @param relPosition The relative position of the neighbour cell.
     */
    public void setPassable(Direction8 relPosition) {
        if (this.room.existsCellAt(x + relPosition.xOffset(), y + relPosition.yOffset())) {
            this.room.getCell(x + relPosition.xOffset(),
                    y + relPosition.yOffset()).internalSetPassable(relPosition.invert());
        }
        internalSetPassable(relPosition);
    }

    private void internalSetPassable(Direction8 relPosition) {
        bounds.remove(relPosition);
    }

    /**
     * Asks whether the direct way to a neighbour cell of this cell is clear.
     *
     * @param relPosition The relative position of the neighbour cell
     * @return {@code true} if the way is clear or {@code false} if the way is blocked.
     */
    public boolean isPassable(Direction8 relPosition) {
        return !bounds.contains(relPosition);
    }

    /**
     * Returns the level difference between this cell and the cell at the relative position {@code relPosition}. If the
     * level has never been set explicitly, Equal is returned.
     *
     * @param direction The square in this direction is considered.
     * @return the level of the square in direction {@code direction} (higher, equal or lower).
     */
    public Level getLevel(Direction8 direction) {
        return levels.containsKey(direction) ? levels.get(direction) : Level.Equal;
    }

    @Override
    /**
     * Returns a copy of itself as a new Object.
     */
    public abstract EvacCell clone();

    public EvacCell clone(boolean cloneIndividual) {
        return clone();
    }

    // TODO?
//  HashCode und Equals auskommentiert: Wenn zwei Zellen schon gleich sind,
//  wenn sie im gleichen Raum liegen und die gleichen Koordinaten haben,
//  verlieren wir die MÃ¶glichkeit, Zellen zu Klonen und in einer HashMap
//  Klone auf ihre Originale abzubilden. Dies wird an mehreren Stellen
//  benoetigt. Die oben beschriebene Gleichheit wird nirgendwo benutzt und
//  war fuer mehrere Bugs verantwortlich.
//
//

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EvacCell)) {
            return false;
        }

        EvacCell c = (EvacCell) obj;

        if (c.getRoom() == null && this.getRoom() == null) {
            return (c == this);
        }

        if (c.getRoom() == null || this.getRoom() == null) {
            return false;
        }

        if (this.room.equals(c.getRoom()) && this.x == c.getX() && this.y == c.getY()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // This result must be brought independently of the current value of the
        // room variable because this obejct must be able to be serialized using
        // XStream, and this causes an error because it invokes this method before
        // the room field is filled with its former content again.
        return hash;
    }

    /**
     * Returns the type of the cell (D for door-, E for exit-, R for room-, S for save-, T for stair-cell), coordinates
     * of the cell, the speedfactor, if it's occupied and its room
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + "),speedfactor=" + speedFactor + ";is occupied="
                + (getState().getIndividual() == null ? "false;" : "true;") + "id=" + hashCode() + " R: " + room + ";";
    }

    /**
     * A string representation that only consists of the coordinates.
     *
     * @return a tupel of the {@code x} and {@code y} coordinates.
     */
    public String coordToString() {
        return "(" + x + "," + y + ")";
    }

    @SuppressWarnings("fallthrough")
    protected ArrayList<EvacCell> getNeighbours(boolean passableOnly, boolean freeOnly) {
        ArrayList<EvacCell> neighbours = new ArrayList<>();
        Room cellRoom = this.getRoom();
        for (Direction8 direction : Direction8.values()) {
            int cellx = this.getX() + direction.xOffset();
            int celly = this.getY() + direction.yOffset();
            if (cellRoom.existsCellAt(cellx, celly) && (!passableOnly || !bounds.contains(direction))
                    && (!freeOnly || cellRoom.getCell(cellx, celly).getState().getIndividual() == null)) {
                // Test again for the diagonal directions. if next to the position an individual stands. than this direction is removed!
                boolean add = true;
                switch (direction) {
                    case DownLeft:
                    case DownRight:
                    case TopLeft:
                    case TopRight:
                        boolean f1 = false;
                        boolean f2 = false;
                        if (cellRoom.existsCellAt(this.getX() + direction.xOffset(),
                                this.getY()) && cellRoom.getCell(this.getX() + direction.xOffset(),
                                        this.getY()).getState().getIndividual() != null) {
                            f1 = true;
                        } else if (cellRoom.existsCellAt(this.getX() + direction.xOffset(),
                                this.getY()) && cellRoom.getCell(this.getX() + direction.xOffset(),
                                        this.getY()).getState().getIndividual() != null) {
                            f2 = true;
                        }
                        if (f1 && f2) {
                            add = false;
                        }
                }
                add = true;
                if (add) {
                    neighbours.add(cellRoom.getCell(cellx, celly));
                } else {
                    System.err.println("Neuer Fall ist eingetreten!");
                }
            }
        }

        return neighbours;
    }

    /**
     * Returns the neighbour in a given direction.
     *
     * @param dir the direction.
     * @return the neighbour, if exists. null else.
     */
    //@Override
    public EvacCell getNeighbor(Direction8 dir) {
        int cellx = getX() + dir.xOffset();
        int celly = getY() + dir.yOffset();
        if (getRoom().existsCellAt(cellx, celly)) {
            return getRoom().getCell(cellx, celly);
        } else {
            return null;
        }
    }

    public boolean isOccupied() {
        return getState().getIndividual() != null;
    }

    public boolean isOccupied(double time) {
        return getState().getIndividual() != null || time < occupiedUntil;
    }

    public double getOccupiedUntil() {
        return occupiedUntil;
    }

    public void setOccupiedUntil(double occupiedUntil) {
        this.occupiedUntil = occupiedUntil;
    }

    /**
     * Returns a string with a graphic like representation of this room.
     *
     * @return a string representing this room.
     */
    public String graphicalToString() {
        return getState().getIndividual() == null ? graphicalRepresentation + " " + graphicalRepresentation
                : graphicalRepresentation + "I" + graphicalRepresentation;
    }

    public boolean equals(EvacCell c) {
        return (this.getX() == c.getX() && this.getY() == c.getY() && this.hashCode() == c.hashCode());
    }

    protected <T extends EvacCell> T basicClone(T aClone, boolean cloneIndividual) {
        aClone.setSpeedFactor(this.getSpeedFactor());

        if (cloneIndividual && this.getIndividual() != null) {
            aClone.getState().setIndividual(this.getIndividual().clone());
            aClone.getState().getIndividual().setCell(aClone);
        } else {
            aClone.getState().setIndividual(this.getIndividual());
        }

        aClone.room = this.room;

        for (Direction8 bound : this.bounds) {
            aClone.bounds.add(bound);
        }

        return aClone;
    }

    // TODO ob das so funktioniert?

    /**
     * @param c the cell that is compared to
     * @return -1, 0 or 1
     */
    @Override
    public int compareTo(EvacCell c) {
        if (c.x == x && c.y == y) {
            if (c.hashCode() == hashCode()) {
                return 0;
            } else if (c.hashCode() < hashCode()) {
                return 1;
            } else {
                return -1;
            }
        } else if (c.x < x || (c.x == x && c.y < y)) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Returns the direction in which the cell {@code c} lies. This has to be a neighbor cell.
     *
     * @param c a neighbor cell
     * @return the direction in which {@code c} lies
     */
    final public Direction8 getRelative(EvacCell c) {
        try {
            Direction8.getDirection(c.getAbsoluteX() - getAbsoluteX(), c.getAbsoluteY() - getAbsoluteY());
        } catch (AssertionError e) {
            System.out.println(e);
        }

        return Direction8.getDirection(c.getAbsoluteX() - getAbsoluteX(), c.getAbsoluteY() - getAbsoluteY());
    }

    @Override
    public Iterator<EvacCell> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}