package gs.momokun.homeautomationx.tools;


public class DataStateLog {

    private int _id;
    private String _date;
    private String _type;

    DataStateLog(){

    }

    public DataStateLog(int id, String date, String type){
        this._id = id;
        this._date = date;
        this._type = type;
    }

    public DataStateLog(String date, String type){
        this._date = date;
        this._type = type;
    }

    public int get_id() {
        return _id;
    }

    void set_id(int _id) {
        this._id = _id;
    }

    public String get_date() {
        return _date;
    }

    void set_date(String _date) {
        this._date = _date;
    }

    public String get_type() {
        return _type;
    }

    void set_type(String _type) {
        this._type = _type;
    }
}
