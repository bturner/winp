import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.winp.WinProcess;
import org.jvnet.winp.WinpException;

/**
 * @author Kohsuke Kawaguchi
 */
public class TheTest {

    @Test
    public void testEnumProcesses() {
        for (WinProcess p : WinProcess.all()) {
            System.out.print(p.getPid());
            System.out.print(' ');
        }
        System.out.println();
    }

    @Test
    public void testGetCommandLine() {
        int failed = 0;
        int total = 0;
        for (WinProcess p : WinProcess.all()) {
            if(p.getPid()<10)   continue;
            try {
                ++total;
                System.out.println(p.getPid() + ": " + p.getCommandLine());
            } catch (WinpException e) {
                // Note: On newer (is it really still "newer" to say Vista and higher? In 2012?) versions of
                // Windows, "protected processes" have been introduced. Unless this test is run with full
                // administrative privileges (and we're not that stupid, are we?), it's bound to run across
                // some processes in the full list it's not allowed to tinker with, even if we skip past the
                // first ten. So, if the error code is ERROR_ACCESS_DENIED (see winerror.h), ignore it
                ++failed;
                if (e.getWin32ErrorCode() != 5) { //ERROR_ACCESS_DENIED
                    System.out.println("Unexpected failure getting command line for process " + p.getPid() +
                            ": (" + e.getWin32ErrorCode() + ") " + e.getMessage());
                }
            }
        }
        System.out.println("Failed to get " + failed + " of " + total + " processes");
    }

    @Test(expected = WinpException.class)
    public void testErrorHandling() {
        new WinProcess(0).getEnvironmentVariables();
    }

    @Test
    public void testKill() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("notepad");
        pb.environment().put("TEST","foobar");
        Process p = pb.start();

        WinProcess wp = new WinProcess(p);
        System.out.println("pid="+wp.getPid());

        System.out.println(wp.getCommandLine());
        Assert.assertTrue(wp.getCommandLine().contains("notepad"));

        System.out.println(wp.getEnvironmentVariables());
        Assert.assertEquals("foobar", wp.getEnvironmentVariables().get("TEST"));

        Thread.sleep(100);
        new WinProcess(p).killRecursively();
    }

    @BeforeClass
    public static void enableDebug() {
        WinProcess.enableDebugPrivilege();
    }
}
