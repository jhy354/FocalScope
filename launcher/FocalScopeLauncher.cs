using System;
using System.Diagnostics;
using System.IO;
using System.Management;
using System.Windows.Forms;

internal static class FocalScopeLauncher
{
    [STAThread]
    private static void Main(string[] args)
    {
        string baseDir = AppDomain.CurrentDomain.BaseDirectory;
        string java = Path.Combine(baseDir, @"runtime\bin\javaw.exe");
        string jar = Path.Combine(baseDir, "FocalScope.jar");

        if (!File.Exists(java))
        {
            MessageBox.Show("Bundled Java runtime not found:\n" + java,
                "FocalScope", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }
        if (!File.Exists(jar))
        {
            MessageBox.Show("FocalScope.jar not found:\n" + jar,
                "FocalScope", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }

        string arguments = "-Xmx1024m -Dsun.java2d.dpiaware=true -jar \"" + jar + "\"";
        foreach (string arg in args)
        {
            arguments += " \"" + arg.Replace("\"", "\\\"") + "\"";
        }

        // FocalScope keeps an RMI stub in %TEMP%. A stale stub (left behind by a
        // forced shutdown) makes the next launch wait for the RMI connect
        // timeout, which can take about five seconds. Remove it only when no
        // FocalScope window is running; otherwise the existing instance is used.
        if (!IsFocalScopeRunning())
        {
            try
            {
                string stub = GetStubPath();
                if (File.Exists(stub))
                {
                    File.SetAttributes(stub, FileAttributes.Normal);
                    File.Delete(stub);
                }
            }
            catch (Exception)
            {
                // Ignore: the normal single-instance check will handle it.
            }
        }

        ProcessStartInfo info = new ProcessStartInfo();
        info.FileName = java;
        info.Arguments = arguments;
        info.WorkingDirectory = baseDir;
        info.UseShellExecute = false;
        info.CreateNoWindow = true;

        try
        {
            Process.Start(info);
        }
        catch (Exception ex)
        {
            MessageBox.Show("Could not start FocalScope:\n" + ex.Message,
                "FocalScope", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }

    private static string GetStubPath()
    {
        return Path.Combine(Path.GetTempPath(),
            "FocalScope-" + Environment.UserName + "-57294.stub");
    }

    private static bool IsFocalScopeRunning()
    {
        string[] names = { "javaw.exe", "java.exe" };
        foreach (string name in names)
        {
            try
            {
                string query = "SELECT CommandLine FROM Win32_Process WHERE Name='" + name + "'";
                using (ManagementObjectSearcher searcher = new ManagementObjectSearcher(query))
                {
                    foreach (ManagementObject process in searcher.Get())
                    {
                        string commandLine = process["CommandLine"] as string;
                        if (commandLine != null &&
                            (commandLine.IndexOf("FocalScope.jar", StringComparison.OrdinalIgnoreCase) >= 0 ||
                             commandLine.IndexOf("focalscope-", StringComparison.OrdinalIgnoreCase) >= 0))
                        {
                            return true;
                        }
                    }
                }
            }
            catch (Exception)
            {
                // Fall back to the window title if WMI is unavailable.
            }
        }
        foreach (string name in new string[] { "javaw", "java" })
        {
            try
            {
                foreach (Process p in Process.GetProcessesByName(name))
                {
                    try
                    {
                        if (p.MainWindowTitle == "FocalScope") return true;
                    }
                    catch (Exception)
                    {
                        // The process may have exited while it was inspected.
                    }
                }
            }
            catch (Exception)
            {
                // Ignore and assume no other instance is running.
            }
        }
        return false;
    }
}
