package com.tenten.computer.tenten;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * This small application is a computer simulator.
 *
 * The simple Activity has three panels, one showing a program stack of instructions, another one showing a stack of values, and a third one showing the execution output.
 *
 * The Computer class has a set of supported instructions. Its main members are:
 *   - an *array* representing an instruction set,
 *   - and an actual *stack* for the arguments passed to the instructions.
 *   - Additionally, it's got a program-counter which points to the instructions' stack.
 *   - And a Handler to delay the execution of the instructions, just for visual purposes.
 *
 *   - It also contains an Instruction sub-class which wraps a command and an optional argument.
 *
 *   - It's got some interfaces to interact with the Activity:
 *     + to write the required output,
 *     + and to inform the Activity that an execution has ended.
 */

public class MainActivity extends Activity implements View.OnClickListener, Computer.ExecutionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGo = (Button)findViewById(R.id.buttonGo);
        btnGo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonGo:
                findViewById(R.id.buttonGo).setEnabled(false);
                goTenTen();
        }
    }

    private static int PRINT_TENTEN_BEGIN = 50;
    private static int MAIN_BEGIN = 0;

    private void goTenTen() {

        // Create new computer with a stack of 100 addresses
        Computer computer = new Computer(100);



        // Helper methods to visualize the execution
        computer.setProgramStackOutput(new Computer.ComputerOutput() {
            @Override
            public void print(String string) {
                TextView tvProgramStack = (TextView)findViewById(R.id.tvProgStack);
                tvProgramStack.setText(string);

            }
        });
        computer.setValueStackOutput(new Computer.ComputerOutput() {
            @Override
            public void print(String string) {
                TextView tvValueStack = (TextView)findViewById(R.id.tvValStack);
                tvValueStack.setText(string);

            }
        });
        computer.setExecutionOutput(new Computer.ComputerOutput() {
            @Override
            public void print(String string) {
                TextView tvOutput = (TextView)findViewById(R.id.tvOutput);
                CharSequence cs = tvOutput.getText();
                cs = cs + string;
                tvOutput.setText(cs);

            }
        });



        // Instructions for the print_tenten function
        computer.setAddress(PRINT_TENTEN_BEGIN).insert("MULT").insert("PRINT").insert("RET");

        // The start of the main function
        computer.setAddress(MAIN_BEGIN).insert("PUSH", 1009).insert("PRINT");

        // Return address for when print_tenten function finishes
        computer.insert("PUSH", 6);

        // Setup arguments and call print_tenten
        computer.insert("PUSH", 101).insert("PUSH", 10).insert("CALL", PRINT_TENTEN_BEGIN);

        // Stop the program
        computer.insert("STOP");
        computer.setAddress(MAIN_BEGIN).execute(this);
    }

    @Override
    public void onExecutionFinished() {
        findViewById(R.id.buttonGo).setEnabled(true);
    }
}
