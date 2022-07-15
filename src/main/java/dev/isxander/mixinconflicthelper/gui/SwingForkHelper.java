package dev.isxander.mixinconflicthelper.gui;

import dev.isxander.mixinconflicthelper.utils.Mod;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class cannot depend on ANY external dependencies,
 * the only valid classes are the ones found in this mod.
 */
public class SwingForkHelper {
    public static void forkSwing(Mod mod1, Mod mod2, String stacktrace) throws Exception {
        var javaBinPath = Paths.get(System.getProperty("java.home"), "bin");
        if (Files.exists(javaBinPath)) {
            javaBinPath = javaBinPath.toRealPath();
        } else {
            javaBinPath = javaBinPath.toAbsolutePath().normalize();
        }

        var executables = new String[]{ "javaw.exe", "java.exe", "java" };
        Path javaPath = null;
        for (String executable : executables) {
            Path path = javaBinPath.resolve(executable);

            if (Files.isRegularFile(path)) {
                javaPath = path;
                break;
            }
        }

        if (javaPath == null) throw new RuntimeException("Couldn't find java executable in " + javaBinPath);

        var codeSource = Paths.get(SwingForkHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var process = new ProcessBuilder(javaPath.toString(), "-cp", codeSource.toString(), SwingForkHelper.class.getName())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();

        try (var os = new DataOutputStream(process.getOutputStream())) {
            mod1.writeTo(os);
            mod2.writeTo(os);
            os.writeUTF(stacktrace);
        }

        var returnVal = process.waitFor();
        if (returnVal != 0) throw new IOException("subprocess exited with code " + returnVal);
    }

    public static void main(String[] args) throws Exception {
        var is = new DataInputStream(System.in);
        var mod1 = Mod.fromDataInputStream(is);
        var mod2 = Mod.fromDataInputStream(is);
        var stacktrace = is.readUTF();

        SwingUtilities.invokeAndWait(() -> {
            try {
                if (GraphicsEnvironment.isHeadless()) {
                    throw new HeadlessException();
                }

                System.setProperty("apple.awt.application.appearance", "system");
                System.setProperty("apple.awt.application.name", "Mixin Conflict Helper");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                SwingPopups.conflict(mod1, mod2, stacktrace);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.exit(0);
    }
}
