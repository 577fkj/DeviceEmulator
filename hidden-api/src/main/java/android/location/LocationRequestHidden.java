package android.location;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(LocationRequest.class)
public class LocationRequestHidden {
    public String getProvider() {
        throw new RuntimeException("Stub!");
    }
}
