# tentencomputer
A small application simulating a computer


 This small application is a computer simulator.
 
 The simple Activity has three panels, one showing a program stack of instructions, another one showing a stack of values, and a third one showing the execution output.
 
 The Computer class has a set of supported instructions. Its main members are:
    - an *array* representing an instruction set,
    - and an actual *stack* for the arguments passed to the instructions.
    - Additionally, it's got a program-counter which points to the instructions' stack.
    - And a Handler to delay the execution of the instructions, just for visual purposes.
 
    - It also contains an Instruction sub-class which wraps a command and an optional argument.
 
    - It's got some interfaces to interact with the Activity:
      + to write the required output,
      + and to inform the Activity that an execution has ended.
