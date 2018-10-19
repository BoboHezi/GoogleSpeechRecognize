package eli.google.recognize.model;

/**
 * Created by zhanbo.zhang on 2018/9/14.
 */

public class AccelerateData {

    public float AX;
    public float AY;
    public float AZ;
    public long timeStamp;

    public AccelerateData(float AX, float AY, float AZ) {
        this.AX = AX;
        this.AY = AY;
        this.AZ = AZ;
    }
}
