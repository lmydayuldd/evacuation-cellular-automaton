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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zet.cellularautomaton.results.CAStateChangedAction;
import org.zet.cellularautomaton.results.DieAction;
import org.zet.cellularautomaton.results.ExitAction;
import org.zet.cellularautomaton.results.MoveAction;
import org.zet.cellularautomaton.results.SwapAction;
import org.zet.cellularautomaton.results.VisualResultsRecorder;
import org.zet.cellularautomaton.potential.DynamicPotential;
import org.zetool.simulation.cellularautomaton.SquareCellularAutomaton;
import org.zetool.simulation.cellularautomaton.tools.CellMatrixFormatter;

/**
 * This class represents the structure of the cellular automaton. It holds the individuals, the rooms and the floor
 * fields which are important for the behavior of individuals. It also contains an object of the IndividualCreator which
 * is responsible for creating individuals with random attributes based upon the choices made by the user.
 *
 * @author Jan-Philipp Kappmeier
 * @author Matthias Woste
 */
public class EvacuationCellularAutomaton extends SquareCellularAutomaton<EvacCell> implements EvacuationCellularAutomatonInterface {

    private boolean recordingStarted;


    /**
     * the state of the cellular automaton.
     */
    public enum State {

        /** If all values and individuals are set, the simulation can be executed. */
        READY,
        /** If a simulation is running. */
        RUNNING,
        /** If a simulation is finished. in this case all individuals are removed or in save areas. */
        FINISHED
    }
    /** An ArrayList of all Individual objects in the cellular automaton. */
    private List<Individual> individuals;
    /** An ArrayList of all Individual objects, which are already out of the simulation because they are evacuated. */
    private List<Individual> evacuatedIndividuals;
    /** An ArrayList of all Individual objects, which are marked as "dead". */
    private List<Individual> deadIndividuals;
    /** An {@code ArrayList} marked to be removed. */
    private List<Individual> markedForRemoval;
    /** An ArrayList of all ExitCell objects (i.e. all exits) of the building. */
    private List<ExitCell> exits;
    /** A map of rooms to identification numbers. */
    private Map<Integer, Room> rooms;
    /** A mapping floor-id <-> floor-name. */
    private List<String> floorNames;
    /** A mapping floor <-> rooms. */
    private List<ArrayList<Room>> roomsByFloor;
    /** HashMap mapping UUIDs of AssignmentTypes to Individuals. */
    private Map<UUID, HashSet<Individual>> typeIndividualMap;
    /** Maps name of an assignment types to its unique id. */
    private Map<String, UUID> assignmentTypes;
    /** Maps Unique IDs to individuals. */
    private Map<Integer, Individual> individualsByID;
    /** A mapping that maps individuals to exits. */
    private IndividualToExitMapping individualToExitMapping;
    /** A mapping that maps exits to their capacity. */
    private Map<StaticPotential, Double> exitToCapacityMapping;
    /** Reference to all Floor Fields. */
    //private PotentialManager potentialManager;
    /** The current time step. */
    //private int timeStep;
    /** The current state of the cellular automaton, e.g. RUNNING, stopped, ... */
    private State state;
    private double absoluteMaxSpeed;
    private double secondsPerStep;
    private double stepsPerSecond;
    private int notSaveIndividualsCount = 0;
    private int initialIndividualCount;
    private static final String ERROR_NOT_IN_LIST = "Specified individual is not in list individuals.";
    /** A {@code TreeMap} of all StaticPotentials. */
    private final HashMap<Integer, StaticPotential> staticPotentials;
    /** The safe potential*/
    private StaticPotential safePotential;
    /** The single DynamicPotential. */
    private DynamicPotential dynamicPotential;

    /**
     * Constructs a EvacuationCellularAutomaton object with empty default objects.
     */
    public EvacuationCellularAutomaton() {
        super(null);
        //timeStep = 0;
        individuals = new ArrayList<>();
        individualsByID = new HashMap<>();
        evacuatedIndividuals = new ArrayList<>();
        deadIndividuals = new ArrayList<>();
        markedForRemoval = new ArrayList<>();
        exits = new ArrayList<>();
        rooms = new HashMap<>();
        assignmentTypes = new HashMap<>();
        typeIndividualMap = new HashMap<>();
        roomsByFloor = new LinkedList<>();
        //potentialManager = new PotentialManager();
        absoluteMaxSpeed = 1;
        secondsPerStep = 1;
        stepsPerSecond = 1;
        state = State.READY;
        floorNames = new LinkedList<>();
        recordingStarted = false;
        staticPotentials = new HashMap<>();
        dynamicPotential = new DynamicPotential();
        safePotential = new StaticPotential();
    }

