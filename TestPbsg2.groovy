// ---------------------------------------------------------------------------------
// P B S G 2
//
// Copyright (C) 2023-Present Wesley M. Conner
//
// LICENSE
// Licensed under the Apache License, Version 2.0 (aka Apache-2.0, the
// "License"), see http://www.apache.org/licenses/LICENSE-2.0. You may
// not use this file except in compliance with the License. Unless
// required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
// implied.
// ---------------------------------------------------------------------------------

import com.hubitat.hub.domain.Event as Event

// The Groovy Linter generates NglParseError on Hubitat #include !!!
#include wesmc.lHExt
#include wesmc.lHUI

definition (
  name: 'TestPbsg2',
  namespace: 'wesmc',
  author: 'Wesley M. Conner',
  description: 'Preview Pbsg functionality',
  singleInstance: true,
  iconUrl: '',
  iconX2Url: ''
)

preferences {
  page(name: 'TestPbsgPage')
}

// The psuedo-class "pbsgConfig" solicits the button data that drives
// PBSG creation.

String pbsgConfig_SolicitName(String nameKey) {
  input(
    name: nameKey, title: '<b>PBSG NAME</b>', width: 2,
    type: 'text', submitOnChange: true, required: false, multiple: false
  )
  return settings."${nameKey}"
}

ArrayList pbsgConfig_SolicitButtons(String buttonsKey) {
  input(
    name: buttonsKey, title: '<b>PBSG BUTTONS</b> (space delimited)', width: 6,
    type: 'text', submitOnChange: true, required: false, multiple: false
  )
  return settings."${buttonsKey}"?.tokenize(' ')
}

String pbsgConfig_SolicitDefault(String defaultButtonKey, ArrayList buttons) {
  input(
    name: defaultButtonKey, title: '<b>DEFAULT BUTTON</b>', width: 2,
    type: 'enum', submitOnChange: true, required: false, multiple: false,
    options: buttons
  )
  return settings."${defaultButtonKey}"
}

String pbsgConfig_SolicitActive(String activeButtonKey, ArrayList buttons) {
  input(
    name: activeButtonKey, title: '<b>ACTIVE BUTTON</b>', width: 2,
    type: 'enum', submitOnChange: true, required: false, multiple: false,
    options: buttons
  )
  return settings."${activeButtonKey}"
}

Map pbsgConfig_SolicitInstance(Integer settingsKeySuffix) {
  // Solicit logical button data used to create a Pbsg instance.
  Map pbsgConfig = [:]
  String nameKey = "pbsgName^${settingsKeySuffix}"
  String name = pbsgConfig_SolicitName(nameKey)
  if (name) {
    String buttonsKey = "pbsgButtons^${settingsKeySuffix}"
    String defaultKey = "pbsgDefault^${settingsKeySuffix}"
    String activeKey = "pbsgActive^${settingsKeySuffix}"
    ArrayList buttons = pbsgConfig_SolicitButtons(buttonsKey)
    String defaultButton = pbsgConfig_SolicitDefault(defaultKey, buttons)
    String activeButton = pbsgConfig_SolicitActive(activeKey, buttons)
    pbsgConfig = [
      'name': name,
      'buttons': buttons,
      'defaultButton': defaultButton,
      'activeButton': activeButton
    ]
  } else {
     paragraph('', width: 10)  // Filler for a 12 cell row
  }
  return pbsgConfig
}

// The psuedo-class "pbsg" creates and manages the VSWs that realize a
// PushButton Switch Group (PBSG).

DevW pbsg_GetDevice(ArrayList deviceFifo, String dni) {
  DevW result = null
  deviceFifo.eachWithIndex{ d, i ->
    if (d.deviceNetworkId == dni) { result = deviceFifo.getAt(i) }
  }
  return result
}

DevW pbsg_GetAndRemoveDevice(ArrayList deviceFifo, String dni) {
  DevW foundIndex = null
  deviceFifo.eachWithIndex{ d, i ->
    if (d.deviceNetworkId == dni) { foundIndex = i }
  }
  return (foundIndex != null) ? deviceFifo.removeAt(foundIndex) : null
}

void pbsg_RemoveDevice(ArrayList deviceFifo, DevW device) {
  Integer foundIndex = null
  deviceFifo.eachWithIndex{ d, i ->
    if (d == device) { foundIndex = i }
  }
  if (foundIndex != null) { deviceFifo.removeAt(foundIndex) }
}

