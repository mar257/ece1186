package TrainControllerComps;

import TrackModel.TrackModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.Timer;

import TrainModel.*;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This class is a GUI that is used to control a selected train. The selected train
 * is a Train object that is selected by using the train drop down menu.
 *
 * This class communicates with other components such as:
 *
 * TCTrainInfoPanel - displays basic train info such as speed, power, authority, and suggested speed.
 * TCSpeedController - allows the user to control the train's speed.
 *
 * @author Andrew Lendacky
 */
public class TrainController extends javax.swing.JFrame {

    /**
     * Used to get a dispatched train by its ID.
     **/
    private HashMap<String, Train> trainList = new HashMap<String, Train>();

    /**
     * Train the TrainController is controlling. 
     */
    private Train selectedTrain; // the train that the Train Controller is controlling.

    /**
     * Used to tell if the Train Controller is in Manual mode.
     */
    private boolean manualMode; // used to tell if the Train Controller is in Manual mode
    
    /**
     * Used to tell if the Train Controller is in Automatic mode.
     */
    private boolean automaticMode; // used to tell if the Train Controller is in Automatic mode

    /**
     * Used to tell if the Train Controller is in Manual mode.
     */
    private boolean normalMode; // used to tell if the Train Controller is in Manual mode
    
    /**
     * Used to tell if the Train Controller is in Automatic mode
     */
    private boolean testingMode; 
    
    // FOR TESTING!
    //ArrayList<Train> trains = new ArrayList<Train>();

    /**
     * Test Console to test the Train Controller. 
     */
    private TCTestConsole testConsole = null;
 
    /**
     * Train Handler to manage the dispatched trains. 
     * Used mainly to get updated lists of dispatched trains. 
     */
    TrainHandler trainHandler; 

    /**
     * Indicates if the trainUI is open.
     */
    public boolean detailedTrainWindowOpen;

    /**
     * Train Model GUI used to display more information for the selected train.
     */
    TrainModeUI trainUI;
    
    /**
     * Indicates if the test console is open.
     */
    boolean testWindowOpen;
        
    
    public Timer clock;    
    
    // MARK: - Constructors

    /**
     * Constructor that creates a Train Controller.
     * By default the starting mode is 'Manual' mode.
     *
     */
    public TrainController() {

        initComponents();

        this.initHashMaps();
        this.setTrainListComboBox();
        this.setMode("Manual", "Normal");

        this.speedController.setOperatingLogs(this.operatingLogs);
        this.utilityPanel.setVitalsButton(this.vitals);
        this.utilityPanel.setAnnouncementLog(this.annoucementLogs);
        this.utilityPanel.setOperatingLog(this.operatingLogs);
        this.brakePanel.setSpeedController(this.speedController);
        
        this.detailedTrainWindowOpen = false;
    }

