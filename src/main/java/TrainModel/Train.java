package TrainModel;
import TrackModel.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.Random;

//doxygen comment format below!
/**
 * a normal member taking two arguments and returning an integer value.
 * @param a an integer argument.
 * @param s a constant character pointer.
 * @see Javadoc_Test()
 * @see ~Javadoc_Test()
 * @see testMeToo()
 * @see publicVar()
 * @return The test results
 */
public class Train implements Serializable {

	//variables for train values.
	GPS currAuthority;
	Random rand;
	Block currBlock;
	Double mass, length, velocity, oldVelocity, power, currGrade;
	Double netForce;
	Double currTemp, currThermostat, distance, setPointSpeed;
	Double acceleration;
	Double Kp, Ki;
	Double safeDistSB, safeDistEB;
	Double startTime;
	Double frictionC = 0.42; 			//coefficient of friction of steel wheels on lubricated steel rails
	Double SBrate = -1.2, EBrate = -2.73;
	Double g = 9.8;
	//Double Fx, Fy, Ax, Ay, Vx, Vy, oldVx, oldVy;				//gravity constant in m/s^2
	boolean engineFailure, signalFailure, brakeFailure;
	Integer trainID;
	int numPassengers, numCars, numCrew;
	int statusAC, statusHeater, statusLeftDoor, statusRightDoor, statusEB, statusSB, statusLights;
	String messageBoard;
	GPS trainLocation;
	TrackModel globalTrack;
    Block prevBlock;
	Antenna trainAntenna;


	//train data
	Double maxPower = 120000.00; 		//maximum power is 120 kW
	Double maxVelocity = 19.4444; 		//maximum velocity is 70 kph or 19.444 m/s
	Double maxGrade = Math.toDegrees(Math.atan2(-5, 100));;		//maximum grade on the track is going to be -5
	Double Height = 3.42;				//height of train in m
	Double Width = 2.65;				//width of train in m
	int maxPassengers = 222; 		//max number of passengers that can fit on train
	Double weightPass = 75.0; 				//mass of single passenger in kg
	Double weightCar =40.9 * 907.185;  	//mass of empty car in kg;
	Double maxWeight = weightCar + (maxPassengers * weightPass) + weightPass; //maximum weight of a car
	Double lengthCar = 8.69;			//length of one car in feet




        /**
     * Default Constructor to create a new Train object based on Assigned ID
     */
	public Train(){
		trainAntenna = new Antenna();
	}

	/**
     * Constructor to create a new Train object based on Assigned ID
     * @param a an integer argument to assign to Trains new ID.
     */
	public Train(Integer ID, TrackModel gTrack){
		mass = weightCar;  		//mass of empty car in kg
		mass = mass + weightPass;
		trainAntenna = new Antenna();
		numCrew = 1;
		velocity = 0.0;
		rand = new Random();
		trainAntenna.setCurrVelocity(velocity * 2.236);
		oldVelocity =0.0;
		length = lengthCar;
		trainID = ID;
		power = 0.0;
		currGrade = 0.0;
		globalTrack = gTrack;
		trainLocation = new GPS();
		currAuthority = new GPS();
		statusEB =0;
		statusSB =0;
		currTemp = 60.0;
		currThermostat = 60.0;
		numPassengers =0;
		numCars =0;
		statusAC = 0;
		statusHeater =0;
		statusEB =0;
		statusSB = 0;
		statusLeftDoor = 0;
		statusRightDoor =0;
		statusLights =0 ;
		engineFailure = false;
		brakeFailure = false;
		signalFailure = false;

	}