ArrayList pbsg_PopulateFifo(Map config) {
  ArrayList deviceFifo = []
  config.buttons.each{ button ->
    String vswDNI = "${config.name}_${button}"
    DevW device = getChildDevice(vswDNI) ?: {
      addChildDevice(
        'hubitat',          // namespace
        'Virtual Switch',   // typeName
        vswDNI,             // device's unique DNI
        [isComponent: true, name: vswDNI]
      )
    }
    deviceFifo << device
  }
  return deviceFifo
}

Map pbsg_CreateInstance(Map config) {
  // Use provided pbsgConfig to create VSWs and initialize FIFO et al.
  Map pbsg = [:]
  pbsg.deviceFifo = pbsg_PopulateFifo(config)
  if (config.defaultButton) {
    String defaultDNI = "${config.name}_${config.defaultButton}"
    pbsg.defaultDevice = pbsg_GetDevice(pbsg.deviceFifo, defaultDNI)
  } else {
    pbsg.defaultDevice = null
  }
  if (config.activeButton) {
    String activeDNI = "${config.name}_${config.activeButton}"
    pbsg.activeDevice = pbsg_GetAndRemoveDevice(pbsg.deviceFifo, activeDNI)
  } else if (pbsg.defaultDevice) {
    pbsg.activeDevice = pbsg.defaultDevice
    pbsg_RemoveDevice(pbsg.deviceFifo, pbsg.activeDevice)
  } else {
    pbsg.activeDevice = null
  }
  logInfo('pbsg_CreateInstance', "pbsg: ${pbsg}")
  return pbsg
}

void pbsg_DniTurnedOn () {
  // Process a 'VSW turned on' event
}

void pbsg_DniTurnedOff () {
  // Process a 'VSW turned off' event
}

void pbsg_TurnOnDni () {
  // Caused a DNI to be turned on (typically due to a RA2 scene LED
  // activation or a PRO2 scene button press)
}

void pbsg_TurnOffDni () {
  // Caused a DNI to be turned off (typically due to a RA2 scene LED
  // deactivation. When changing scenes, RA2 typically communicates one
  // scene turning off before another is turned on; so, it may be
  // necessary to defer action since a competing turn on event may
  // cause the dni to be off already.
}

void pbsg_ToggleOnDni () {
  // Caused a DNI to be toggled by leveraging the TurnOn/IffDni methods
  // above.
}

void pbsg_ActivatePrior () {
  // Swap the currently active DNI with the DNI at the front of the FIFO.
}

ArrayList pbsg_SummarizeState () {
  // Provide a visual summary of FIFO and underlying DNI state.
  //   Active: <active> Fifo: [...]
  // Note any discrepancies between the PBSG and underlying VSWs.
}

ArrayList pbsg_ReconcileFifoToVsws () {
  // Find issues and (optionally) "force" corrections.
}

// The psuedo-class "pbsgMgr" facilitates managing multiple PBSGs in one
// application.

void pbsgMgr_StorePbsg (Map pbsg) {
  // Add a PBSG to a map of PBSGs saved to application state.
}

Map pbsgMgr_RetrievePbsg () {
  // Retrieve a PBSG from a map of PBSGs saved to application state.
}


/*

// MAP OF PBSGS


void logPbsgs(Map m) {
  ArrayList lines = ['']
  m.each{ pbsgName, map ->
    lines << "<b>${pbsgName}</b>"
    lines << "    map.dnis: ${map.dnis}"
  }
  logInfo('logPbsgs', lines)
}

void createStatePbsgs(Map pbsgs) {
  logPbsgs(pbsgs)
  pbsgs.each{ name, values ->
    if (values.defaultButton) {
      pbsg."${name}".defaultDni = "${name}_${values.defaultButton}"
    }
    if (values.activeButton) {
      pbsg."${name}".activeDni = "${name}_${values.activeButton}"
    }
    ArrayList dnis = []
    ArrayList devices = []
    values.buttons.each{ button ->
      String vswDNI = "${name}_${button}"
      DevW device = getChildDevice(vswDNI)
      if (!device) {
        logWarn('createStatePbsgs', "Adding child device '${vswDNI}'")
        device = addChildDevice(
          'hubitat',          // namespace
          'Virtual Switch',   // typeName
          vswDNI,          // device's unique DNI
          [isComponent: true, name: vswDNI]
        )
      }
      dnis << vswDNI
      devices << device
    }
    pbsg."${name}".dnis = dnis
    pbsg."${name}".devices = devices
  }
  logInfo('createStatePbsgs', "pbsg: >${pbsg}<")
  //logPbsgs(pbsgs)
  state.pbsgs = pbsgs
}

Map pbsgs() {
  return state.pbsgs as Map ?: [:]
}

ArrayList pbsgDnis() {
  ArrayList results = []
  pbsgs().each{ pbsgName, pbsgVals ->
    logInfo('pbsgDnis', ['',
      "pbsgName: ${pbsgName}",
      "dnis: ${pbsgVals.dnis}"
    ])
    results << pbsgVals.dnis
  }
  return results
}
*/

