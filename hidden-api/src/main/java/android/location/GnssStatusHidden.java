package android.location;

import android.os.Build;

import androidx.annotation.RequiresApi;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(GnssStatus.class)
public class GnssStatusHidden {

    @RequiresApi(Build.VERSION_CODES.Q)
    public static GnssStatus wrap(int svCount, int[] svidWithFlags, float[] cn0DbHzs,
                                  float[] elevations, float[] azimuths, float[] carrierFrequencies,
                                  float[] basebandCn0DbHzs) {
        throw new RuntimeException("Stub!");
    }
}
