// ---------------------------------------------------------------------------------
// U T I L S   L I B R A R Y
//
//  Copyright (C) 2023-Present Wesley M. Conner
//
// Licensed under the Apache License, Version 2.0 (aka Apache-2.0, the
// "License"); you may not use this file except in compliance with the
// License. You may obtain a copy of the License at
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ---------------------------------------------------------------------------------
import com.hubitat.app.DeviceWrapper as DevW
import com.hubitat.app.InstalledAppWrapper as InstAppW
import com.hubitat.hub.domain.Event as Event
import com.hubitat.hub.domain.Hub as Hub

library (
 name: 'libUtils',
 namespace: 'wesmc',
 author: 'WesleyMConner',
 description: 'General-Purpose Methods that are Reusable across Hubitat Projects',
 category: 'general purpose',
 documentationLink: 'https://github.com/WesleyMConner/Hubitat-libUtils/README.adoc',
 importUrl: 'https://github.com/WesleyMConner/Hubitat-libUtils.git'
)

//----
//---- HUBITAT EXTENSIONS
//----

List<String> getModeNames () {
  return getLocation().getModes().collect{ it.name }
}

String eventDetails (Event e, Boolean DEEP = false) {
  String rows = """
    <tr>
      <th align='right'>descriptionText</th>
      <td>${e.descriptionText}</td>
    </tr>
    <tr>
      <th align='right'>displayName</th>
      <td>${e.displayName}</td>
    </tr>
    <tr>
      <th align='right'>deviceId</th>
      <td>${e.deviceId}</td>
    </tr>
    <tr>
      <th align='right'>name</th>
      <td>${e.name}</td>
    </tr>
    <tr>
      <th align='right'>value</th>
      <td>${e.value}</td>
    </tr>
    <tr>
      <th align='right'>isStateChange</th>
      <td>${e.isStateChange}</td>
    </tr>
    """
  if (DEEP) rows += """
    <tr>
      <th align='right'>date</th>
      <td>${e.date}</td>
    </tr>
    <tr>
      <th align='right'>class</th>
      <td>${e.class}</td>
    </tr>
    <tr>
      <th align='right'>unixTime</th>
      <td>${e.unixTime}</td>
    </tr>"""
  return "<table>${rows}</table>"
}

String getAppInfo (InstAppW app) {
  return "${app?.getLabel() ?: 'MISSING_LABEL'} (${app?.getId() ?: 'MISSING_ID'})"
}

String getDeviceInfo (def device) {
  // Design Note:
  //   - The parameter is passed as 'def' in lieu of 'DevW'.
  //   - When devices are used from a LinkedHashMap (e.g., settings, state),
  //     the original DevW type is lost - resulting in method call fail that
  //     reports a type mismatch.
  return device ? "${device.displayName} (${device.id})" : null
}

void detectChildAppDupsForLabelsV1 (List<String> keepLabels) {
  List<String> result = []
  result += '<table>'
  result += '<tr><th>LABEL</th><th>ID</th><th>DEVICES</th></tr>'
  app.getAllChildApps().each{ a ->
    result += "<tr><td>${a.getLabel()}</td><td>${a.getId()}</td><td>${a.getChildDevices().size()}</td></tr>"
  }
  result += '</table>'
  Linfo(
    'detectChildAppDupsForLabelsV1()',
    result.join()
  )
}

void detectChildAppDupsForLabelsV2 (List<String> keepLabels) {
  List<String> result = []
  result += '<table>'
  result += '<tr><th>LABEL</th><th>ID</th><th>DEVICES</th></tr>'
  app.getAllChildApps().each{ a ->
    result += "<tr>${tdCtr(a.getLabel())}${tdCtr(a.getId())}${tdCtr(a.getChildDevices().size())}</tr>"
  }
  result += '</table>'
  Linfo(
    'detectChildAppDupsForLabels()',
    result.join()
  )
}

void detectChildAppDupsForLabelsV3 (List<String> keepLabels) {
  List<String> result = []
  result += '<table>'
  result += '<tr><th>LABEL</th><th>DUP</th><th>ID</th><th>DEVICES</th></tr>'
  app.getAllChildApps()?.groupBy{ app -> app.getLabel() }.each{ label, apps ->
    apps.eachWithIndex{ a, index ->
      Boolean isDup = index > 0
      result += "<tr>${tdCtr(label)}${tdCtr(isDup)}${tdCtr(a.getId())}${tdCtr(a.getChildDevices().size())}</tr>"
    }
  }
  result += '</table>'
  Linfo(
    'detectChildAppDupsForLabels()',
    result.join()
  )
}

