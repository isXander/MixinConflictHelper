package dev.isxander.mixinconflicthelper.gui;

import dev.isxander.mixinconflicthelper.MixinConflictHelper;
import dev.isxander.mixinconflicthelper.utils.Mod;
import net.fabricmc.loader.api.ModContainer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class SwingPopups {
    public static void showConflict(ModContainer mod1Container, ModContainer mod2Container, Throwable th) throws Exception {
        var mod1 = Mod.fromModContainer(mod1Container);
        var mod2 = Mod.fromModContainer(mod2Container);

        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        var stacktrace = sw.toString();
        var issueTemplate = MixinConflictHelper.makeIssueTemplate(mod1, mod2, stacktrace);

        MixinConflictHelper.LOGGER.info("Forking JVM to display Swing GUI (bypass LWJGL)!");
        SwingForkHelper.forkSwing(mod1, mod2, issueTemplate);
    }

    public static void conflict(Mod mod1, Mod mod2, String issueTemplate) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        var message = createPopupMessage(mod1, mod2);

        var option = JOptionPane.showOptionDialog(
                null,
                message, "Mixin Conflict Helper",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null,
                new String[]{ mod1.name(), mod2.name(), "Copy issue template" },
                0
        );

        switch (option) {
            case 0 -> openUrl(mod1.issuesUrl().orElseThrow());
            case 1 -> openUrl(mod2.issuesUrl().orElseThrow());

            case 2 -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(issueTemplate), null);

                conflict(mod1, mod2, issueTemplate);
            }
        }
    }

    private static String createPopupMessage(Mod mod1, Mod mod2) {
        var mod1Conflicts = mod1.conflicts().stream().anyMatch((dep) -> dep.equals(mod2.id()));
        var mod2Conflicts = mod2.conflicts().stream().anyMatch((dep) -> dep.equals(mod1.id()));

        var sb = new StringBuilder();
        sb.append("There has been a mod conflict due to Mixin.").append("\n\n");
        sb.append(mod1.name()).append(" conflicted with ").append(mod2.name()).append(". ").append("\n");
        if (mod1Conflicts || mod2Conflicts) {
            sb.append("The creator of ").append(mod1Conflicts ? mod1.name() : mod2.name()).append(" knows about the issue, so they advise you to use either mod.");
        } else {
            sb.append("To continue running the game, you should remove one of the mods.");
        }
        sb.append("\n\n");

        sb.append("Pressing the buttons below takes you to their respected issue page.");

        return sb.toString();
    }

    private static void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}
