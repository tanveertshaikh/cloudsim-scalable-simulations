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
 *
 * @author Tanveer Shaikh
 */

// main-class
object HybridFaaSSimulationScalable extends App with LazyLogging {

  // Lightbend (Typesafe) configuration manager loading .conf files
  val defaultConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
  val fallbackConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
    .withFallback(HybridFaaSSimulationScalable.defaultConfig).resolve
  logger.info("Configuration files loaded")

  // Creates a container to store VMs. This list is passed to the broker later
  def createVM(userId: Int, vms: Int) = {

    val defaultConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
    val fallbackConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
      .withFallback(defaultConfig).resolve
    logger.info("Configuration files loaded")

    val list = new mutable.ListBuffer[Vm]()

    //VM Parameters describing the virtual machine
    val size = fallbackConfig.getInt("vm.size")

    //image size (MB)
    val ram = fallbackConfig.getInt("vm.ram")

    //vm memory (MB)
    val mips = fallbackConfig.getInt("vm.mips")

    val bw = fallbackConfig.getInt("vm.bw")

    //number of CPUs
    val pesNumber = fallbackConfig.getInt("vm.pesNumber")

    //VMM name
    val vmm = fallbackConfig.getString("vm.vmm")

    //create VMs
    val vm = new Array[Vm](vms)

    val range_vms = immutable.List.range(0, vms)(Numeric.IntIsIntegral)
    range_vms.foreach((vm_no: Int) => {

      val cloudletScheduling = 4
      //val cloudletScheduling = fallbackConfig.getInt("vm.cloudletScheduling")

      vm(vm_no) = cloudletScheduling match {
        case 1 => new Vm(vm_no, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerDynamicWorkload(mips, pesNumber))
        case 2 => new Vm(vm_no, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared)
        case 3 => new Vm(vm_no, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared)
          //broker generated cloudlet scheduling policy (algorithm)
        case 4 => new Vm(vm_no, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerHybridScheduling(mips, pesNumber))
      }
      vm(vm_no)

      //vm(vm_no) = new Vm(vm_no, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared)
      //for creating a VM with a space shared scheduling policy for cloudlets:
      //vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());
      list += vm.apply(vm_no)
    })
    list
  }

  // Creates a container to store Cloudlets
  def createCloudlet(userId: Int, cloudlets: Int) = {

    val defaultConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
    val fallbackConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
      .withFallback(defaultConfig).resolve
    logger.info("Configuration files loaded")

    val list = new mutable.ListBuffer[Cloudlet]()

    // Cloudlet properties (parameters to create Cloudlet instance)
    val length = fallbackConfig.getInt("cloudlet.length")
    val fileSize = fallbackConfig.getInt("cloudlet.fileSize")
    val outputSize = fallbackConfig.getInt("cloudlet.outputSize")
    val pesNumber = fallbackConfig.getInt("cloudlet.pesNumber")

    val utilizationModelToObject = Map(
      1 -> new UtilizationModelFull,
      2 -> new UtilizationModelNull,
      3 -> new UtilizationModelStochastic
    )

    val cloudletUtilizationModel = utilizationModelToObject(fallbackConfig.getInt("cloudlet.cloudletUtilizationModel"))

    val cloudlet = new Array[Cloudlet](cloudlets)
    val range_cloudlets = immutable.List.range(0, cloudlets)(Numeric.IntIsIntegral)
    range_cloudlets.foreach((cloudlet_no: Int) => {
      cloudlet(cloudlet_no) = new CloudletWithCost(cloudlet_no, length, pesNumber, fileSize, outputSize,
        cloudletUtilizationModel, cloudletUtilizationModel, cloudletUtilizationModel)
      // setting the owner of these Cloudlets
      cloudlet.apply(cloudlet_no).setUserId(userId)
      list += cloudlet.apply(cloudlet_no)
    })
    list
  }

  /*
   * Creates main() to run this example
   */
  Log.printLine("Starting FaaSSimulationScalable...")

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
    // TODO - DataCenters with similar cost of resources are created
    @SuppressWarnings(Array.apply("unused")) val datacenter0 = HybridFaaSSimulationScalable.createDatacenter("Datacenter_0")
    @SuppressWarnings(Array.apply("unused")) val datacenter1 = HybridFaaSSimulationScalable.createDatacenter("Datacenter_1")
    logger.info("CIS created")
    logger.info("Datacenter creation completed")

    //Third step: Create Broker
    val Some(broker: DatacenterBroker) = HybridFaaSSimulationScalable.createBroker
    logger.info("Broker instance created successfully")

    val brokerId = broker.getId

    // Fourth step: Create VMs and Cloudlets and send them to broker
    // The Vm List
    // add the VM to the vmList
    val total_vms = fallbackConfig.getInt("vm.num_vms")
    val vmlist = HybridFaaSSimulationScalable.createVM(brokerId, total_vms) //creating 20 vms
    logger.info("Virtual Machines (VM) created successfully")

    //The Cloudlet List
    // add the cloudlet to the list
    val total_cloudlets = fallbackConfig.getInt("cloudlet.num_cloudlets")
    val cloudletList = HybridFaaSSimulationScalable.createCloudlet(brokerId, total_cloudlets) // creating 40 cloudlets
    logger.info("All of the Cloudlets spawned successfully")

    // submit vm list to the broker
    broker.submitVmList(vmlist.toList.asJava)
    logger.info("VM List submitted to Broker")

    // submit cloudlet list to the broker
    broker.submitCloudletList(cloudletList.toList.asJava)
    logger.info("CloudletList submitted to broker")

    // Fifth step: Starts the simulation
    CloudSim.startSimulation
    logger.info("Simulation started")

