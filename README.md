# CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds [![Build Status](https://travis-ci.com/tanveertshaikh/cloudsim-scalable-simulations.svg?branch=master)](https://travis-ci.com/tanveertshaikh/cloudsim-scalable-simulations)
## Description: Create cloud simulators for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.
### Name: Tanveer Shaikh

### Instructions

Install [IntelliJ](https://www.jetbrains.com/student/), JDK, Scala runtime, IntelliJ Scala plugin and the [Simple Build Toolkit (SBT)](https://www.scala-sbt.org/1.x/docs/index.html) and make sure that you can run Java monitoring tools.

Open the project in IntelliJ and build the project. This may take some time as the Library Dependendcies mentioned in the build.sbt file will be downloaded and added to the classpath of the project.

Open sbt shell in IntelliJ and give the command `clean` to clean the project. Then, type `compile` to compile the project and finally run the project by typing `run` and pressing return.

The three simulations in the project have 3 main classes. Hence, the sbt shell will ask the user to enter which one of these 3 classes should it execute. Please type in the corresponding number and press return when prompted.

Alternatively, you can also compile test and run the simulations by runnning the script in .bat file or .sh files provided in the root directory of the project.

For running the tests separately, you may also open the provided Scala objects and run all the tests in each of the simulations. There are a total of 12 test cases provided to maintain the simulations and keep them error-free.

The results of the simulation alongwith the logiing statements will printed in the console

The inputs are provided using the configuration files present in src/main/resources folder. You can play around with the inputs and experiment with different parameters.
