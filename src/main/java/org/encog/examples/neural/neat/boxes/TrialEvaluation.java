package org.encog.examples.neural.neat.boxes;

import org.encog.mathutil.IntPair;
import org.encog.ml.MLMethod;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNetwork;

public class TrialEvaluation {

	private final MLMethod phenotype;
	private final BoxTrialCase test;
	private double accDistance;
	private double accRange;
	private double minActivation;
    private double maxActivation;
    private MLData output;
	
	public TrialEvaluation(MLMethod thePhenotype, BoxTrialCase theTest) {
		this.phenotype = thePhenotype;
		this.test = theTest;
	}

	/**
	 * @return the phenotype
	 */
	public MLMethod getPhenotype() {
		return phenotype;
	}

	/**
	 * @return the test
	 */
	public BoxTrialCase getTest() {
		return test;
	}

	public void accumulate(double distance, double range) {
		this.accDistance+=distance;
		this.accRange+=range;
	}

	/**
	 * @return the accDistance
	 */
	public double getAccDistance() {
		return accDistance;
	}

	/**
	 * @param accDistance the accDistance to set
	 */
	public void setAccDistance(double accDistance) {
		this.accDistance = accDistance;
	}

	/**
	 * @return the accRange
	 */
	public double getAccRange() {
		return accRange;
	}

	/**
	 * @param accRange the accRange to set
	 */
	public void setAccRange(double accRange) {
		this.accRange = accRange;
	}

	public double calculateFitness() {
        final double threshold = BoxesScore.EDGE_LEN * BoxesScore.SQR_LEN;
        double rmsd = Math.sqrt(this.accDistance / 75.0);
        double fitness;
        if(rmsd > threshold) {
            fitness = 0.0;
        } else {
            fitness = (((threshold-rmsd) * 100.0) / threshold) + (this.accRange / 7.5);
        }

        return fitness;
	}
	
	public IntPair query(int resolution) {
		// first, create the input data
    	int inputIdx = 0;
        MLData inputData = new BasicMLData(resolution*resolution);
        double pixelSize = 2.0 / resolution;
        double orig = -1.0 + (pixelSize/2.0);
        
        double yReal = orig;
        for(int y=0; y<resolution; y++, yReal += pixelSize)
        {
            double xReal = orig;
            for(int x=0; x<resolution; x++, xReal += pixelSize, inputIdx++)
            {
            	inputData.setData(inputIdx, this.test.getPixel(xReal, yReal));
            }
        }
        
        // second, query the network
        output = ((NEATNetwork)this.phenotype).compute(inputData);
        
        // finally, process the output
        minActivation = Double.POSITIVE_INFINITY;
        maxActivation = Double.NEGATIVE_INFINITY;
        int idx = 0;

        for(int i=0; i<output.size(); i++)
        {
            double d = output.getData(i);

            if(d > maxActivation)
            {
                maxActivation = d;
                idx = i;
            } 
            else if(d < minActivation)
            {
                minActivation = d;
            }
        }

        int y = idx / resolution;
        int x = idx - (y * resolution);
        return new IntPair(x, y);
	}

	/**
	 * @return the minActivation
	 */
	public double getMinActivation() {
		return minActivation;
	}

	/**
	 * @return the maxActivation
	 */
	public double getMaxActivation() {
		return maxActivation;
	}

	/**
	 * @return the output
	 */
	public MLData getOutput() {
		return output;
	}

	public int normalize(double d, int i) {
		return (int)(((d-this.minActivation)/(this.maxActivation-this.minActivation))*i);
	}
	
	
	
}