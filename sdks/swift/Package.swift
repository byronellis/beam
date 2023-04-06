// swift-tools-version: 5.7
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "Beam",
    products: [
        .library(
            name: "BeamProto",
            targets: ["BeamProto"]
        ),
        // Products define the executables and libraries a package produces, and make them visible to other packages.
        .library(
            name: "Beam",
            targets: ["Beam"]),
    ],
    dependencies: [
        .package(path:"../../../swift-protobuf")
    ],
    targets: [
        .target(
            name: "BeamProto",
            dependencies: [
                .product(name:"SwiftProtobuf",package:"swift-protobuf")
            ],
            plugins: [
                .plugin(name:"SwiftProtobufPlugin",package:"swift-protobuf")
            ]),
        .target(
            name: "Beam",
            dependencies: [ "BeamProto" ]
        ),

        .testTarget(
            name: "BeamTests",
            dependencies: ["Beam"]),
    ]
)
