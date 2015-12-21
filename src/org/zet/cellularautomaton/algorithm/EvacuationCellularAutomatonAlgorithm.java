package org.zet.cellularautomaton.algorithm;

import static org.zetool.common.util.Helper.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import org.zet.cellularautomaton.algorithm.rule.EvacuationRule;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.Individual;
import org.zet.algo.ca.util.IndividualDistanceComparator;
import org.zet.cellularautomaton.EvacuationCellularAutomatonInterface;
import org.zet.cellularautomaton.algorithm.parameter.ParameterSet;
import org.zet.cellularautomaton.algorithm.rule.EvacuationState;
import org.zet.cellularautomaton.statistic.CAStatisticWriter;
import org.zetool.algorithm.simulation.cellularautomaton.AbstractCellularAutomatonSimulationAlgorithm;

/**
 * An implementation of a general cellular automaton algorithm specialized for evacuation simulation. The cells of the
 * cellular automaton are populized by {@link Individual}s and the simulation is rulebased performed only on these
 * populated cells. The algorithm is itself abstract and implementations have to specify the order in which the rules
 * are executed for the populating individuals.
 *
 * @author Jan-Philipp Kappmeier
 */
public class EvacuationCellularAutomatonAlgorithm
        extends AbstractCellularAutomatonSimulationAlgorithm<EvacuationCellularAutomatonInterface, EvacCell,
        EvacuationSimulationProblem, EvacuationSimulationResult> {

    /** The order in which the individuals are asked for. */
    public static final Function<List<Individual>,Iterator<Individual>> DEFAULT_ORDER = x -> x.iterator();
    /** The distance comparator. */
    private static final IndividualDistanceComparator DISTANCE_COMPARATOR = new IndividualDistanceComparator<>();
    /** Sorts the individuals by increasing distance to the exit. */
    public static final Function<List<Individual>, Iterator<Individual>> FRONT_TO_BACK = (List<Individual> t) -> {
        List<Individual> copy = new ArrayList<>(t);
        Collections.sort(copy, DISTANCE_COMPARATOR);
        return copy.iterator();
    };
    /** Sorts the individuals by decreasing distance to the exit. */
    public static final Function<List<Individual>, Iterator<Individual>> BACK_TO_FRONT = (List<Individual> t) -> {
        List<Individual> copy = new ArrayList<>(t);
        Collections.sort(copy, DISTANCE_COMPARATOR);
        Collections.reverse(copy);
        return copy.iterator();
    };
    /** The ordering used in the evacuation cellular automaton. */
    private Function<List<Individual>,Iterator<Individual>> reorder;
    
    public EvacuationCellularAutomatonAlgorithm() {
        this(DEFAULT_ORDER);
    }

    public EvacuationCellularAutomatonAlgorithm(Function<List<Individual>,Iterator<Individual>> reorder) {
        this.reorder = reorder;
    }

    @Override
    protected void initialize() {
        initRulesAndState();
        setMaxSteps(getProblem().getEvacuationStepLimit());
        log.log(Level.INFO, "{0} is executed. ", toString());

        getProblem().getCellularAutomaton().start();
        Individual[] individualsCopy = getProblem().getCellularAutomaton().getIndividuals().toArray(
                new Individual[getProblem().getCellularAutomaton().getIndividuals().size()]);
        for (Individual i : individualsCopy) {
            Iterator<EvacuationRule> primary = getProblem().getRuleSet().primaryIterator();
            EvacCell c = i.getCell();
            while (primary.hasNext()) {
                EvacuationRule r = primary.next();
                r.execute(c);
            }
        }
        getProblem().getCellularAutomaton().removeMarkedIndividuals();
    }

    EvacuationState es;
    /** The minimal number of steps that is needed until all movements are FINISHED. */
    private int neededTime;
    public void setNeededTime(int i) {
        neededTime = i;
    }

    private void initRulesAndState() {
        es = new EvacuationState() {
            public CAStatisticWriter caStatisticWriter = new CAStatisticWriter();

            @Override
            public int getTimeStep() {
                return getStep();
            }

            @Override
            public void setNeededTime(int i) {
                neededTime = i;
            }

            @Override
            public int getNeededTime() {
                return neededTime;
            }
            
            

            @Override
            public CAStatisticWriter getStatisticWriter() {
                return caStatisticWriter;
            }

            @Override
            public void setIndividualDead(Individual individual, DeathCause deathCause) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setIndividualSave(Individual savedIndividual) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void swapIndividuals(EvacCell cell1, EvacCell cell2) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void moveIndividual(EvacCell from, EvacCell targetCell) {
                getCellularAutomaton().moveIndividual(from, targetCell);
            }

            @Override
            public void increaseDynamicPotential(EvacCell targetCell) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public EvacuationCellularAutomatonInterface getCellularAutomaton() {
                return getProblem().getCellularAutomaton();
            }

            @Override
            public void markIndividualForRemoval(Individual individual) {
                getCellularAutomaton().markIndividualForRemoval(individual);
            }

            @Override
            public ParameterSet getParameterSet() {
                return getProblem().getParameterSet();
            }
        };
        for( EvacuationRule r : getProblem().getRuleSet()) {
            r.setEvacuationSimulationProblem(es);
        }
    }

    @Override
    protected void performStep() {
        super.performStep();
        super.increaseStep();

        getProblem().getCellularAutomaton().removeMarkedIndividuals();
        getProblem().getCellularAutomaton().updateDynamicPotential(
        getProblem().getParameterSet().probabilityDynamicIncrease(),
        getProblem().getParameterSet().probabilityDynamicDecrease());
        //getProblem().getCellularAutomaton().nextTimeStep();

        fireProgressEvent(getProgress(), String.format("%1$s von %2$s individuals evacuated.",
                getProblem().getCellularAutomaton().getInitialIndividualCount() - getProblem().getCellularAutomaton().getIndividualCount(),
                getProblem().getCellularAutomaton().getInitialIndividualCount()));
    }

    @Override
    protected final void execute(EvacCell cell) {

        Individual i = Objects.requireNonNull(cell.getState().getIndividual(),
                "Execute called on EvacCell that does not contain an individual!");
        //Iterator<EvacuationRule> loop = getProblem().getRuleSet().loopIterator();
        //while (loop.hasNext()) { // Execute all rules
        for( EvacuationRule r : in(getProblem().getRuleSet().loopIterator())) {
          //  EvacuationRule r = loop.next();
            r.execute(i.getCell());
        }
    }

    @Override
    protected EvacuationSimulationResult terminate() {
        // let die all individuals which are not already dead and not safe
        if (getProblem().getCellularAutomaton().getNotSafeIndividualsCount() != 0) {
            Individual[] individualsCopy = getProblem().getCellularAutomaton().getIndividuals().toArray(
                    new Individual[getProblem().getCellularAutomaton().getIndividuals().size()]);
            for (Individual i : individualsCopy) {
                if (!i.getCell().getState().getIndividual().isSafe()) {
                    getProblem().getCellularAutomaton().setIndividualDead(i, DeathCause.NOT_ENOUGH_TIME);
                }
            }
        }
        fireProgressEvent(1, "Simulation complete.");

        EvacuationSimulationProblem p = getProblem();
        p.getCellularAutomaton().stop();
        log("Time steps: " + getStep());
        return new EvacuationSimulationResult(getStep());
    }

    @Override
    protected boolean isFinished() {
        boolean thisFinished = allIndividualsSave() && timeOver();
        return super.isFinished() || thisFinished;
    }
    
    private boolean allIndividualsSave() {
        return getProblem().getCellularAutomaton().getNotSafeIndividualsCount() == 0;
    }
    
    private boolean timeOver() {
        return getStep() > neededTime;
    }

    /**
     * Sends a progress event. The progress is defined as the maximum of the percentage of already evacuated individuals
     * and the fraction of time steps of the maximum amount of time steps already simulated.
     *
     * @return the current progress as percentage of safe individuals
     */
    @Override
    protected final double getProgress() {
        double timeProgress = super.getProgress();
        double individualProgress = 1.0 - ((double) getProblem().getCellularAutomaton().getIndividualCount()
                / getProblem().getCellularAutomaton().getInitialIndividualCount());
        return Math.max(individualProgress, timeProgress);
    }
    
    /**
     * An iterator that iterates over all cells of the cellular automaton that contains an individual. The rules of the
     * simulation algorithm are being executed on each of the occupied cells.
     *
     * @return iterator of all occupied cells
     */
    @Override
    public final Iterator<EvacCell> iterator() {
        return new CellIterator(reorder.apply(getProblem().getCellularAutomaton().getIndividuals()));
    }

    /**
     * A simple iterator that iterates over all cells of the cellular automaton that contain an individual. The
     * iteration order equals the order of the individuals given.
     */
    private static class CellIterator implements Iterator<EvacCell> {

        private final Iterator<Individual> individuals;

        /**
         * Initializes the object with a list of individuals whose cells are iterated over.
         *
         * @param individuals the individuals
         */
        private CellIterator(Iterator<Individual> individuals) {
            this.individuals = Objects.requireNonNull(individuals, "Individuals list must not be null.");
        }

        @Override
        public boolean hasNext() {
            return individuals.hasNext();
        }

        @Override
        public EvacCell next() {
            return individuals.next().getCell();
        }

        @Override
        public void remove() {
            throw new AssertionError("Attempted cell removal.");
        }
    }
}
