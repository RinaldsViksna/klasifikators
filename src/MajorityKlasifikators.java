import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;

import weka.classifiers.Classifier;

public class MajorityKlasifikators implements Classifier{
	
	public double majorityClass;
	
	public MajorityKlasifikators() {
		
	}
	
	public void buildClassifier(Instances instances){
		
		double[] classes = new double[instances.classAttribute().numValues()];
		
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            classes[(int) instance.classValue()]++; // Saskaita, cik instances pieder katrai klasei; Classes count: [215.0, 406.0, 176.0]
//            System.out.print(instances.classAttribute().value( (int) instance.classValue()) );

        }
        
        int maxIndex = 0;
        for (int i = 1; i < classes.length; i++) {
            double count = classes[i];
            if ((count > classes[maxIndex])) {
                maxIndex = i;
            }
        }
        System.out.println(maxIndex);
        this.majorityClass = maxIndex; // Saglabā dominantās klases indeksu instances.classAttribute()
	}

	@Override
	public double classifyInstance(Instance arg0) throws Exception {
		return this.majorityClass;
	}

	@Override
	public double[] distributionForInstance(Instance arg0) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}
}


