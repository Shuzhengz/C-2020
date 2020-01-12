package com.team1678.frc2020.planners;

import com.team1678.frc2020.Constants;
import com.team254.util.test.ControlledActuatorLinearSim;
import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IndexerMotionPlannerTest {

    @Test
    public void testSmallOffset() {
        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(-90, 90);
        double angleGoal = motion_planner.findAngleToGoal(slotGoal, -90, 90);
        Assert.assertEquals(3, slotGoal);
        Assert.assertEquals(-36, angleGoal, Constants.kTestEpsilon);
    }
    
    @Test
    public void testBigOffset() {
        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(-135, 135);
        double angleGoal = motion_planner.findAngleToGoal(slotGoal, -135, 135);
        Assert.assertEquals(4, slotGoal);
        Assert.assertEquals(-18, angleGoal, Constants.kTestEpsilon);
    }

    @Test
    public void testMegaOffset() {
        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(-450, 451);
        double angleGoal = motion_planner.findAngleToGoal(slotGoal, -450, 451);
        Assert.assertEquals(3, slotGoal);
        Assert.assertEquals(-35, angleGoal, Constants.kTestEpsilon);
    }

    @Test
    public void testMegaPositiveOffset() {
        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(100, 500);
        double angleGoal = motion_planner.findAngleToGoal(slotGoal, 100, 500);
        Assert.assertEquals(1, slotGoal);
        Assert.assertEquals(-32, angleGoal, Constants.kTestEpsilon);
    }

    @Test
    public void testMegaNegativeOffset() {
        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(-1323, -1678);
        double angleGoal = motion_planner.findAngleToGoal(slotGoal, -1323, -1678);
        Assert.assertEquals(0, slotGoal);
        Assert.assertEquals(5, angleGoal, Constants.kTestEpsilon);
    }

    @Test
    public void testNextSlot() {
        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(-135, 135);
        double angleGoal = motion_planner.findAngleToGoal(slotGoal, -135, 135);
        Assert.assertEquals(4, slotGoal);
        Assert.assertEquals(-18, angleGoal, Constants.kTestEpsilon);

        slotGoal = motion_planner.findNextSlot(-153, 135);
        angleGoal = motion_planner.findAngleToGoal(slotGoal, -153, 135);
        Assert.assertEquals(0, slotGoal);
        Assert.assertEquals(-72, angleGoal, Constants.kTestEpsilon);
    }

    @Test
    public void testMovingIndexer() {

        ControlledActuatorLinearSim turretSim = new ControlledActuatorLinearSim(-1000, 1000, 10);
        ControlledActuatorLinearSim indexerSim = new ControlledActuatorLinearSim(-1000, 1000, 45);

        turretSim.reset(90);
        indexerSim.reset(0);

        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(0, 90);
        double angleGoal = 0;
        for (double t = 0; t < 10; t += 0.05) {
            indexerSim.setCommandedPosition(angleGoal);
            turretSim.setCommandedPosition(90);
            angleGoal = motion_planner.findAngleGoal(slotGoal, indexerSim.update(0.05), turretSim.update(0.05));
        }
        final double indexer_angle = motion_planner.WrapDegrees(indexerSim.update(0));
        final double turret_angle = motion_planner.WrapDegrees(turretSim.update(0));
        Assert.assertEquals(1, motion_planner.findNearestSlot(indexer_angle, turret_angle));
        Assert.assertTrue(motion_planner.isAtGoal(slotGoal, indexer_angle, turret_angle));
    }

    @Test
    public void testMovingIndexerAndTurret() {

        ControlledActuatorLinearSim turretSim = new ControlledActuatorLinearSim(-1000, 1000, 10);
        ControlledActuatorLinearSim indexerSim = new ControlledActuatorLinearSim(-1000, 1000, 45);

        turretSim.reset(90);
        indexerSim.reset(0);

        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(0, 90);
        double angleGoal = 0;
        for (double t = 0; t < 10; t += 0.05) {
            indexerSim.setCommandedPosition(angleGoal);
            turretSim.setCommandedPosition(190);
            angleGoal = motion_planner.findAngleGoal(slotGoal, indexerSim.update(0.05), turretSim.update(0.05));
        }
        final double indexer_angle = motion_planner.WrapDegrees(indexerSim.update(0));
        final double turret_angle = motion_planner.WrapDegrees(turretSim.update(0));
        Assert.assertEquals(1, motion_planner.findNearestSlot(indexer_angle, turret_angle));
        Assert.assertTrue(motion_planner.isAtGoal(slotGoal, indexer_angle, turret_angle));
    }

    @Test
    public void testMovingIndexerAndTurretOpposite() {

        ControlledActuatorLinearSim turretSim = new ControlledActuatorLinearSim(-1000, 1000, 10);
        ControlledActuatorLinearSim indexerSim = new ControlledActuatorLinearSim(-1000, 1000, 45);
;
        turretSim.reset(90);
        indexerSim.reset(0);

        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(0, 90);
        double angleGoal = 0;
        for (double t = 0; t < 10; t += 0.05) {
            indexerSim.setCommandedPosition(angleGoal);
            turretSim.setCommandedPosition(-10);
            angleGoal = motion_planner.findAngleGoal(slotGoal, indexerSim.update(0.05), turretSim.update(0.05));
        }
        final double indexer_angle = motion_planner.WrapDegrees(indexerSim.update(0));
        final double turret_angle = motion_planner.WrapDegrees(turretSim.update(0));
        Assert.assertEquals(1, motion_planner.findNearestSlot(indexer_angle, turret_angle));
        Assert.assertTrue(motion_planner.isAtGoal(slotGoal, indexer_angle, turret_angle));
    }

    @Test
    public void testMovingIndexerAndTurretReflexively() {

        ControlledActuatorLinearSim turretSim = new ControlledActuatorLinearSim(-1000, 1000, 10);
        ControlledActuatorLinearSim indexerSim = new ControlledActuatorLinearSim(-1000, 1000, 45);

        turretSim.reset(0);
        indexerSim.reset(0);

        IndexerMotionPlanner motion_planner = new IndexerMotionPlanner();
        int slotGoal = motion_planner.findNearestSlot(0, 0);
        double angleGoal = 0;
        for (double t = 0; t < 69; t += 0.05) {
            indexerSim.setCommandedPosition(angleGoal);
            turretSim.setCommandedPosition(-360);
            angleGoal = motion_planner.findAngleGoal(slotGoal, indexerSim.update(0.05), turretSim.update(0.05));
        }
        final double indexer_angle = motion_planner.WrapDegrees(indexerSim.update(20));
        final double turret_angle = motion_planner.WrapDegrees(turretSim.update(0));
        Assert.assertEquals(0, motion_planner.findNearestSlot(indexer_angle, turret_angle));
        Assert.assertTrue(motion_planner.isAtGoal(slotGoal, indexer_angle, turret_angle));
    }
}