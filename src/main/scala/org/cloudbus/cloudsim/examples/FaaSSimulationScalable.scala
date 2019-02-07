package org.cloudbus.cloudsim.examples

import java.text.DecimalFormat
import java.util
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.cloudbus.cloudsim._
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}

import scala.collection.JavaConverters._
import scala.collection.{immutable, mutable}

/*
 * A CloudSim Simulation showing how to create scalable simulations.
 */

// main-class
object FaaSSimulationScalable extends App with LazyLogging {

  // Lightbend (Typesafe) configuration manager loading .conf files
  val defaultConfig = ConfigFactory.parseResources("defaults.conf")
  val fallbackConfig = ConfigFactory.parseResources("overrides.conf").withFallback(defaultConfig).resolve
  logger.info("Configuration files loaded")

  // Creates a container to store VMs. This list is passed to the broker later
  private def createVM(userId: Int, vms: Int) = {
    val list = new mutable.ListBuffer[Vm]()

    //VM Parameters
    //VM Description
    val size = 10000
    //image size (MB)
    val ram = 512
    //vm memory (MB)
    val mips = 1000
    val bw = 1000
    val pesNumber = 1
    //number of cpus
    val vmm = "Xen"
    //VMM name
    //create VMs
    val vm = new Array[Vm](vms)
    val range_vms = immutable.List.range(0, vms)
    range_vms.foreach(vm_no => {
      vm(vm_no) = new Vm(vm_no, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared)
      //for creating a VM with a space shared scheduling policy for cloudlets:
      //vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());
      list += vm(vm_no)
    })
    list
  }

