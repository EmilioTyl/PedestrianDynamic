package itba.edu.ar.simulation.data;

public class PedestrianData extends SiloData {

	private double lowerRadio;
	private double upperRadio;
	private double maxRadio = 0;
	
	public PedestrianData(int totalParticles, double mass, double length, double destinationY, double width,
			double lowerRadio, double upperRadio) {
		super(totalParticles, mass, lowerRadio, length, destinationY, width);
		this.lowerRadio=lowerRadio;
		this.upperRadio=upperRadio;
	}

	@Override
	public double getRadio() {
		double radio = Math.random()*(upperRadio-lowerRadio)+lowerRadio;
		maxRadio = Math.max(maxRadio, radio);
		return radio;
	}
	
	public double getMaxRadio(){
		return maxRadio;
	}
	
	
}