    /**
     * Constructor that creates a Train Controller for a give train in Manual and Normal mode.
     *
     * @param train the train the controller will launch with.
     */
    public TrainController(Train train){

        initComponents();
        this.setMode("Manual", "Normal");

        this.selectedTrain = train;

        this.speedController.setOperatingLogs(this.operatingLogs);
        this.utilityPanel.setVitalsButton(this.vitals);
        this.utilityPanel.setAnnouncementLog(this.annoucementLogs);
        this.utilityPanel.setOperatingLog(this.operatingLogs);
        
        this.brakePanel.setSpeedController(this.speedController);
        this.brakePanel.setAnnouncementLogs(this.annoucementLogs);
        
        this.detailedTrainWindowOpen = false;

        // check if kp/ki is set
        if (this.selectedTrain.powerConstantsSet() == false){

            TCEngineerPanel engPanel = new TCEngineerPanel(this.selectedTrain);
            engPanel.setVisible(true);
            engPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }

    /**
     * Constructor that creates a Train Controller for a give train in Manual and Normal mode.
     *
     * @param train the train the controller will launch with.
     */
    public TrainController(Train train, Boolean test){

        initComponents();
        this.setMode("Manual", "Normal");

        this.selectedTrain = train;

        this.speedController.setOperatingLogs(this.operatingLogs);
        this.utilityPanel.setVitalsButton(this.vitals);
        this.utilityPanel.setAnnouncementLog(this.annoucementLogs);
        this.utilityPanel.setOperatingLog(this.operatingLogs);
        this.brakePanel.setSpeedController(this.speedController);
        
        this.detailedTrainWindowOpen = false;

        // check if kp/ki is set
        if (this.selectedTrain.powerConstantsSet() == false && test == false){

            TCEngineerPanel engPanel = new TCEngineerPanel(this.selectedTrain);
            engPanel.setVisible(true);
            engPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }

    /**
     * Constructor that creates a Train Controller in a given Test and Play mode with a given train.
     *
     * @param train the train the Train Controller with control.
     * @param playMode the mode (Manual or Automatic) that the Train Controller will launch in.
     * @param testMode the mode (Normal or Testing) that the Train Controller will launch in.
     */
    public TrainController(Train train, String playMode, String testMode){

        initComponents();
        this.selectedTrain = train;
        this.setMode(playMode, testMode);
        
        this.testWindowOpen = false; 
        this.speedController.setOperatingLogs(this.operatingLogs);
        this.utilityPanel.setVitalsButton(this.vitals);
        this.utilityPanel.setAnnouncementLog(this.annoucementLogs);
        this.utilityPanel.setOperatingLog(this.operatingLogs);
        this.brakePanel.setSpeedController(this.speedController);
        this.brakePanel.setAnnouncementLogs(this.annoucementLogs);
        
        this.detailedTrainWindowOpen = false;

        // set kp and ki to some default value if in automatic mode
        if (this.automaticMode == true){ train.setKpAndKi(4000.0, 200.0); }
          
        // check if kp/ki is set
        if (this.selectedTrain.powerConstantsSet() == false){

            TCEngineerPanel engPanel = new TCEngineerPanel(this.selectedTrain);
            engPanel.setVisible(true);
            engPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }        
        //this.playNormal();
    }
    
    /**
     * Updates the train controller and some of its sub windows. 
     */
    public void updateTrainController(){
     
        if (selectedTrain != null && selectedTrain.powerConstantsSet() ){ refreshComponents(); }

        if (testWindowOpen){ testConsole.refreshUI(); }
                
        if (detailedTrainWindowOpen == true){ trainUI.updateGUI(selectedTrain); }
    }
        
    /**
     * Sets the test console to use with this Train Controller. 
     * 
     * @param testConsole The test console object.
     */
    public void setTestConsole(TCTestConsole testConsole){
    
        this.testConsole = testConsole; 
    }
    
    /**
     * Sets the train handler to be used for this Train Controller. 
     * 
     * @param trainHandler train handler object.
     */
    public void setTrainHandler(TrainHandler trainHandler){
        
        this.trainHandler = trainHandler; 
    }   

    /**
     * Retrieves the TrainInfoPanel of the Train Controller.
     *
     * @return returns the Train Info panel.
     */
    public TCTrainInfoPane getTrainInfoPane(){

        return this.trainInfoPanel;
    }

    /**
     * Retrieves the SpeedController component of the Train Controller.
     * 
     * @return returns the Speed Controller panel.
     */
    public TCSpeedController getSpeedController(){

        return this.speedController;
    }

    /**
     * Returns the block info panel. 
     * 
     * @return the block info panel object.
     */
    public TCBlockInfoPanel getBlockInfoPane(){

        return this.blockInfoPane;
    }
    
    /**
     * Returns the brake panel. 
     * 
     * @return brake panel object.
     */
    public TCBrakePanel getBrakePanel(){
        
        return this.brakePanel;
    }

    // MARK: - Mode Setting and Getting

    /**
     * Sets the modes of the Train Controller.
     *
     * @param playMode the mode (Manual or Automatic) that the Train Controller will launch in.
     * @param testMode the mode (Normal or Testing) that the Train Controller will launch in.
     */
    public void setMode(String playMode, String testMode){

        if (playMode.equals("Automatic")){

            this.automaticMode = true;
            this.manualMode = false;
            this.automaticModeRadioButton.setSelected(true);


        }else if (playMode.equals("Manual")){

            this.manualMode = true;
            this.automaticMode = false;
            this.manualModeRadioButton.setSelected(true);

        }else{
            System.out.println("No Real Mode Picked. Must be 'Automatic' or 'Manual' ");
        }


        if (testMode.equals("Normal")){

            this.normalMode = true;
            this.testingMode = false;
            this.normalModeRadioButton.setSelected(true);

        }else if (testMode.equals("Testing")){

            this.testingMode = true;
            this.normalMode = false;
            this.testingModeRadioButton.setSelected(true);

        }else{
            System.out.println("No Real Mode Picked. Must be 'Testing' or 'Normal' ");
        }
    }

    /**
     * Retrieves which Play mode the Train Controller is in.
     *
     * @return returns either Manual if the Train Controller is in manual mode, and "Automatic" if in automatic mode.
     */
    public String getPlayMode(){

        if (this.manualMode == true){ return "Manual"; }
        else if(this.automaticMode == true){ return "Automatic"; }
        else{ return null; } // no mode was set
    }

    /**
     * Retrieves which Test mode the Train Controller is in.
     *
     * @return returns either Testing if the Train Controller is in testing mode, and "Normal" if in normal mode.
     */
    public String getTestMode(){

        if (this.testingMode == true){ return "Testing"; }
        else if(this.normalMode == true){ return "Normal"; }
        else{ return null; } // no mode was set
    }

    // MARK: - Configure Data Structures

    /**
     * Takes the list of dispatched trains, and stores them in a HashMap with the key-value pair as
     * the train's id and the train object.
     */
    private void initHashMaps(){

        if (this.trainHandler != null){
        
            // get the list of dispatched trains
            for (Train train : this.trainHandler.getTrains()){
                // add them to the hashmaps
                this.trainList.put(Integer.toString(train.getID()), train );
            }
        }
    }

    // MARK: - Train Stuff

    /**
     * Sets the selected train that the Train Controller will be controlling.
     *
     * @param train the train object that the Train Controller will control.
     */
    private void setTrain(Train train){

        this.selectedTrain = train;
        this.switchTrain(); 
    }

    /**
     * Retrieves the selected train that the Train Controller is controlling.
     *
     * @return returns the selected train that the Train Controller is controlling, or returns null if no train is selected.
     */
    public Train getTrain(){

        if (this.selectedTrain == null){
            System.out.println("No train is selected");
            return null;
        }else{
            return this.selectedTrain;
        }
    }
    
    /**
     * Refreshes the components of a train controller when a train is switched to. 
     */
    public void switchTrain(){
        
        this.refreshComponents();
    }

    // MARK: - Configure UI

    /**
     * Updates the combo box that contains the dispatched trains.
     */
    public void setTrainListComboBox(){

        this.dispatchedTrains.removeAllItems();
        this.dispatchedTrains.addItem("No Train Selected");

        if (this.trainHandler != null){ 
            
            this.initHashMaps(); 
            System.out.println(this.trainHandler.getNumTrains()); 
            for (Train train : this.trainHandler.getTrains()){

                this.dispatchedTrains.addItem(Integer.toString(train.getID()) );
            }
        }
    }
    
    /**
     * Sets the item that is selected in the train dropdown combo box.
     * 
     * @param pos the index of the selected item.
     */
    public void setSelectedItem(int pos){
    
        /**
         * @bug The train controller doesn't always update the train that it is controlling 
         * in the combo box, but is still controlling the correct train. 
         * 
         */
        this.dispatchedTrains.setSelectedIndex(pos);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        auto_manGroup = new javax.swing.ButtonGroup();
        normal_testGroup = new javax.swing.ButtonGroup();
        trainInfoTitle = new javax.swing.JLabel();
        trainSelectionTitle = new javax.swing.JLabel();
        uiSeparatorThree = new javax.swing.JSeparator();
        uiSeparatorTwo = new javax.swing.JSeparator();
        timeLabel = new javax.swing.JLabel();
        dateLabel = new javax.swing.JLabel();
        dispatchedTrains = new javax.swing.JComboBox<>();
        switchTrains = new javax.swing.JButton();
        setKpAndKi = new javax.swing.JButton();
        vitals = new javax.swing.JButton();
        modeSelectionTitle = new javax.swing.JLabel();
        uiSeparatorOne = new javax.swing.JSeparator();
        automaticModeRadioButton = new javax.swing.JRadioButton();
        manualModeRadioButton = new javax.swing.JRadioButton();
        normalModeRadioButton = new javax.swing.JRadioButton();
        testingModeRadioButton = new javax.swing.JRadioButton();
        notificationsTitle = new javax.swing.JLabel();
        utilitiesTitle = new javax.swing.JLabel();
        uiSeparatorFive = new javax.swing.JSeparator();
        uiSeparatorSix = new javax.swing.JSeparator();
        operatingLogsScrollPane = new javax.swing.JScrollPane();
        operatingLogs = new javax.swing.JTextArea();
        errorLogsLabel = new javax.swing.JLabel();
        operatingLogsLabel = new javax.swing.JLabel();
        announcementsLabel = new javax.swing.JLabel();
        annoucementDropDown = new javax.swing.JComboBox<>();
        chooseAnnouncementLabel = new javax.swing.JLabel();
        makeAnnouncementButton = new javax.swing.JButton();
        uiSeparatorFour = new javax.swing.JSeparator();
        clearOperatingLog = new javax.swing.JButton();
        clearErrorLog = new javax.swing.JButton();
        clearAnnouncements = new javax.swing.JButton();
        trainInfoPanel = new TrainControllerComps.TCTrainInfoPane();
        utilityPanel = new TrainControllerComps.TCUtilityPanel();
        announcementScrollPane1 = new javax.swing.JScrollPane();
        annoucementLogs = new javax.swing.JTextArea();
        brakePanel = new TrainControllerComps.TCBrakePanel();
        speedController = new TrainControllerComps.TCSpeedController();
        blockInfoPane = new TrainControllerComps.TCBlockInfoPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorLogs = new javax.swing.JTextPane();
        time = new javax.swing.JLabel();
        date = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        viewMenu = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        TrainDetailMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        trainInfoTitle.setFont(new java.awt.Font("Lucida Grande", 1, 22)); // NOI18N
        trainInfoTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        trainInfoTitle.setText("Train Info");

        trainSelectionTitle.setFont(new java.awt.Font("Lucida Grande", 1, 24)); // NOI18N
        trainSelectionTitle.setText("Train Selection");

        uiSeparatorThree.setForeground(new java.awt.Color(0, 0, 0));

        uiSeparatorTwo.setForeground(new java.awt.Color(0, 0, 0));
        uiSeparatorTwo.setOrientation(javax.swing.SwingConstants.VERTICAL);

        timeLabel.setText("Time:");

        dateLabel.setText("Date:");

        dispatchedTrains.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No Train Selected" }));

        switchTrains.setText("Switch");
        switchTrains.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchTrains(evt);
            }
        });

