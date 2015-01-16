package cgl.imr.samples.pwa.quasar.nonsym;

import java.util.List;

import cgl.imr.base.Key;
import cgl.imr.base.ReduceOutputCollector;
import cgl.imr.base.ReduceTask;
import cgl.imr.base.TwisterException;
import cgl.imr.base.Value;
import cgl.imr.base.impl.JobConf;
import cgl.imr.base.impl.ReducerConf;

/**
 * @author Yang Ruan (yangruan at cs dot indiana dot edu)
 */
public class PWAReduceTask implements ReduceTask {
	@Override
	public void close() throws TwisterException {
	}

	@Override
	public void configure(JobConf jobConf, ReducerConf mapConf)
			throws TwisterException {
	}

	@Override
	public void reduce(ReduceOutputCollector collector, Key key,
                       List<Value> values) throws TwisterException {
        MaxValues maxes = new MaxValues(0, 0, 0, 0);
        System.out.println("Reducer Started!");
        for (Value value : values) {
        	System.out.println("Into the loop!");
        	System.out.println("Value: " + value);
        	MaxValues cur = (MaxValues) value;
        	maxes.setDist5d(Math.max(maxes.getDist5d(), cur.getDist5d()));
        	maxes.setWeight5d(Math.max(maxes.getWeight5d(), cur.getWeight5d()));
        	maxes.setDist10d(Math.max(maxes.getDist10d(), cur.getDist10d()));
        	maxes.setWeight10d(Math.max(maxes.getWeight10d(), cur.getWeight10d()));
        }
        collector.collect(key, maxes);
	}
}
