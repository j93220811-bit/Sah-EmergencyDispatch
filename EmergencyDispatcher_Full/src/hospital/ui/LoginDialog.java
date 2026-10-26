package hospital.ui;

import hospital.model.User;
import hospital.service.HospitalService;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private User user;
    public LoginDialog(Frame owner) {
        super(owner,"Sign in — Emergency Dispatcher",true);
        setSize(430,340);setResizable(false);setLocationRelativeTo(owner);
        JPanel root=new JPanel(new BorderLayout(14,14));root.setBorder(BorderFactory.createEmptyBorder(24,28,22,28));
        JLabel title=new JLabel("EMERGENCY DISPATCHER",SwingConstants.CENTER);title.setFont(new Font("SansSerif",Font.BOLD,20));
        JLabel sub=new JLabel("Sign in to manage triage and ward resources",SwingConstants.CENTER);sub.setForeground(new Color(100,110,125));
        JPanel heading=new JPanel(new GridLayout(2,1,0,6));heading.add(title);heading.add(sub);root.add(heading,BorderLayout.NORTH);
        JTextField username=new JTextField("coordinator");JPasswordField password=new JPasswordField("coord123");
        JPanel form=new JPanel(new GridLayout(2,2,10,12));form.add(new JLabel("Username"));form.add(username);form.add(new JLabel("Password"));form.add(password);root.add(form,BorderLayout.CENTER);
        JButton login=new JButton("Sign in");login.setBackground(new Color(28,78,128));login.setForeground(Color.WHITE);
        JLabel hint=new JLabel("<html><center>Demo: coordinator / coord123<br>nurse / nurse123 · admin / admin123 · doctor / doctor123</center></html>",SwingConstants.CENTER);hint.setForeground(new Color(100,110,125));
        JPanel bottom=new JPanel(new BorderLayout(0,10));bottom.add(login,BorderLayout.NORTH);bottom.add(hint,BorderLayout.SOUTH);root.add(bottom,BorderLayout.SOUTH);
        login.addActionListener(e->{try{
            HospitalService auth=new HospitalService(new hospital.model.StaffUser("login",hospital.model.UserRole.NURSE));
            user=auth.authenticate(username.getText(),new String(password.getPassword()));
            if(user==null)JOptionPane.showMessageDialog(this,"Login failed. Check credentials or account lockout.","Sign in failed",JOptionPane.WARNING_MESSAGE);
            else dispose();
        }catch(SQLException ex){JOptionPane.showMessageDialog(this,"Database error: "+ex.getMessage());}});
        getRootPane().setDefaultButton(login);setContentPane(root);
    }
    public User getUser(){return user;}
}
