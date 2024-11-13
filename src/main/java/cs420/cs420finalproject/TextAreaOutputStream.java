package cs420.cs420finalproject;

import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TextAreaOutputStream extends OutputStream {

    private PrintStream originalStream;
    private TextArea textArea;
    public TextAreaOutputStream(PrintStream originalStream, TextArea textArea) {
        this.originalStream = originalStream;
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        originalStream.write(b);  // Write to original System.out
        textArea.appendText(String.valueOf((char) b));  // Append to TextArea
    }

    @Override
    public void flush() throws IOException {
        originalStream.flush();
        super.flush();
    }

    @Override
    public void close() throws IOException {
        originalStream.close();
        super.close();
    }

}