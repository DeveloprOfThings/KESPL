//
//  SplashView.swift
//  HelloV1
//
//  Created by Jonathan Davis on 1/19/26.
//

import SwiftUI

/// Reproduces the LaunchScreen.storyboard UI so we can programmatically control how long the "Splash" UI is displayed
struct SplashView: View {
    var body: some View {
        ZStack {
            background
            foreground
        }
    }
    
    var background: some View {
        // Wrap background in GeometryReader so that we can force the Image to match the parent view's size
        GeometryReader { proxy in
            Image("SplashBackground")
                .resizable()
                .scaledToFill()
                .frame(width: proxy.size.width, height: proxy.size.height)
        }
    }
    
    var foreground: some View {
        Image("SplashForeground")
            .resizable()
            // match width x height dimensions used in LaunchScreen.storyboard
            .frame(width: 150, height: 150)
    }
}

#Preview {
    SplashView()
}
