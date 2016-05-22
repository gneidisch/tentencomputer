package com.tenten.computer.tenten;

import android.os.Handler;

import java.util.*;

/**
 * Created by ichyo on 5/22/16.
 */

/**
 * Computer class.
 * It allows a set of instructions to be executed.
 * The instructions are registered in a program stack.
 */
public class Computer {

    // supported instructions
    public static final String MULT = "MULT";   // Pop the 2 arguments from the stack, multiply them and push the result back to the stack
    public static final String CALL = "CALL";   // CALL <addr> : Set the program counter (PC) to addr
    public static final String RET  = "RET";    // Pop address from stack and set PC to address
    public static final String STOP = "STOP";   // Exit the program
    public static final String PRINT = "PRINT"; // Pop value from stack and print it
    public static final String PUSH = "PUSH";   // PUSH <arg> : Push argument to the stack

    // the program stack and the program counter: the Computer class allows instructions to be pushed to the program stack
    private Instruction[] programStack;
    private int programCounter = 0;

    // the value stack: the arguments passed to the instructions in the program stack will be pushed/popped into/from this stack
    private Stack<Integer> valueStack;

    /**
     * Constructos
     * @param size  the initial size of the program stack
     */
    public Computer(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid size for stack");
        }

        programStack = new Instruction[size];
        valueStack = new Stack<Integer>();
    }

    /**
     * Set the program counter to the given address.
     * Throws an exception if the address is beyond the stack size
     * @param address
     * @return
     */
    public Computer setAddress(int address) {
        if (address < 0 || address > programStack.length) {
            throw new IllegalArgumentException("Invalid address");
        }
        programCounter = address;

        showProgramStack();

        return this;
    }

    /**
     * Register an instruction in the program stack. It validates the passed arguments if they're required.
     *
     * @param command
     * @param arg
     * @return
     */
    public Computer insert(String command, int... arg) {
        if (MULT.equals(command) || PRINT.equals(command) || RET.equals(command) || STOP.equals(command)) {
            programStack[programCounter++] = new Instruction(command);
        } else if (CALL.equals(command) || PUSH.equals(command)) {
            if (arg.length != 1) {
                throw new IllegalArgumentException("Missing argument for instruction");
            }
            programStack[programCounter++] = new Instruction(command, arg[0]);
        } else {
            throw new IllegalArgumentException("Non supported instruction");
        }

        // refresh current status of the stack
        showProgramStack();
        showValueStack();


        return this;

    }

    /**
     * A Handler to execute the registered instructions with some delay, for purely visual purposes
     */
    private Handler mHandler = new Handler();

    /**
     * Execute the instructions currently registered.
     * The program counter indicates the currently executed instruction. After each execution
     * the program counter is incremented or set to the instruction specified value.
     * @param eListener
     */
    public void execute(final ExecutionListener eListener) {

        // Execute instruction after 1 second, just for visual purposes
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                boolean incrementCounter = true;
                boolean triggerNext = true;

                Instruction instruction = programStack[programCounter];

                if (instruction != null) {


                    // Instruction STOP
                    if (STOP.equals(instruction.getCommand())) {
                        System.out.println("<END>");
                        printOutput("<END>");

                        triggerNext = false;


                    // Instruction MULT
                    }  if (MULT.equals(instruction.getCommand())) {

                        try {
                            Integer value1 = valueStack.pop();
                            Integer value2 = valueStack.pop();
                            valueStack.push(value1 * value2);
                        } catch (EmptyStackException e) {
                            System.err.println(e.getMessage());
                        }


                    // Instruction PRINT
                    } else if (PRINT.equals(instruction.getCommand())) {
                        try {
                            Integer value = valueStack.pop();
                            System.out.println(value);
                            printOutput(value.toString());
                        } catch (EmptyStackException e) {
                            System.err.println(e.getMessage());
                        }


                    // Instruction PUSH
                    } else if (PUSH.equals(instruction.getCommand())) {
                        valueStack.push(instruction.getValue());


                    // Instruction RET - it sets the program counter, so it won't be incremented
                    } else if (RET.equals(instruction.getCommand())) {

                        try {
                            Integer value = valueStack.pop();
                            setAddress(value);
                        } catch (EmptyStackException e) {
                            System.err.println(e.getMessage());
                        }

                        incrementCounter = false;


                    // Instruction CALL - it sets the program counter, so it won't be incremented
                    } else if (CALL.equals(instruction.getCommand())) {
                        setAddress(instruction.getValue());

                        incrementCounter = false;

                    }


                }

                // If the currently pointed instruction happened to be null
                // we increment the program-counter and trigger the next execution


                // Display the stack(s): both program and value stack
                showProgramStack();
                showValueStack();


                // increment the program counter so that the next stack-ed execution can be triggered
                // finish execution if we're at the top of the program stack
                if (incrementCounter) {
                    if (programCounter + 1 >= programStack.length) {
                        triggerNext = false;
                    } else {
                        programCounter++;
                    }
                }

                // trigger next instruction or finish the execution
                if (triggerNext) {
                    execute(eListener);
                } else {
                    eListener.onExecutionFinished();
                }

            }
        }, 1000);

    }


    // Helper members to visualize execution

    /**
     * ExecutionListener Interface, to inform the Activity that an execution has finished
     */
    interface ExecutionListener {
        void onExecutionFinished();
    }


    /**
     * ComputerOutput interface, to pass the output of the instructions to the Activity.
     */
    interface ComputerOutput {
        void print(String string);
    }

    ComputerOutput programStackOutput;
    ComputerOutput valueStackOutput;
    ComputerOutput executionOutput;

    public void setProgramStackOutput(ComputerOutput programStackOutput) {
        this.programStackOutput = programStackOutput;
    }

    public void setValueStackOutput(ComputerOutput valueStackOutput) {
        this.valueStackOutput = valueStackOutput;
    }

    public void setExecutionOutput(ComputerOutput executionOutput) {
        this.executionOutput = executionOutput;
    }

    /**
     * Fancy dump of the program stack, marking the current program-counter
     */
    private void showProgramStack() {

        boolean lastEllipsis = false;

        StringBuilder sb = new StringBuilder();
        for (int i = programStack.length - 1; i >= 0; i--) {
            if (programStack[i] == null) {
                if (!lastEllipsis) {
                    lastEllipsis = true;
                    sb.append("...\n");
                }
            } else {
                sb.append("[").append(i).append("]").
                        append("[").append(programStack[i].toString()).append("]");
                if (i == programCounter) {
                    sb.append(" <<<");
                }
                sb.append("\n");

                lastEllipsis = false;

            }
        }

        programStackOutput.print(sb.toString());
    }

    /**
     * Fancy dump of the value stack
     */
    private void showValueStack() {
        StringBuilder sb = new StringBuilder();
        for (Iterator it = valueStack.iterator(); it.hasNext();) {
            sb.append("[").append(it.next()).append("]\n");
        }

        valueStackOutput.print(sb.toString());
    }

    /**
     * print the execution output
     * @param string
     */
    private void printOutput(String string) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(string).append("\n");
        executionOutput.print(sb.toString());
    }
}


/**
 * Instruction class: a wraper for a command and an argument
 *
 */
class Instruction {

    private String command;
    private Integer value;

    Instruction(String instruction) {
        this(instruction, null);
    }

    Instruction(String command, Integer value) {
        this.command = command;
        this.value = value;
    }

    public String getCommand() {
        return command;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return command;
        }
        return new StringBuilder(command).append(" ").append(value).toString();
    }
}