        setKpAndKi.setText("Set Kp/Ki");
        setKpAndKi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setKpAndKi(evt);
            }
        });

        vitals.setText("Vitals");
        vitals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewVitals(evt);
            }
        });

        modeSelectionTitle.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        modeSelectionTitle.setText("Mode Selection");

        uiSeparatorOne.setForeground(new java.awt.Color(0, 0, 0));

        auto_manGroup.add(automaticModeRadioButton);
        automaticModeRadioButton.setText("Automatic");
        automaticModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchToAutoMode(evt);
            }
        });

        auto_manGroup.add(manualModeRadioButton);
        manualModeRadioButton.setText("Manual");
        manualModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchToManualMode(evt);
            }
        });

        normal_testGroup.add(normalModeRadioButton);
        normalModeRadioButton.setText("Normal");
        normalModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                normalModeSelected(evt);
            }
        });

        normal_testGroup.add(testingModeRadioButton);
        testingModeRadioButton.setText("Testing");
        testingModeRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testModeSelected(evt);
            }
        });

        notificationsTitle.setFont(new java.awt.Font("Lucida Grande", 1, 24)); // NOI18N
        notificationsTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        notificationsTitle.setText("Notifications");

        utilitiesTitle.setFont(new java.awt.Font("Lucida Grande", 1, 24)); // NOI18N
        utilitiesTitle.setText("Utilities");

        uiSeparatorFive.setForeground(new java.awt.Color(0, 0, 0));
        uiSeparatorFive.setOrientation(javax.swing.SwingConstants.VERTICAL);

        uiSeparatorSix.setForeground(new java.awt.Color(0, 0, 0));
        uiSeparatorSix.setOrientation(javax.swing.SwingConstants.VERTICAL);

        operatingLogs.setColumns(20);
        operatingLogs.setRows(5);
        operatingLogsScrollPane.setViewportView(operatingLogs);

        errorLogsLabel.setText("Error Logs:");

        operatingLogsLabel.setText("Operating Logs:");

        announcementsLabel.setText("Announcements:");

        annoucementDropDown.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No Announcement", "Announce Arrival", "Announce Departure", "Announce Next Station" }));

        chooseAnnouncementLabel.setText("Choose Announcement");

        makeAnnouncementButton.setText("Make Announcement");
        makeAnnouncementButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeAnnouncement(evt);
            }
        });

        uiSeparatorFour.setForeground(new java.awt.Color(0, 0, 0));
        uiSeparatorFour.setOrientation(javax.swing.SwingConstants.VERTICAL);

        clearOperatingLog.setText("Clear");
        clearOperatingLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearOperatingLogs(evt);
            }
        });

        clearErrorLog.setText("Clear");
        clearErrorLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearErrorLogs(evt);
            }
        });

        clearAnnouncements.setText("Clear");
        clearAnnouncements.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAnnouncements(evt);
            }
        });

        annoucementLogs.setColumns(20);
        annoucementLogs.setRows(5);
        announcementScrollPane1.setViewportView(annoucementLogs);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 22)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Speed Controller");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 22)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Brakes");

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 1, 22)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Block Info");

        jScrollPane1.setViewportView(errorLogs);

        time.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        time.setText("0");

        date.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        date.setText("0");

        viewMenu.setText("View");

        jMenuItem5.setText("Dispatched Trains");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDispatchedTrains(evt);
            }
        });
        viewMenu.add(jMenuItem5);

        TrainDetailMenuItem.setText("Selected Train Detail");
        TrainDetailMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTrainGUI(evt);
            }
        });
        viewMenu.add(TrainDetailMenuItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(68, 68, 68)
                                    .addComponent(utilitiesTitle)
                                    .addGap(89, 89, 89))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(utilityPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                            .addComponent(uiSeparatorFive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(operatingLogsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(operatingLogsLabel)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(clearOperatingLog, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(annoucementDropDown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(makeAnnouncementButton, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(6, 6, 6)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(errorLogsLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(clearErrorLog, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(layout.createSequentialGroup()
                                                    .addComponent(announcementsLabel)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(clearAnnouncements, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(announcementScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE)
                                                .addComponent(chooseAnnouncementLabel)
                                                .addComponent(jScrollPane1)))))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(54, 54, 54)
                                    .addComponent(notificationsTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(uiSeparatorSix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(speedController, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(blockInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(1, 1, 1)
                                    .addComponent(uiSeparatorFour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(brakePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(normalModeRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(automaticModeRadioButton))
                                    .addComponent(dispatchedTrains, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(vitals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(setKpAndKi, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(switchTrains, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(manualModeRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(testingModeRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(28, 28, 28)
                                    .addComponent(modeSelectionTitle))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(24, 24, 24)
                                    .addComponent(trainSelectionTitle))
                                .addComponent(uiSeparatorOne, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(uiSeparatorTwo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(12, 12, 12)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(timeLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(time, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(trainInfoTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(37, 37, 37)
                                    .addComponent(dateLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(date, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(trainInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(uiSeparatorThree, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1006, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(uiSeparatorTwo, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(uiSeparatorFour, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(trainInfoTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(timeLabel)
                                                .addComponent(dateLabel)
                                                .addComponent(time)
                                                .addComponent(date))
                                            .addGap(3, 3, 3)))
                                    .addComponent(trainInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(brakePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(trainSelectionTitle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dispatchedTrains, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(switchTrains)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(vitals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(setKpAndKi, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(uiSeparatorOne, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modeSelectionTitle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(automaticModeRadioButton)
                                    .addComponent(manualModeRadioButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(normalModeRadioButton)
                                    .addComponent(testingModeRadioButton))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(uiSeparatorThree, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(uiSeparatorFive)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(utilitiesTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(utilityPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(notificationsTitle))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(operatingLogsLabel)
                                    .addComponent(clearOperatingLog, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(errorLogsLabel)
                                    .addComponent(clearErrorLog, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(announcementsLabel)
                                    .addComponent(clearAnnouncements, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(announcementScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseAnnouncementLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(annoucementDropDown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(makeAnnouncementButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(blockInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addGap(10, 10, 10)
                                .addComponent(speedController, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(operatingLogsScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(uiSeparatorSix))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

    }//GEN-LAST:event_jButton2ActionPerformed


    /**
     * The action that is performed when the "Switch" button is pressed. This method
     * switches the Train Controller's selected train to that of the train that is
     * picked from the dispatched train drop down menu.
     *
     * @param evt the sender of the action, i.e., the "Switch" button.
     */
    private void switchTrains(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchTrains

        // we dont want to do anything if "No Train Selected" is picked...
        System.out.println(this.dispatchedTrains.getSelectedIndex());
        if (this.dispatchedTrains.getSelectedIndex() != 0){

            // get the train id that is selected
            String trainId = (String) this.dispatchedTrains.getSelectedItem();

            // get the train from the hashMap
            this.selectedTrain = this.trainList.get(trainId);

            if (this.selectedTrain.powerConstantsSet() == false && this.NoTrainSelected() == false){
                System.out.println("Opening Engineering Panel");
                // open up the engineering panel
                TCEngineerPanel engPanel = new TCEngineerPanel(this.selectedTrain);
                engPanel.setVisible(true);
                engPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            }
            
            this.setTrains(this.selectedTrain);

            //this.refreshComponents(); // populate the other components with train info
        }else{ System.out.println((String) this.dispatchedTrains.getSelectedItem()); }

    }//GEN-LAST:event_switchTrains

    public void setTrains(Train train){

        this.speedController.setTrain(train);
        this.utilityPanel.setSelectedTrain(train);
        this.trainInfoPanel.setSelectedTrain(train);
        this.brakePanel.setSelectedTrain(train);
    }
    
    public void setSelectedTrain(Train train){
        
        this.selectedTrain = train; 
    }
    
    /**
     * Opens up the Engineering Panel so the engineer can change the Kp and Ki
     * manually.
     *
     * @param evt the sender of the event, i.e., the "Set Kp/Ki" Button.
     */
    private void setKpAndKi(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setKpAndKi

        if (this.NoTrainSelected() == false){

            // open up the engineering panel
            TCEngineerPanel engPanel = new TCEngineerPanel(this.selectedTrain);
            engPanel.setVisible(true);
            engPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }//GEN-LAST:event_setKpAndKi

    /**
     * Opens up the Failures Panel so that the train's vitals can be viewed.
     * These failures consist of Power, Antenna, and Brake.
     *
     * @param evt the sender of the action, i.e., the "View Vitals" button.
     */
    private void viewVitals(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewVitals

        if (this.NoTrainSelected()){
            System.out.println("No Train Selected");
        }else if (this.NoTrainSelected() == false){

            // open up a vitals window to monitor vitals
            TCFailures vitalPanel = new TCFailures(this.selectedTrain);
            vitalPanel.setVisible(true);
            vitalPanel.setErrorLogs(this.errorLogs);
            vitalPanel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }//GEN-LAST:event_viewVitals

    /**
     * Clears the text of the Operating Logs.
     *
     * @param evt the sender of the action, i.e., the "Clear" button.
     */
    private void clearOperatingLogs(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearOperatingLogs
        this.operatingLogs.setText("");
    }//GEN-LAST:event_clearOperatingLogs

    /**
     * Clears the text of the Error Logs.
     *
     * @param evt the sender of the action, i.e., the "Clear" button.
     */
    private void clearErrorLogs(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearErrorLogs
        this.errorLogs.setText("");
    }//GEN-LAST:event_clearErrorLogs

    /**
     * Clears the text of the Announcement Logs.
     *
     * @param evt the sender of the action, i.e., the "Clear" button.
     */
    private void clearAnnouncements(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAnnouncements
        this.annoucementLogs.setText("");
    }//GEN-LAST:event_clearAnnouncements

    /**
     * Opens a window that displays the list of dispatched trains.
     * This allows the user to open multiple Train Controllers for selected trains.
     *
     * @param evt the sender of the action, i.e., the "Dispatched Trains" button from the menu bar.
     */
    private void openDispatchedTrains(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDispatchedTrains
        TCDispatchedTrainFrame dispatched = new TCDispatchedTrainFrame(this.trainHandler);
        dispatched.setVisible(true);
        dispatched.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }//GEN-LAST:event_openDispatchedTrains

    /**
     * Prints the announcement that is selected by the Announcement Combo Box to the Announcement Logs.
     *
     * @param evt the sender of the action, i.e., the "Make Announcement" button.
     */
    private void makeAnnouncement(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeAnnouncement

        String dropDownText = (String) this.annoucementDropDown.getSelectedItem();
        
        if (dropDownText.equals("Announce Arrival") && this.brakePanel.approachingStationName != null){
        
            dropDownText = "Arriving at " + this.brakePanel.approachingStationName; 
            this.annoucementLogs.setText(this.annoucementLogs.getText() + dropDownText + "\n");
        }else if(dropDownText.equals("Announce Next Station") && this.brakePanel.approachingStationName != null){
        
            dropDownText = dropDownText + " " + this.brakePanel.approachingStationName; 
            this.annoucementLogs.setText(this.annoucementLogs.getText() + dropDownText + "\n");
        }else if (dropDownText.equals("Announce Departure") && this.brakePanel.approachingStationName != null){
        
            dropDownText = "Departing from " + this.brakePanel.approachingStationName; 
            this.annoucementLogs.setText(this.annoucementLogs.getText() + dropDownText + "\n");
        }
    }//GEN-LAST:event_makeAnnouncement

    /**
     * Opens up the Test Console when the Testing mode radio button is clicked.
     *
     * @param evt the sender of the action, i.e., the "Testing" radio button.
     */
    private void testModeSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testModeSelected

        if (this.testWindowOpen == true && this.selectedTrain != null){
        
            this.testConsole.setTrain(this.selectedTrain);
        }        
    }//GEN-LAST:event_testModeSelected

    private void normalModeSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normalModeSelected

        this.normalMode = true;
        this.testingMode = false;

        System.out.println("Normal Mode: " + this.normalMode + " Testing Mode: " + this.testingMode);
    }//GEN-LAST:event_normalModeSelected

    private void switchToAutoMode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchToAutoMode
        // switch modes
        this.manualMode = false;
        this.automaticMode = true;
    }//GEN-LAST:event_switchToAutoMode

    private void switchToManualMode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchToManualMode
        // switch mode
        this.manualMode = true;
        this.automaticMode = false;
    }//GEN-LAST:event_switchToManualMode

    private void openTrainGUI(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTrainGUI
        
        // open up the train GUI
        this.trainUI = new TrainModeUI();

        trainUI.updateGUI(this.selectedTrain);

        trainUI.frmTrainModel.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        trainUI.frmTrainModel.setVisible(true);

        this.detailedTrainWindowOpen = true;
    }//GEN-LAST:event_openTrainGUI

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
     * Checks if there is a selected train or not.
     *
     * @return returns true if no train is selected, false if a train is selected.
     */
    private boolean NoTrainSelected(){

        if (this.selectedTrain == null){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Returns the Utility Panel object contained in the Train Controller. 
     * 
     * @return the TCUtilityPanel object used by the Train Controller class.
     */
    public TCUtilityPanel getUtilityPane(){
    
        return this.utilityPanel;
    }

    /**
     * Populates the other components of the Train Controller with the information
     * from the selected train. This functions calls the other component's refreshUI()
     * functions.
     *
     * FIXME:
     * For now, it only updates the train's speed and power in the TrainInfoPanel.
     * Flesh this out more!
     */
    public void refreshComponents(){

        this.updateTime();

        //this.initHashMaps();

        if (this.NoTrainSelected() == false){

            // assign other componets the selected train

            this.speedController.setTrain(this.selectedTrain);
            this.speedController.setManualMode(this.manualMode);
            this.speedController.setBrakePanel(this.brakePanel);
            this.speedController.refreshUI();
            
            this.trainInfoPanel.setSelectedTrain(this.selectedTrain);
            this.trainInfoPanel.refreshUI();
            
//            // FIX ME: TrainInfoPanelStuff should be put in the refreshUI method in the
//            // TrainInfoPanelClass
//

            this.blockInfoPane.setSelectedTrain(this.selectedTrain);
            this.blockInfoPane.refreshUI();

            this.utilityPanel.setManualMode(this.manualMode);
            this.utilityPanel.setSelectedTrain(this.selectedTrain);
            this.utilityPanel.refreshUI();
                    
            this.brakePanel.setSelectedTrain(this.selectedTrain);
            this.brakePanel.setOperatingLogs(this.operatingLogs);
            this.brakePanel.inManualMode(this.manualMode);
            this.brakePanel.refreshUI();

   
        }
    }
    
    /**
     * Updates the time and date label of the Train Controller. This method is called
     * every second via the Timer object, t.
     */
    private void updateTime(){

        // update time and date label
        this.time.setText(this.getTime());
        this.date.setText(this.getDate());
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
            java.util.logging.Logger.getLogger(TrainController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TrainController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TrainController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TrainController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TrainController().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem TrainDetailMenuItem;
    private javax.swing.JComboBox<String> annoucementDropDown;
    private javax.swing.JTextArea annoucementLogs;
    private javax.swing.JScrollPane announcementScrollPane1;
    private javax.swing.JLabel announcementsLabel;
    private javax.swing.ButtonGroup auto_manGroup;
    private javax.swing.JRadioButton automaticModeRadioButton;
    private TrainControllerComps.TCBlockInfoPanel blockInfoPane;
    private TrainControllerComps.TCBrakePanel brakePanel;
    private javax.swing.JLabel chooseAnnouncementLabel;
    private javax.swing.JButton clearAnnouncements;
    private javax.swing.JButton clearErrorLog;
    private javax.swing.JButton clearOperatingLog;
    private javax.swing.JLabel date;
    private javax.swing.JLabel dateLabel;
    private javax.swing.JComboBox<String> dispatchedTrains;
    private javax.swing.JTextPane errorLogs;
    private javax.swing.JLabel errorLogsLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton makeAnnouncementButton;
    private javax.swing.JRadioButton manualModeRadioButton;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel modeSelectionTitle;
    private javax.swing.JRadioButton normalModeRadioButton;
    private javax.swing.ButtonGroup normal_testGroup;
    private javax.swing.JLabel notificationsTitle;
    private javax.swing.JTextArea operatingLogs;
    private javax.swing.JLabel operatingLogsLabel;
    private javax.swing.JScrollPane operatingLogsScrollPane;
    private javax.swing.JButton setKpAndKi;
    private TrainControllerComps.TCSpeedController speedController;
    private javax.swing.JButton switchTrains;
    private javax.swing.JRadioButton testingModeRadioButton;
    private javax.swing.JLabel time;
    private javax.swing.JLabel timeLabel;
    private TrainControllerComps.TCTrainInfoPane trainInfoPanel;
    private javax.swing.JLabel trainInfoTitle;
    private javax.swing.JLabel trainSelectionTitle;
    private javax.swing.JSeparator uiSeparatorFive;
    private javax.swing.JSeparator uiSeparatorFour;
    private javax.swing.JSeparator uiSeparatorOne;
    private javax.swing.JSeparator uiSeparatorSix;
    private javax.swing.JSeparator uiSeparatorThree;
    private javax.swing.JSeparator uiSeparatorTwo;
    private javax.swing.JLabel utilitiesTitle;
    private TrainControllerComps.TCUtilityPanel utilityPanel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JButton vitals;
    // End of variables declaration//GEN-END:variables

}
