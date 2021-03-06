package TrainControllerComps;

import TrainModel.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * A component of the Train Controller responsible for the selected train's speed, power, authority, and suggested speed. 
 *
 * This class collaborates with the Train Controller and Train class. 
 * 
 * @author Andrew Lendacky
 */
public class TCTrainInfoPane extends javax.swing.JPanel {

    /**
     * The train used to update the UI based on its information.
     */
    private Train selectedTrain; 
    
    /**
     * Constructor for creating a TCTrainInfoPane object with no selected train.
     * The selected train must be set by the Train Controller before being used. 
     */
    public TCTrainInfoPane() {
        initComponents(); 
    }
    
    /**
     * Sets the train whose info to display to the user. 
     * 
     * @param train the train controlled by the Train Controller.
     */
    public void setSelectedTrain(Train train){
    
        this.selectedTrain = train;
        this.refreshUI();
    }
    
    /**
     * Returns the train that the train info panel is using.
     * 
     * @return the train object
     */
    public Train getSelectedTrain(){
    
        return this.selectedTrain;
    }
    
    /**
     * Sets the speed the train is currently going. 
     * 
     * @param speed the speed the train is going. 
     */
    public void setSpeedLabel(Double speed){
       
        this.currentSpeed.setText(String.format("%.2f", this.selectedTrain.getVelocity()));
    }
    

    /**
     * Sets the power the train is currently producing in kW. 
     * 
     * @param power the power the train is producing. 
     */
    public void setPowerLabel(Double power){
        
        this.currentPower.setText(String.format("%.2f", (this.selectedTrain.getPower()/1000)));
    }
    
    /**
     * Sets the suggested speed for the train. This speed comes from the CTC. 
     * 
     * @param suggSpeed the suggested speed for the train. 
     */
    public void setSuggestSpeedLabel(Double suggSpeed){
        
        this.suggestedSpeed.setText(String.format("%.2f", suggSpeed));
    }
    
    /**
     * Retrieves the speed the train is going from the speed label. 
     * 
     * @return returns the text corresponding to the speed of the train from the speed label. 
     */
    public String getSpeedLabel(){
    
        return this.currentSpeed.getText(); 
    }
    
    /**
     * Retrieves the power the train is applying from the power label. 
     * 
     * @return returns the text corresponding to the power of the train from the power label. 
     */
    public String getPowerLabel(){
    
        return this.currentPower.getText(); 
    }
     
    /**
     * Retrieves the suggested speed of the train from the suggested speed label. 
     * 
     * @return returns the text corresponding to the suggested speed of the train from the suggested speed label. 
     */
    public String getSuggestedSpeedLabel(){
    
        return this.suggestedSpeed.getText(); 
    }
    
    /**
     * Retrieves the max power the train can go from the max power label.
     * 
     * @return returns the text corresponding to the max power of the train from the suggested speed label. 
     */   
    public String getMaxPowerLabel(){
    
        return this.maxPower.getText();
    }

    /**
     * Sets the max power label.
     * 
     * @param maxPower the max power the train can produce.
     * 
     */
    public void setMaxPowerLabel(Double maxPower){
    
        this.maxPower.setText(Double.toString( maxPower/1000) );
    }
    
   /**
     * Retrieves the authority of the train from the authority label.
     * 
     * @return returns the text corresponding to the authority of the train from the authority label. 
     */ 
    public String getAuthorityLabel(){
    
        return this.authorityLabel.getText();
    }
    
    /**
     * Sets the authority label for the train.
     * 
     * @param blockNum the authority, as a block number.
     */
    public void setAuthorityLabel(Integer blockNum){
    
        this.maxAuthority.setText(Integer.toString(blockNum)); 
    }
    
    /**
     * Updates the labels with the information based on the selected train.
     * 
     */
    public void refreshUI(){
         
        // these should never be null
        this.setMaxPowerLabel(this.selectedTrain.getMaxPower() );
        this.setSpeedLabel( this.selectedTrain.getVelocity() );
        this.setPowerLabel( this.selectedTrain.getPower() );
        
        if (this.selectedTrain.getSuggestedSpeed() != null){
            
            Double blockSuggestedSpeed = .621371*this.selectedTrain.getSuggestedSpeed();
            this.setSuggestSpeedLabel( blockSuggestedSpeed );
        }
        
        if (this.selectedTrain.getAuthority().getCurrBlock() != null){
        
            this.setAuthorityLabel(this.selectedTrain.getAuthority().getCurrBlock().blockNum());
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        speedLabel = new javax.swing.JLabel();
        powerLabel = new javax.swing.JLabel();
        currentSpeed = new javax.swing.JLabel();
        currentPower = new javax.swing.JLabel();
        suggSpeedLabel = new javax.swing.JLabel();
        authorityLabel = new javax.swing.JLabel();
        suggestedSpeed = new javax.swing.JLabel();
        maxAuthority = new javax.swing.JLabel();
        uiSeparatorOne = new javax.swing.JSeparator();
        speedUnit = new javax.swing.JLabel();
        powerUnit = new javax.swing.JLabel();
        suggSpeedUnit = new javax.swing.JLabel();
        powerDivider = new javax.swing.JLabel();
        maxPower = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));

        speedLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        speedLabel.setText("Speed:");

        powerLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        powerLabel.setText("Power:");

        currentSpeed.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        currentSpeed.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        currentSpeed.setText("0.00");

        currentPower.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        currentPower.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        currentPower.setText("0.00");

        suggSpeedLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        suggSpeedLabel.setText("Sugg. Speed:");

        authorityLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        authorityLabel.setText("Authority (Block ID):");

        suggestedSpeed.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        suggestedSpeed.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        suggestedSpeed.setText("0");

        maxAuthority.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        maxAuthority.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        maxAuthority.setText("0");

        speedUnit.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        speedUnit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        speedUnit.setText("MPH");

        powerUnit.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        powerUnit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        powerUnit.setText("kW");

        suggSpeedUnit.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        suggSpeedUnit.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        suggSpeedUnit.setText("MPH");

        powerDivider.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        powerDivider.setText("/");

        maxPower.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        maxPower.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        maxPower.setText("0.00");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(speedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(powerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 101, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(currentSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(speedUnit))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(currentPower, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(powerDivider)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxPower, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(powerUnit)))
                .addGap(31, 31, 31))
            .addComponent(uiSeparatorOne)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(suggSpeedLabel)
                    .addComponent(authorityLabel))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(suggestedSpeed, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(suggSpeedUnit))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maxAuthority, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(speedUnit)
                            .addComponent(currentSpeed))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(currentPower)
                                .addComponent(maxPower)
                                .addComponent(powerDivider))
                            .addComponent(powerUnit)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(speedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(powerLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(uiSeparatorOne, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(suggSpeedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(authorityLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(suggSpeedUnit, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(suggestedSpeed, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(maxAuthority)))
                .addGap(21, 21, 21))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorityLabel;
    private javax.swing.JLabel currentPower;
    private javax.swing.JLabel currentSpeed;
    private javax.swing.JLabel maxAuthority;
    private javax.swing.JLabel maxPower;
    private javax.swing.JLabel powerDivider;
    private javax.swing.JLabel powerLabel;
    private javax.swing.JLabel powerUnit;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JLabel speedUnit;
    private javax.swing.JLabel suggSpeedLabel;
    private javax.swing.JLabel suggSpeedUnit;
    private javax.swing.JLabel suggestedSpeed;
    private javax.swing.JSeparator uiSeparatorOne;
    // End of variables declaration//GEN-END:variables
}
