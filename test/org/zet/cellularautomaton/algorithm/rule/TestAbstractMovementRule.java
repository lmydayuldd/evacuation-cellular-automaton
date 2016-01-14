package org.zet.cellularautomaton.algorithm.rule;

import org.zet.cellularautomaton.algorithm.EvacuationState;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.EvacuationCellState;
import org.zet.cellularautomaton.EvacuationCellularAutomaton;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.IndividualBuilder;
import org.zet.cellularautomaton.algorithm.EvacuationSimulationProblem;
import org.zet.cellularautomaton.algorithm.IndividualState;
import org.zet.cellularautomaton.algorithm.rule.TestInitialConcretePotentialRule.TestIndividualState;
import org.zetool.common.util.Direction8;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TestAbstractMovementRule {
    private final static Direction8 DEFAULT_DIRECTION = Direction8.Top;
    private final static IndividualBuilder builder = new IndividualBuilder();
    private Mockery context = new Mockery();
    private TestIndividualState is;

    private class FakeEvacCell extends EvacCell {
        private final boolean isFreeNeighbors;
        private List<EvacCell> neighbors = Collections.EMPTY_LIST;
        boolean safe;
        Direction8 direction = DEFAULT_DIRECTION;
        public FakeEvacCell() {
            this(false);
        }
        public FakeEvacCell(boolean freeNeighbors) {
            super(new EvacuationCellState(null), 1, 0, 0);
            this.isFreeNeighbors = freeNeighbors;
        }

        @Override
        public EvacCell clone() {
            return null;
        }

        @Override
        public List<EvacCell> getFreeNeighbours() {
            if(!isFreeNeighbors) {
                throw new AssertionError("Free neighbors not set!");
            }
            return neighbors;
        }

        @Override
        public List<EvacCell> getNeighbours() {
            if(isFreeNeighbors) {
                throw new AssertionError("Free neighbors set!");
            }
            return neighbors;
        }

        @Override
        public boolean isSafe() {
            return safe;
        }
        
        @Override
        public Direction8 getRelative(EvacCell c) {
            if( c instanceof FakeEvacCell ) {
                return ((FakeEvacCell)c).direction;
            }
            return direction;
        }
    }

    AbstractMovementRule rule;
    
    @Before
    public void initRule() {
        rule = new AbstractMovementRule() {

            @Override
            public void move(EvacCell from, EvacCell target) {
            }

            @Override
            public void swap(EvacCell cell1, EvacCell cell2) {
            }

            @Override
            protected void onExecute(EvacCell cell) {
            }
        };
        EvacuationState es = context.mock(EvacuationState.class);
        is = new TestIndividualState();
        context.checking(new Expectations() {
            {
                allowing(es).getIndividualState();
                will(returnValue(is));
            }
        });
        rule.setEvacuationState(es);
    }
        
    @Test
    public void testPossibleTargetsNeighbors() {
        Individual i = builder.withAge(0).build();

        EvacCell cell = new FakeEvacCell(true);
        cell.getState().setIndividual(i);
        
        assertThat(rule.computePossibleTargets(cell, true), is(empty()));

        cell = new FakeEvacCell(false);
        cell.getState().setIndividual(i);
        
        assertThat(rule.computePossibleTargets(cell, false), is(empty()));
    }
    
    @Test
    public void testSafeNeighbors() {
        Individual i = builder.build();
        is.addIndividual(i);
        is.setSafe(i);
        i.setDirection(DEFAULT_DIRECTION);
        
        List<EvacCell> cellList = new LinkedList<>();
        
        EvacCell n1 = new FakeEvacCell();
        cellList.add(n1);
        FakeEvacCell n2 = new FakeEvacCell();
        n2.safe = true;
        cellList.add(n2);
        FakeEvacCell n3 = new FakeEvacCell();
        n3.safe = true;
        cellList.add(n3);
        EvacCell n4 = new FakeEvacCell();
        cellList.add(n4);
        
        FakeEvacCell cell = new FakeEvacCell();
        cell.getState().setIndividual(i);
        cell.neighbors = cellList;
        
        
        List<EvacCell> result = rule.computePossibleTargets(cell, false);
        assertThat(result, hasSize(2));
        assertThat(result, Matchers.hasItem(n2));
        assertThat(result, Matchers.hasItem(n3));
    }
    
    @Test
    public void testDirections() {
        Individual individual = builder.build();
        is.addIndividual(individual);
        individual.setDirection(DEFAULT_DIRECTION);
        
        List<EvacCell> cellList = new ArrayList<>(Direction8.values().length);

        for( Direction8 dir : Direction8.values()) {
            FakeEvacCell cell = new FakeEvacCell();
            cell.direction = dir;
            cellList.add(cell);
        }
        
        FakeEvacCell cell = new FakeEvacCell();
        cell.getState().setIndividual(individual);
        cell.neighbors = cellList;


        List<EvacCell> result = rule.computePossibleTargets(cell, false);
        assertThat(result, hasSize(5));
        for( int i : new int[] {0, 1, 2, 6, 7}) {
            assertThat(result, Matchers.hasItem(cellList.get(i)));        
        }
    }
    
    @Test
    public void testTargetSelection() {
        List<EvacCell> cells = new LinkedList<>();
        FakeEvacCell cell1 = new FakeEvacCell();
        FakeEvacCell cell2 = new FakeEvacCell();
        cells.add(cell1);
        cells.add(cell2);
        
        EvacCell result = rule.selectTargetCell(new FakeEvacCell(), cells);
        assertThat(result, is(equalTo(cell1)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTargetSelectionRequiresNonEmptyList() {
        rule.selectTargetCell(new FakeEvacCell(), Collections.EMPTY_LIST);
    }
    
    @Test
    public void testSway() {
        Individual individual = builder.build();
        individual.setDirection(DEFAULT_DIRECTION);
        assertThat(rule.getSwayDelay(individual, Direction8.Top), is(closeTo(0, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.TopLeft), is(closeTo(0.5, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.TopRight), is(closeTo(0.5, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.Left), is(closeTo(1, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.Right), is(closeTo(1, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.DownLeft), is(closeTo(2, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.DownRight), is(closeTo(2, 10e-8)));
        assertThat(rule.getSwayDelay(individual, Direction8.Down), is(closeTo(2, 10e-8)));
    }
    
    @Test
    public void testStepEndTime() {
        EvacuationSimulationProblem esp = context.mock(EvacuationSimulationProblem.class);
        EvacuationCellularAutomaton eca = new EvacuationCellularAutomaton();
        context.checking(new Expectations() {
            {
                allowing(esp).getCellularAutomaton();
                will(returnValue(eca));
                allowing(rule.es).setNeededTime(with(3));
            }
        });        
        
        Individual i = builder.build();
        rule.setStepEndTime(i, 2.68);

        assertThat(i.getStepEndTime(), is(closeTo(2.68, 10e-8)));
    }
    
    @Test
    public void testInitialization() {
        assertThat(rule.isDirectExecute(), is(true));
        assertThat(rule.isMoveCompleted(), is(false));
        
    }
    
    @Test
    public void testSimpleGetters() {
        rule.setMoveRuleCompleted(true);
        rule.setDirectExecute(false);
        assertThat(rule.isMoveCompleted(), is(true));
        assertThat(rule.isDirectExecute(), is(false));
    }
    
}
