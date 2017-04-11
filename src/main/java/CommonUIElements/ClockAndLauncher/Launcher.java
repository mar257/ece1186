/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CommonUIElements.ClockAndLauncher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Random;
import java.util.ArrayList;
import javax.swing.Timer;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.IOException;


import sun.audio.*;

// import modules here:
import TrainControllerComps.*;
import WaysideController.*;
import TrackModel.TrackModel;
import TrackModel.TrackGUI;
import MBO.*;
import CTC.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import TrainModel.*;



/**
 * This class is responsible for refreshing the system on a given clock period as
 * well as launch the various GUI for each of the models.
 */
public class Launcher extends javax.swing.JFrame {

    /**
     * The speed that the system is running in.
     */
    int systemSpeed;



    /**
     * The timer used to refresh the modules during some given time period.
     */
    private Timer systemClock;

    //References to ACTIVE modules

    /**
     * Plays train sounds when opening
     * @bug WARNING WARNING WARNING WARNING ON AUDIO CRAP
     */
    private void playSound(){

        try{
            // Open an audio input stream.
            File soundFile = new File("classes/soundclips/TrainWhistle.wav");
            InputStream in = new FileInputStream(soundFile);

            AudioStream audioIn = new AudioStream(in);
            AudioPlayer.player.start(audioIn);
        }catch(Exception e){



            System.out.println(e.getMessage());
        }
    }


    //Track
    private TrackModel globalTrack;
    private TrackGUI trackGUI;
    //Wayside
    private ArrayList<WS> waysideList = new ArrayList<WS>();
    private WaysideGUI WaysideGui;
    //Train
    private TrainHandler trainHandler;
    private TrainModeUI trainGUI;
    //CTC
    private ArrayList<TrainManager> trainManagers = new ArrayList<TrainManager>();
    private CTCgui ctc;
    //MBO
    private MovingBlockOverlay mbo;

    /**
     * Constructor for creating a Launcher object. By default, the system begins operating
     * in normal speed, i.e., wall clock speed.
     *
     */
    public Launcher() {
        initComponents();

        this.playSound();
        this.normalSpeedRadioButton.setSelected(true);
        // for now, we start in normal mode
        this.systemSpeed = 1000;

        //Generate globalTrack
        this.globalTrack = new TrackModel("GlobalTrack");
        String path = "test-classes/redline.csv";
        String[] fNames = {path};
        File redline = new File(path);
        
        this.globalTrack.readCSV(fNames);
        this.trackGUI = new TrackGUI(globalTrack);

        //Cycle through number of lines and generate a WS and Train Manager for each line
        for(String s : this.globalTrack.trackList.keySet()){
          //System.out.println(s);
          WS ws = new WS(s, this.globalTrack);
          this.waysideList.add(ws);
          trainManagers.add(new TrainManager(s, generateTrack("TrainManager - " + s)));
        }

        //Set Wayside GUI for WS's
        this.WaysideGui = new WaysideGUI(this.globalTrack, this.waysideList);
        for(WS ws : this.waysideList)
          ws.setGUI(this.WaysideGui);

        this.trainHandler = new TrainHandler(globalTrack);
        this.trainGUI = new TrainModeUI();

        this.ctc = new CTCgui(trainManagers, generateTrack("CTC"), this.waysideList, globalTrack);
        this.mbo = new MovingBlockOverlay(generateTrack("MBO"), trainManagers, this.trainHandler, this.ctc);
        this.ctc.setMBO(this.mbo);

        this.systemClock = new Timer(this.systemSpeed, new ActionListener(){
            Random rand = new Random();
            public void actionPerformed(ActionEvent e) {

                updateDateAndTime();

                //Update WaysideGui and WS's
                WaysideGui.update();
                for(WS ws : waysideList){
                  try{
                    ws.update();
                  }catch(ScriptException ex){}
                }
                
                mbo.updateTrains();

                // what should be called every tick

                // CTC - update track panel on gui w/ info from WS
                ctc.getTrackPanel().updateTrackInfo(ctc.getTrackPanel().getBlockWS());
                // CTC - calls wayside to get updated list of track occupancy
                ctc.getTrainPanel().updateTrainPositionsToManager(trainManagers.get(0));
                // CTC - prints active list of trains from train manager to GUI
                //ctc.getTrainManagerPanel().updateTable(trainManagers.get(0));

                        trainHandler.pollYard();
                if(trainHandler.getNumTrains() != 0){
                  trainGUI.updateGUI(trainGUI.getCurrT());
                }

                // CTC - ask track for trainId
                ctc.getTrainPanel().updateTrainIDinList(trainManagers.get(0), globalTrack);


            }
        });

       this.systemClock.start();
    }

