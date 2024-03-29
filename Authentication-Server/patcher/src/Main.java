import commands.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.prefs.Preferences;

/**
 * Main class
 *
 * @author András Rutkai
 * @since 2015.08.03.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "patch-hosts":
                checkRoles();
                patchHosts();
                break;
            case "unpatch-hosts":
                checkRoles();
                unpatchHosts();
                break;
            case "install-certs":
                checkRoles();
                installCerts();
                break;
            case "patch-launcher":
                patchLauncher();
                break;
            case "patch-authlib":
                patchAuthlib();
                break;
            case "patch-server":
                patchServer();
                break;
            default:
                System.out.println("No command line arguments!");
                printHelp();
                break;
        }
    }

    private static void printHelp() {
        System.out.println("The following commands are available:");
        System.out.println("  help            : prints this help");
        System.out.println("  patch-hosts     : patches the hosts file to use your own server");
        System.out.println("  unpatch-hosts   : un-patches the hosts file to use the original");
        System.out.println("  install-certs   : installs trusted certificates from your server server (requires patch-hosts)");
        System.out.println("  patch-launcher  : downloads a new client and patches it with your certificates");
        System.out.println("  patch-authlib   : patches the latest downloaded authlib to use your certificate");
        System.out.println("  patch-server    : downloads a new server and patches it with your certificates");
    }

    private static void checkRoles() {
        if (!isAdmin()) {
            System.out.println("You'll need administrator/root access for this feature!");
            System.setErr(new PrintStream(new OutputStream() {  // Suppress second warning
                @Override
                public void write(int b) throws IOException {
                    // Ignore warning
                }
            }));
            System.exit(100);
        }
    }

    private static boolean isAdmin() {
        Preferences prefs = Preferences.systemRoot();
        PrintStream systemErr = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // Ignore warning
            }
        }));
        try {
            prefs.put("foo", "bar"); // SecurityException on Windows
            prefs.remove("foo");
            prefs.flush(); // BackingStoreException on Linux
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            System.setErr(systemErr);
        }
    }

    private static void patchHosts() {
        System.out.print("Please enter your server's address: ");
        String address = System.console().readLine().trim();
        if ("".equals(address)) {
            System.exit(1);
        }

        HostsPatcher patcher = new HostsPatcher();
        try {
            patcher.patch(address);
        } catch (UnknownHostException e) {
            System.out.println("Host is unknown!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Hosts file cannot be read! Try to use this script as administrator/root.");
            System.exit(1);
        }
        System.out.println("Hosts file has been patched!");
    }

    private static void unpatchHosts() {
        HostsPatcher patcher = new HostsPatcher();
        try {
            patcher.unpatch();
        } catch (IOException e) {
            System.out.println("Hosts file cannot be read! Try to use this script as administrator/root.");
            System.exit(1);
        }
        System.out.println("Hosts file has been reverted!");
    }

    private static void installCerts() {
        CertInstaller installer = new CertInstaller();
        try {
            installer.installCerts();
        } catch (Exception e) {
            System.out.println("Unhandled exception during certificate installation:");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Certifications from hosts file auth servers has been installed!");
    }

    private static void patchLauncher() {
        System.out.print("Downloading and patching launcher...");
        LauncherPatcher patcher = new LauncherPatcher();
        try {
            patcher.savePatchedLauncher();
        } catch (Exception e) {
            System.out.println("Unhandled exception during the process:");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("done!");
    }

    private static void patchAuthlib() {
        System.out.print("Patching existing authlib...");
        AuthlibPatcher patcher = new AuthlibPatcher();
        try {
            patcher.patch();
        } catch (Exception e) {
            System.out.println("Unhandled exception during patching:");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("done!");
    }

    private static void patchServer() {
        try {
            ServerPatcher patcher = new ServerPatcher();
            String version = getServerVersion(patcher);
            System.out.print("Downloading and patching server...");
            patcher.savePatchedLauncher(version);
        } catch (Exception e) {
            System.out.println("Unhandled exception during the process:");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("done!");
    }

    private static String getServerVersion(ServerPatcher patcher) throws Exception {
        System.out.println("Available versions:");
        System.out.println(patcher.versions().getVersions());
        System.out.print("Version to download (default: " + patcher.versions().latest.release + "): ");
        String version = System.console().readLine().trim();
        if ("".equals(version)) {
            version = patcher.versions().latest.release;
        }
        if (!patcher.versions().hasVersion(version)) {
            throw new Exception("Version is not valid!");
        }
        return version;
    }

}
