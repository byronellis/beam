* Swift SDK

** Transferring Protobuf files

The Swift package manager is fairly opinionated and in particular does not allow plugins to access files in directories above the package directory. This is probably a good thing, but it does present some issues if you want to build the Protobuf files contained in core/.