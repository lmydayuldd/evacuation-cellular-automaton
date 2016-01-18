package org.zet.cellularautomaton;

import java.util.Collection;
import java.util.Map;
import org.zet.cellularautomaton.potential.DynamicPotential;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zetool.simulation.cellularautomaton.CellularAutomaton;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface EvacuationCellularAutomatonInterface extends CellularAutomaton<EvacCell> {

    public void setIndividualDead(Individual i, DeathCause cause);

    public void setIndividualSave(Individual i);

    public double getStepsPerSecond();

    public double absoluteSpeed(double relativeSpeed);

    public void moveIndividual(EvacCell from, EvacCell to);

    public double getSecondsPerStep();

    public void swapIndividuals(EvacCell cell1, EvacCell cell2);

    public Map<StaticPotential, Double> getExitToCapacityMapping();

    public Collection<Room> getRooms();

// new
    public Collection<StaticPotential> getStaticPotentials();

    public DynamicPotential getDynamicPotential();

    public StaticPotential getSafePotential();

    public StaticPotential minPotentialFor(EvacCell c);

    // TODO: remove these
    public void start();

    public void stop();

    //public void removeMarkedIndividuals(); // marking should probably be stored in the algorithm, or an additional current-step datastructure

    //public void markIndividualForRemoval(Individual i);
    
    public void setIndividualEvacuated(Individual i);

    public IndividualToExitMapping getIndividualToExitMapping(); // this should be only stored for the best response?

    public void updateDynamicPotential(double probabilityDynamicIncrease, double probabilityDynamicDecrease);

}