    /**
     * Creates a {@code Cellularautomaton} from an {@link InitialConfiguration} that is stored in an visual results
     * recorder. This is used to replay a simulation.
     *
     * @param initialConfiguration the initial configuration of the simulation.
     */
    public EvacuationCellularAutomaton(InitialConfiguration initialConfiguration) {
        this();
        stopRecording();
        
        // set up floors and rooms
        initialConfiguration.getFloors().stream().forEach(floor -> addFloor(floor));
        initialConfiguration.getRooms().stream().forEach(room -> addRoom(room));

        for (Room room : initialConfiguration.getRooms()) {
            for (EvacCell cell : room.getAllCells()) {
                if (!cell.getState().isEmpty()) {
                    addIndividual(cell, cell.getState().getIndividual());
                }
            }
        }

        for (StaticPotential staticPot : initialConfiguration.getStaticPotentials()) {
            addStaticPotential(staticPot);
        }

        setDynamicPotential(initialConfiguration.getDynamicPotential());
        setAbsoluteMaxSpeed(initialConfiguration.getAbsoluteMaxSpeed());
    }

    @Override
    public int getDimension() {
        return 2;
    }

    /**
     * Returns the number of cells in the whole cellular automaton
     *
     * @return the number of cells
     */
    public int getCellCount() {
        int count = 0;
        count = getRooms().stream().map(room -> room.getCellCount(false)).reduce(count, Integer::sum);
        return count;
    }

    public void startRecording() {
        recordingStarted = true;
        VisualResultsRecorder.getInstance().setInitialConfiguration(new InitialConfiguration(floorNames, rooms.values(), getStaticPotentials(), getDynamicPotential(), absoluteMaxSpeed));
        VisualResultsRecorder.getInstance().startRecording();
    }

    public final void stopRecording() {
        recordingStarted = false;
        VisualResultsRecorder.getInstance().stopRecording();
    }

    /**
     * Assigns the UUID to the name of the assignment type
     *
     * @param uid UUID of the assignment type
     * @param s name of the assignment type
     */
    public void setAssignmentType(String s, UUID uid) {
        assignmentTypes.put(s, uid);
    }

    /**
     * Returns UUID of the assignment type with the given name
     *
     * @param name of the assignment type
     * @return uid UUID of the assignment type
     */
    public UUID getAssignmentUUIS(String name) {
        return assignmentTypes.get(name);
    }

    public double getAbsoluteMaxSpeed() {
        return absoluteMaxSpeed;
    }

    public Map<String, UUID> getAssignmentTypes() {
        return assignmentTypes;
    }

    /**
     * Sets the maximal speed that any individual can walk. That means an individual with speed = 1 moves with 100
     * percent of the absolute max speed.
     *
     * @param absoluteMaxSpeed
     * @throws java.lang.IllegalArgumentException if absoluteMaxSpeed is less or equal to zero
     */
    public final void setAbsoluteMaxSpeed(double absoluteMaxSpeed) {
        if (absoluteMaxSpeed <= 0) {
            throw new java.lang.IllegalArgumentException("Maximal speed must be greater than zero!");
        }
        this.absoluteMaxSpeed = absoluteMaxSpeed;
        this.stepsPerSecond = absoluteMaxSpeed / 0.4;
        this.secondsPerStep = 0.4 / absoluteMaxSpeed;
        if (recordingStarted) {
            VisualResultsRecorder.getInstance().getInitialConfiguration().setAbsoluteMaxSpeed(absoluteMaxSpeed);
        }
    }

    /**
     * Returns the seconds one step needs.
     *
     * @return the seconds one step needs
     */
    @Override
    public double getSecondsPerStep() {
        return secondsPerStep;
    }