	/**
     * Modifier to apply a new power command to the current train and adjust velocity accordingly
     * @param a Double argument to assign as the new applied power command.
     * @see changeSpeed()
     */
	public void powerCommand(Double newPower){

		Double forceApp;
		if (velocity == 0)
		{
			//to avoid division by zero
			forceApp = newPower;
		}else if (statusEB == 1 || statusSB == 1){
			//if either brake is engaged
			Double brakeDeceleration = 0.0;
			if (statusSB == 1)
			{
				brakeDeceleration = SBrate;
			}else if(statusEB == 1)
			{
				brakeDeceleration = EBrate;
			}

			forceApp = mass * brakeDeceleration;
		}else{
			forceApp = newPower / velocity;
		}

		power = newPower;
		if(newPower >= maxPower){
			//if power command is greater than or equal to max power do nothing
			power = maxPower;
			// System.out.println("Hey you!");
			changeSpeed(forceApp);
		}else if (newPower < 0.0){
			//invalid power command so nothing will happen
		}else{
			//if power command calls for increase of speed
                         //System.out.println("Hey you!");
			changeSpeed(forceApp);
		}

	}




	/**
     * Modifier to change current speed based on force applied to system
     * @param a Double argument to assign as the new applied force.
     * @see myCos()
     * @see mySin()
     * @see magnitude()
     */
	private void changeSpeed(Double Fapp) {
		oldVelocity = velocity;
		//compute force lost due to friction
		Double Fs = mass * g * mySin(currGrade) * frictionC;
		//compute net force based on applied force and friction
		netForce = Fapp - Fs;
		//compute acceleration based on net force and current mass
		acceleration = netForce / mass;
		//max acceleration is 0.5
		if (acceleration > 0.5){
			acceleration = 0.5;
		}

		//compute new velocity based on old velocity and acceleration
		velocity = velocity + acceleration;
		if (velocity > 19.4444)              //70 kph in m/s (max velocity)
		{
			velocity = 19.4444;
		}
		if (velocity < 0)
		{
			velocity = 0.0;
		}

		//using S = Vi(t) + (1/2)(a)(t^2)  to compute distance
		distance = (oldVelocity) + (0.5)*acceleration;

		updateCurrBlock(distance);
		updateSpeedAndAuthority();
		updateSafeBrakingDist();
		trainAntenna.setCurrVelocity(velocity * 2.236);
	}

/**
     * Method to update safeBraking Distance of train
     */
	private void updateSafeBrakingDist(){
		safeDistSB = getSafeBrakingDistSB();
		safeDistEB = getSafeBrakingDistEB();
	}



	/**
     * Method to determine what block the train is in now
     * @param a Double which corresponds to the distance travelled by the train
     */
	private void updateCurrBlock(Double distTravelled){
		Double distBlock = trainLocation.getDistIntoBlock();
		Double dist = distBlock + distTravelled;
		currBlock.setOccupied(true);
		//check if distance exceeds length of block (if so enter new block) if not update location
		while (dist > trainLocation.getCurrBlock().getLen())
		{
                        //System.out.println("Next block forward" + currBlock.nextBlockForward().blockNum());
			dist = dist - getCurrBlock().getLen();
			currBlock.setOccupied(false);

                        Block blockForward = currBlock.nextBlockForward();
                        Block blockBackward = currBlock.nextBlockBackward();


                        //System.out.println("forward block: " + blockForward.blockNum());
                      //  System.out.println("backward block: " + blockBackward.blockNum());
                        if(blockForward != null && blockBackward != null){
                            //theres both a forward and backward. go to the one that wasnt last visited
                            if (blockBackward.compareTo(prevBlock) == 0)
                            {
                                //we were just in blockBackward so go forward
                                prevBlock = currBlock;
                                currBlock = currBlock.nextBlockForward();
                            }else{
                                //go backwards
                                prevBlock = currBlock;
                                currBlock = currBlock.nextBlockBackward();
                            }
                        }else if (blockForward == null)
                        {
                            //go backwards
                            if (blockBackward != null )
                            {
                                prevBlock = currBlock;
                                currBlock = currBlock.nextBlockBackward();
                            }
                        }else if(blockBackward == null)
                        {
                            //go forward
                            if (blockForward != null )
                            {
                                prevBlock = currBlock;
                                currBlock = currBlock.nextBlockForward();
                            }

                        }

                        //System.out.println("Going to next block: " + currBlock.blockNum());
                        //System.out.println("Block length: " + currBlock.getLen());
			currBlock.setOccupied(true);
		}
		trainLocation.setCurrBlock(currBlock);
		if (this.getCurrBlock().getGrade() != null){
			currGrade = getCurrBlock().getGrade();
		}
        //System.out.println("Dist: " + dist);
		trainLocation.setDistIntoBlock(dist);
		trainAntenna.setGPS(trainLocation);

	}

