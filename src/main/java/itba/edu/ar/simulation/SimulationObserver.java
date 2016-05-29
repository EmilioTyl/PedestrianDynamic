package itba.edu.ar.simulation;

import java.io.IOException;
import java.util.List;

import itba.edu.ar.cellIndexMethod.data.particle.Particle;

public interface SimulationObserver {

	public void simulationEnded(double time) throws IOException;

	public void stepEnded(List<Particle> particles,double time) throws IOException;

}
