package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CloudletSchedulerHybridScheduling extends CloudletSchedulerDynamicWorkload {
    public CloudletSchedulerHybridScheduling(double mips, int numberOfPes) {
        super(mips, numberOfPes);
    }

    // getter and setter methods are not overridden

    public double updateVmProcessing(double currentTime, List<Double> mipsShare) {
        this.setCurrentMipsShare(mipsShare);
        double timeSpan = currentTime - this.getPreviousTime();
        double nextEvent = 1.7976931348623157E308D;
        List<ResCloudlet> cloudletsToFinish = new ArrayList();
        Iterator var8 = this.getCloudletExecList().iterator();

        ResCloudlet rgl;
        while(var8.hasNext()) {
            rgl = (ResCloudlet)var8.next();
            rgl.updateCloudletFinishedSoFar((long)(timeSpan * this.getTotalCurrentAllocatedMipsForCloudlet(rgl, this.getPreviousTime()) * 1000000.0D));
            if (rgl.getRemainingCloudletLength() == 0L) {
                cloudletsToFinish.add(rgl);
            } else {
                double estimatedFinishTime = this.getEstimatedFinishTime(rgl, currentTime);
                if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
                    estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
                }

                if (estimatedFinishTime < nextEvent) {
                    nextEvent = estimatedFinishTime;
                }
            }
        }

        var8 = cloudletsToFinish.iterator();

        while(var8.hasNext()) {
            rgl = (ResCloudlet)var8.next();
            this.getCloudletExecList().remove(rgl);
            this.cloudletFinish(rgl);
        }

        this.setPreviousTime(currentTime);
        return this.getCloudletExecList().isEmpty() ? 0.0D : nextEvent;
    }
}
