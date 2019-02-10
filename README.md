# Name: Tanveer Shaikh
# Homework 1
### Description: create cloud simulators for evaluating executions of applications in cloud datacenters with different characteristics and deployment models.
### Grade: 5% + bonus up to 3%
#### You can obtain this Git repo using the command git clone git@bitbucket.org:cs441_spring2019/homework1.git

## Preliminaries
### CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds

In a controlled env you will be running our app, simulating an app - benefits: open the possiblity of evaluating the assumptions made about that app
overcome bottlenecks or limitations in your app quickly
developed different workload mixes and resuorce mappings
support for modelling and simulation of large scale cloud computing infra, including data centers on a single physical computing node
availiabilty of virtualization engine which aids in creation and management of multiple, independent, and co-hosted virtualized services on a datacenter node
simulate various cloud computing policies (scheduling and provisioning) VM alloc, VM scheduling and cloudlet (task) scheduling

Developers hate servers. The idea of serverless architectures is a panacea for developers. That said, I don’t see FaaS as being a complete replacement for normal application architectures. For a basic web application, it would take a lot of functions.

In my humble opinion, function based apps are a perfect fit for replacing potential microservice style architectures and background type services.

## Functionality
Once you installed and configured CloudSim, your job is create two or more simulations where you will evaluate two or more datacenters with different characteristics (e.g., operating systems, costs, devices) and policies. Imagine that you are a cloud computing broker and you purchase computing time in bulk from different cloud providers and you sell this time to your customers, so that they can execute their jobs, i.e., cloudlets on the infrastructure of these cloud providers that have different policies and constraints. As a broker, your job is to buy the computing time cheaply and sell it at a good markup. One way to achieve it is to take cloudlets from your customers and estimate how long they will execute. Then you charge for executing cloudlets some fixed fee that represent your cost of resources summarily. Some cloudlets may execute longer than you expected, the other execute faster. If your revenue exceeds your expenses for buying the cloud computing time in bulk, you are in business, otherwise, you will go bankrupt!

There are different policies that datacenters can use for allocating Virtual Machines (VMs) to hosts, scheduling them for executions on those hosts, determining how network bandwidth is provisioned, and for scheduling cloudlets to execute on different VMs. Randomly assigning these cloudlets to different datacenters may result in situation where the executions of these cloudlets are inefficient and they takes a long time. As a result, you exhaust your supply of the purchased cloud time and you may have to refund the money to your customers, since you cannot fulfil the agreement, and you will go bankrupt. Modeling and simulating the executions of cloudlets in your clouds may help you chose a proper model for your business. The bonus of up to 3% will be added based on the sophistication of your simulated clouds.

## Baseline
Your baseline project is based on the examples that come from the repo CloudSim. To be considered for grading, your project should include at least one of your simulation programs written in Scala, your project should be buildable using the SBT, and your documentation must specify how you create and evaluate your simulated clouds based on the cloud models that we learn in the class. Your documentation must include the results of your simulation and your explanations of how these results help you with your simulation objectives (e.g., choose the right cloud model and configuration). Simply copying Java programs from examples and modifying them a bit will result in rejecting your submission.


## Submission deadline and logistics
Monday, February 4 at 3AM CST via the bitbucket repository. Your submission will include the code for the simulator program, your documentation with instructions and detailed explanations on how to assemble and deploye your cloud simulation along with the results of your simulation and a document that explains these results based on the characteristics and the parameters of your simulations, and what the limitations of your implementation are. Again, do not forget, please make sure that you will give both your TAs and your instructor the read access to your private forked repository. Your name should be shown in your README.md file and other documents. Your code should compile and run from the command line using the commands **sbt clean compile test** and **sbt clean compile run**. Also, you project should be IntelliJ friendly, i.e., your graders should be able to import your code into IntelliJ and run from there. Use .gitignore to exlude files that should not be pushed into the repo.


## Evaluation criteria
- the maximum grade for this homework is 5% with the bonus up to 3%. Points are subtracted from this maximum grade: for example, saying that 2% is lost if some requirement is not completed means that the resulting grade will be 5%-2% => 3%; if the core homework functionality does not work, no bonus points will be given;
- only some basic cloud simulation examples from the cloudsim repo are given and nothing else is done: up to 5% lost;
- having less than five unit and/or integration tests: up to 4% lost;
- missing comments and explanations from the simulation program: up to 3% lost;
- logging is not used in the simulation programs: up to 3% lost;
- hardcoding the input values in the source code instead of using the suggested configuration libraries: up to 4% lost;
- no instructions in README.md on how to install and run your simulator: up to 4% lost;
- the program crashes without completing the core functionality: up to 2% lost;
- the documentation exists but it is insufficient to understand how you assembled and deployed all components of the cloud: up to 4% lost;
- the minimum grade for this homework cannot be less than zero.
