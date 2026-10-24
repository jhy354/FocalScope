using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

internal static class ImageJLauncher
{
    [STAThread]
    private static void Main(string[] args)
    {
        string baseDir = AppDomain.CurrentDomain.BaseDirectory;
        string java = Path.Combine(baseDir, @"runtime\bin\javaw.exe");
        string jar = Path.Combine(baseDir, "ij.jar");

        if (!File.Exists(java))
        {
            MessageBox.Show("Bundled Java runtime not found:\n" + java,
                "ImageJ", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }
        if (!File.Exists(jar))
        {
            MessageBox.Show("ij.jar not found:\n" + jar,
                "ImageJ", MessageBoxButtons.OK, MessageBoxIcon.Error);
            return;
        }

        string arguments = "-Xmx1024m -jar \"" + jar + "\"";
        foreach (string arg in args)
        {
            arguments += " \"" + arg.Replace("\"", "\\\"") + "\"";
        }

        // ImageJ keeps an RMI stub in %TEMP%. A stale stub (left behind by a
        // forced shutdown) makes the next launch wait for the RMI connect
        // timeout, which can take about five seconds. Remove it only when no
        // ImageJ window is running; otherwise the existing instance is used.
        if (!IsImageJRunning())
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
            MessageBox.Show("Could not start ImageJ:\n" + ex.Message,
                "ImageJ", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }

    private static string GetStubPath()
    {
        return Path.Combine(Path.GetTempPath(),
            "ImageJ-" + Environment.UserName + "-57294.stub");
    }

    private static bool IsImageJRunning()
    {
        string[] names = { "javaw", "java" };
        foreach (string name in names)
        {
            try
            {
                foreach (Process p in Process.GetProcessesByName(name))
                {
                    try
                    {
                        if (p.MainWindowTitle == "ImageJ") return true;
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
