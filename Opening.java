package ling.ttd;

import java.io.*;
import java.awt.GraphicsEnvironment;

public class Opening {

    public static void main (String[] args) throws IOException {
        Console console = System.console();
        if (console == null && !GraphicsEnvironment.isHeadless()) {
            String filename = Opening.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
        } else {
            Main.main(new String[0]);
            System.out.println("Выполнение программы закончено, вы можете закрыть консоль.");
        }
    }
}
