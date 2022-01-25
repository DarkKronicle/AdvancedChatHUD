package io.github.darkkronicle.advancedchathud;

import java.util.ArrayList;
import java.util.List;

public interface ResolutionEventHandler {

    List<ResolutionEventHandler> ON_RESOLUTION_CHANGE = new ArrayList<>();

    void onResolutionChange();

}
