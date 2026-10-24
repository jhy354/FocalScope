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
}
