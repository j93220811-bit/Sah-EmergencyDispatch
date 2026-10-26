package hospital.ui;

import hospital.model.*;
import hospital.service.HospitalService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class MainWindow extends JFrame {
    private final HospitalService service;
    private final User user;
    private final Color navy=new Color(20,43,72), blue=new Color(37,99,155), pale=new Color(242,246,250);
    private JTable queueTable,resourceTable,auditTable,vitalsTable;
    private DefaultTableModel queueModel,resourceModel,auditModel,vitalsModel;
    private JLabel totalLabel,waitingLabel,criticalLabel,resourceLabel;
    private JComboBox<Integer> patientPicker;

    public MainWindow(User user) {
        super("Emergency Ward Triage & Surgical Resource Dispatcher");
        this.user=user;this.service=new HospitalService(user);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setMinimumSize(new Dimension(1100,720));setSize(1280,820);setLocationRelativeTo(null);
        getContentPane().setBackground(pale);setLayout(new BorderLayout());
        add(header(),BorderLayout.NORTH);
        JTabbedPane tabs=new JTabbedPane();tabs.setFont(new Font("SansSerif",Font.BOLD,13));
        tabs.addTab("Overview",overview());tabs.addTab("Patient Triage",triage());tabs.addTab("Vitals",vitals());
        tabs.addTab("Resources & Dispatch",resources());tabs.addTab("Audit Log",audit());
        add(tabs,BorderLayout.CENTER);
        refreshAll();
    }
    private JPanel header(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(navy);p.setBorder(new EmptyBorder(18,24,18,24));
        JPanel left=new JPanel(new GridLayout(2,1,0,5));left.setOpaque(false);
        JLabel title=new JLabel("Emergency Ward Dispatcher");title.setForeground(Color.WHITE);title.setFont(new Font("SansSerif",Font.BOLD,24));
        JLabel sub=new JLabel("TRIAGE  /  RESOURCE MATCHING  /  WARD STATUS");sub.setForeground(new Color(190,207,225));sub.setFont(new Font("SansSerif",Font.PLAIN,11));
        left.add(title);left.add(sub);p.add(left,BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8));right.setOpaque(false);
        JLabel who=new JLabel(user.getUsername()+"  ·  "+user.getRole());who.setForeground(Color.WHITE);who.setFont(new Font("SansSerif",Font.BOLD,13));
        JButton refresh=new JButton("↻ Refresh");refresh.addActionListener(e->refreshAll());
        JButton logout=new JButton("Log out");logout.addActionListener(e->{dispose();SwingUtilities.invokeLater(()->{LoginDialog d=new LoginDialog(null);d.setVisible(true);if(d.getUser()!=null)new MainWindow(d.getUser()).setVisible(true);else System.exit(0);});});
        right.add(who);right.add(refresh);right.add(logout);p.add(right,BorderLayout.EAST);return p;
    }
    private JPanel overview(){
        JPanel p=page();JPanel cards=new JPanel(new GridLayout(1,4,14,0));cards.setOpaque(false);
        totalLabel=metric("TOTAL PATIENTS","0",new Color(37,99,155));waitingLabel=metric("WAITING","0",new Color(180,116,32));criticalLabel=metric("RED / CRITICAL","0",new Color(190,55,55));resourceLabel=metric("AVAILABLE RESOURCES","0",new Color(30,130,95));
        cards.add(totalLabel.getParent());cards.add(waitingLabel.getParent());cards.add(criticalLabel.getParent());cards.add(resourceLabel.getParent());
        p.add(cards,BorderLayout.NORTH);
        JPanel content=panelCard("Live priority queue");queueModel=model("ID","Patient","Age","Triage","Score","Status","Arrival","Vitals");
        queueTable=new JTable(queueModel);styleTable(queueTable);content.add(new JScrollPane(queueTable),BorderLayout.CENTER);
        JButton refresh=new JButton("Refresh dashboard");refresh.addActionListener(e->refreshAll());content.add(refresh,BorderLayout.SOUTH);
        p.add(content,BorderLayout.CENTER);return p;
    }
    private JLabel metric(String name,String value,Color color){
        JPanel card=new JPanel(new BorderLayout(0,8));card.setBackground(Color.WHITE);card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,4,0,0,color),new EmptyBorder(16,18,16,14)));
        JLabel l=new JLabel(value);l.setFont(new Font("SansSerif",Font.BOLD,30));l.setForeground(color);
        JLabel t=new JLabel(name);t.setFont(new Font("SansSerif",Font.BOLD,11));t.setForeground(new Color(105,115,128));
        card.add(t,BorderLayout.NORTH);card.add(l,BorderLayout.CENTER);card.putClientProperty("value",l);
        // The returned label is used as a handle; its parent is the card.
        l.putClientProperty("card",card);
        return l;
    }
    private void setMetric(JLabel handle,String value){handle.setText(value);}
    private JPanel triage(){
        JPanel p=page();JPanel form=panelCard("Register a patient");
        JTextField name=new JTextField(),age=new JTextField(),contact=new JTextField();
        JComboBox<SeverityLevel> severity=new JComboBox<>(SeverityLevel.values());JSpinner score=new JSpinner(new SpinnerNumberModel(5,1,10,1));
        JTextArea notes=new JTextArea(4,24);notes.setLineWrap(true);notes.setWrapStyleWord(true);
        JPanel fields=new JPanel(new GridBagLayout());fields.setOpaque(false);GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(7,7,7,7);g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;
        addField(fields,g,0,"Patient name",name);addField(fields,g,1,"Age",age);addField(fields,g,2,"Contact",contact);addField(fields,g,3,"Triage level",severity);addField(fields,g,4,"Severity score (1–10)",score);
        g.gridx=0;g.gridy=5;g.weightx=0;fields.add(new JLabel("Intake notes"),g);g.gridx=1;g.weightx=1;fields.add(new JScrollPane(notes),g);
        form.add(fields,BorderLayout.CENTER);
        JButton save=primary("Register patient");save.addActionListener(e->{try{
            int a=Integer.parseInt(age.getText().trim());
            service.addPatient(name.getText(),a,contact.getText(),(SeverityLevel)severity.getSelectedItem(),(Integer)score.getValue(),notes.getText());
            JOptionPane.showMessageDialog(this,"Patient registered and added to the priority queue.");name.setText("");age.setText("");contact.setText("");notes.setText("");refreshAll();
        }catch(NumberFormatException ex){error("Age must be a number.");}catch(Exception ex){error(ex.getMessage());}});
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.setOpaque(false);bottom.add(save);form.add(bottom,BorderLayout.SOUTH);p.add(form,BorderLayout.NORTH);
        JPanel list=panelCard("Registered patients");JTable table=new JTable(queueModel);styleTable(table);list.add(new JScrollPane(table),BorderLayout.CENTER);p.add(list,BorderLayout.CENTER);return p;
    }
    private JPanel vitals(){
        JPanel p=page();JPanel form=panelCard("Record patient vitals");
        patientPicker=new JComboBox<>();JTextField sys=new JTextField("120"),pulse=new JTextField("80"),spo2=new JTextField("98"),temp=new JTextField("37.0"),rr=new JTextField("16");
        JPanel fields=new JPanel(new GridLayout(0,2,10,10));fields.setOpaque(false);
        fields.add(new JLabel("Patient"));fields.add(patientPicker);fields.add(new JLabel("Systolic BP"));fields.add(sys);fields.add(new JLabel("Pulse"));fields.add(pulse);fields.add(new JLabel("SpO₂ (%)"));fields.add(spo2);fields.add(new JLabel("Temperature (°C)"));fields.add(temp);fields.add(new JLabel("Respiratory rate"));fields.add(rr);
        form.add(fields,BorderLayout.CENTER);JButton save=primary("Save vitals");save.addActionListener(e->{try{
            if(patientPicker.getSelectedItem()==null)throw new IllegalArgumentException("Register a patient first.");
            int id=(Integer)patientPicker.getSelectedItem();
            boolean alert=service.addVitals(id,new Vitals(Integer.parseInt(sys.getText()),Integer.parseInt(pulse.getText()),Integer.parseInt(spo2.getText()),Double.parseDouble(temp.getText()),Integer.parseInt(rr.getText())));
            JOptionPane.showMessageDialog(this,alert?"⚠ Threshold alert! One or more readings exceed the configured limits.":"Vitals saved. No configured threshold alert detected.",alert?"Vitals alert":"Vitals saved",alert?JOptionPane.WARNING_MESSAGE:JOptionPane.INFORMATION_MESSAGE);refreshAll();
        }catch(Exception ex){error("Could not save vitals: "+ex.getMessage());}});
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT));bottom.setOpaque(false);bottom.add(save);form.add(bottom,BorderLayout.SOUTH);p.add(form,BorderLayout.NORTH);
        JPanel history=panelCard("Vitals history for selected patient");vitalsModel=model("Recorded at","Systolic","Pulse","SpO₂","Temp °C","Resp. rate","Result");vitalsTable=new JTable(vitalsModel);styleTable(vitalsTable);history.add(new JScrollPane(vitalsTable),BorderLayout.CENTER);
        patientPicker.addActionListener(e->refreshVitals());p.add(history,BorderLayout.CENTER);return p;
    }
    private JPanel resources(){
        JPanel p=page();JPanel box=panelCard("Resource inventory and dispatch");
        resourceModel=model("ID","Resource","Type","Status","Patient ID");resourceTable=new JTable(resourceModel);styleTable(resourceTable);box.add(new JScrollPane(resourceTable),BorderLayout.CENTER);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));actions.setOpaque(false);
        JButton assign=primary("Assign selected patient");assign.addActionListener(e->{Integer id=selectedPatientId();if(id==null){error("Select a patient in the Overview or Patient Triage queue first.");return;}try{String msg=service.assignResources(id);if(msg.startsWith("No complete")&&((SeverityLevel)selectedSeverity(id))==SeverityLevel.RED&&user.canManageResources()){int choice=JOptionPane.showConfirmDialog(this,msg+"\n\nPause an active assignment to free resources for this RED emergency?","Emergency pre-emption",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);if(choice==JOptionPane.YES_OPTION)msg=service.preemptForCritical(id);}JOptionPane.showMessageDialog(this,msg);refreshAll();}catch(Exception ex){error(ex.getMessage());}});
        JButton status= new JButton("Change resource status");status.addActionListener(e->{int row=resourceTable.getSelectedRow();if(row<0){error("Select a resource first.");return;}int id=Integer.parseInt(""+resourceModel.getValueAt(row,0));String[] choices={"AVAILABLE","OCCUPIED","ON_BREAK","UNAVAILABLE","RESERVED"};String s=(String)JOptionPane.showInputDialog(this,"New status:","Resource status",JOptionPane.PLAIN_MESSAGE,null,choices,choices[0]);if(s!=null)try{service.setResourceStatus(id,s);refreshAll();}catch(Exception ex){error(ex.getMessage());}});
        JButton complete=new JButton("Complete selected patient");complete.addActionListener(e->{Integer id=selectedPatientId();if(id==null){error("Select a patient in the queue first.");return;}try{service.updateStatus(id,"COMPLETED");JOptionPane.showMessageDialog(this,"Patient marked completed. Set assigned resources to AVAILABLE using the resource table.");refreshAll();}catch(Exception ex){error(ex.getMessage());}});
        actions.add(assign);actions.add(status);actions.add(complete);box.add(actions,BorderLayout.SOUTH);p.add(box,BorderLayout.CENTER);
        JLabel note=new JLabel("Dispatching requires Coordinator or Administrator access. Resource changes are recorded in the audit log.");note.setForeground(new Color(95,105,118));p.add(note,BorderLayout.SOUTH);return p;
    }
    private JPanel audit(){
        JPanel p=page();JPanel box=panelCard("Recent activity");auditModel=model("Time","User","Action","Details");auditTable=new JTable(auditModel);styleTable(auditTable);box.add(new JScrollPane(auditTable),BorderLayout.CENTER);
        JButton refresh= new JButton("Refresh audit log");refresh.addActionListener(e->refreshAudit());box.add(refresh,BorderLayout.SOUTH);p.add(box,BorderLayout.CENTER);return p;
    }
    private JPanel page(){JPanel p=new JPanel(new BorderLayout(16,16));p.setBackground(pale);p.setBorder(new EmptyBorder(18,18,18,18));return p;}
    private JPanel panelCard(String title){JPanel p=new JPanel(new BorderLayout(12,12));p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(224,230,237)),new EmptyBorder(16,16,16,16)));JLabel t=new JLabel(title);t.setFont(new Font("SansSerif",Font.BOLD,16));t.setForeground(navy);p.add(t,BorderLayout.NORTH);return p;}
    private JButton primary(String text){JButton b=new JButton(text);b.setBackground(blue);b.setForeground(Color.WHITE);b.setFont(new Font("SansSerif",Font.BOLD,12));return b;}
    private DefaultTableModel model(String... cols){return new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};}
    private void styleTable(JTable t){t.setRowHeight(30);t.setFont(new Font("SansSerif",Font.PLAIN,12));t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setFillsViewportHeight(true);}
    private void addField(JPanel panel,GridBagConstraints g,int row,String label,Component c){g.gridy=row;g.gridx=0;g.weightx=0;panel.add(new JLabel(label),g);g.gridx=1;g.weightx=1;panel.add(c,g);}
    private void refreshAll(){
        try{
            List<Patient> ps=service.getPatients();
            if(queueModel!=null){queueModel.setRowCount(0);int waiting=0,critical=0;for(Patient x:ps){queueModel.addRow(new Object[]{x.getId(),x.getName(),x.getAge(),x.getSeverity(),x.getSeverityScore(),x.getStatus(),x.getArrivalTime(),x.hasVitalsAlert()?"⚠ ALERT":"—"});if("WAITING".equals(x.getStatus()))waiting++;if(x.getSeverity()==SeverityLevel.RED&&!x.getStatus().equals("COMPLETED")&&!x.getStatus().equals("CANCELLED"))critical++;}
                setMetric(totalLabel,""+ps.size());setMetric(waitingLabel,""+waiting);setMetric(criticalLabel,""+critical);
            }
            if(patientPicker!=null){Object selected=patientPicker.getSelectedItem();patientPicker.removeAllItems();for(Patient x:ps)if(!"COMPLETED".equals(x.getStatus())&&!"CANCELLED".equals(x.getStatus()))patientPicker.addItem(x.getId());if(selected instanceof Integer)patientPicker.setSelectedItem(selected);refreshVitals();}
            refreshResources();refreshAudit();
        }catch(Exception ex){error("Refresh failed: "+ex.getMessage());}
    }
    private void refreshResources(){
        if(resourceModel==null)return;
        try{resourceModel.setRowCount(0);int available=0;for(String[] r:service.getResources()){resourceModel.addRow(r);if("AVAILABLE".equals(r[3]))available++;}setMetric(resourceLabel,""+available);}catch(Exception ex){error(ex.getMessage());}
    }
    private void refreshAudit(){
        if(auditModel==null)return;try{auditModel.setRowCount(0);for(String[] r:service.getAuditLogs())auditModel.addRow(r);}catch(Exception ex){error(ex.getMessage());}
    }
    private void refreshVitals(){
        if(vitalsModel==null||patientPicker==null||patientPicker.getSelectedItem()==null)return;
        try{vitalsModel.setRowCount(0);for(String[] r:service.getVitals((Integer)patientPicker.getSelectedItem()))vitalsModel.addRow(r);}catch(Exception ex){error(ex.getMessage());}
    }
    private Integer selectedPatientId(){
        if(queueTable!=null&&queueTable.getSelectedRow()>=0)return Integer.parseInt(""+queueTable.getValueAt(queueTable.getSelectedRow(),0));
        return null;
    }
    private SeverityLevel selectedSeverity(int id){
        try{for(Patient p:service.getPatients())if(p.getId()==id)return p.getSeverity();}catch(Exception ignored){}return SeverityLevel.GREEN;
    }
    private void error(String text){JOptionPane.showMessageDialog(this,text==null?"Unknown error":text,"Unable to complete action",JOptionPane.ERROR_MESSAGE);}
}
