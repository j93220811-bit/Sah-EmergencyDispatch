package hospital;

import hospital.data.Database;
import hospital.ui.LoginDialog;
import hospital.ui.MainWindow;
import javax.swing.*;
import java.awt.*;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        try { Class.forName("org.sqlite.JDBC"); Database.initialize(); }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null,"Could not initialize SQLite database.\nCheck that lib/sqlite-jdbc.jar is present.\n\n"+e.getMessage(),"Startup error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            LoginDialog dialog=new LoginDialog((Frame)null);dialog.setVisible(true);
            if(dialog.getUser()!=null)new MainWindow(dialog.getUser()).setVisible(true);
            else System.exit(0);
        });
    }
}