        /**
     * Returns the current time of the system in "HH:mm:ss a" format.
     * HH - the hours
     * mm - the minutes
     * ss - the seconds
     * a - AM or PM
     *
     * @return the current system time.
     */
    private String getTime(){

        DateFormat sdf;
        Calendar cal = Calendar.getInstance();
        sdf = new SimpleDateFormat("HH:mm:ss a");

        // get time
        return sdf.format(cal.getTime());
    }

    public static long getCurrTime(){
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }

        /**
     * Returns the current time of the system in "MM/dd/yyyy" format.
     * MM - month
     * dd - day
     * yyyy - year
     *
     * @return the current date of the system.
     */
    private String getDate(){

        LocalDate localDate = LocalDate.now();

        return DateTimeFormatter.ofPattern("MM/dd/yyyy").format(localDate);
    }

    /**
     * Returns the timer object that's being used.
     *
     * @return the timer object
     */
    public Timer getSystemClock(){

        return this.systemClock;
    }

    /**
     * Updates the date and time labels of the system.
     *
     */
    private void updateDateAndTime(){

        String date = this.getDate();
        String time = this.getTime();

        this.date.setText(date);
        this.time.setText(time);
        //System.out.println("Date Updated");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        speedButtonGroup = new javax.swing.ButtonGroup();
        launchTrackControllerButton = new javax.swing.JButton();
        launchTrainButton = new javax.swing.JButton();
        launchTrackButton = new javax.swing.JButton();
        launchTrainControllerButton = new javax.swing.JButton();
        launchMBOandScheduleButton = new javax.swing.JButton();
        launchCTCButton = new javax.swing.JButton();
        normalSpeedRadioButton = new javax.swing.JRadioButton();
        fastSpeedRadioButton = new javax.swing.JRadioButton();
        clockSpeedLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        timeLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        time = new javax.swing.JLabel();
        date = new javax.swing.JLabel();
        launchMBOandScheduleButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        launchTrackControllerButton.setText("Track Controller");
        launchTrackControllerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTrackController(evt);
            }
        });

        launchTrainButton.setText("Train");
        launchTrainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTrain(evt);
            }
        });

        launchTrackButton.setText("Track");
        launchTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTrack(evt);
            }
        });

        launchTrainControllerButton.setText("Train Controller");
        launchTrainControllerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTrainController(evt);
            }
        });

        launchMBOandScheduleButton.setText("MBO & Scheduler");
        launchMBOandScheduleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMBOandScheduler(evt);
            }
        });

        launchCTCButton.setText("CTC");
        launchCTCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openCTC(evt);
            }
        });

        speedButtonGroup.add(normalSpeedRadioButton);
        normalSpeedRadioButton.setText("x1");
        normalSpeedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playNormalSpeed(evt);
            }
        });

        speedButtonGroup.add(fastSpeedRadioButton);
        fastSpeedRadioButton.setText("x10");
        fastSpeedRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playFastSpeed(evt);
            }
        });

        clockSpeedLabel.setText("Clock Speed:");

        timeLabel.setText("Time:");

        dateLabel.setText("Date: ");

        time.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        date.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        launchMBOandScheduleButton1.setText("Logger");
        launchMBOandScheduleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLogger(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(launchCTCButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(timeLabel)
                        .addGap(33, 33, 33)
                        .addComponent(time, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(clockSpeedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(normalSpeedRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fastSpeedRadioButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(launchMBOandScheduleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(launchTrainControllerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(launchTrainButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(launchTrackControllerButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(launchTrackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(launchMBOandScheduleButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clockSpeedLabel)
                    .addComponent(normalSpeedRadioButton)
                    .addComponent(fastSpeedRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeLabel)
                    .addComponent(time))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateLabel)
                    .addComponent(date))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(launchCTCButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchTrackButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchTrackControllerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchTrainButton)
                .addGap(5, 5, 5)
                .addComponent(launchTrainControllerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchMBOandScheduleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchMBOandScheduleButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Switches the speed of the system to update every 1 second, 1000 ms.
     *
     * @param evt the event that triggered the action, i.e., the x1 radio button.
     */
    private void playNormalSpeed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playNormalSpeed

        this.systemSpeed = 1000;
        System.out.println("System should play in x1 speed.");

        this.systemClock = new Timer(this.systemSpeed, new ActionListener(){
            Random rand = new Random();
            public void actionPerformed(ActionEvent e) {

                updateDateAndTime();
                // what should be called every tick
            }
        });

        System.out.println(this.systemClock.getDelay());
    }//GEN-LAST:event_playNormalSpeed

    /**
     * Switches the speed of the system to update every 1/10 s, 100 ms.
     *
     * @param evt the event that triggered the action, i.e., the x10 radio button.
     */
    private void playFastSpeed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playFastSpeed

        // set the system speed
        this.systemSpeed = 100;
        System.out.println("System should play in x10 speed.");

        this.systemClock = new Timer(this.systemSpeed, new ActionListener(){
            Random rand = new Random();
            public void actionPerformed(ActionEvent e) {

                updateDateAndTime();

                // what should be called every tick
            }
        });

        System.out.println(this.systemClock.getDelay());
    }//GEN-LAST:event_playFastSpeed

    /**
     * Opens a Train Controller module.
     *
     * @param evt the sender of the event, i.e., the "Train Controller" button.
     */
    private void openTrainController(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTrainController

        TrainController tc = new TrainController();
        tc.setVisible(true);
        tc.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }//GEN-LAST:event_openTrainController

    /**
     * Opens the CTC module.
     *
     * @param evt
     */
    private void openCTC(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openCTC
        ctc.getFrame().setVisible(true);
        ctc.getFrame().setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }//GEN-LAST:event_openCTC

    /**
     * Opens the Track module.
     *
     * @param evt
     */
    private void openTrack(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTrack
        this.trackGUI.getFrame().setVisible(true);
        this.trackGUI.getFrame().setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }//GEN-LAST:event_openTrack

    /**
     * Opens the Track Controller module.
     *
     * @param evt
     */
    private void openTrackController(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTrackController
          WaysideGui.getFrame().setVisible(true);
          WaysideGui.getFrame().setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }//GEN-LAST:event_openTrackController

    /**
     * Opens the Train module.
     *
     * @param evt
     */
    private void openTrain(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTrain
        // TODO add your handling code here:
		trainGUI.frmTrainModel.setVisible(true);
        trainGUI.frmTrainModel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		trainGUI.setTrainArray(this.trainHandler.getTrains());
    }//GEN-LAST:event_openTrain

    /**
     * Opens the MBO and Scheduler module.
     *
     * @param evt
     */
    private void openMBOandScheduler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMBOandScheduler
        // TODO add your handling code here:
        mbo.initGUI();
    }//GEN-LAST:event_openMBOandScheduler

    private void createLogger(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLogger

        // logger stuff here...
        System.out.println("Logger Stuff!");

    }//GEN-LAST:event_createLogger

    /**
     * Generates a "Dummy Track" and returns it.
     * @return Dummy Track
     * @bug REDLINE ONLY CURRENTLY
     */
    public TrackModel generateTrack(String module){
      String[] fNames = {"test-classes/redline.csv"};

      TrackModel globalTrack = new TrackModel(module);
  	  globalTrack.readCSV(fNames);
      return globalTrack;
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Launcher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Launcher().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clockSpeedLabel;
    private javax.swing.JLabel date;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JRadioButton fastSpeedRadioButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton launchCTCButton;
    private javax.swing.JButton launchMBOandScheduleButton;
    private javax.swing.JButton launchMBOandScheduleButton1;
    private javax.swing.JButton launchTrackButton;
    private javax.swing.JButton launchTrackControllerButton;
    private javax.swing.JButton launchTrainButton;
    private javax.swing.JButton launchTrainControllerButton;
    private javax.swing.JRadioButton normalSpeedRadioButton;
    private javax.swing.ButtonGroup speedButtonGroup;
    private javax.swing.JLabel time;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables
}
