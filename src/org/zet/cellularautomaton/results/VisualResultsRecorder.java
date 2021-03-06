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
package org.zet.cellularautomaton.results;

import java.util.HashMap;
import java.util.Vector;

import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.MultiFloorEvacuationCellularAutomaton;
import org.zet.cellularautomaton.DoorCell;
import org.zet.cellularautomaton.potential.DynamicPotential;
import org.zet.cellularautomaton.InitialConfiguration;
import org.zet.cellularautomaton.Room;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.zet.cellularautomaton.Exit;
import org.zet.cellularautomaton.RoomImpl;
import org.zet.cellularautomaton.algorithm.state.PropertyAccess;
import org.zet.cellularautomaton.potential.Potential;
import org.zet.cellularautomaton.potential.StaticPotential;

/**
 * This class helps you to store all the parts of a simulation that are
 * needed to visualize the simulation at a later point. This is done by
 * recording {@code Action}s to a {@code EvacuationRecording}-Object.
 * The actions are stored together with a full deep clone of the initial configuration 
 * of your cellular automaton. The cloning is done in the constructor of this 
 * class, so the automaton is saved in the state that it has when you <b>create</b> 
 * the {@code  VisualResultsRecorder}. 
 * 
 * Usage: Build your initial configuration and THEN instantiate a new
 * VisualResultsRecorder with it. Call {@code startRecording()}. 
 * You can now record Actions by calling {@code recordAction}. 
 * The recorded actions are stored based on the simulation time when they occur, 
 * starting at time {@code t=0}. Every time your simulation time advances, 
 * call {@code nextTimeStep()}. Thus, all actions that are recorded 
 * afterwards will be stored at time {@code t+1}. 
 * All actions are stored in the order of their recording.
 * 
 * To replay the simulation, call {@code getRecording()} to get all 
 * recorded actions nicely packed in a {@code EvacuationRecording}.
 * 
 * @author Daniel R. Schmidt
 *
 */
public class VisualResultsRecorder {
    static PropertyAccess es;
    /** 
     * The current simulation time. Serves as a time stamp for the
     * storage of actions.
     */
    private int timeStep;
    private static VisualResultsRecorder instance = null;
    /**
     * A clone of the initial configuration. All actions are stored
     * based on this configuration. 
     */
    private InitialConfiguration clonedInitialConfig;
    private MultiFloorEvacuationCellularAutomaton clonedCA;
    /**
     * Stores a vector of actions for every time step
     * Each vector holds the actions for its time step in the
     * order of occurrence.
     */
    private Vector<Vector<Action>> actions;
    /**
     * This maps the cells in the original initial configuration to the
     * corresponding cells in the cloned initial configuration. 
     * We need this to convert  the incoming actions (which are based on 
     * the original configuration) to the stored actions (which are based 
     * on the cloned configuration). 
     * 
     * This is bad design and must be changed!
     */
    private HashMap<EvacCell, EvacCell> cellMap;
    private HashMap<Potential, Potential> staticPotentialMap;
    private boolean doRecord;

    protected VisualResultsRecorder() {
        reset();
    }

    /**
     * Creates a new instance of the VisualResultsRecorder and does a deep copy of the initial configuration for
     * storage. Sets current time step to zero.
     *
     * @param initialConfig The initial configuration of the cellular automaton. Will be cloned as is for storage.
     */
    public VisualResultsRecorder(InitialConfiguration initialConfig) {
        this();
        setInitialConfiguration(initialConfig);
    }

    public void startRecording() {
        if (clonedInitialConfig == null) {
            throw new RuntimeException("The initial configuration has not yet been set. "
                    + "Please call setInitialConfiguration() at least once before using this method.");

        }
        this.doRecord = true;
    }

    public void stopRecording() {
        this.doRecord = false;
    }

    public final void reset() {
        this.actions = new Vector<>();
        this.actions.add(new Vector<>());
        this.timeStep = 0;
        this.cellMap = new HashMap<>();
        this.staticPotentialMap = new HashMap<>();
        this.clonedInitialConfig = null;
        this.clonedCA = null;
        stopRecording();
    }

