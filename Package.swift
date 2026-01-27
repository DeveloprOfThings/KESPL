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
            path: "./callbacks/build/XCFrameworks/release/\(packageName).xcframework"
        )
        ,
    ]
)