soop
====

Simple lightweight workflow scheduling and coordination for Hadoop and whatever else you need to run.

Start Soop by running net.csgeek.soop.Driver

****

So you have Hadoop jobs, you have batch jobs, you have Java jobs ... you have a lot of work to get done and no way to coordinate it all. What you need is a supervisor to plan, schedule, and coordinate all of these jobs. Soop is your supervisor for Hadoop and pretty much any other job you need done. And just like that supervisor at the job you had in highschool, this soop isn't interested in a lot of extra crap getting in the way of getting the job done! Soop is simple and easy to use. 

Soop uses a cron like syntax for scheduling tasks:
```
*     *     *   *    *  
-     -     -   -    -
|     |     |   |    |
|     |     |   |    +----- day of week (0 - 6) (Sunday=0)
|     |     |   +------- month (1 - 12)
|     |     +--------- day of        month (1 - 31)
|     +----------- hour (0 - 23)
+------------- min (0 - 59)
```

Soop uses a simple flat-file syntax for it's work docket. The file is named "docket" and expected to exist in the working directory of Soop. The docket file syntax looks like this:
```
* * * * *  <---- Cron style line to mark a scheduled block of tasks
        type:command arg1 arg2 arg3  <---- one or more task entry lines in each scheduled block
 ----- -----   -----  --------------
   |    |       |          |
   |    |       |          +--------- list of arguments
   |    |       +-------------------- command to run or java class to launch
   |    +---------------------------- type of task to execute
   +--------------------------------- leading white-space indicates this line as a task entry in this schedule block
           <---- Blank lines are ignored
# Everything after a '#' to the end of the line is a comment
```

Soop offers a few built in tasks types, but you can add any types you like by implementing the TaskEntry interface. The built-in types are:

Type Name | Description
--------- | ---------------
Cascading | Executes a class which implements the FlowFactory interface and returns one or more [Cascading](http://www.cascading.org) Flows
Java      | Executes a Pojo which has been annotated with [Riffle](https://github.com/cwensel/riffle) annotations
Shell     | Executes any shell command as a new process
ToolRunner | Executes any Java class which implements the Hadoop [Tool](http://hadoop.apache.org/docs/r2.3.0/api/org/apache/hadoop/util/Tool.html) interface using the [ToolRunner](http://hadoop.apache.org/docs/r2.3.0/api/org/apache/hadoop/util/ToolRunner.html) class

Custom tasks types can be added by including them in the classpath and referring to them by fully qualified class name.

Soop also provide a simple way to pass in values for your jobs, or pass values between your jobs. Soop uses the Java System Properties for sharing values. Soop uses a flat file named "state" in the Soop working directory to store these properties periodically in order to aid recover in case of a crash. You may modify the state file and reload to feed values into Soop.

Your Java classes can access these properties directly for reading and writing. You can pass properties to your Shell command via the arguments. Entries in the arguments of the form **$${property name}** will be interpolated from the System Properties, This applies to all task types, not just Shell. To set properties from your shell command, have it print **!SOOP:$$property=value** to standard out. For example, if your bash script prints out *!SOOP:$$numFiles=5*, Soop will populate the property "num_files" with the value "5".

There are two special Soop System Properties which are always available:
- **net.csgeek.soop.args** is the fully interpolated argument string from the task entry
- **net.csgeek.soop.command** is the command string from the task entry

The special synatx **!SOOP:** is used to tell some specific information to Soop. You can think of it as shouting something to the supervisor like "Hey Soop, the value of numFiles should be 5!" This syntax is used in two places. The first (which was mentioned above) is for setting properties as part of the output of a shell command. The second is for indicating the inputs and outputs of Shell or ToolRunner commands. These are necessary for properly coordinating dependencies between the command and other tasks scheduled in the same block. Use **!SOOP:input=some_input_name** to indicate an input dependency. Use **!SOOP:output=some_output_name** to indicate an output which some other job in the task block might depend on. The input and output entries may appear anywhere in the args section of the task entry, and each may appear as often as necessary to list all of the inputs and outputs. The args interpolation will process and remove these entries from the args string.

For the **ToolRunner** task type there are actually two ways of indicating the inputs and outputs. The first (discussed above) is using the **!SOOP:input=...** and **!SOOP:output=...** entries. The other is to add methods with the [@DependencyIncoming](https://github.com/cwensel/riffle/blob/master/src/java/riffle/process/DependencyIncoming.java) and / or [@DependencyOutgoing](https://github.com/cwensel/riffle/blob/master/src/java/riffle/process/DependencyOutgoing.java) Riffle annotations. Soop will automatically find these annotated methods and use the information they return. If both techniques are used, the information in the docket (!SOOP:input / !SOOP:output) will take precendence.

***
Soop is built on [Cascading](http://www.cascading.org) for dependency chaining of jobs, and [cron4j](http://www.sauronsoftware.it/projects/cron4j/) for scheduling.