void detectChildAppDupsForLabels (List<String> keepLabels, InstAppW appBase) {
  List<String> result = []
  result += '<table>'
  result += '<tr><th>LABEL</th><th>DUP</th><th>ID</th><th>DEVICES</th></tr>'
  appBase.getAllChildApps()?.groupBy{ app -> app.getLabel() }.each{ label, apps ->
    apps.eachWithIndex{ a, index ->
      Boolean isDup = index > 0
      result += "<tr>${tdCtr(label)}${tdCtr(isDup)}${tdCtr(a.getId())}${tdCtr(a.getChildDevices().size())}</tr>"
    }
  }
  result += '</table>'
  Linfo(
    'detectChildAppDupsForLabels()',
    result.join()
  )
}

//  app.getAllChildApps()?.groupBy{ app -> app.getLabel() }.each{ label, appObjs ->


void removeAllChildApps () {
  app.getAllChildApps().each{ child ->
    Ldebug(
      'removeAllChildApps()',
      "deleting child: <b>${getAppInfo(appObj)}</b>"
    )
    deleteChildApp(child.getId())
  }
}

String getSwitchState (DevW d) {
  List<String> stateValues = d.collect({ it.currentStates.value }).flatten()
  return stateValues.contains('on')
      ? 'on'
      : stateValues.contains('off')
        ? 'off'
        : 'unknown'
}

String getStateBulletsAsIs() {
  List<String> result = []
  atomicState.sort().each{ k, v ->
    result += bullet1("<b>${k}</b> → ${v}")
  }
  return result.size() != 0 ? result.join('<br/>') : bullet1('<i>NO DATA AVAILABLE</i>')
}

String getSettingsBulletsAsIs() {
  List<String> result = []
  settings.sort().each{ k, v ->
    result += bullet1("<b>${k}</b> → ${v}")
  }
  return result.size() != 0 ? result.join('<br/>') : bullet1('<i>NO DATA AVAILABLE</i>')
}

void identifyLedButtonsForListItems(
  List<String> list,
  List<DevW> ledDevices,
  String prefix
  ) {
  // Keypad LEDs are used as a proxy for Keypad buttons.
  //   - The button's displayName is meaningful to clients.
  //   - The button's deviceNetworkId is <KPAD Dni> hyphen <BUTTON #>
  list.each{ item ->
    input(
      name: "${prefix}_${item}",
      title: heading2("Identify LEDs/Buttons for <b>${item}</b>"),
      type: 'enum',
      width: 6,
      submitOnChange: true,
      required: false,
      multiple: true,
      options: ledDevices.collect{ d ->
        "${d.getLabel()}: ${d.getDeviceNetworkId()}"
      }?.sort()
    )
  }
}

void populateStateKpadButtons (String prefix) {
  // Design Note
  //   The Keypad LEDs collected by selectForMode() function as a proxy for
  //   Keypad button presses. Settings data includes the user-friendly
  //   LED displayName and the LED device ID, which is comprised of 'Keypad
  //   Device Id' and 'Button Number', concatenated with a hyphen. This
  //   method populates "atomicState.[<KPAD DNI>]?.[<KPAD Button #>] = mode".
  //
  // Sample Settings Data
  //     key: LEDs_Day,
  //   value: [Central KPAD 2 - DAY: 5953-2]
  //           ^User-Friendly Name
  //                                 ^Keypad DNI
  //                                      ^Keypad Button Number
  // The 'value' is first parsed into a list with two components:
  //   - User-Friendly Name
  //   - Button DNI               [The last() item in the parsed list.]
  // The Button DNI is further parsed into a list with two components:
  //   - Keypad DNI
  //   - Keypad Button number
  String stateKey = "${prefix}Map"
  state[stateKey] = [:]
  settings.each{ key, value ->
    if (key.contains("${prefix}_")) {
      String base = key.minus("${prefix}_")
      value.each{ item ->
        List<String> kpadDniAndButtons = item?.tokenize(' ')?.last()?.tokenize('-')
        if (kpadDniAndButtons.size() == 2 && base) {
          if (state[stateKey][kpadDniAndButtons[0]] == null) {
            state[stateKey][kpadDniAndButtons[0]] = [:]
          }
          state[stateKey][kpadDniAndButtons[0]][kpadDniAndButtons[1]] = base
        }
      }
    }
  }
}
