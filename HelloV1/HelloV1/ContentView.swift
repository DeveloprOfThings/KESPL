//
//  ContentView.swift
//  HelloV1
//

import SwiftUI
import ComposeAppKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}


struct ContentView: View {
    
    var mainViewModel = MainViewModel_iosKt.getMainVM()
    
    var body: some View {
        ZStack {
            ComposeView()

            // Observe the holdSplashScreen flow so we know when to show/dismiss
            // the splash UI
            Observing(mainViewModel.holdSplashScreen) { holdSplash in
                if(holdSplash.boolValue) {
                    splashScreen
                }
            }
        }
        .ignoresSafeArea(.all)
    }
    
    var splashScreen: some View {
        SplashView()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            // Force the Splash UI to draw ontop of the compose UI
            .zIndex(1.0)
    }
}

#Preview {
    ContentView()
}