    public final void setInitialConfiguration(InitialConfiguration initialConfig) {
        reset();
        this.clonedInitialConfig = cloneConfig(initialConfig, cellMap, staticPotentialMap);
        this.clonedCA = new MultiFloorEvacuationCellularAutomaton(clonedInitialConfig);
    }

    public InitialConfiguration getInitialConfiguration() {
        return clonedInitialConfig;
    }

    /**
     * Records an action at the current time step. The action should refer to cells in the original initial
     * configuration passed to the constructor of this class. The method will convert it automatically to match the
     * cloned configuration (which has clones of the original cells). The method throws
     * {@code IllegalArgumentExceptions} if you try to pass an action that does not refer to the original configuration.
     *
     * @param action An action that you want to record. The parameters of the action should refer to the original
     * configuration.
     */
    public void recordAction(Action action) throws Action.CADoesNotMatchException {
        //@//if( !ZETLoader.useVisualization )
        //@//    return;
        if (doRecord) {
            //Action adoptedAction = action.adoptToCA(this.clonedCA);
            //actions.get(timeStep).add(adoptedAction);
        }
    }

    /**
     * Increases the current time stamp by one. All actions recorded afterwards will be stored at the new time stamp.
     */
    public void nextTimestep() {
        if (doRecord) {
            timeStep++;
            actions.add(new Vector<>());
        }
    }

    /**
     * Get the current time step.
     *
     * @return The time step at which all actions are currently being recorded.
     */
    public int getTimeStep() {
        return timeStep;
    }

    /**
     * Get the result of the recording. This will include the clone of the initial configuration and all actions ordered
     * by time steps and in the order of their recording. The references to cells and individuals in the returned
     * actions refer to objects in the cloned configuration. A new recording is constructed each time you call this
     * method.
     *
     * @return A new {@code EvacuationRecording} containing all recorded actions and the corresponding configuration.
     */
    public EvacuationRecording getRecording() {
        return new EvacuationRecording(clonedInitialConfig, actions);
    }

    public static VisualResultsRecorder getInstance() {
        if (instance == null) {
            instance = new VisualResultsRecorder();
        }

        return instance;
    }