	/**
     * Method to update speed and authority (and other block properties)
     */
	private void updateSpeedAndAuthority(){

			//check antenna for MBO speed and Authority, if not there check block for fixed block speed and authority
			GPS authMBO = trainAntenna.getCurrAuthority();
			if (authMBO != null){
				Double speedMBO = trainAntenna.getSuggestedSpeed();
				if(speedMBO != null){
					setPointSpeed = speedMBO;
				}
				currAuthority = authMBO;
			}else if (currBlock != null){
                if (currBlock.getSuggestedSpeed() != null){

                    setPointSpeed = currBlock.getSuggestedSpeed();
					currBlock.setSuggestedSpeed(null);
                }

                if (currBlock.getAuthority() != null){

                    currAuthority.setCurrBlock(getCurrBlock().getAuthority());
					currAuthority.setDistIntoBlock(null);
					currBlock.setAuthority(null);
				}
            }

        }


	/**
	 * Method to calculate maximum possible safe Braking Distance of train based on its current velocity and mass using service brake
	 * @return a Double which corresponds to the maximum amount of distance required to stop the train using the service brake
	 */
	public Double getMaxSafeBrakingDistSB(){
		Train dummyTrain = new Train();
		dummyTrain.setMaxConditions();
		Double maxSBD = dummyTrain.getSafeBrakingDistSB();
		return maxSBD;
	}

		/**
	 * Method to calculate maximum possible safe Braking Distance of train based on its current velocity and mass using emergency brake
	 * @return a Double which corresponds to the maximum amount of distance required to stop the train using the service brake
	 */
	public Double getMaxSafeBrakingDistEB(){
		Train dummyTrain = new Train();
		dummyTrain.setMaxConditions();
		Double maxEBD = dummyTrain.getSafeBrakingDistEB();
		return maxEBD;
	}

		/**
	 * Method to set test train to maximum conditions. this will be used for the maximum safe braking distance method
	 */
	public void setMaxConditions(){
		velocity = maxVelocity;
		mass = 2 * maxWeight;
		currGrade = maxGrade;
	}

		/**
	 * Method to set velocity for test train
	 */
	public void setVelocity(Double newV){

		velocity = newV * 0.447;				//convert MPH to m/s
		trainAntenna.setCurrVelocity(velocity * 2.236);
	}



	/**
     * Method to calculate safe Braking Distance of train based on its current velocity and mass
     * @return a Double which corresponds to the amount of distance required to stop the train using the service brake
     */
	public Double getSafeBrakingDistSB(){
		Double decRate = deccelRate(SBrate);
		Double timeSB = timeToStop(decRate);
		Double SBD = distanceToStop(decRate,timeSB);
		trainAntenna.setSafeBrakingDistSB(SBD);
		return SBD;
	}

	/**
     * Method to calculate safe emergency Braking Distance of train based on its current velocity and mass
     * @return a Double which corresponds to the amount of distance required to stop the train using the emergency brake
     */
	public Double getSafeBrakingDistEB(){
		Double decRate = deccelRate(EBrate);
		Double timeEB = timeToStop(decRate);
		Double SEBD = distanceToStop(decRate,timeEB);
		trainAntenna.setSafeBrakingDistEB(SEBD);
		return SEBD;
	}

/**
     * Method to calculate decceration rate based on brake rates and mass of train
     * @param a Double which corresponds to the deceleration rate of the brakes
     * @return a Double which corresponds to the deceleration rate based on brakes and mass
     */
	public Double deccelRate(Double Drate){

		Double forceApplied = mass * Drate;
		Double Fs = mass * g * mySin(currGrade) * frictionC;
		Double netF = forceApplied - Fs;
		//compute acceleration based on net force and current mass
		Double decceleration = netF / mass;
		return decceleration; 				//deceleration rate based on brakes and mass
	}


