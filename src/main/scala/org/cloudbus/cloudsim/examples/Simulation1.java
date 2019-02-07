package org.cloudbus.cloudsim.examples;

import ch.qos.logback.classic.Logger;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * A CloudSim Simulation showing how to create a data center with one host and run one cloudlet on it.
 */
public class Simulation1 {

    static final Logger LOG = (Logger) LoggerFactory.getLogger(Simulation1.class);

    /**
     * The cloudlet list.
     */
    private static List<Cloudlet> cloudletList;
    /**
     * The vmlist.
     */
    private static List<Vm> vmlist;

    /**
     * Creates main() to run this example.
     *
     * @param args the args
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {

        Config defaultConfig = ConfigFactory.parseResources("defaults.conf");

        Config fallbackConfig = ConfigFactory.parseResources("overrides.conf")
                .withFallback(defaultConfig)
                .resolve();

        Log.printLine("Starting CloudSimExample1...");

        try {
            // First step: Initialize the CloudSim package. It should be called before creating any entities.
            int num_user = fallbackConfig.getInt("cloud.num_user"); // number of cloud users
            Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
            boolean trace_flag = false; // trace events

            /* Comment Start - Dinesh Bhagwat
             * Initialize the CloudSim library.
             * init() invokes initCommonVariable() which in turn calls initialize() (all these 3 methods are defined in CloudSim.java).
             * initialize() creates two collections - an ArrayList of SimEntity Objects (named entities which denote the simulation entities) and
             * a LinkedHashMap (named entitiesByName which denote the LinkedHashMap of the same simulation entities), with name of every SimEntity as the key.
             * initialize() creates two queues - a Queue of SimEvents (future) and another Queue of SimEvents (deferred).
             * initialize() creates a HashMap of of Predicates (with integers as keys) - these predicates are used to select a particular event from the deferred queue.
             * initialize() sets the simulation clock to 0 and running (a boolean flag) to false.
             * Once initialize() returns (note that we are in method initCommonVariable() now), a CloudSimShutDown (which is derived from SimEntity) instance is created
             * (with numuser as 1, its name as CloudSimShutDown, id as -1, and state as RUNNABLE). Then this new entity is added to the simulation
             * While being added to the simulation, its id changes to 0 (from the earlier -1). The two collections - entities and entitiesByName are updated with this SimEntity.
             * the shutdownId (whose default value was -1) is 0
             * Once initCommonVariable() returns (note that we are in method init() now), a CloudInformationService (which is also derived from SimEntity) instance is created
             * (with its name as CloudInformatinService, id as -1, and state as RUNNABLE). Then this new entity is also added to the simulation.
             * While being added to the simulation, the id of the SimEntitiy is changed to 1 (which is the next id) from its earlier value of -1.
             * The two collections - entities and entitiesByName are updated with this SimEntity.
             * the cisId(whose default value is -1) is 1
             * Comment End - Dinesh Bhagwat
             */
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            // Datacenters are the resource providers in CloudSim. We need at
            // list one of them to run a CloudSim simulation
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            LOG.info("Broker is being created");
            // Third step: Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Fourth step: Create one virtual machine
            vmlist = new ArrayList<Vm>();

            // VM description
            int vmid = fallbackConfig.getInt("vm.vmid");
            int mips = fallbackConfig.getInt("vm.mips");
            long size = fallbackConfig.getLong("vm.size"); // image size (MB)
            int ram = fallbackConfig.getInt("vm.ram"); // vm memory (MB)
            long bw = fallbackConfig.getLong("vm.bw");
            int pesNumber = fallbackConfig.getInt("vm.pesNumber"); // number of cpus
            String vmm = fallbackConfig.getString("vm.vmm");
            ; // VMM name

            // create VM
            int cloudletSchedulingPolicy = fallbackConfig.getInt("vm.cloudletScheduling");
            Vm vm = null;
            switch (cloudletSchedulingPolicy) {
                case 1:
                    vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips, pesNumber));
                    break;
                case 2:
                    vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
                    break;
                case 3:
                    vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
                    break;
            }

            // add the VM to the vmList
            vmlist.add(vm);

            // submit vm list to the broker
            broker.submitVmList(vmlist);

            // Fifth step: Create one Cloudlet
            cloudletList = new ArrayList<Cloudlet>();

            // Cloudlet properties
            int id = fallbackConfig.getInt("cloudlet.id");
            long length = fallbackConfig.getLong("cloudlet.length");
            long fileSize = fallbackConfig.getLong("cloudlet.fileSize");
            long outputSize = fallbackConfig.getLong("cloudlet.outputSize");
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet cloudlet =
                    new Cloudlet(id, length, pesNumber, fileSize,
                            outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(vmid);

            // add the cloudlet to the list
            cloudletList.add(cloudlet);

            // submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            //Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);

            Log.printLine("CloudSimExample1 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the datacenter.
     *
     * @param name the name
     * @return the datacenter
     */
    private static Datacenter createDatacenter(String name) {

        Config defaultConfig = ConfigFactory.parseResources("defaults.conf");

        Config fallbackConfig = ConfigFactory.parseResources("overrides.conf")
                .withFallback(defaultConfig)
                .resolve();

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        // our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = fallbackConfig.getInt("pe.mips");

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        // 4. Create Host with its id and list of PEs and add them to the list
        // of machines
        int hostId = fallbackConfig.getInt("host.hostId");
        int ram = fallbackConfig.getInt("host.ram"); // host memory (MB)
        long storage = fallbackConfig.getLong("host.storage"); // host storage
        int bw = fallbackConfig.getInt("host.bw");

        int vmSchedulingPolicy = fallbackConfig.getInt("host.vmScheduling");

        Host currentHost = null;
        switch (vmSchedulingPolicy) {
            case 1:
                currentHost = new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList));
                break;
            case 2:
                currentHost = new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerSpaceShared(peList));
                break;
            case 3:
                currentHost = new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeSharedOverSubscription(peList));
                break;
        }

        hostList.add(currentHost); // This is our machine

        // 5. Create a DatacenterCharacteristics object that stores the
        // properties of a data center: architecture, OS, list of
        // Machines, allocation policy: time- or space-shared, time zone
        // and its price (G$/Pe time unit).
        String arch = fallbackConfig.getString("datacenter.arch"); // system architecture
        String os = fallbackConfig.getString("datacenter.os"); // operating system
        String vmm = fallbackConfig.getString("datacenter.vmm");
        double time_zone = fallbackConfig.getDouble("datacenter.time_zone"); // time zone this resource located
        double cost = fallbackConfig.getDouble("datacenter.cost"); // the cost of using processing in this resource
        double costPerMem = fallbackConfig.getDouble("datacenter.costPerMem"); // the cost of using memory in this resource
        double costPerStorage = fallbackConfig.getDouble("datacenter.costPerStorage"); // the cost of using storage in this
        // resource
        double costPerBw = fallbackConfig.getDouble("datacenter.costPerBw"); // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    // We strongly encourage users to develop their own broker policies, to
    // submit vms and cloudlets according
    // to the specific rules of the simulated scenario

    /**
     * Creates the broker.
     *
     * @return the datacenter broker
     */
    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}