    /**
     * This does a deep copy of an initial configuration for storage. All objects contained in the configuration are
     * copied. Also, all references are updated, so that they refer to the copies rather than to the original objects.
     * Thus, the copy is completely independent of the original.
     *
     * @param orig The initial configuration that you want to clone
     * @param cellMapping A HashMap mapping the cells in the original configuration to their copies. If this is
     * {@code null}, a new HashMap will be created. Else, the mapping is added to the existing entries in the HashMap.
     * Existing entries may be overwritten.
     * @return A deep copy of {@code orig}.
     */
    private static InitialConfiguration cloneConfig(InitialConfiguration orig,
            HashMap<EvacCell, EvacCell> cellMapping, HashMap<Potential, Potential> potentialMapping) {

        if (cellMapping == null) {
            cellMapping = new HashMap<>();
        }

        LinkedList<String> clonedFloors = new LinkedList<>();
        for (String s : orig.getFloors()) {
            clonedFloors.add(s);
        }

        // First of all, clone all rooms and update the door links
        // Store cloned rooms in clonedRooms
        Vector<Room> clonedRooms = new Vector<>();
        // This maps the original rooms to the cloned ones
        HashMap<Room, Room> roomMap = new HashMap<>();

        // Clone the rooms and store the mapping between the
        // original and the cloned cells
        for (Room room : orig.getRooms()) {
            HashMap<EvacCell, EvacCell> addMapping = new HashMap<>();
            Room clone = cloneRoom(room, addMapping);
            clonedRooms.add(clone);
            roomMap.put(room, clone);
            cellMapping.putAll(addMapping);
        }

        for (Room room : clonedRooms) {
            for (DoorCell door : room.getDoors()) {
                //door.removeAllTargets();
                door.removeAllNextDoorsWithoutNotify();
            }
        }

        // To update the door links, iterate over all doors and
        // move the nextDoor-link of each cloned door to the clone of
        // the original nextDoor
        for (Room room : orig.getRooms()) {
            for (DoorCell oldDoor : room.getDoors()) {
                DoorCell clonedDoor = (DoorCell) cellMapping.get(oldDoor);
                for (int i = 0; i < oldDoor.targetCount(); i++) {
                    DoorCell oldNextDoor = oldDoor.getTarget(i);
                    DoorCell newNextDoor = (DoorCell) cellMapping.get(oldNextDoor);
                    clonedDoor.addTarget(newNextDoor);
                }
            }
        }

        // Now we can clone the potentials. To do so, iterate over
        // all cells and all potentials. The cells are iterated room
        // by room
        //PotentialManager globals = orig.getPotentialManager();
        Map<Exit, Potential> statics = orig.getStaticPotentials();
        //PotentialManager clonedGlobals = new PotentialManager();
        Map<Exit, Potential> staticClone = new HashMap<>();
        if (statics != null) {
            for (Entry<Exit, Potential> entry : statics.entrySet()) {
                StaticPotential clone = new StaticPotential();
                staticClone.put(entry.getKey(), clone);
                potentialMapping.put(entry.getValue(), clone);

                for (Room room : orig.getRooms()) {
                    // For every room do:
                    for (int x = 0; x < room.getWidth(); x++) {
                        for (int y = 0; y < room.getHeight(); y++) {
                            // For every cell in the room do:
                            EvacCell cell = room.getCell(x, y);
                            if (cell != null && entry.getValue().hasValidPotential(cell)) {
                                clone.setPotential(cellMapping.get(cell), entry.getValue().getPotentialDouble(cell));
                            }
                        }
                    }
                }
            }
        }

        // At last we need to clone the dynamic potential. This is done by 
        // iterating over all cells again.
        DynamicPotential dynOrig = orig.getDynamicPotential();
        DynamicPotential dynClone = new DynamicPotential();
        if (dynOrig != null) {
            for (Room room : orig.getRooms()) {
                // For every room do:
                for (int x = 0; x < room.getWidth(); x++) {
                    for (int y = 0; y < room.getHeight(); y++) {
                        // For every cell in the room do:
                        EvacCell cell = room.getCell(x, y);
                        if (cell != null && dynOrig.hasValidPotential(cell)) {
                            dynClone.setPotential(cellMapping.get(cell), dynOrig.getPotential(cell));
                        }
                    }
                }
            }
        }

        //clonedGlobals.setDynamicPotential(dynClone);

        // Now store the new potentials in the individuals:
        for (Room room : orig.getRooms()) {
            for (int x = 0; x < room.getWidth(); x++) {
                for (int y = 0; y < room.getHeight(); y++) {
                    EvacCell cell = room.getCell(x, y);
                    if (cell != null && cell.getState().getIndividual() != null) {
                        EvacCell clonedCell = cellMapping.get(cell);
                        Potential origPot = es.propertyFor(cell.getState().getIndividual()).getStaticPotential();
                        es.propertyFor(cell.getState().getIndividual()).setStaticPotential(potentialMapping.get(origPot));
                    }
                }
            }
        }

        return new InitialConfiguration(clonedFloors, clonedRooms, staticClone, dynClone, orig.getAbsoluteMaxSpeed());
    }

    /**
     * Does a deep copy of a room and updates all internal references.
     *
     * @param room The room you want to clone
     * @param cellMapping A HashMap mapping the cells in {@code room} to those in its clone. If this is {@code null}, a
     * new mapping is created. Else, existing entries may be overwritten.
     * @return A deep copy of {@code room}
     */
    private static Room cloneRoom(Room room, HashMap<EvacCell, EvacCell> cellMapping) {
        if (cellMapping == null) {
            cellMapping = new HashMap<>();
        }

        
        RoomImpl roomClone = new RoomImpl(room);
        roomClone.clear();
        for (int x = 0; x < room.getWidth(); x++) {
            for (int y = 0; y < room.getHeight(); y++) {
                EvacCell orig = room.getCell(x, y);
                if (orig != null) {
                    EvacCell cClone = cloneCell(orig);
                    roomClone.setCell(cClone);
                    cellMapping.put(orig, cClone);
                }
            }
        }

        return roomClone;
    }

    /**
     * Copies a cell and sets a copy of the original cell's individual on the copied cell (if present).
     *
     * @param orig The original cell
     * @return A cloned cell with a cloned individual
     */
    private static EvacCell cloneCell(EvacCell orig) {
        EvacCell clone = orig.clone(true);
        return clone;
    }
}
