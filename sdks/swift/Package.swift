// swift-tools-version: 5.7
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "Beam",
    products: [
        .library(
            name:"BeamProto",
            targets:["BeamProto"]
        ),
        // Products define the executables and libraries a package produces, and make them visible to other packages.
        .library(
            name: "Beam",
            targets: ["Beam"]),
    ],
    dependencies: [
        // Dependencies declare other packages that this package depends on.
        // .package(url: /* package url */, from: "1.0.0"),
        .package(url:"https://github.com/apple/swift-protobuf.git", from: "1.20.2")
    ],
    targets: [
        // Targets are the basic building blocks of a package. A target can define a module or a test suite.
        // Targets can depend on other targets in this package, and on products in packages this package depends on.
        .target(
            name:"BeamProto",
            dependencies:[
                .product(name:"SwiftProtobuf",package:"swift-protobuf")
            ],
            plugins:[
                .plugin(name:"SwiftProtobufPlugin",package:"swift-protobuf")
            ]
        ),
        .target(
            name: "Beam",
            dependencies: [ "BeamProto" ]),
        .testTarget(
            name: "BeamTests",
            dependencies: ["Beam"]),
    ]
)
