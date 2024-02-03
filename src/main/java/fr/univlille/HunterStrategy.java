package fr.univlille;

import java.util.Random;

import fr.univlille.iutinfo.cam.player.hunter.IHunterStrategy;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;

public class HunterStrategy implements IHunterStrategy {

    private int mazeWidth;
    private int mazeHeight;

    @Override
    public ICoordinate play() {
        Random random = new Random();
        return new Coordinate(random.nextInt(mazeWidth), random.nextInt(mazeHeight));
    }

    @Override
    public void update(ICellEvent arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void initialize(int arg0, int arg1) {
        this.mazeWidth = arg0;
        this.mazeHeight = arg1;
    }

}