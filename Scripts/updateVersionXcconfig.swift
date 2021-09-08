//
// Wire
// Copyright (C) 2021 Wire Swiss GmbH
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see http://www.gnu.org/licenses/.
//

/// Update Cartfile frameworks to latest version and write update content back to it.
/// Example: $swift updateCatfile.swift zenkins:PAT /~/Documents/wire/wire-ios/Cartfile

import Foundation

//MARK: - main

var path: String!
var version: String!

if CommandLine.arguments.count == 3 {
    path = CommandLine.arguments[1]
    version = CommandLine.arguments[2]
} else {
    print("❌ exit: please provide secret and Version.xcconfig path.\nExample: $swift updateVersionXcconfig.swift ./Resources/Configurations/version.xcconfig 1.2.3")

    exit(1)
}

let major = version.components(separatedBy: ".").first!

let versionXcconfigUrl = URL(fileURLWithPath: path)

print("ℹ️ updating: \(path!)")

var lines: [String] = []

do {
    let contents = try String(contentsOf: versionXcconfigUrl, encoding: String.Encoding.utf8)
    
    lines = contents.components(separatedBy: "\n")
    
} catch {
    print("❌ Error: \(error.localizedDescription)")
    exit(1)
}

//MARK: request latest versions

var output: [String] = []

lines.forEach() {
    var components:[String] = $0.components(separatedBy: " ")
    
    if components.count >= 3 {
        
        let flag = components[0]
        print("ℹ️ flag: \(flag)")
        if flag == "MAJOR_VERSION" {
            components[2] = major
        } else if flag == "CURRENT_PROJECT_VERSION" {
            components[2] = version
        }
    }

    print("ℹ️ components: \n\(components)")
    
    output.append(components.joined(separator:" "))
}

//MARK: update version
var updateString: String = ""
updateString = output.joined(separator:"\n")

print("ℹ️ updated content: \n\(updateString)")

//MARK: write to file

print("✅ file written to: \(versionXcconfigUrl)")

do {
    try updateString.write(to: versionXcconfigUrl, atomically: false, encoding: .utf8)
}
catch {
    print("❌ write error: \(error.localizedDescription)")
}