    /**
     * Returns the number of steps performed by the cellular automaton within one second. The time depends of the
     * absolute max speed and is set if {@link #setAbsoluteMaxSpeed(double)} is called.
     *
     * @return the number of steps performed by the cellular automaton within one second.
     */
    @Override
    public double getStepsPerSecond() {
        return stepsPerSecond;
    }

    /**
     * Returns the absolute speed of an individual in meter per second depending on its relative speed which is a
     * fraction between zero and one of the absolute max speed.
     *
     * @param relativeSpeed
     * @return the absolute speed in meter per seconds for a given relative speed.
     */
    @Override
    public double absoluteSpeed(double relativeSpeed) {
        return absoluteMaxSpeed * relativeSpeed;
    }

    /**
     * Increases the actual timeStep of the EvacuationCellularAutomaton about one.
     */
//    @Override
//    public void nextTimeStep() {
//        timeStep++;
//        VisualResultsRecorder.getInstance().nextTimestep();
//    }

    /**
     * Gets the actual timeStep of the EvacuationCellularAutomaton.
     *
     * @return the timeStep
     */
//    @Override
//    public int getTimeStep() {
//        return timeStep;
//    }

    /**
     * Returns an ArrayList of all exists of the building
     *
     * @return the ArrayList of exits
     */
    public List<ExitCell> getExits() {
        return Collections.unmodifiableList(exits);
    }

    /**
     * Returns the object which organizes all potentials
     *
     * @return PotentialManager object
     */
//    public final PotentialManager getPotentialManager() {
//        return potentialManager;
//    }

    /**
     * Returns all Individuals in the given AssignmentType
     *
     * @param id UUID of AssignmentType
     * @return a set of of Individuals
     */
    public Set<Individual> getIndividualsInAssignmentType(UUID id) {
        return typeIndividualMap.get(id);
    }

    /**
     * Returns an ArrayList of all rooms of the building
     *
     * @return the ArrayList of rooms
     */
    @Override
    public Collection<Room> getRooms() {
        return Collections.unmodifiableCollection(rooms.values());
    }

    /**
     * Returns the mapping between individuals and exit cells.
     *
     * @return the mapping between individuals and exit cells
     */
    @Override
    public IndividualToExitMapping getIndividualToExitMapping() {
        return individualToExitMapping;
    }

    /**
     * Sets a mapping between individuals and exit cells.
     *
     * @param individualToExitMapping the mapping
     */
    public void setIndividualToExitMapping(IndividualToExitMapping individualToExitMapping) {
        this.individualToExitMapping = individualToExitMapping;
    }

    @Override
    public Map<StaticPotential, Double> getExitToCapacityMapping() {
        return exitToCapacityMapping;
    }

    public void setExitToCapacityMapping(Map<StaticPotential, Double> exitToCapacityMapping) {
        this.exitToCapacityMapping = exitToCapacityMapping;
    }

    /**
     * Adds an Individual object to the List of all individuals of the cellular automaton and puts this individual into
     * the two mappings of rooms and assignment types.
     *
     * @param c the EvacCell on which the individual stands
     * @param i the Individual object
     * @throws IllegalArgumentException if the the specific individual exits already in the list individuals
     * @throws IllegalStateException if an individual is added after the simulation has been startet.
     */
    public final void addIndividual(EvacCell c, Individual i) throws IllegalArgumentException {
        if (this.state != State.READY) {
            throw new IllegalStateException("Individual added after simulation has started.");
        }
        if (individuals.contains(i)) {
            throw new IllegalArgumentException("Individual with id " + i.id() + " exists already in list individuals.");
        } else {
            individuals.add(i);
            individualsByID.put(i.getNumber(), i);
            if (typeIndividualMap.get(i.getUid()) == null) {
                typeIndividualMap.put(i.getUid(), new HashSet<>());
            }
            if (!typeIndividualMap.get(i.getUid()).contains(i)) {
                typeIndividualMap.get(i.getUid()).add(i);
            }
        }
        c.getRoom().addIndividual(c, i);

        // assign shortest path potential to individual, so it is not null.
        int currentMin = -1;
        for (StaticPotential sp : getStaticPotentials()) {
            if (currentMin == -1) {
                i.setStaticPotential(sp);
                currentMin = sp.getPotential(c);
            } else if (sp.getPotential(c) > -1 && sp.getPotential(c) < currentMin) {
                currentMin = sp.getPotential(c);
                i.setStaticPotential(sp);
            }
        }
    }

