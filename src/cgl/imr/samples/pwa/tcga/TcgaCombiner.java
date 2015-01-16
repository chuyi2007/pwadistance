package cgl.imr.samples.pwa.tcga;

import java.util.Iterator;
import java.util.Map;
import cgl.imr.base.Combiner;
import cgl.imr.base.Key;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.types.DoubleValue;

public class TcgaCombiner implements Combiner{
	private double maximumDistance;

	@Override
	public void combine(Map<Key, Value> keyValues) throws TwisterException {
		//System.out.println("Combiner reached!");
		Iterator<Key> ite = keyValues.keySet().iterator();

		while (ite.hasNext()) {
			Key key = ite.next();
			DoubleValue dVal = (DoubleValue) keyValues.get(key);
			if(dVal.getVal() > maximumDistance)
				maximumDistance = dVal.getVal();
		}
	}

	@Override
	public void configure(JobConf jobConf) throws TwisterException {
		// TODO Auto-generated method stub
		maximumDistance = -Double.MAX_VALUE;
	}

	public double getResults() {
		//System.out.println("Debug 6:");
		return maximumDistance;
	}
}