  // Creates a container to store Cloudlets
  private def createCloudlet(userId: Int, cloudlets: Int) = {
    val list = new mutable.ListBuffer[Cloudlet]()
    //cloudlet parameters
    val length = 1000
    val fileSize = 300
    val outputSize = 300
    val pesNumber = 1
    val utilizationModel = new UtilizationModelFull
    val cloudlet = new Array[Cloudlet](cloudlets)
    val range_cloudlets = immutable.List.range(0, cloudlets)
    range_cloudlets.foreach(cloudlet_no => {
      cloudlet(cloudlet_no) = new Cloudlet(cloudlet_no, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
      // setting the owner of these Cloudlets
      cloudlet(cloudlet_no).setUserId(userId)
      list += cloudlet(cloudlet_no)
    })
    list
  }

  /*
   * Creates main() to run this example
   */
  Log.printLine("Starting CloudSimExample6...")

  // First step: Initialize the CloudSim package. It should be called before creating any entities
  try {

    // number of grid (cloud) users
    val num_user = fallbackConfig.getInt("broker.num_user")

    // Calendar whose fields have been initialized with the current date and time
    val calendar = Calendar.getInstance

    // boolean to toggle trace events
    val trace_flag = false

    /*
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
     */

    // Initialize the CloudSim library
    CloudSim.init(num_user, calendar, trace_flag)
    logger.info("Cloudsim library initialized")

    // Second step: Create Datacenters
    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
    @SuppressWarnings(Array("unused")) val datacenter0 = createDatacenter("Datacenter_0")
    @SuppressWarnings(Array("unused")) val datacenter1 = createDatacenter("Datacenter_1")
    logger.info("CIS created")
    logger.info("Datacenter creation completed")

    //Third step: Create Broker
    val Some(broker) = createBroker
    logger.info("Broker instance created")

    val brokerId = broker.getId

    // Fourth step: Create VMs and Cloudlets and send them to broker
    // The Vm List
    val vmlist = createVM(brokerId, 20) //creating 20 vms

    //The Cloudlet List
    val cloudletList = createCloudlet(brokerId, 40) // creating 40 cloudlets

    broker.submitVmList(vmlist.toList.asJava)
    broker.submitCloudletList(cloudletList.toList.asJava)
    logger.info("CloudletList and vmList submitted to broker")

    // Fifth step: Starts the simulation
    CloudSim.startSimulation
    logger.info("Simulation started")

    // Final step: Print results when simulation is over
    val newList = broker.getCloudletReceivedList
    CloudSim.stopSimulation()
    logger.info("Simulation stopped")

    printCloudletList(newList)
    Log.printLine("CloudSimExample6 finished!")
  } catch {
    case e: Exception =>
      e.printStackTrace()
      Log.printLine("The simulation has been terminated due to an unexpected error")
  }

  private def createDatacenter(name: String) = { // Here are the steps needed to create a PowerDatacenter:

    val defaultConfig = ConfigFactory.parseResources("defaults.conf")

    val fallbackConfig = ConfigFactory.parseResources("overrides.conf").withFallback(defaultConfig).resolve

    // 1. We need to create a list to store one or more
    //    Machines
    val hostList = new util.ArrayList[Host]
    // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
    //    create a list to store these PEs before creating
    //    a Machine.
    val peList1 = new util.ArrayList[Pe]
    val mips = 1000
    // 3. Create PEs and add these into the list.
    //for a quad-core machine, a list of 4 PEs is required:
    peList1.add(new Pe(0, new PeProvisionerSimple(mips))) // need to store Pe id and MIPS Rating

    peList1.add(new Pe(1, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(2, new PeProvisionerSimple(mips)))
    peList1.add(new Pe(3, new PeProvisionerSimple(mips)))

    //Another list, for a dual-core machine
    val peList2 = new util.ArrayList[Pe]
    peList2.add(new Pe(0, new PeProvisionerSimple(mips)))
    peList2.add(new Pe(1, new PeProvisionerSimple(mips)))

    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    val hostId1 = 0
    val ram = 2048
    //host memory (MB)
    val storage = 1000000
    //host storage
    val bw = 10000
    hostList.add(new Host(hostId1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeShared(peList1))) // This is our first machine

    val hostId2 = 1
    hostList.add(new Host(hostId2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2, new VmSchedulerTimeShared(peList2))) // Second machine

    /*


            //To create a host with a space-shared allocation policy for PEs to VMs:
            hostList.add(
                    new Host(
                  hostId,
                  new RamProvisionerSimple(ram),
                  new BwProvisionerSimple(bw),
                  storage,
                  peList1,
                  new VmSchedulerSpaceShared(peList1)
                )
              );
            */
    //To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
    //hostList.add(
    //		new Host(
    //			hostId,
    //			new CpuProvisionerSimple(peList1),
    //			new RamProvisionerSimple(ram),
    //			new BwProvisionerSimple(bw),
    //			storage,
    //			new VmSchedulerOportunisticSpaceShared(peList1)
    //		)
    //	);
    // 5. Create a DatacenterCharacteristics object that stores the
    //    properties of a data center: architecture, OS, list of
    //    Machines, allocation policy: time- or space-shared, time zone
    //    and its price (G$/Pe time unit).
    val arch = "x86"
    // system architecture
    val os = "Linux"
    // operating system
    val vmm = "Xen"
    val time_zone = 10.0
    // time zone this resource located
    val cost = 3.0
    // the cost of using processing in this resource
    val costPerMem = 0.05
    // the cost of using memory in this resource
    val costPerStorage = 0.1
    // the cost of using storage in this resource
    val costPerBw = 0.1
    // the cost of using bw in this resource
    val storageList = new util.LinkedList[Storage]
    //we are not adding SAN devices by now
    val characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw)
    // 6. Finally, we need to create a PowerDatacenter object.
    try {
      val datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
      Some(datacenter)
    }
    catch {
      case e: Exception => e.printStackTrace()
        None
    }
  }

  //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
  //to the specific rules of the simulated scenario
  private def createBroker = try {
    val broker = new DatacenterBroker("Broker")
    Some(broker)
  }
  catch {
    case e: Exception => e.printStackTrace()
      None
  }

  /**
    * Prints the Cloudlet objects
    *
    * @param list list of Cloudlets
    */
  private def printCloudletList(list: util.List[_ <: Cloudlet]): Unit = {
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time")
    val dft = new DecimalFormat("###.##")
    list.forEach(cloudlet => {
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime))
      }
    })
  }
}
// TODO - ad switch case of time sapce, logerr.warn, fallback, cloudlet settings and vm settings
