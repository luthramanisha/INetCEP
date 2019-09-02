# Testing
This folder contains all setup necessary to make your system capable of running the unit tests.

## Setup
In order to run tests you need to set the system Path.
To do that go into `src/main/scala/config/NodeConfig.scala` and in line 27 where `systemPath` is defined enter the directory of your Project.
Additionally you have to set the system path in `src/main/java/SACEPICN/NodeMapping.java` in line 23 at the beginning of the NodeMapping function.

NOTE: It is important to set both paths back to MA-Ali since this is the standard folder where everything gets copied to on the VMs.
## Tests
Tests generated for the extended services are Tests for
*  Heatmap
*  Prediction 1
*  Prediction 2
*  Window
<a/>
Other Tets have not been created but are yet to come