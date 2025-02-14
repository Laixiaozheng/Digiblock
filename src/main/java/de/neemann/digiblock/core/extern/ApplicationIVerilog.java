/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digiblock.core.extern;

import de.neemann.digiblock.core.element.Keys;
import de.neemann.digiblock.core.extern.handler.ProcessInterface;
import de.neemann.digiblock.core.extern.handler.StdIOInterface;
import de.neemann.digiblock.gui.Settings;
import de.neemann.digiblock.lang.Lang;
import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Abstraction of the iverilog Application.
 * See http://iverilog.icarus.com/
 */
public class ApplicationIVerilog extends ApplicationVerilogStdIO {
    private String iverilogFolder;
    private String iverilog;
    private String vvp;
    private final boolean hasIverilog;

    /**
     * Initialize a new instance
     */
    public ApplicationIVerilog() {
        iverilogFolder = "";
        hasIverilog = findIVerilog();
    }

    @Override
    public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        File file = null;

        if (!hasIverilog) {
            throw new IOException(Lang.get("err_iverilogNotInstalled"));
        }

        try {
            String ivlModuleDir = iverilogFolder + File.separator + "lib" + File.separator + "ivl";

            file = createVerilogFile(label, code, inputs, outputs);
            String testOutputName = label + ".out";
            String m1 = ProcessStarter.start(file.getParentFile(), iverilog, "-y","~/Digital/blockbox","-tvvp", "-o" + testOutputName, file.getName());
            ProcessBuilder pb = new ProcessBuilder(vvp, "-M", ivlModuleDir, testOutputName).redirectErrorStream(true).directory(file.getParentFile());
            return new IVerilogProcessInterface(pb.start(), file.getParentFile());
        } catch (IOException e) {
            if (file != null)
                ProcessStarter.removeFolder(file.getParentFile());
            if (iverilogNotFound(e))
                throw new IOException(Lang.get("err_iverilogNotInstalled"));
            else
                throw e;
        }
    }

    private boolean iverilogNotFound(Throwable e) {
        while (e != null) {
            if (e instanceof ProcessStarter.CouldNotStartProcessException)
                return true;
            e = e.getCause();
        }
        return false;
    }

    @Override
    public boolean checkSupported() {
        return true;
    }

    @Override
    public String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        File file = null;

        if (!hasIverilog) {
            throw new IOException(Lang.get("err_iverilogNotInstalled"));
        }
        try {
            file = createVerilogFile(label, code, inputs, outputs);
            String testOutputName = label + ".out";

            return ProcessStarter.start(file.getParentFile(), iverilog, "-y","","-tvvp", "-o" + testOutputName, file.getName());
        } catch (IOException e) {
            if (iverilogNotFound(e))
                throw new IOException(Lang.get("err_iverilogNotInstalled"));
            else
                throw e;
        } finally {
            if (file != null)
                ProcessStarter.removeFolder(file.getParentFile());
        }
    }

    private boolean findIVerilog() {
        Path ivp = null;
        File ivDir = Settings.getInstance().get(Keys.SETTINGS_IVERILOG_PATH);

        if (ivDir != null) {
            Path p = Paths.get(ivDir.getAbsolutePath());

            if (Files.isExecutable(p)) {
                ivp = p;
                if (Files.isSymbolicLink(p)) {
                    try {
                        ivp = Files.readSymbolicLink(ivp);
                    } catch (IOException ex) {
                        return false;
                    }
                }
            }
        }

        if (ivp == null) {
            // Let's try to find iverilog in the system path
            String[] strPaths = System.getenv("PATH").split(File.pathSeparator);

            for (String sp : strPaths) {
                Path p = Paths.get(sp, "iverilog");

                if (Files.isExecutable(p)) {
                    ivp = p;
                    if (Files.isSymbolicLink(p)) {
                        try {
                            ivp = Files.readSymbolicLink(ivp);
                        } catch (IOException ex) {
                            return false;
                        }
                    }
                    break;
                }
            }
        }

        if (ivp != null) {
            iverilogFolder = ivp.getParent().getParent().toString();
            iverilog = ivp.getParent().resolve("iverilog").toString();
            vvp = ivp.getParent().resolve("vvp").toString();

            return true;
        } else {
            return false;
        }
    }

    private static final class IVerilogProcessInterface extends StdIOInterface {
        private final File folder;

        private IVerilogProcessInterface(Process process, File folder) {
            super(process);
            this.folder = folder;
        }

        @Override
        public String getConsoleOutNoWarn(LinkedList<String> consoleOut) {
            StringBuilder sb = new StringBuilder();
            for (String s : consoleOut) {
                if (!s.contains(": warning:") && !s.contains(":        :"))
                    sb.append(s).append("\n");
            }
            return sb.toString();
        }

        @Override
        public void close() throws IOException {
            super.close();
            ProcessStarter.removeFolder(folder);
        }
    }
}
