//
//  HelloV1App.swift
//  HelloV1
//

import SwiftUI
import ComposeAppKit

@main
struct HelloV1App: App {
    
    init() {
        #if targetEnvironment(simulator)
        Modules_iosKt.doInitApp(
            isSimulator: true
        )
        #else
        Modules_iosKt.doInitApp(
            isSimulator: false
        )
        #endif
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