	/**
     * Method to calculate time to stop based on brake rate, mass and velocity
     * @param a Double which corresponds to the deceleration rate of the brakes
     * @return a Double which corresponds to the amount of time required to stop the train using the brakes
     */
	private Double timeToStop(Double Drate){
		Double time = 0.0;
		Double tempVelocity = velocity;

                //System.out.println(tempVelocity);
		while (tempVelocity > 0.0)
		{
			tempVelocity = tempVelocity + Drate;
			time++;
		}

		return time; 				//time required to stop the train in seconds
	}

	/**
     * Method to calculate distance to stop based on brake rate, mass and velocity
     * @param a Double which corresponds to the deceleration rate of the brakes
     * @param a Double which corresponds to the time that it will take the train to stop
     * @return a Double which corresponds to the amount of distance required to stop the train using the brakes
     */
	private Double distanceToStop(Double Drate, Double stopTime){
		//using S = Vi(t) + (1/2)(a)(t^2)  to compute distance
		Double stopDist = (velocity)*(stopTime) + (1/2)*(Drate)*(Math.pow(stopTime, 2));
		return stopDist; 				//time required to stop the train in seconds
	}


	/**
     * Mutator to set the current Block that the train is on
     * @param Block object to set curr block to
     */
	public void setCurrBlock(Block newBlock){

            this.trainLocation.setCurrBlock(newBlock);
            this.trainLocation.setDistIntoBlock(0.0);
			trainAntenna.setGPS(trainLocation);
            this.currBlock = newBlock;
            this.prevBlock = currBlock;
			currBlock.setOccupied(true);


	}

	/**
     * Accessor to get the current Block that the train is on
     * @return Block object to return curr block
     */
	public Block getCurrBlock(){
		return this.currBlock;
	}

	/**
     * Accessor to get the train Antenna of the train
     * @return Antenna object onboard the train
     */
	public Antenna getAntenna(){
		return trainAntenna;
	}



	/**
     * Method to update temperature based on current temp and thermostat setting. This method will be called periodically at each cycle of the system
     */
	public void updateTemp(){
		//Using Newtons Law of cooling
		//T(t) = Ta + (To - Ta)e^(-kt)
		//where T(t) is new temperature
		//Ta is current Temperature of train
		//and To is temperature of thermostat
		Double k = 0.054 * 60; 		//k is 0.054 per minute (convert to seconds)
		if (statusAC == 1 || statusHeater == 1){
			currTemp = currTemp + (currThermostat - currTemp)* Math.exp((-1*k)*1);
		}

	}





	/* functions to integrate with track. adding and removing people

	 * Public Integer loadPassengers (Integer maxPassengers)
		return random numeber

		public void addDepartingPassengers(Integer numPassengers)
	 */
	  /**
     * Method to update number of passengers on board train. this will be called once train stops at a station
     */
	 public void updatePassengerCount() {
		 //get station that the train is stopped at
		 Station currStation = currBlock.getAssociatedStation();
		 //allow people to get off at station
		 //random amount will leave based on current amount on board train
		 Integer numUnboarding = passengersUnboarding();
		 changePassengers(-1*numUnboarding);
		 currStation.addDepartingPassengers(numUnboarding);

		 //allow people to get on train
		 Integer spaceLeft = maxPassengers - numPassengers;
		 Integer numBoarding = currStation.loadPassengers(spaceLeft);
		 changePassengers(numBoarding);
	 }

	 /**
     * Method to update temperature based on current temp and thermostat setting. This method will be called periodically at each cycle of the system
     */
	public int passengersUnboarding(){
		//random number selected between number of people on train.
		if (numPassengers != 0){
			return rand.nextInt(numPassengers);
		}
		return 0;
		
	}




