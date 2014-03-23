soop
====

Simple lightweight workflow scheduling and coordination for Hadoop and whatever else you need to run.

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

Soop uses a simple flat-file syntax for it's work docket, like this:
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
# Comments look like this line, with a leading "#"
```

Soop offers a few built in tasks types, but you can add any types you like by implementing the TaskEntry interface. The built-in types are:
  Cascading   <---- Executes a class which implements the FlowFactory interface and returns one or more Cascading Flows
  Java    <-------- Executes a Pojo which has been annotated with Riffle annotations
  Shell   <-------- Executes any shell command as a new process

Custom tasks types can be added by including them in the classpath and referring to them by fully qualified class name.
