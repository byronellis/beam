public struct KV<K,V> {
    let key: K
    let value: V
}

extension KV: Encodable where K: Encodable, V: Encodable { }
extension KV: Decodable where K: Decodable, V: Decodable { }