	//CODE BELOW THIS LINE IS DONE. DO NOT TOUCH.
	//*************************************************************************************************************************************
	//*************************************************************************************************************************************
	//*************************************************************************************************************************************
	//*************************************************************************************************************************************
	//*************************************************************************************************************************************


    /**
     * Accessor to return current train's ID

     * @return an Integer which corresponds to the current train's ID.
     */
	public Integer getID(){

		return trainID;
	}

	/**
     * Accessor to return current train's setpoint speed
     * @return an Double object which corresponds to train's current suggested speed.
     */
	public Double getSuggestedSpeed(){
		return setPointSpeed;
	}

	/**
     * Accessor to return current train's Authority
     * @return an GPS object which corresponds to train's current authority.
     */
	public GPS getAuthority(){
		return currAuthority;
	}

	/**
     * Accessor to return current train's Velocity
     * @return an Double object which corresponds to train's current velocity. This value will be converted from m/s to MPH prior to returning.
     */
	public Double getVelocity(){
		return (velocity * 2.236);			//convert velocity to MPH from m/s
	}

	/**
     * Accessor to return current train's mass
     * @return an Double object which corresponds to train's current mass. This value will be converted from kg to lbs prior to returning.
     */
	public Double getMass(){
		return ((mass) * 2.204);				//convert mass from Kg to lbs before display
	}

	/**
     * Accessor to return current train's location
     * @return an GPS object which corresponds to train's current location
     */
	public GPS getGPS(){
		return trainLocation;
	}

    /**
     * Accessor to get the current thermostat setting onboard the train
     * @return a Double which denotes the thermostat setting on board the train in Fahrenheit
     */
	public Double getThermostat(){
		return currThermostat;
	}

	/**
     * Accessor to get engine failure status
     * @return a boolean which denotes whether or not there is a failure in the engines. False means no failure and true means failure.
     */
	public boolean isEngineFailure(){
		return engineFailure;
	}

	/**
     * Accessor to get signal failure status
     * @return a boolean which denotes whether or not there is a failure in the signaling system. False means no failure and true means failure.
     */
	public boolean isSignalFailure(){
		return signalFailure;
	}

	/**
     * Accessor to get brake failure status
     * @return a boolean which denotes whether or not there is a failure in the service brake. False means no failure and true means failure.
     */
	public boolean isBrakeFailure(){
		return brakeFailure;
	}

        /**
         * Puts a message on the current block signaling that the train needs to be repaired.
         *
         * @param needsMaint indicates if the train needs maintenance. 
         */
        public void requestFix(boolean needsMaint){

            // put on the current block we are broken..
            this.getCurrBlock().setBroken(true);
        }

	/**
     * Accessor to get the current temperature onboard the train
     * @return a Double which denotes the temperature on board the train in Fahrenheit
     */
	public Double getTemp(){
		return currTemp;
	}

	/**
     *Accessor to get the status of the service brake of the train
     * @return an int which corresponds to the service brake's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
	public int getServiceBrake(){
		return statusSB; 		//1 = on, 0 = off, -1 = failure
	}

	/**
     * Accessor to get the status of the Emergency brake of the train
     * @return an int which corresponds to the Emergency brake's status. 1 means on,and 0 means off
     */
	public int getEmergencyBrake(){
		return statusEB; 		//1 = on, 0 = off
	}

	/**
     * Accessor to get the max power the train can go.
     * @return a Double corresponding to the max power the train can go.
     */
    public Double getMaxPower(){
        return this.maxPower;
    }

    /**
     * Accessor to return current train's applied power
     * @return an Double object which corresponds to train's current power command.
     */
	public Double getPower() {
		return power;
	}

	/**
     * Accessor to get current train's Kp
     * @return an Double object which corresponds to the new Kp.
     */
	public Double getKp(){
		return Kp;
	}

	/**
     * Accessor to get current train's Ki
     * @return an Double object which corresponds to the new Ki.
     */
	public Double getKi(){
		return Ki;
	}