Map TestPbsgPage() {
  return dynamicPage(
    name: 'TestPbsgPage',
    title: [
      heading1("TestPbsgPage - ${app.id}"),
      bullet1('Click <b>Done</b> to enable subscriptions.')
    ].join('<br/>'),
    install: true,
    uninstall: true
  ) {
    //---------------------------------------------------------------------------------
    // REMOVE NO LONGER USED SETTINGS AND STATE
    //   - https://community.hubitat.com/t/issues-with-deselection-of-settings/36054/42
    //-> app.removeSetting('..')
    //-> state.remove('..')
    //---------------------------------------------------------------------------------
    app.updateLabel('TestPbsgPage')
    state.remove('childVsws')
    // Map mapOfPbsgs = [:]
    Map pbsgInst = [:]
    section {
      solicitLogThreshold('appLogThresh', 'INFO')  // 'ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'
      state.logLevel = logThreshToLogLevel(settings.appLogThresh) ?: 5
      // NOTE: state.pbsgs is ALWAYS rebuilt from settings and child VSW discovery.
      for (i in [0]) {
        Map pbsgConfig = pbsgConfig_SolicitInstance(i)
        pbsgInst = pbsg_CreateInstance(pbsgConfig)
        //mapOfPbsgs.putAll(pbsgInst)
      }
    }
    // state.mapOfPbsgs = mapOfPbsgs
  }
}

/*
void deleteOrphanedDevices() {
  // At application scope, identify orphaned child devices by:
  //   - Beginning with current child devices
  //   - Dropping any expected / current devices
  ArrayList dnis = getChildDevices().collect{ d -> d.deviceNetworkId }
  //logInfo('deleteOrphanedDevices', "All Child Devices: ${dnis}")
  // Drop dnis currently associated with PBSGs

  logInfo('deleteOrphanedDevices()', 'ENTERED')
  logPbsgs(state.pbsgs)
  /*
  logInfo('deleteOrphanedDevices', ['',
    "Child Dnis: ${dnis}",
    " Pbsg Dnis: ${pbsgDnis()}"
  ])
  */
  /*
  pbsgs().each{ pbsgName, map ->
    logInfo('deleteOrphanedDevices', pbsgName)
    logInfo('deleteOrphanedDevices', ['',
      "dnis In: ${dnis}",
      "Dropping: ${map.dnis}"
    ])
    dnis.removeAll{ item -> map.dnis.contains(item) }
  }
  logInfo('deleteOrphanedDevices', "Child Devices Less PBSG-owned: ${dnis}")
  //}

  /*
  ArrayList orphanedDNIs = getChildDevices().

  ArrayList orphanedDnis = childVswsAtStart.minus(childVswsCurrent)
      paragraph("<b>orphanedDnis</b>: ${orphanedDnis}")
      orphanedDnis.each{ dni ->
        paragraph("Deleting orphaned device DNI: '${d.deviceNetworkId}'")
        deleteChildDevice(dni)
      }

  Map pbsgs = state.pbsgs
    ArrayList parsedDeviceName = d.name.tokenize('_')
    String pbsgName = parsedDeviceName[0]
    String buttonName = parsedDeviceName[1]
    Boolean isActive = pbsgs."${pbsgName}".buttons.contains(buttonName)
    logInfo('initialize', [
     "<b>pbsgName</b>: ${pbsgName}, ",
     "<b>pbsgDni</b>: ${d.deviceNetworkId}, ",
     "<b>buttonName</b>: ${buttonName}, ",
     "<b>isActive</b>: ${isActive}"
    ].join())
    if (isActive == false) {
      logInfo('initialize',
        "Deleting unaffiliated device '${d.name} with dni '${d.deviceNetworkId}'"
      )
      deleteChildDevice(d.deviceNetworkId)
    }
  }
}
*/

void initialize() {
  //deleteOrphanedDevices()
}

void installed() {
  logWarn('installed', 'Entered')
  unsubscribe()  // Suspend event processing to rebuild state variables.
  initialize()
}

void uninstalled() {
  logWarn('uninstalled', 'Entered')
  removeAllChildApps()
}

void updated() {
  logWarn('updated', 'Entered')
  unsubscribe()  // Suspend event processing to rebuild state variables.
  initialize()
}