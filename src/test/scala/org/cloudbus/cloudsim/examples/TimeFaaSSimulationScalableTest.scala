package org.cloudbus.cloudsim.examples

import java.util.Calendar

import com.typesafe.scalalogging.LazyLogging
import org.cloudbus.cloudsim.{Vm, _}
import org.cloudbus.cloudsim.core.CloudSim
import org.junit.{After, Before}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.collection.mutable.ListBuffer

class TimeFaaSSimulationScalableTest extends FunSuite with BeforeAndAfterEach with LazyLogging {

  @Before
  override def beforeEach(): Unit = {
    val num_user = 2
    val calendar = Calendar.getInstance
    val trace_flag = false
    CloudSim.init(num_user, calendar, trace_flag)
  }

  test("TimeFaaSSimulationScalable.createVMReturnsValidList") {
    logger.info("Verifying VM Creation returns list of specified size")
    val actualVmList: ListBuffer[Vm] = TimeFaaSSimulationScalable.createVM(4, 23)
    assert(actualVmList.size === 23)
  }

  test("TimeFaaSSimulationScalable.createCloudletReturnsValidList") {
    logger.info("Verifying Cloudlet Creation returns list of specified size")
    val actualCloudletList: ListBuffer[Cloudlet] = TimeFaaSSimulationScalable.createCloudlet(5, 42)
    assert(actualCloudletList.size === 42)
  }

  test("TimeFaaSSimulationScalable.createDataCenter") {
    logger.info("Verifying Cloudlet Creation returns a Datacenter Object")
    val dataCenter = TimeFaaSSimulationScalable.createDatacenter("Datacenter_0")
    assert(dataCenter.isInstanceOf[Some[Datacenter]])
  }

  test("TimeFaaSSimulationScalable.createBroker") {
    logger.info("Verifying Broker Creation returns a DatacenterBroker Object")
    val broker = TimeFaaSSimulationScalable.createBroker
    assert(broker.isInstanceOf[Some[DatacenterBroker]])
  }

  /*
   * @Test
   * def verifyCreateVM()= {
   *  val actualVmList: ListBuffer[Vm] = TimeFaaSSimulationScalable.createVM(4, 23)
   *  assert(actualVmList.size === 23)
   * }
   *
   */

  @After
  override def afterEach(): Unit = {
    // Do Nothing
  }
}