	/**
     * accessor to get the status of the right doors.
     * @return int which corresponds to the right door's status. 1 means open, 0 means closed, and -1 denotes a failure.
     */
    public int getRightDoor(){
        return this.statusRightDoor;
    }

    /**
     * Accessor to get the status of the left door.
     * @return int which corresponds to the left door's status. 1 means open, 0 means closed, and -1 denotes a failure.
     */
    public int getLeftDoor(){
        return this.statusLeftDoor;
    }

	/**
     * Accessor to get the status of the interior lights onboard the train
     * @return an int which corresponds to the light's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
	public int getLights(){
		return statusLights; 		//1 = on, 0 = off, -1 = failure
	}

	/**
     * Accessor to get the status of the AC onboard the train
     * @return an int which corresponds to the AC's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
    public int getAC(){
        return this.statusAC;
    }


    /**
     * Accessor to get the status of the Heat onboard the train
     * @return an int which corresponds to the Heat's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
    public int getHeat(){
        return this.statusHeater;
    }

	/**
     * Mutator to set current train's Authority
     * @param an Double object which corresponds to the new authority.
     */
	public void setAuthority(GPS goToBlock){
		currAuthority = goToBlock;
	}

	/**
     * Mutator to set current train's Kp and Ki
     * @param an Double object which corresponds to the new Kp.
     * @param an Double object which corresponds to the new Ki.
     */
	public void setKpAndKi(Double newKp, Double newKi){
		Kp = newKp;
		Ki = newKi;
	}

	/**
     * Modifier to change the current grade the train is residing at
     * @param an Double object which corresponds to train's current grade
     */
	public void setGrade(Double grade) {
		currGrade = Math.toDegrees(Math.atan2(grade, 100));
	}

	/**
     * Modifier to change the current mass of the train
     * @param an Double object which corresponds to the change in mass to apply. To decrease mass a negative number should be passed to this method.
     */
	public void changeMass(Double mass2) {
		mass = mass + mass2;
	}

	/**
     * Modifier to change the status of the right doors
     * @param an int which corresponds to the right door's status. 1 means open, 0 means closed, and -1 denotes a failure.
     */
	public void setRightDoor(int status){
		statusRightDoor = status; 		//1 = open, 0 = closed, -1 = failure
	}

	/**
     * Modifier to change the status of the left doors
     * @param an int which corresponds to the left door's status. 1 means open, 0 means closed, and -1 denotes a failure.
     */
	public void setLeftDoor(int status){
		statusLeftDoor = status; 		//1 = open, 0 = closed, -1 = failure
	}

	/**
     * Modifier to change the status of the interior lights onboard the train
     * @param an int which corresponds to the light's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
	public void setLights(int status){
		statusLights = status; 		//1 = on, 0 = off, -1 = failure
	}

	/**
     * Modifier to change the status of the AC onboard the train
     * @param an int which corresponds to the AC's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
	public void setAC(int status){
		statusAC = status; 		//1 = on, 0 = off, -1 = failure
	}

	/**
     * Modifier to change the status of the heater onboard the train
     * @param an int which corresponds to the heater's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
	public void setHeat(int status){
		statusHeater = status; 		//1 = on, 0 = off, -1 = failure
	}

	/**
     * Mutator to set the current thermostat setting onboard the train
     * @param a Double argument which denotes the thermostat setting on board the train in Fahrenheit
     */
	public void setThermostat(Double newThermostat){
		currThermostat = newThermostat;
		this.updateTemp();
	}

	/**
     * Mutator to set engine failure status
     * @param a boolean argument is passed to denote whether or not there is a failure in the engines. False means no failure and true means failure.
     */
	public void setEngineFailure(boolean engineFail){
		engineFailure = engineFail;
	}

	/**
     * Mutator to set Signal failure status
     * @param a boolean argument is passed to denote whether or not there is a failure in the signaling system. False means no failure and true means failure.
     */
	public void setSignalFailure(boolean signalFail){
		signalFailure = signalFail;
	}

