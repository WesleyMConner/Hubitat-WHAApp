//---- CORE METHODS (External)

//---- CORE METHODS (Internal)

void AllAuto ()
void _updateLutronKpadLeds (String currMode)
void _buttonOnCallback (String button)
void _removeAllChildApps ()

//---- EVENT HANDLERS

void seeTouchSpecialFnButtonHandler (Event e)
void seeTouchModeButtonHandler (Event e)

//---- SYSTEM CALLBACKS

void installed ()
void uninstalled ()
void updated ()
void initialize ()

//---- GUI / PAGE RENDERING

void _authSpecialFnMainRepeaterAccess ()
void _authSeeTouchKpadAccess ()
void _identifySpecialFnButtons ()
void _populateStateKpadButtonDniToSpecialFnButtons ()
void _wireSpecialFnButtons ()
void _identifyKpadModeButtons ()
void _populateStateKpadButtonDniToTargetMode ()
void _wireModeButtons ()
void _identifyParticipatingRooms ()
void _displayInstantiatedRoomHrefs ()
void _createModePbsgAndPageLink ()
Map whaPage ()