    /**
     * Removes an individual from the list of all individuals of the building
     *
     * @param i specifies the Individual object which has to be removed from the list
     * @throws IllegalArgumentException if the the specific individual does not exist in the list individuals
     */
    private void removeIndividual(Individual i) {
        i.getCell().getState().removeIndividual();
        if (!individuals.remove(i)) {
            throw new IllegalArgumentException(ERROR_NOT_IN_LIST);
        }
    }

    /**
     * Move the individual standing on the "from"-EvacCell to the "to"-EvacCell.
     *
     * @param from The cell on which the individual, which shall be moved, stays.
     * @param to The destination-cell for the moving individual.
     * @throws java.lang.IllegalArgumentException if the individual should be moved from an empty EvacCell, which is not
     * occupied by an Individual, or if the ''to''-EvacCell is already occupied by another individual.
     */
    @Override
    public void moveIndividual(EvacCell from, EvacCell to) {
        if (from.getState().isEmpty()) {
            throw new IllegalArgumentException("No Individual standing on the ''from''-Cell!");
        }
        if (from.equals(to)) {
            VisualResultsRecorder.getInstance().recordAction(new MoveAction(from, from, from.getState().getIndividual()));
            return;
        }
        if (!to.getState().isEmpty()) {
            throw new IllegalArgumentException("Individual " + to.getState().getIndividual() + " already standing on the ''to''-Cell!");
        }

        VisualResultsRecorder.getInstance().recordAction(new MoveAction(from, to, from.getState().getIndividual()));
        if (from.getRoom().equals(to.getRoom())) {
            from.getRoom().moveIndividual(from, to);
        } else {
            Individual i = from.getState().getIndividual();
            from.getRoom().removeIndividual(i);
            to.getRoom().addIndividual(to, i);
        }

        //Statistic.instance.getSpecificFlowCollector().collect( timeStep, from, to );
    }

    @Override
    public void swapIndividuals(EvacCell cell1, EvacCell cell2) {
        if (cell1.getState().isEmpty()) {
            throw new IllegalArgumentException("No Individual standing on cell #1!");
        }
        if (cell2.getState().isEmpty()) {
            throw new IllegalArgumentException("No Individual standing on cell #2!");
        }
        if (cell1.equals(cell2)) {
            throw new IllegalArgumentException("The cells are equal. Can't swap on equal cells.");
        }
        VisualResultsRecorder.getInstance().recordAction(new SwapAction(cell1, cell2));
        if (cell1.getRoom().equals(cell2.getRoom())) {
            cell1.getRoom().swapIndividuals(cell1, cell2);
        } else {
            Individual c1i = cell1.getState().getIndividual();
            Individual c2i = cell2.getState().getIndividual();
            cell1.getRoom().removeIndividual(c1i);
            cell2.getRoom().removeIndividual(c2i);
            cell1.getRoom().addIndividual(cell1, c2i);
            cell2.getRoom().addIndividual(cell2, c1i);
        }
    }

    public Individual getIndividual(Integer id) {
        return individualsByID.get(id);
    }

    /**
     * Removes an individual from the list of all individuals of the building and adds it to the list of individuals,
     * which are out of the simulation because the are evacuated.
     *
     * @throws java.lang.IllegalArgumentException if the the specific individual does not exist in the list individuals
     * @param i specifies the Individual object which has to be removed from the list and added to the other list
     */
    public void setIndividualEvacuated(Individual i) {
        if (!individuals.remove(i)) {
            throw new IllegalArgumentException(ERROR_NOT_IN_LIST);
        }
//        if (getTimeStep() == 0) {
//            throw new IllegalStateException();
//        }
        VisualResultsRecorder.getInstance().recordAction(new ExitAction((ExitCell) i.getCell()));
        EvacCell evacCell = i.getCell();

        i.getCell().getRoom().removeIndividual(i);
        i.setCell(evacCell);
        evacuatedIndividuals.add(i);
        notSaveIndividualsCount--;
        i.setEvacuated();
    }

    @Override
    public void markIndividualForRemoval(Individual i) {
        markedForRemoval.add(i);
    }

    @Override
    public void removeMarkedIndividuals() {
        markedForRemoval.stream().forEach(individual -> setIndividualEvacuated(individual));
        markedForRemoval.clear();
    }

