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

import java.util.Collection;

/**
 * This class is a container for an initial configuration of the cellular automaton. The configuration is given by all
 * of the automatons rooms (which include all cells and the initial placing of all individuals), its global potentials
 * and its initial dynamic potential.
 *
 * @author Daniel R. Schmidt
 *
 */
public class InitialConfiguration {

    /** The rooms of the cellular automaton, including cells. */
    private final Collection<Room> rooms;
    private final Collection<String> floors;
    /** The global potential of the cellular automaton. */
    private final PotentialManager potentialManager;

    private double absoluteMaxSpeed;

    /**
     * Constructs a new initial configuration of a cellular automaton.
     *
     * @param floors floors
     * @param rooms The automaton's rooms, including cells and the initial placing of individuals
     * @param potentialManager the potential manager
     * @param absoluteMaxSpeed the maximal speed that any individual can have at maximum
     */
    public InitialConfiguration(Collection<String> floors, Collection<Room> rooms, PotentialManager potentialManager,
            double absoluteMaxSpeed) {
        this.rooms = rooms;
        this.floors = floors;
        this.potentialManager = potentialManager;
        this.absoluteMaxSpeed = absoluteMaxSpeed;
    }

    public double getAbsoluteMaxSpeed() {
        return absoluteMaxSpeed;
    }

    /**
     * Get the global static potential layers of the automaton
     *
     * @return The initial static potentials
     */
    public PotentialManager getPotentialManager() {
        return potentialManager;
    }

    /**
     * Get all rooms, including all cells and the initial placing of individuals
     *
     * @return The rooms of the automaton
     */
    public Collection<Room> getRooms() {
        return rooms;
    }

    /**
     * Get all floors, including the empty floors. (A list of possible floors, not actual used floors)
     *
     * @return
     */
    public Collection<String> getFloors() {
        return floors;
    }

    /**
     * @return a string representation of the configuration
     */
    @Override
    public String toString() {
        String representation = "";

        for (Room aRoom : rooms) {
            representation += "\n Room (" + aRoom + "):\n";
            representation += aRoom.graphicalToString();
        }

        return representation;
    }

    void setAbsoluteMaxSpeed(double absoluteMaxSpeed) {
        this.absoluteMaxSpeed = absoluteMaxSpeed;
    }
}