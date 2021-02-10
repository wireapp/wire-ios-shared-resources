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

func parseJson(data: Data) -> String? {
    do {
        
        if let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] {
            
            return json["tag_name"] as? String
        }
    } catch {
        print("❌ JSON parse error: \(error)")
        print(String(decoding: data, as: UTF8.self))
    }
    
    return nil
}

func urlRequest(secret: String,
                url: URL,
                completion: @escaping (Data?) -> ()) {
    
    var request = URLRequest(url:url)
    request.httpMethod = "GET"
    request.setValue(secret, forHTTPHeaderField: "Authorization")
    
    let task = URLSession.shared.dataTask(with: request) {(data, response, error) in
        if let error = error {
            print("❌ Request error: \(error)")
        }
        
        completion(data)
    }
    task.resume()
}

//MARK: - main

let secret = CommandLine.arguments[1]
let path = CommandLine.arguments[2]
let cartfileUrl = URL(fileURLWithPath: path)

print("ℹ️ updating: \(path)")

var lines: [String] = []

do {
    let contents = try String(contentsOf: cartfileUrl, encoding: String.Encoding.utf8)
    
    lines = contents.components(separatedBy: "\n")
    
} catch {
    print("❌ Error: \(error.localizedDescription)")
    exit(1)
}

var updatedResult: [String: [String]] = [:]
var repoOrder:[String] = []

let group = DispatchGroup()

//MARK: request latest versions

lines.forEach() {
    var components:[String] = $0.components(separatedBy: " ")
    
    if components.count >= 2 {
        
        let repo = components[1].replacingOccurrences(of: "\"", with: "")
        print("ℹ️ fetching: \(repo)")
        repoOrder.append(repo)
        
        group.enter()
        
        let url = URL(string: "https://api.github.com/repos/\(repo)/releases/latest")!
        urlRequest(secret: secret, url: url) { data in
            if let data = data,
               let version = parseJson(data: data) {
                var oldversion = components.popLast()
                oldversion = oldversion?.replacingOccurrences(of: "\"", with: "")
                
                components.append(version)
                
                if oldversion != version {
                    print("💡 \(repo) has a new version: \(oldversion ?? "invalid old version") -> \(version)")
                }
                
                updatedResult[repo] = components
                
            } else {
                print("❌ data.length: \(data?.count ?? -1)")
                if let data = data {
                    print(String(decoding: data, as: UTF8.self))
                }
            }
            
            group.leave()
            
        }
    }
    
}

group.wait()

//MARK: update version

var updateString: String = ""

///TODO: create a data struct [[String: [String]]] to fix sorting
repoOrder.forEach() {
    if let result: [String] = updatedResult[$0] {
        // for repo without "~>", the version need to be quoted
        if result.count == 3 {
            let joinedString = "\(result[0]) \(result[1]) \"\(result[2])\""
            updateString += joinedString + "\n"
        } else {
            updateString += (updatedResult[$0]?.joined(separator: " "))! + "\n"
        }
    }
}
print("ℹ️ updated Cartfile content: \n\(updateString)")

//MARK: write to file

print("✅ file written to: \(cartfileUrl)")

do {
    try updateString.write(to: cartfileUrl, atomically: false, encoding: .utf8)
}
catch {
    print("❌ write error: \(error.localizedDescription)")
}