    CloudSim.stopSimulation()
    logger.info("Simulation stopped")

    // Final step: Print results when simulation is over
    val newList = broker.getCloudletReceivedList

    HybridFaaSSimulationScalable.printCloudletList(newList)
    logger.info("Simulation results printed")

    Log.printLine("FaaSSimulationScalable finished!")
  } catch {
    case e: Exception =>
      e.printStackTrace()
      Log.printLine("The simulation has been terminated due to an unexpected error")
  }

  /**
    * Creates the datacenter.
    *
    * @param name the name
    * @return the datacenter
    */
  def createDatacenter(name: String) : Option[Datacenter] = { // Here are the steps needed to create a PowerDatacenter:

    // Lightbend (Typesafe) configuration manager loading .conf files
    val defaultConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
    val fallbackConfig: Config = ConfigFactory.parseResources("defaults_simulation3.conf")
      .withFallback(defaultConfig).resolve
    logger.info("Configuration files loaded")

    // Here are the steps needed to create a PowerDatacenter:
    // 1. We need to create a list to store one or more
    //    Machines
    val hostList = new util.ArrayList[Host]
    // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
    //    create a list to store these PEs before creating
    //    a Machine.
    val peList1 = new util.ArrayList[Pe]
    val mips = fallbackConfig.getInt("pe.mips")
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

    logger.info(name + " - All Processing Elements added to respective Pe lists")

    //4. Create Hosts with its id and list of PEs and add them to the list of machines
    val ram = fallbackConfig.getInt("host.ram")
    //host memory (MB)

    val storage = fallbackConfig.getInt("host.storage")
    //host storage

    val bw = fallbackConfig.getInt("host.bw")

    val vmScheduling1 = fallbackConfig.getInt("host.vmScheduling1")
    val hostId1 = fallbackConfig.getInt("host.hostId1")

    val host1 = vmScheduling1 match {
      case 1 => new Host(hostId1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerSpaceShared(peList1))
      case 2 => new Host(hostId1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeShared(peList1))
      case 3 => new Host(hostId1, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList1, new VmSchedulerTimeSharedOverSubscription(peList1))
    }
    hostList.add(host1) // This is our first machine

    val vmScheduling2 = fallbackConfig.getInt("host.vmScheduling2")
    val hostId2 = fallbackConfig.getInt("host.hostId2")

    val host2 = vmScheduling2 match {
      case 1 => new Host(hostId2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2, new VmSchedulerSpaceShared(peList2))
      case 2 => new Host(hostId2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2, new VmSchedulerTimeShared(peList2))
      case 3 => new Host(hostId2, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList2, new VmSchedulerTimeSharedOverSubscription(peList2))
    }
    hostList.add(host2) // Second machine

    logger.info(name + " - Pe provisioned and added to Hosts")
    logger.info(name + " - Hosts added to Host Lists")

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

    // 5. Create a DatacenterCharacteristics object that stores the
    //    properties of a data center: architecture, OS, list of
    //    Machines, allocation policy: time- or space-shared, time zone
    //    and its price (G$/Pe time unit).

    val arch = fallbackConfig.getString("datacenter.arch")
    // system architecture
    val os = fallbackConfig.getString("datacenter.os")
    // operating system
    val vmm = fallbackConfig.getString("datacenter.vmm")
    // virtual machine manager
    val time_zone = fallbackConfig.getInt("datacenter.time_zone")
    // time zone this resource located
    val costPerSec = fallbackConfig.getInt("datacenter.costPerSec")
    // the cost of using processing in this resource
    val costPerMem = fallbackConfig.getInt("datacenter.costPerMem")
    // the cost of using memory in this resource
    val costPerStorage = fallbackConfig.getInt("datacenter.costPerStorage")
    // the cost of using storage in this resource
    val costPerBw = fallbackConfig.getInt("datacenter.costPerBw")
    // the cost of using bw in this resource
    val storageList = new util.LinkedList[Storage]
    //we are not adding SAN devices by now

    val characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, costPerSec, costPerMem, costPerStorage, costPerBw)

    logger.info(name + " - Datacenter Characteristics finalized")

    // 6. Finally, we need to create a PowerDatacenter object.
    try {
      val datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0)
      Some.apply(datacenter)
    }
    catch {
      case e: Exception => e.printStackTrace()
        scala.None
    }
  }

  //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
  //to the specific rules of the simulated scenario
  /**
    * Creates the broker.
    *
    * @return the datacenter broker
    */
  def createBroker : Option[DatacenterBroker] = {
    try {
      val broker = new DatacenterBroker("Broker")
      Some.apply(broker)
    }
    catch {
      case e: Exception => e.printStackTrace()
        scala.None
    }
  }

  /**
    * Prints the Cloudlet objects
    *
    * @param list list of Cloudlets
    */
  def printCloudletList(list: util.List[_ <: Cloudlet]): Unit = {
    val indent = "    "
    Log.printLine()
    Log.printLine("========== OUTPUT ==========")
    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent +
      "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Processing Cost")
    val dft = new DecimalFormat("###.##")

    var totalCost: Double = 0.0D
    list.forEach((cloudlet: Cloudlet) => {
      Log.print(indent + cloudlet.getCloudletId + indent + indent)
      if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {
        Log.print("SUCCESS")
        Log.printLine(indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent +
          indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) +
          indent + indent + indent + dft.format(cloudlet.getFinishTime) + indent + indent + indent +
          (Math.round(cloudlet.getProcessingCost * 100D)/100D))
        totalCost += cloudlet.getProcessingCost
      }
    })
    Log.printLine()
    Log.printLine("Total cost of execution of " + list.size + " Cloudlets = $" + Math.round(totalCost * 100D)/100D)
  }
}
