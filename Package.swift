//
// Created by Jonathan Davis on 1/26/26.
//

// swift-tools-version:5.8
import PackageDescription

let packageName = "KESPLCallbacksKit"

let package = Package(
    name: packageName,
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: packageName,
            targets: [packageName]
        ),
    ],
    targets: [
        .binaryTarget(
            name: packageName,
            path: "./KESPLCallbacksKit/build/XCFrameworks/release/\(packageName).xcframework"
        )
        ,
    ]
)