    /**
     * Checks whether an individual is marked for removal by the next call to {@link #removeMarkedIndividuals() }.
     * @param toEvacuate the individual
     * @return  {@code true} if the individual is removed 
     */
    public boolean isIndividualMarked(Individual toEvacuate) {
        return markedForRemoval.contains(toEvacuate);
    }


    /**
     * Sets the specified individual to the status safe and sets the correct safety time.
     *
     * @param i
     */
    @Override
    public void setIndividualSave(Individual i) {
        notSaveIndividualsCount--;
        i.setSafe(true);
        i.setSafetyTime((int) Math.ceil(i.getStepEndTime()));
    }

    /**
     * Removes an individual from the list of all individuals of the building and adds it to the list of individuals,
     * which are "dead".
     *
     * @throws java.lang.IllegalArgumentException if the the specific individual does not exist in the list individuals
     * @param i specifies the Individual object which has to be removed from the list and added to the other list
     * @param cause the dead cause of the individual
     */
    @Override
    public void setIndividualDead(Individual i, DeathCause cause) {
        if (!individuals.remove(i)) {
            throw new IllegalArgumentException(ERROR_NOT_IN_LIST);
        }
        VisualResultsRecorder.getInstance().recordAction(new DieAction(i.getCell(), cause, i.getNumber()));
        EvacCell c = i.getCell();
        c.getRoom().removeIndividual(i);
        i.setCell(c);
        deadIndividuals.add(i);
        notSaveIndividualsCount--;
        i.die(cause);
    }

    /**
     * Returns the number of individuals that were in the cellular automaton when the simulation starts.
     *
     * @return the number of individuals
     */
    @Override
    public int getInitialIndividualCount() {
        return initialIndividualCount;
    }

    /**
     * Returns the number of individuals that currently in the cellular automaton.
     *
     * @return the number of individuals in the cellular automaton
     */
    @Override
    public int getIndividualCount() {
        return individuals.size();
    }

