package com.team1678.frc2020.auto.actions;

import com.team1678.frc2020.subsystems.Shooter;

/**
 * Waits until the shooter and feeder are up to speed, used befoer shooting
 * 
 * @see Action
 */
public class WaitForSpinupAction implements Action {

    private Shooter mShooter = Shooter.getInstance();

    public WaitForSpinupAction() {
    }

    @Override
    public boolean isFinished() {
        return mShooter.spunUp();

    }

    @Override
    public void update() {
    }

    @Override
    public void done() {
    }

    @Override
    public void start() {
    }
}