package com.example.lonejourneyman.buoynow;

import android.app.Application;


/**
 * Created by lonejourneyman on 8/26/17.
 */

public class BuoyNowApplication extends Application {

    Boolean initialRun = true;
    public Boolean getInitialRun() {
        return initialRun;
    }
    public void setInitialRun(Boolean initialRun) {
        this.initialRun = initialRun;
    }

}