	/**
     * Mutator to set brake failure status
     * @param a boolean argument is passed to denote whether or not there is a failure in the service brake. False means no failure and true means failure.
     */
	public void setBrakeFailure(boolean brakeFail){
		brakeFailure = brakeFail;
		if (brakeFail)
		{
			statusSB = -1;
		}else {
			statusSB = 0;
		}
	}

	/**
     * Modifier to change the status of the service brake of the train
     * @param an int which corresponds to the service brake's status. 1 means on, 0 means off, and -1 denotes a failure.
     */
	public void setServiceBrake(int status){
		statusSB = status; 		//1 = on, 0 = off, -1 = failure
		if (status == 1)
		{
			this.powerCommand(0.0);
		}

	}

        public HashMap<Block, Beacon> getBeacons(){

            return this.globalTrack.viewBeaconMap();
        }

	/**
     * Modifier to change the status of the Emergency brake of the train
     * @param an int which corresponds to the Emergency brake's status. 1 means on,and 0 means off
     */
	public void setEmergencyBrake(int status){
		statusEB = status; 		//1 = on, 0 = off
		if (status == 1)
		{
			this.powerCommand(0.0);
		}
	}

	/**
     * Modifier to change the amount of passengers onboard the train
     * @param an int which corresponds to the number of passengers to add or remove. To remove a negative number should be sent to the method
     * @see changeMass()
     */
	public void changePassengers(int pass)
	{
		numPassengers = numPassengers + pass;
		Double massPass = pass * weightPass;
		changeMass(massPass);
	}

	/**
     * Accessor to see how many passengers are on the Train
	 * @return a int corresponding to number of passengers on board the train
     */
	public int getNumPassengers()
	{
		return numPassengers;
	}

	/**
     * Modifier to change the amount of cars connected to the train
     * @param an int which corresponds to the number of cars to add or remove. To remove a negative number should be sent to the method
     * @see changeMass()
     * @see changeLength()
     */
	public void changeCar(int car)
	{
		numCars = numCars + car;
		Double massCar = car * weightCar;
		changeMass(massCar);
		Double carLength = car * lengthCar;
		changeLength(carLength);
	}

/**
     * Accessor to get number of cars on train
	 * @return a int corresponding to number of cars
     */
	public int getNumCars()
	{
		return numCars;
	}

	/**
     * Modifier to change the current length of the train
     * @param an Double object which corresponds to the change in length to apply. To decrease length a negative number should be passed to this method.
     */
	public void changeLength(Double length2) {
		length = length + length2;
	}

	/**
     * Accessor to get length of train
	 * @return a Double corresponding to length of train
     */
	public Double getLength()
	{
		return length;
	}

	/**
     * Mutator to set current train's setpoint speed
     * @param an Double object which corresponds to train's current suggested speed.
     */
	public void setSpeed(Double speed) {
		setPointSpeed = speed;
	}


	/**
     * Checks to see if both the Kp and Ki are set.
     * @return returns true if they are both set, and false otherwise.
     */
    public boolean powerConstantsSet(){

        if (this.Kp != null && this.Ki != null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Method to calculate magnitude of vector based on X and Y components
     * @param a double argument to be used as x component of vector.
     * @param a double argument to be used as y component of vector.
     * @return a Double which corresponds to the computed magnitude of the desired vector
     */
	private Double magnitude(Double x, Double y) {
		Double sum = x*x + y*y;
		return Math.sqrt(sum);
	}

	/**
     * Method to calculate Cosine of an angle given in terms of degrees
     * @param a double argument to be used as the degree of the angle being computed
     * @return a Double which corresponds to the computed cosine value of the desired angle
     */
	private Double myCos (Double deg){
		return Math.cos(Math.toRadians(deg));
	}

	/**
     * Method to calculate sine of an angle given in terms of degrees
     * @param a double argument to be used as the degree of the angle being computed
     * @return a Double which corresponds to the computed sine value of the desired angle
     */
	private Double mySin (Double deg){
		return Math.sin(Math.toRadians(deg));
	}

}
