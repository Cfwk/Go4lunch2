
package cfwz.skiti.go4lunch.model.googleplaces;


import com.google.gson.annotations.SerializedName;


public class Period {
    @SerializedName("close")
    private Close mClose;
    @SerializedName("open")
    private Open mOpen;

    public Close getClose() {
        return mClose;
    }
    public void setClose(Close close) {
        mClose = close;
    }
    public Open getOpen() {
        return mOpen;
    }
    public void setOpen(Open open) {
        mOpen = open;
    }
}
