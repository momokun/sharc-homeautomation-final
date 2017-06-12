package gs.momokun.homeautomationx.tools;


public class ArduinoStateOnReceived {

    private int getState;

    ArduinoStateOnReceived(int state){
        this.getState = state;
    }

    public int getStateArduino(){
        return getState;
    }



}