    /**
     * Calculates the number of individuals that died by a specified death cause.
     *
     * @param deathCause the death cause
     * @return the number of individuals died by the death cause
     */
    public int getDeadIndividualCount(DeathCause deathCause) {
        int count = 0;
        for (Individual i : getDeadIndividuals()) {
            if (i.getDeathCause().compareTo(deathCause) == 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getNotSafeIndividualsCount() {
        return notSaveIndividualsCount;
    }

    /*
     * Adds a new floor.
     */
    public final void addFloor(String name) {
        floorNames.add(name);
        roomsByFloor.add(new ArrayList<>());
    }

    /**
     * Adds a room to the List of all rooms of the building
     *
     * @param room the Room object to be added
     * @throws IllegalArgumentException Is thrown if the the specific room exists already in the list rooms
     */
    final public void addRoom(Room room) {
        if (rooms.containsKey(room.getID())) {
            throw new IllegalArgumentException("Specified room exists already in list rooms.");
        } else {
            rooms.put(room.getID(), room);
            Integer floorID = room.getFloorID();
            if (roomsByFloor.size() < floorID + 1) {
                throw new IllegalStateException("No Floor with id " + floorID + " has been added before.");
            }
            roomsByFloor.get(floorID).add(room);
            addExits(room);
        }
    }
    
    /**
     * Adds the in the given room into the list of exits. Throws an exception if any of the exits is already known,
     * as any exit can only be in one room.
     * @param room 
     */
    private void addExits(Room room) {
        for (EvacCell cell : room.getAllCells()) {
            if (cell instanceof ExitCell) {
                if (exits.contains((ExitCell)cell)) {
                    throw new IllegalArgumentException("Specified exit exists already in list exits.");
                } else {
                    exits.add((ExitCell) cell);
                }
            }
        }
    }

    /**
     * Removes a room from the list of all rooms of the building
     *
     * @param room specifies the Room object which has to be removed from the list
     * @throws IllegalArgumentException Is thrown if the the specific room does not exist in the list rooms
     */
    public void removeRoom(Room room) {
        if (rooms.remove(room.getID()) == null) {
            throw new IllegalArgumentException("Specified room is not in list rooms.");
        }
        rooms.remove(room.getID());
    }

    /**
     * This method recognizes clusters of neighbouring ExitCells (that means ExitCells lying next to another ExitCell)
     * and returns an ArrayList, which contains one ArrayList of ExitCells for each Cluster of ExitCells.
     *
     * @return An ArrayList, which contains one ArrayList of ExitCells for each Cluster of ExitCells.
     */
    public List<List<ExitCell>> clusterExitCells() {
        Set<ExitCell> alreadySeen = new HashSet<>();
        List<List<ExitCell>> allClusters = new ArrayList<>();
        List<ExitCell> allExitCells = this.getExits();
        for (ExitCell e : allExitCells) {
            if (!alreadySeen.contains(e)) {
                List<ExitCell> singleCluster = new ArrayList<>();
                singleCluster = this.findExitCellCluster(e, singleCluster, alreadySeen);
                allClusters.add(singleCluster);
            }
        }
        return allClusters;
    }

    /**
     * Private sub-method for finding a Cluster of neighboring ExitCells recursively.
     *
     * @param currentCell The cell from which the algorithm starts searching neighboring ExitCells.
     * @param cluster An empty ArrayList, in which the cluster will be created.
     * @param alreadySeen A HashSet storing all already clustered ExitCells to prevent them of being clustered a second
     * time.
     * @return Returns one Cluster of neighboring ExitCells as an ArrayList.
     */
    private List<ExitCell> findExitCellCluster(ExitCell currentCell, List<ExitCell> cluster, Set<ExitCell> alreadySeen) {
        if (!alreadySeen.contains(currentCell)) {
            cluster.add(currentCell);
            alreadySeen.add(currentCell);
            Collection<EvacCell> cellNeighbours = currentCell.getDirectNeighbors();
            List<ExitCell> neighbours = new ArrayList<>();
            for (EvacCell c : cellNeighbours) {
                if (c instanceof ExitCell) {
                    neighbours.add((ExitCell) c);
                }
            }
            for (ExitCell c : neighbours) {
                cluster = this.findExitCellCluster(c, cluster, alreadySeen);
            }
        }
        return cluster;
    }

    public String graphicalToString() {
        StringBuilder representation = new StringBuilder();

        CellMatrixFormatter formatter = new CellMatrixFormatter();
        formatter.registerFormatter(EvacCell.class, new EvacuationCellularAutomatonCellFormatter() );
        for (Room aRoom : rooms.values()) {
            representation.append(formatter.graphicalToString(aRoom)).append("\n\n");
        }
        return representation.toString();
    }

    public double getStaticPotential(EvacCell cell, int id) {
        return getStaticPotential(id).getPotential(cell);
    }

    public int getDynamicPotential(EvacCell cell) {
        return getDynamicPotential().getPotential(cell);
    }

    public void setDynamicPotential(EvacCell cell, double value) {
        getDynamicPotential().setPotential(cell, value);
    }

    @Override
    public void updateDynamicPotential(double probabilityDynamicIncrease, double probabilityDynamicDecrease) {
        dynamicPotential.update(probabilityDynamicIncrease, probabilityDynamicDecrease);
    }
    

    /**
     * Get a Collection of all staticPotentials.
     *
     * @return The Collection of all staticPotentials
     */
    @Override
    public Collection<StaticPotential> getStaticPotentials() {
        return staticPotentials.values();
    }

    /**
     * Adds the StaticPotential into the List of staticPotentials. The method throws {@code IllegalArgumentException} if
     * the StaticPtential already exists.
     *
     * @param potential The StaticPotential you want to add to the List.
     * @throws IllegalArgumentException if the {@code StaticPotential} already exists
     */
    public void addStaticPotential(StaticPotential potential) throws IllegalArgumentException {
        if (staticPotentials.containsKey(potential.getID())) {
            throw new IllegalArgumentException("The StaticPtential already exists!");
        }
        Integer i = potential.getID();
        staticPotentials.put(i, potential);
    }

    /**
     * Get the StaticPotential with the specified ID. The method throws {@code IllegalArgumentException} if the
     * specified ID not exists.
     *
     * @param id
     * @return The StaticPotential
     * @throws IllegalArgumentException
     */
    public StaticPotential getStaticPotential(int id) throws IllegalArgumentException {
        if (!(staticPotentials.containsKey(id))) {
            throw new IllegalArgumentException("No StaticPotential with this ID exists!");
        }
        return staticPotentials.get(id);
    }

    /**
     * Set the dynamicPotential.
     *
     * @param potential The DynamicPotential you want to set.
     */
    public void setDynamicPotential(DynamicPotential potential) {
        dynamicPotential = potential;
    }

    /**
     * Get the dynamicPotential. Returns null if the DynamicPotential not exists.
     *
     * @return The DynamicPotential
     */
    @Override
    public DynamicPotential getDynamicPotential() {
        return dynamicPotential;
    }

    @Override
    public StaticPotential getSafePotential() {
        return safePotential;
    }

    public void setsafePotential(StaticPotential potential) {
        safePotential = potential;
    }
   
    /**
     * Returns the current state of the cellular automaton.
     *
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the current state of the cellular automaton.
     *
     * @param state the new state
     */
    public void setState(State state) {
        this.state = state;
        VisualResultsRecorder.getInstance().recordAction(new CAStateChangedAction(state));
    }

    @Override
    public void start() {
        setState(State.RUNNING);
        notSaveIndividualsCount = individuals.size();
        initialIndividualCount = individuals.size();
    }

    public void stop() {
        setState(State.FINISHED);
    }

    /**
     * Resets the cellular automaton in order to let it run again. All individuals are deleted, timestamp and lists are
 reseted and the status is set to READY. The recording of actions is stopped; Call {@code startRecording()} after
     * you placed individuals in the cellular automaton to start recording again.
     */
    public void reset() {
        VisualResultsRecorder.getInstance().stopRecording();
        VisualResultsRecorder.getInstance().reset();
        Individual[] individualsCopy = individuals.toArray(new Individual[individuals.size()]);

        for (Individual individual : individualsCopy) {
            removeIndividual(individual);
        }
        deadIndividuals.clear();
        evacuatedIndividuals.clear();
        individuals.clear();
        typeIndividualMap.clear();

        //timeStep = 0;
        state = State.READY;
    }

    /**
     * Returns a view of all individuals.
     *
     * @return the view
     */
    @Override
    public List<Individual> getIndividuals() {
        return Collections.unmodifiableList(individuals);
    }

    public List<Individual> getRemainingIndividuals() {
        List<Individual> remaining = new ArrayList<>(individuals.size() - evacuatedIndividuals.size());

        individuals.stream().filter(i -> (!i.isEvacuated() || i.isDead())).forEach(i -> remaining.add(i));

        return Collections.unmodifiableList(remaining);
    }

    public List<Individual> getNotDeadIndividuals() {
        List<Individual> notDeadIndividuals = Collections.unmodifiableList(individuals);
        evacuatedIndividuals.stream().forEach(individual -> notDeadIndividuals.add(individual));
        return notDeadIndividuals;
    }

    /**
     * Returns a view of all evacuated individuals.
     *
     * @return the view
     */
    public List<Individual> getEvacuatedIndividuals() {
        return Collections.unmodifiableList(evacuatedIndividuals);
    }

    public int evacuatedIndividualsCount() {
        return evacuatedIndividuals.size();
    }

    /**
     * Returns a view of all dead individuals.
     *
     * @return the view
     */
    public List<Individual> getDeadIndividuals() {
        return Collections.unmodifiableList(deadIndividuals);
    }

    public int deadIndividualsCount() {
        return deadIndividuals.size();
    }

    @Override
    public Iterator<Individual> iterator() {
        return individuals.iterator();
    }

    public Room getRoom(int id) {
        return rooms.get(id);
    }

    /**
     * Returns a collection containing all floor ids.
     *
     * @return the collection of floor ids
     */
    public List<String> getFloors() {
        return Collections.unmodifiableList(floorNames);
    }

    /**
     * Returns the name of the floor with a specified id. The id corresponds to the floor numbers in the z-format.
     *
     * @param id the floor id
     * @return the floors name
     */
    public String getFloorName(int id) {
        return floorNames.get(id);
    }

    /**
     * Returns a collection of all rooms on a specified floor.
     *
     * @param floorID the floor id
     * @return the collection of rooms
     */
    public Collection<Room> getRoomsOnFloor(Integer floorID) {
        return roomsByFloor.get(floorID);
    }
}
