package org.cloudbus.cloudsim.examples

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

import java.text.DecimalFormat
import java.util
import java.util.Calendar

import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}
import org.cloudbus.cloudsim.{Cloudlet, CloudletSchedulerTimeShared, Datacenter, DatacenterBroker, DatacenterCharacteristics, Host, Log, Pe, Storage, UtilizationModelFull, Vm, VmAllocationPolicySimple, VmSchedulerTimeShared}

import scala.collection.JavaConverters._


/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


/**
  * A simple example showing how to create a data center with one host and run one cloudlet on it.
  */
object Simulation2 extends App {
  /** The cloudlet list. */
  val cloudletList = List[Cloudlet]()
  /** The vmlist. */
  val vmlist = List[Vm]()

  //@SuppressWarnings(Array("unused")) def main(args: Array[String]): Unit = {
  Log.printLine("Starting CloudSimExample1...")
  try { // First step: Initialize the CloudSim package. It should be called before creating any entities.
    val num_user = 1
    // number of cloud users
    val calendar = Calendar.getInstance()
    // Calendar whose fields have been initialized with the current date and time.
    val trace_flag = false // trace events
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
           */ CloudSim.init(num_user, calendar, trace_flag)
    // Second step: Create Datacenters
    // Datacenters are the resource providers in CloudSim. We need at
    // list one of them to run a CloudSim simulation
    val datacenter0 = createDatacenter("Datacenter_0")
    // Third step: Create Broker
    val Some(broker) = createBroker
    val brokerId = broker.getId
    // Fourth step: Create one virtual machine
    //val vmlist = new util.ArrayList[Vm]
    // VM description
    val vmid = 0
    val mips = 1000
    val size = 10000
    // image size (MB)
    val ram = 512
    // vm memory (MB)
    val bw = 1000
    val pesNumber = 1
    // number of cpus
    val vmm = "Xen"
    // VMM name
    // create VM
    val vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared)
    // add the VM to the vmList
    val vmlist = List(vm)
    // submit vm list to the broker
    broker.submitVmList(vmlist.asJava)
    // Fifth step: Create one Cloudlet
    //cloudletList = new util.ArrayList[Cloudlet]
    // Cloudlet properties
    val id = 0
    val length = 400000
    val fileSize = 300
    val outputSize = 300
    val utilizationModel = new UtilizationModelFull
    val cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
    cloudlet.setUserId(brokerId)
    cloudlet.setVmId(vmid)
    // add the cloudlet to the list
    val cloudletList = List(cloudlet)
    // submit cloudlet list to the broker
    broker.submitCloudletList(cloudletList.asJava)
    // Sixth step: Starts the simulation
    CloudSim.startSimulation()
    CloudSim.stopSimulation()
    //Final step: Print results when simulation is over
    val newList = broker.getCloudletReceivedList
    printCloudletList(newList)
    Log.printLine("CloudSimExample1 finished!")
  } catch {
    case e: Exception =>
      e.printStackTrace()
      Log.printLine("Unwanted errors happen")
  }
  //}

  /**
    * Creates the datacenter.
    *
    * @param name the name
    * @return the datacenter
    */
  private def createDatacenter(name: String): Option[Datacenter] = { // Here are the steps needed to create a PowerDatacenter:
    // 1. We need to create a list to store
    // our machine
    val hostList = new util.ArrayList[Host]
    // 2. A Machine contains one or more PEs or CPUs/Cores.
    // In this example, it will have only one core.
    val peList = new util.ArrayList[Pe]
    val mips = 1000
    // 3. Create PEs and add these into a list.
    peList.add(new Pe(0, new PeProvisionerSimple(mips))) // need to store Pe id and MIPS Rating

    // 4. Create Host with its id and list of PEs and add them to the list
    // of machines
    val hostId = 0
    val ram = 2048
    // host memory (MB)
    val storage = 1000000
    // host storage
    val bw = 10000
    hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList))) // This is our machine

    // 5. Create a DatacenterCharacteristics object that stores the
    // properties of a data center: architecture, OS, list of
    // Machines, allocation policy: time- or space-shared, time zone
    // and its price (G$/Pe time unit).
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
    val costPerStorage = 0.001
    // the cost of using storage in this
    // resource
    val costPerBw = 0.0
    // the cost of using bw in this resource
    val storageList = new util.LinkedList[Storage]
    // we are not adding SAN
    // devices by now
    val characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw)
    // 6. Finally, we need to create a PowerDatacenter object.
    try {
      //val datacenter: Datacenter = null
      val datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
      Some(datacenter)
    }
    catch {
      case e: Exception => e.printStackTrace()
        None
    }
  }

  /**
    * Creates the broker.
    *
    * @return the datacenter broker
    */
  private def createBroker: Option[DatacenterBroker] = {
    //var broker: DatacenterBroker = null
    try {
      val broker = new DatacenterBroker("Broker")
      Some(broker)
    }
    catch {
      case e: Exception => e.printStackTrace()
        None
    }
    //broker
  }

  /**
    * Prints the Cloudlet objects.
    *
    * @param list list of Cloudlets
    */
  private def printCloudletList(list: util.List[_ <: Cloudlet]): Unit = {
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time")
    val dft = new DecimalFormat("###.##")
    list.forEach(cloudlet => {
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent +
          indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime)
          + indent + indent + dft.format(cloudlet.getFinishTime))
      }
    })
  }
}
