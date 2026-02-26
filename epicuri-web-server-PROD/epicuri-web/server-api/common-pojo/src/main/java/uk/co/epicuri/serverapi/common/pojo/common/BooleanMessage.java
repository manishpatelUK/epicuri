package uk.co.epicuri.serverapi.common.pojo.common;

public class BooleanMessage {
    private boolean flag = false;

    public BooleanMessage(){}

    public BooleanMessage(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
