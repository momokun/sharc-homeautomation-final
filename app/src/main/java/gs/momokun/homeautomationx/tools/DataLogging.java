package gs.momokun.homeautomationx.tools;

public class DataLogging {

    private int _id;
    private long _date;
    private String _temp;
    private String _voltage;
    private String _amps;
    private String _watt;
    private String _energy;
    private String hr;

    DataLogging(){

    }

    public DataLogging(int id, String hr, long date, String temp, String voltage, String amps, String watt, String energy){
        this._id = id;
        this._date = date;
        this._temp = temp;
        this._voltage = voltage;
        this._amps = amps;
        this._watt = watt;
        this._energy = energy;
        this.hr = hr;
    }

    public DataLogging(long date, String temp, String voltage, String amps, String watt, String energy){
        this._date = date;
        this._temp = temp;
        this._voltage = voltage;
        this._amps = amps;
        this._watt = watt;
        this._energy = energy;
    }

    public DataLogging(String hr, long date, String temp, String voltage, String amps, String watt, String energy){
        this.hr = hr;
        this._date = date;
        this._temp = temp;
        this._voltage = voltage;
        this._amps = amps;
        this._watt = watt;
        this._energy = energy;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) {
        this.hr = hr;
    }

    public int get_id() {
        return _id;
    }

    void set_id(int _id) {
        this._id = _id;
    }

    public long get_date() {
        return _date;
    }

    void set_date(long _date) {
        this._date = _date;
    }

    public String get_temp() {
        return _temp;
    }

    void set_temp(String _temp) {
        this._temp = _temp;
    }

    public String get_energy() {
        return _energy;
    }

    void set_energy(String _energy) {
        this._energy = _energy;
    }

    public String get_watt() {
        return _watt;
    }

    void set_watt(String _watt) {
        this._watt = _watt;
    }

    public String get_amps() {
        return _amps;
    }

    void set_amps(String _amps) {
        this._amps = _amps;
    }

    public String get_voltage() {
        return _voltage;
    }

    void set_voltage(String _voltage) {
        this._voltage = _voltage;
    }
}